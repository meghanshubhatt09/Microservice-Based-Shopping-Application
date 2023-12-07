package com.shoppingapp.inventoryservice;

import com.shoppingapp.inventoryservice.model.Inventory;
import com.shoppingapp.inventoryservice.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient // Enable eureka client.
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(InventoryRepository inventoryRepository) {
		return (args) -> {

			Inventory inventory = new Inventory();
			inventory.setSkuCode("iphone_14");
			inventory.setQuantity(100);
			inventoryRepository.save(inventory);

			inventory = new Inventory();
			inventory.setSkuCode("iphone_15");
			inventory.setQuantity(0);
			inventoryRepository.save(inventory);

		};
	}

}
