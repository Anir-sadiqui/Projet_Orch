package com.membership.product;

import com.membership.product.application.service.ProductService;
import com.membership.product.domain.entity.Product;
import com.membership.product.domain.entity.ProductCategory;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductServiceTest {

	@Autowired
	ProductService service;

	@Test
	void shouldCreateProduct() {
		Product p = new Product();
		p.setName("Pc portable");
		p.setDescription("Lenovo Yogo tatata");
		p.setPrice(BigDecimal.valueOf(1200));
		p.setStock(10);
		p.setCategory(ProductCategory.ELECTRONICS);

		Product saved = service.create(p);

		assertNotNull(saved.getId());
		assertEquals(10, saved.getStock());
		assertEquals(ProductCategory.ELECTRONICS, saved.getCategory());
	}

	@Test
	void shouldUpdateStock() {
		Product p = new Product();
		p.setName("Livre");
		p.setDescription("Livre cuisine");
		p.setPrice(BigDecimal.valueOf(40));
		p.setStock(5);
		p.setCategory(ProductCategory.BOOKS);

		Product saved = service.create(p);
		service.updateStock(saved.getId(), 3);

		Product updated = service.findById(saved.getId());
		assertEquals(3, updated.getStock());
	}

	@Test
	void shouldFailWhenUpdatingStockWithNegativeValue() {
		Product p = new Product();
		p.setName("Telephone");
		p.setDescription("Iphone 16 pro max");
		p.setPrice(BigDecimal.valueOf(500));
		p.setStock(5);
		p.setCategory(ProductCategory.ELECTRONICS);

		Product saved = service.create(p);

		assertThrows(IllegalArgumentException.class, () -> {
			service.updateStock(saved.getId(), -1);
		});
	}
}
