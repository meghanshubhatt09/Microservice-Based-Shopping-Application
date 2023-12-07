package com.shoppingapp.inventoryservice.cotroller;

import com.shoppingapp.inventoryservice.dto.InventoryResponse;
import com.shoppingapp.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    //    @GetMapping("/{sku-code}") and @PathVariable("sku-code") String skuCode
    // http://localhost:8082/api/inventory/iphone-12,iphone-11,iphone-10

    // GetMapping and @RequestParam List<String> skuCode
    // http://localhost:8082/api/inventory?skuCode=iphone-12&skuCode=iphone-11&skuCode=iphone-10
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode) {
        return inventoryService.isInStock(skuCode);
    }
}
