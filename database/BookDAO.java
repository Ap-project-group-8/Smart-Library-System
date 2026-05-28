package db;

import model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Book CRUD operations.
 * All queries use PreparedStatements to prevent SQL injection.
 */
public class BookDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    public boolean addBook(Book book) throws SQLException {
        String sql = "INSERT INTO books (isbn, title, author, publisher, publish_year, " +
                     "category_id, total_copies, available_copies, shelf_location, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getPublisher());
            ps.setInt(5, book.getPublishYear());
            ps.setInt(6, book.getCategoryId());
            ps.setInt(7, book.getTotalCopies());
            ps.setInt(8, book.getAvailableCopies());
            ps.setString(9, book.getShelfLocation());
            ps.setString(10, book.getDescription());
            return ps.executeUpdate() > 0;
        }
    }

    // ── READ ───────────────────────────────────────────────────────────────────

    public List<Book> getAllBooks() throws SQLException {
        String sql = "SELECT b.*, c.name AS category_name " +
                     "FROM books b LEFT JOIN categories c ON b.category_id = c.category_id " +
                     "ORDER BY b.title";
        List<Book> books = new ArrayList<>();
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) books.add(mapRow(rs));
        }
        return books;
    }

    public Book getBookById(int bookId) throws SQLException {
        String sql = "SELECT b.*, c.name AS category_name " +
                     "FROM books b LEFT JOIN categories c ON b.category_id = c.category_id " +
                     "WHERE b.book_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Book> searchBooks(String keyword) throws SQLException {
        String sql = "SELECT b.*, c.name AS category_name " +
                     "FROM books b LEFT JOIN categories c ON b.category_id = c.category_id " +
                     "WHERE b.title LIKE ? OR b.author LIKE ? OR b.isbn LIKE ? " +
                     "ORDER BY b.title";
        String pattern = "%" + keyword + "%";
        List<Book> books = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) books.add(mapRow(rs));
            }
        }
        return books;
    }

    public List<Book> getAvailableBooks() throws SQLException {
        String sql = "SELECT b.*, c.name AS category_name " +
                     "FROM books b LEFT JOIN categories c ON b.category_id = c.category_id " +
                     "WHERE b.available_copies > 0 ORDER BY b.title";
        List<Book> books = new ArrayList<>();
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) books.add(mapRow(rs));
        }
        return books;
    }

    public int getTotalBooks() throws SQLException {
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM books")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getTotalAvailableCopies() throws SQLException {
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery("SELECT SUM(available_copies) FROM books")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────

    public boolean updateBook(Book book) throws SQLException {
        String sql = "UPDATE books SET isbn=?, title=?, author=?, publisher=?, publish_year=?, " +
                     "category_id=?, total_copies=?, available_copies=?, shelf_location=?, description=? " +
                     "WHERE book_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getPublisher());
            ps.setInt(5, book.getPublishYear());
            ps.setInt(6, book.getCategoryId());
            ps.setInt(7, book.getTotalCopies());
            ps.setInt(8, book.getAvailableCopies());
            ps.setString(9, book.getShelfLocation());
            ps.setString(10, book.getDescription());
            ps.setInt(11, book.getBookId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean decrementAvailableCopies(int bookId) throws SQLException {
        String sql = "UPDATE books SET available_copies = available_copies - 1 " +
                     "WHERE book_id = ? AND available_copies > 0";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean incrementAvailableCopies(int bookId) throws SQLException {
        String sql = "UPDATE books SET available_copies = available_copies + 1 " +
                     "WHERE book_id = ? AND available_copies < total_copies";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────

    public boolean deleteBook(int bookId) throws SQLException {
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── MAPPER ─────────────────────────────────────────────────────────────────

    private Book mapRow(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setBookId(rs.getInt("book_id"));
        b.setIsbn(rs.getString("isbn"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setPublisher(rs.getString("publisher"));
        b.setPublishYear(rs.getInt("publish_year"));
        b.setCategoryId(rs.getInt("category_id"));
        b.setCategoryName(rs.getString("category_name"));
        b.setTotalCopies(rs.getInt("total_copies"));
        b.setAvailableCopies(rs.getInt("available_copies"));
        b.setShelfLocation(rs.getString("shelf_location"));
        b.setDescription(rs.getString("description"));
        b.setCoverUrl(rs.getString("cover_url"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) b.setCreatedAt(ca.toLocalDateTime());
        return b;
    }
}
