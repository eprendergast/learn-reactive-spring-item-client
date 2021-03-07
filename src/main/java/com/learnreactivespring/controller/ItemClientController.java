package com.learnreactivespring.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.learnreactivespring.domain.Item;

import reactor.core.publisher.Flux;

@RestController
public class ItemClientController {

    private WebClient webClient = WebClient.create("http://localhost:8080");

    @GetMapping("/client/retrieve")
    public Flux<Item> getAllItemsUsingRetrieve() {
        return webClient.get()
                        .uri("/v1/items")
                        .retrieve() // gives you access to the response body directy
                        .bodyToFlux(Item.class)
                        .log("Items in Client Project retrieve : ");
    }

    @GetMapping("/client/exchange")
    public Flux<Item> getAllItemsUsingExchange() {
        return webClient.get()
                        .uri("/v1/items")
                        .exchange() // gives access to the entire response
                        .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Item.class))
                        .log("Items in Client Project exchange : ");
    }


}
