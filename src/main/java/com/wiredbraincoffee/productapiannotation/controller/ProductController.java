package com.wiredbraincoffee.productapiannotation.controller;

import com.wiredbraincoffee.productapiannotation.Product;
import com.wiredbraincoffee.productapiannotation.ProductEvent;
import com.wiredbraincoffee.productapiannotation.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@RestController
@RequestMapping("/products")
public class ProductController {

    ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public Flux<Product> getAllProduct() {
        return productRepository.findAll();
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Product>> getProduct(@PathVariable("id") String id) {
        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok(product))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Mono<ServerResponse> getProduct(ServerRequest serverRequest) {
        Mono<Product> product = productRepository.findById(serverRequest.pathVariable("id"));
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        return product.flatMap(product1 ->
          ServerResponse.ok()
                  .contentType(MediaType.APPLICATION_JSON)
                  .body(fromValue(product1)))
                .switchIfEmpty(notFound);
    }

    @PostMapping
    public Mono<ServerResponse> saveProduct(ServerRequest serverRequest) {
        Mono<Product> product = serverRequest.bodyToMono(Product.class);
        return product.flatMap(product1 ->
                ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productRepository.save(product1), Product.class));
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Product> saveProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Product>> updateProduct(@PathVariable("id") String id, @RequestBody Product product) {
        return productRepository.findById(id)
                .flatMap(existingProduct -> {
                   existingProduct.setId(product.getId());
                   existingProduct.setName(product.getName());
                   existingProduct.setPrice(product.getPrice());
                   return productRepository.save(existingProduct);
                })
                .map(updateProduct-> ResponseEntity.ok(updateProduct))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable("id") String id) {
        return productRepository.findById(id)
                .flatMap( product -> productRepository.delete(product).then(Mono.just(ResponseEntity.ok().build())));
    }

    @DeleteMapping
    public Mono<Void> deleteAll() {
        return productRepository.deleteAll();
    }

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductEvent> getEvents() {
        return Flux.interval(Duration.ofSeconds(1)).map(val ->
             new ProductEvent(val , "Product Event")
        );
    }
}
