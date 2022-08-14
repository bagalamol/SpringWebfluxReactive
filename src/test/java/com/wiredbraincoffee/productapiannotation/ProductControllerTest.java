package com.wiredbraincoffee.productapiannotation;

import com.wiredbraincoffee.productapiannotation.controller.ProductController;
import com.wiredbraincoffee.productapiannotation.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ProductControllerTest {

    private WebTestClient webTestClient;

    private List<Product> expctedProductList;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void beforeEach() {
          this.webTestClient =
            WebTestClient.bindToController(new ProductController(productRepository))
                    .configureClient()
                    .baseUrl("/products")
                    .build();
          this.expctedProductList = this.productRepository.findAll().collectList().block();
    }

    @Test
    void testGetLLProducts() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Product.class)
                .isEqualTo(expctedProductList);
    }

    @Test
    void testNoProductFoundForGivenId() {
        webTestClient.get()
                .uri("/{id}")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void testValidProductId() {
        Product product = expctedProductList.get(0);
        webTestClient.get()
                .uri("{id}", product.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Product.class)
                .isEqualTo(product);
    }

    @Test
    void testProductEvents() {
        ProductEvent expectedProductEvent = new ProductEvent(0L, "Product Event");

        FluxExchangeResult<ProductEvent> result =
                webTestClient.get()
                        .uri("/events")
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(ProductEvent.class);
        StepVerifier.create(result.getResponseBody())
                .expectNext(expectedProductEvent)
                .expectNextCount(2)
                .consumeNextWith(event -> assertEquals(Long.valueOf(3),event.getEventId()))
                .thenCancel()
                .verify();
    }
}
