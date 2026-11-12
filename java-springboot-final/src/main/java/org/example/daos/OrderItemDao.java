package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.OrderItem;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Component
public class OrderItemDao {
    private final JdbcTemplate jdbcTemplate;

    public OrderItemDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<OrderItem> getOrderItems() {
        return jdbcTemplate.query("SELECT * FROM order_items ORDER BY id", this::mapToOrderItem);
    }

    public OrderItem getOrderItem(int id) {
        String sql = "SELECT * from order_items WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapToOrderItem, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        String sql = "SELECT * FROM order_items WHERE order_id = ? ORDER by id";
        return jdbcTemplate.query(sql, this::mapToOrderItem, orderId);
    }

    public OrderItem createOrderItem(OrderItem orderItem) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?)";
        // Need a KeyHolder since id is auto-incrementing & getId() will result in 0
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, orderItem.getOrderId());
                ps.setInt(2, orderItem.getProductId());
                ps.setInt(3, orderItem.getQuantity());
                return ps;
            }, keyHolder);

            int newId = keyHolder.getKey().intValue();

            return getOrderItem(newId);
        }
        /* Changed the EmptyResultDataAccessException since getOrderItem(id) already catches that exception.
        Changed to DataAccessException to catch a database failure similar to addRole().
         */
        catch (DataAccessException e) {
            throw new DaoException("Failed to create order item.");
        }
    }

    public OrderItem updateOrderItem(OrderItem orderItem) {
        String sql = "UPDATE order_items SET order_id = ?, product_id = ?, quantity = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, orderItem.getOrderId(), orderItem.getProductId(), orderItem.getQuantity(), orderItem.getId());

        if (rowsAffected == 0) {
            throw new DaoException("Zero rows affected, expected at least one.");
        } else {
            return getOrderItem(orderItem.getId());
        }
    }

    public int deleteOrderItem(int id) {
        String sql = "DELETE FROM order_items WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    private OrderItem mapToOrderItem(ResultSet resultSet, int rowNum) throws SQLException {
        int id = resultSet.getInt("id");
        int orderId = resultSet.getInt("order_id");
        int productId = resultSet.getInt("product_id");
        int quantity = resultSet.getInt("quantity");

        return new OrderItem(id, orderId, productId, quantity);
    }
}
