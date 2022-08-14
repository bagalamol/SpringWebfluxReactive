package com.wiredbraincoffee.productapiannotation;

import com.wiredbraincoffee.productapiannotation.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class ProductApiAnnotationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductApiAnnotationApplication.class, args);
	}
	@Bean
	CommandLineRunner init(ProductRepository productRepository) {
		return args -> {
			Flux<Product> productFlux = Flux.just(new Product(null, "Tea", 2.1),
					new Product(null, "Tea Black",3.1),
					new Product(null,"Tea white",2.99))
					.flatMap(product -> productRepository.save(product));

			productFlux.thenMany(productRepository.findAll())
					.subscribe(System.out::println);
	};
	}
}
