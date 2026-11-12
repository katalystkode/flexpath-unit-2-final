package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.Product;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Component
public class ProductDao {
    private final JdbcTemplate jdbcTemplate;

    public ProductDao(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<Product> getProducts() {
        return jdbcTemplate.query("SELECT * FROM products ORDER BY name", this::mapToProduct);
    }

    public Product getProduct(int id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM products WHERE id = ?", this::mapToProduct, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Product createProduct(Product product) {
        String sql = "INSERT INTO products (name, price) VALUES (?, ?)";
        // Need a KeyHolder since id is auto-incrementing & getId() will result in 0
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, product.getName());
                ps.setBigDecimal(2, product.getPrice());
                return ps;
            }, keyHolder);

            int newId = keyHolder.getKey().intValue();

            return getProduct(newId);
        }
        /* Changed the EmptyResultDataAccessException since getProduct(id) already catches that exception.
        Changed to DataAccessException to catch a database failure similar to addRole().
         */
        catch (DataAccessException e) {
            throw new DaoException("Failed to create product.");
        }
    }

    public Product updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, price = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, product.getName(), product.getPrice(), product.getId());

        if (rowsAffected == 0) {
            throw new DaoException("Zero rows affected, expected at least one.");
        } else {
            return getProduct(product.getId());
        }
    }

    public int deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    private Product mapToProduct(ResultSet resultSet, int rowNum) throws SQLException {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        BigDecimal price = resultSet.getBigDecimal("price");

        return new Product(id, name, price);
    }
}
