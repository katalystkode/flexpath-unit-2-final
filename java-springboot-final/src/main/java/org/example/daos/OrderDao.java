package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.Order;
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
public class OrderDao {
    private final JdbcTemplate jdbcTemplate;

    public OrderDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<Order> getOrders() {
        return jdbcTemplate.query("SELECT * FROM orders ORDER BY id", this::mapToOrder);
    }

    public Order getOrder(int id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapToOrder, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Order> getOrdersByUsername(String username) {
        String sql = "SELECT * FROM orders WHERE username = ?";
        return jdbcTemplate.query(sql, this::mapToOrder, username);
    }

    public Order createOrder(Order order) {
        String sql = "INSERT INTO orders (username) VALUES (?)";
        // Need a KeyHolder since id is auto-incrementing & getId() will result in 0
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, order.getUsername());
                return ps;
            }, keyHolder);

            int newId = keyHolder.getKey().intValue();

            return getOrder(newId);
        }
        /* Changed the EmptyResultDataAccessException since getOrder(id) already catches that exception.
        Changed to DataAccessException to catch a database failure similar to addRole().
         */
        catch (DataAccessException e) {
            throw new DaoException("Failed to create order.");
        }
    }

    public Order updateOrder(Order order) {
        String sql = "UPDATE orders SET username = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, order.getUsername(), order.getId());

        if (rowsAffected == 0) {
            throw new DaoException("Zero rows affected, expected at least one.");
        } else {
            return getOrder(order.getId());
        }
    }

    public int deleteOrder(int id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    private Order mapToOrder(ResultSet resultSet, int rowNum) throws SQLException {
        int id = resultSet.getInt("id");
        String username = resultSet.getString("username");

        return new Order(id, username);
    }

}
