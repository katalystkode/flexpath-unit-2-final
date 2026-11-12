package org.example.controllers;

import org.example.models.Order;
import org.example.daos.OrderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.security.Principal;

@RestController
@CrossOrigin
@RequestMapping("/api/orders")
@PreAuthorize("isAuthenticated()")
public class OrderController {
    @Autowired
    private OrderDao orderDao;

    /*
    @GetMapping
    public List<Order> getAll() { return orderDao.getOrders(); }

    Commented out when I added listOrders() method since both requests were mapped to the same destination
    & listOrders() handles both requests
    */

    @GetMapping
    public List<Order> listOrders(@RequestParam(required = false) String username) {
        if (username != null) {
            return orderDao.getOrdersByUsername(username);
        } else {
            return orderDao.getOrders();
        }
    }

    @GetMapping(path = "/{id}")
    public Order get(@PathVariable int id) {
        Order order = orderDao.getOrder(id);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found.");
        }
        return order;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Order create(@RequestBody Order order, Principal principal) {
        String username = principal.getName();
        order.setUsername(username);
        return orderDao.createOrder(order);
    }

    @PutMapping(path = "/{id}")
    public Order update(@PathVariable int id, @RequestBody Order order, Principal principal) {
        String username = principal.getName();
        order.setId(id);
        order.setUsername(username);
        Order existingOrder = orderDao.getOrder(id);
        if (existingOrder == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found.");
        }
        return orderDao.updateOrder(order);
    }
    // Note PUT test to update third order fails because the "test-admin" is not authenticated

    @DeleteMapping(path = "/{id}")
    public int delete(@PathVariable int id) {
        int rowsAffected = orderDao.deleteOrder(id);
        if (rowsAffected == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found.");
        }
        return rowsAffected;
    }

}
