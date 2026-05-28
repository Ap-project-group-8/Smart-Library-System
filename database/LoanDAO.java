package db;

import model.Loan;
import model.Loan.Status;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Loan CRUD operations.
 */
public class LoanDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    public int addLoan(Loan loan) throws SQLException {
        String sql = "INSERT INTO loans (book_id, member_id, librarian_id, loan_date, due_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loan.getBookId());
            ps.setInt(2, loan.getMemberId());
            ps.setInt(3, loan.getLibrarianId());
            ps.setDate(4, Date.valueOf(loan.getLoanDate()));
            ps.setDate(5, Date.valueOf(loan.getDueDate()));
            ps.setString(6, loan.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    // ── READ ───────────────────────────────────────────────────────────────────

    public List<Loan> getAllLoans() throws SQLException {
        String sql = buildJoinQuery("ORDER BY l.loan_date DESC");
        List<Loan> list = new ArrayList<>();
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Loan> getActiveLoans() throws SQLException {
        String sql = buildJoinQuery("WHERE l.status IN ('ACTIVE','OVERDUE') ORDER BY l.due_date");
        List<Loan> list = new ArrayList<>();
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Loan> getOverdueLoans() throws SQLException {
        String sql = buildJoinQuery("WHERE l.status = 'OVERDUE' OR (l.status = 'ACTIVE' AND l.due_date < CURDATE()) ORDER BY l.due_date");
        List<Loan> list = new ArrayList<>();
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Loan> getLoansByMember(int memberId) throws SQLException {
        String sql = buildJoinQuery("WHERE l.member_id = ? ORDER BY l.loan_date DESC");
        List<Loan> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Loan getLoanById(int loanId) throws SQLException {
        String sql = buildJoinQuery("WHERE l.loan_id = ?");
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, loanId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public int getActiveLoansCount() throws SQLException {
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM loans WHERE status IN ('ACTIVE','OVERDUE')")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getOverdueLoansCount() throws SQLException {
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT COUNT(*) FROM loans WHERE status='OVERDUE' OR (status='ACTIVE' AND due_date < CURDATE())")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────

    public boolean returnLoan(int loanId, BigDecimal fine) throws SQLException {
        String sql = "UPDATE loans SET return_date = CURDATE(), status = 'RETURNED', " +
                     "fine_amount = ? WHERE loan_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setBigDecimal(1, fine);
            ps.setInt(2, loanId);
            return ps.executeUpdate() > 0;
        }
    }

    public int updateOverdueStatuses() throws SQLException {
        String sql = "UPDATE loans SET status = 'OVERDUE' " +
                     "WHERE status = 'ACTIVE' AND due_date < CURDATE()";
        try (Statement st = getConn().createStatement()) {
            return st.executeUpdate(sql);
        }
    }

    public boolean markFinePaid(int loanId) throws SQLException {
        String sql = "UPDATE loans SET fine_paid = TRUE WHERE loan_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, loanId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── HELPER ─────────────────────────────────────────────────────────────────

    private String buildJoinQuery(String whereClause) {
        return "SELECT l.*, b.title AS book_title, b.isbn AS book_isbn, " +
               "m.full_name AS member_name, lib.full_name AS librarian_name " +
               "FROM loans l " +
               "JOIN books b ON l.book_id = b.book_id " +
               "JOIN members m ON l.member_id = m.member_id " +
               "LEFT JOIN librarians lib ON l.librarian_id = lib.librarian_id " +
               whereClause;
    }

    private Loan mapRow(ResultSet rs) throws SQLException {
        Loan l = new Loan();
        l.setLoanId(rs.getInt("loan_id"));
        l.setBookId(rs.getInt("book_id"));
        l.setMemberId(rs.getInt("member_id"));
        l.setLibrarianId(rs.getInt("librarian_id"));
        Date ld = rs.getDate("loan_date");
        if (ld != null) l.setLoanDate(ld.toLocalDate());
        Date dd = rs.getDate("due_date");
        if (dd != null) l.setDueDate(dd.toLocalDate());
        Date rd = rs.getDate("return_date");
        if (rd != null) l.setReturnDate(rd.toLocalDate());
        l.setStatus(Status.valueOf(rs.getString("status")));
        l.setFineAmount(rs.getBigDecimal("fine_amount"));
        l.setFinePaid(rs.getBoolean("fine_paid"));
        l.setNotes(rs.getString("notes"));
        l.setBookTitle(rs.getString("book_title"));
        l.setBookIsbn(rs.getString("book_isbn"));
        l.setMemberName(rs.getString("member_name"));
        l.setLibrarianName(rs.getString("librarian_name"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) l.setCreatedAt(ca.toLocalDateTime());
        return l;
    }
}
