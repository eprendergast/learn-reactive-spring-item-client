package com.learnreactivespring.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.learnreactivespring.domain.Item;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
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

    @GetMapping("/client/retrieve/singleItem")
    public Mono<Item> getOneItemUsingRetrieve(@PathVariable String id) {
        return webClient.get()
                        .uri("/v1/items/{id}", id)
                        .retrieve() // gives you access to the response body directy
                        .bodyToMono(Item.class)
                        .log("Items in Client Project retrieve single item : ");
    }

    @GetMapping("/client/exchange/singleItem")
    public Mono<Item> getOneItemUsingExchange(@PathVariable String id) {
        return webClient.get()
                        .uri("/v1/items/{id}", id)
                        .exchange() // gives access to the entire response
                        .flatMap(clientResponse -> clientResponse.bodyToMono(Item.class))
                        .log("Items in Client Project exchange : ");
    }

    @PostMapping("/client/createItem")
    public Mono<Item> createItem(@RequestBody Item item) {
        return webClient.post()
                        .uri("/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(item), Item.class)
                        .retrieve() // gives access to the entire response
                        .bodyToMono(Item.class)
                        .log("Created item is : ");
    }

    @PutMapping("/client/updateItem/{id}")
    public Mono<Item> updateItem(@PathVariable String id,
                                 @RequestBody Item item) {
        return webClient.put()
                        .uri("/v1/items/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(item), Item.class)
                        .retrieve() // gives access to the entire response
                        .bodyToMono(Item.class)
                        .log("Updated item is : ");
    }

    @DeleteMapping("/client/deleteItem/{id}")
    public Mono<Void> deleteItem(@PathVariable String id) {
        return webClient.delete()
                        .uri("/v1/items/{id}", id)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .log("Deleted item is : ");
    }

    @GetMapping("/client/retrieve/error")
    public Flux<Item> errorRetrieve() {
        return webClient.get()
                        .uri("v1/items/runtimeException")
                        .retrieve()
                        .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                            Mono<String> errorMono = clientResponse.bodyToMono(String.class);
                            return errorMono.flatMap(errorMessage -> {
                                log.error("The error message is : " + errorMessage);
                                throw new RuntimeException(errorMessage);
                            });
                        }).bodyToFlux(Item.class);
    }

    @GetMapping("/client/exchange/error")
    public Flux<Item> errorExchange() {
        return webClient.get()
                        .uri("v1/items/runtimeException")
                        .exchange()
                        .flatMapMany(clientResponse -> {
                           if (clientResponse.statusCode().is5xxServerError()) {
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorMessage -> {
                                            log.error("Error Message in errorExchange : " + errorMessage);
                                            throw new RuntimeException(errorMessage);
                                        });
                            } else {
                               return clientResponse.bodyToFlux(Item.class);
                           }
                        });
    }


}
