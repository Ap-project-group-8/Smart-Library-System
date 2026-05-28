package db;

import model.Member;
import model.Member.MembershipType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Member CRUD operations.
 */
public class MemberDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    public boolean addMember(Member m) throws SQLException {
        String sql = "INSERT INTO members (full_name, email, phone, address, membership_type, " +
                     "membership_date, expiry_date, is_active, max_loans) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, m.getFullName());
            ps.setString(2, m.getEmail());
            ps.setString(3, m.getPhone());
            ps.setString(4, m.getAddress());
            ps.setString(5, m.getMembershipType().name());
            ps.setDate(6, Date.valueOf(m.getMembershipDate()));
            ps.setDate(7, m.getExpiryDate() != null ? Date.valueOf(m.getExpiryDate()) : null);
            ps.setBoolean(8, m.isActive());
            ps.setInt(9, m.getMaxLoans());
            return ps.executeUpdate() > 0;
        }
    }

    // ── READ ───────────────────────────────────────────────────────────────────

    public List<Member> getAllMembers() throws SQLException {
        String sql = "SELECT m.*, " +
                     "(SELECT COUNT(*) FROM loans l WHERE l.member_id = m.member_id AND l.status IN ('ACTIVE','OVERDUE')) AS active_loans " +
                     "FROM members m ORDER BY m.full_name";
        List<Member> list = new ArrayList<>();
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Member getMemberById(int memberId) throws SQLException {
        String sql = "SELECT m.*, " +
                     "(SELECT COUNT(*) FROM loans l WHERE l.member_id = m.member_id AND l.status IN ('ACTIVE','OVERDUE')) AS active_loans " +
                     "FROM members m WHERE m.member_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Member> searchMembers(String keyword) throws SQLException {
        String sql = "SELECT m.*, " +
                     "(SELECT COUNT(*) FROM loans l WHERE l.member_id = m.member_id AND l.status IN ('ACTIVE','OVERDUE')) AS active_loans " +
                     "FROM members m WHERE m.full_name LIKE ? OR m.email LIKE ? OR m.phone LIKE ? " +
                     "ORDER BY m.full_name";
        String pattern = "%" + keyword + "%";
        List<Member> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int getTotalMembers() throws SQLException {
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM members WHERE is_active = TRUE")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────

    public boolean updateMember(Member m) throws SQLException {
        String sql = "UPDATE members SET full_name=?, email=?, phone=?, address=?, " +
                     "membership_type=?, expiry_date=?, is_active=?, max_loans=? " +
                     "WHERE member_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, m.getFullName());
            ps.setString(2, m.getEmail());
            ps.setString(3, m.getPhone());
            ps.setString(4, m.getAddress());
            ps.setString(5, m.getMembershipType().name());
            ps.setDate(6, m.getExpiryDate() != null ? Date.valueOf(m.getExpiryDate()) : null);
            ps.setBoolean(7, m.isActive());
            ps.setInt(8, m.getMaxLoans());
            ps.setInt(9, m.getMemberId());
            return ps.executeUpdate() > 0;
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────

    public boolean deleteMember(int memberId) throws SQLException {
        String sql = "DELETE FROM members WHERE member_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, memberId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── MAPPER ─────────────────────────────────────────────────────────────────

    private Member mapRow(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setMemberId(rs.getInt("member_id"));
        m.setFullName(rs.getString("full_name"));
        m.setEmail(rs.getString("email"));
        m.setPhone(rs.getString("phone"));
        m.setAddress(rs.getString("address"));
        m.setMembershipType(MembershipType.valueOf(rs.getString("membership_type")));
        Date md = rs.getDate("membership_date");
        if (md != null) m.setMembershipDate(md.toLocalDate());
        Date ed = rs.getDate("expiry_date");
        if (ed != null) m.setExpiryDate(ed.toLocalDate());
        m.setActive(rs.getBoolean("is_active"));
        m.setMaxLoans(rs.getInt("max_loans"));
        m.setActiveLoans(rs.getInt("active_loans"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) m.setCreatedAt(ca.toLocalDateTime());
        return m;
    }
}
