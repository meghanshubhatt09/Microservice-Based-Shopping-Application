package com.shoppingapp.productservice.repository;


import com.shoppingapp.productservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String>{

}
