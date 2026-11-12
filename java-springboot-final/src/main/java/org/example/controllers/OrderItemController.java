package org.example.controllers;

import org.example.models.OrderItem;
import org.example.daos.OrderItemDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(path = "/api/order-items")
@PreAuthorize("isAuthenticated()")
public class OrderItemController {
    @Autowired
    private OrderItemDao orderItemDao;
    /*
    @GetMapping
    public List<OrderItem> getAll() { return orderItemDao.getOrderItems(); }

    Commented out when I added listOrderItems() method since both requests were mapped to the same destination
    & listOrderItems() handles both requests
    */

    @GetMapping
    public List<OrderItem> listOrderItems(@RequestParam(required = false) Long orderId) {
        if (orderId != null) {
            return orderItemDao.getOrderItemsByOrderId(orderId);
        } else {
            return orderItemDao.getOrderItems();
        }
    }

    @GetMapping(path = "/{id}")
    public OrderItem get(@PathVariable int id) {
        OrderItem orderItem = orderItemDao.getOrderItem(id);
        if (orderItem == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found.");
        }
        return orderItem;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public OrderItem create(@RequestBody OrderItem orderItem) { return orderItemDao.createOrderItem(orderItem); }

    @PutMapping(path = "/{id}")
    public OrderItem update(@PathVariable int id, @RequestBody OrderItem orderItem) {
        orderItem.setId(id);
        OrderItem existingOrderItem = orderItemDao.getOrderItem(id);
        if (existingOrderItem == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found.");
        }
        return orderItemDao.updateOrderItem(orderItem);
    }

    @DeleteMapping(path = "/{id}")
    public int delete(@PathVariable int id) {
        int rowsAffected = orderItemDao.deleteOrderItem(id);
        if (rowsAffected == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found.");
        }
        return rowsAffected;
    }
}
