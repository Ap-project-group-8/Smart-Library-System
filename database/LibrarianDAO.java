package db;

import model.Librarian;
import model.Librarian.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Librarian authentication and management.
 */
public class LibrarianDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Authenticates a librarian by username and password.
     * NOTE: In production, use BCrypt hashing. Here we compare plaintext for simplicity.
     */
    public Librarian authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM librarians WHERE username = ? AND password_hash = ? AND is_active = TRUE";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Librarian lib = mapRow(rs);
                    updateLastLogin(lib.getLibrarianId());
                    return lib;
                }
            }
        }
        return null;
    }

    public List<Librarian> getAllLibrarians() throws SQLException {
        List<Librarian> list = new ArrayList<>();
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM librarians ORDER BY full_name")) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public boolean addLibrarian(Librarian lib) throws SQLException {
        String sql = "INSERT INTO librarians (full_name, email, username, password_hash, phone, role) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, lib.getFullName());
            ps.setString(2, lib.getEmail());
            ps.setString(3, lib.getUsername());
            ps.setString(4, lib.getPasswordHash());
            ps.setString(5, lib.getPhone());
            ps.setString(6, lib.getRole().name());
            return ps.executeUpdate() > 0;
        }
    }

    private void updateLastLogin(int librarianId) throws SQLException {
        String sql = "UPDATE librarians SET last_login = NOW() WHERE librarian_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, librarianId);
            ps.executeUpdate();
        }
    }

    private Librarian mapRow(ResultSet rs) throws SQLException {
        Librarian lib = new Librarian();
        lib.setLibrarianId(rs.getInt("librarian_id"));
        lib.setFullName(rs.getString("full_name"));
        lib.setEmail(rs.getString("email"));
        lib.setUsername(rs.getString("username"));
        lib.setPasswordHash(rs.getString("password_hash"));
        lib.setPhone(rs.getString("phone"));
        lib.setRole(Role.valueOf(rs.getString("role")));
        lib.setActive(rs.getBoolean("is_active"));
        Timestamp ll = rs.getTimestamp("last_login");
        if (ll != null) lib.setLastLogin(ll.toLocalDateTime());
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) lib.setCreatedAt(ca.toLocalDateTime());
        return lib;
    }
}
