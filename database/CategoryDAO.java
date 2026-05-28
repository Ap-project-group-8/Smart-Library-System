package db;

import model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Category operations.
 */
public class CategoryDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Category> getAllCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM categories ORDER BY name")) {
            while (rs.next()) {
                Category c = new Category();
                c.setCategoryId(rs.getInt("category_id"));
                c.setName(rs.getString("name"));
                c.setDescription(rs.getString("description"));
                Timestamp ca = rs.getTimestamp("created_at");
                if (ca != null) c.setCreatedAt(ca.toLocalDateTime());
                list.add(c);
            }
        }
        return list;
    }

    public boolean addCategory(Category c) throws SQLException {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getDescription());
            return ps.executeUpdate() > 0;
        }
    }
}
