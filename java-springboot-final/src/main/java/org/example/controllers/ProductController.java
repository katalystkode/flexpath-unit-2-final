package org.example.controllers;

import org.example.models.Product;
import org.example.daos.ProductDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/products")
@PreAuthorize("isAuthenticated()")
public class ProductController {
    @Autowired
    private ProductDao productDao;

    @GetMapping
    public List<Product> getAll() { return productDao.getProducts(); }

    @GetMapping(path = "/{id}")
    public Product get(@PathVariable int id) {
        Product product = productDao.getProduct(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
        }
        return product;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Product create(@RequestBody Product product) { return productDao.createProduct(product); }

    @PutMapping(path = "/{id}")
    public Product update(@PathVariable int id, @RequestBody Product product) {
        product.setId(id);
        Product existingProduct = productDao.getProduct(id);
        if (existingProduct == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
        }
        return productDao.updateProduct(product);
    }

    @DeleteMapping(path = "/{id}")
    public int delete(@PathVariable int id) {
        int rowsAffected = productDao.deleteProduct(id);
        if (rowsAffected == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
        }
        return rowsAffected;
    }
}
