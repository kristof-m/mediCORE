package sk.medicore.db.dao;

import sk.medicore.db.DatabaseManager;
import sk.medicore.model.Termin;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TerminDAO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Termin> findByLekarId(int lekarId) {
        String sql = "SELECT * FROM terminy WHERE lekar_id = ? ORDER BY datum_cas";
        return query(sql, lekarId);
    }

    public List<Termin> findAvailable(int lekarId) {
        String sql = "SELECT * FROM terminy WHERE lekar_id = ? AND stav = 'DOSTUPNY' AND datum_cas > datetime('now','localtime') ORDER BY datum_cas";
        return query(sql, lekarId);
    }

    public Termin findById(int id) {
        String sql = "SELECT * FROM terminy WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void updateStav(int id, Termin.Stav stav) {
        String sql = "UPDATE terminy SET stav = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, stav.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Termin> query(String sql, int lekarId) {
        List<Termin> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, lekarId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private Termin mapRow(ResultSet rs) throws SQLException {
        Termin t = new Termin();
        t.setId(rs.getInt("id"));
        t.setLekarId(rs.getInt("lekar_id"));
        t.setDatumCas(LocalDateTime.parse(rs.getString("datum_cas"), FMT));
        t.setTrvanieMin(rs.getInt("trvanie_min"));
        t.setStav(Termin.Stav.valueOf(rs.getString("stav")));
        return t;
    }
}
