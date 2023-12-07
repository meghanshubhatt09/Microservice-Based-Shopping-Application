package com.shoppingapp.orderservice.service;

import com.shoppingapp.orderservice.dto.InventoryResponse;
import com.shoppingapp.orderservice.dto.OrderLineItemsDto;
import com.shoppingapp.orderservice.dto.OrderRequest;
import com.shoppingapp.orderservice.event.OrderPlacedEvent;
import com.shoppingapp.orderservice.model.Order;
import com.shoppingapp.orderservice.model.OrderLineItems;
import com.shoppingapp.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest) {
       Order order = new Order();
       order.setOrderNumber(UUID.randomUUID().toString());

       // Collect orderline items in a list
       List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
               .stream()
               .map(this::mapToDto)
               .toList();

       order.setOrderLineItems(orderLineItems);

        List<String> skuCodes = order.getOrderLineItems().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        log.info("Calling inventory service");
        Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");
        try(Tracer.SpanInScope spanInScope = tracer.withSpan(inventoryServiceLookup.start())){
            // Call Inventory Service to check if the products are available
            InventoryResponse[] inventoryResponsesArray = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

            boolean allProductsInStock = Arrays.stream(inventoryResponsesArray).allMatch(InventoryResponse::isInStock);

            if (allProductsInStock) {
                orderRepository.save(order);
                kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
                return "Order placed successfully";
            }
            else {
                throw new IllegalArgumentException("Some of the products in the order may not be available");
            }
        }
        finally {
            inventoryServiceLookup.end();
        }

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
