package sk.medicore.db.dao;

import sk.medicore.db.DatabaseManager;
import sk.medicore.notifikator.Notifikacia;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotifikaciaDAO {

    public void insert(Notifikacia n) {
        String sql = "INSERT INTO notifikacie (pacient_id, typ, sprava) VALUES (?, ?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, n.getPacientId());
            ps.setString(2, n.getTyp().name());
            ps.setString(3, n.getSprava());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Chyba pri ukladaní notifikácie", e);
        }
    }

    public List<Notifikacia> findUnread(int pacientId) {
        String sql = "SELECT * FROM notifikacie WHERE pacient_id = ? AND precitana = 0 ORDER BY vytvorena_at DESC";
        List<Notifikacia> result = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, pacientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Chyba pri načítavaní notifikácií", e);
        }
        return result;
    }

    public void markAllRead(int pacientId) {
        String sql = "UPDATE notifikacie SET precitana = 1 WHERE pacient_id = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, pacientId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Chyba pri označovaní notifikácií", e);
        }
    }

    private Notifikacia map(ResultSet rs) throws SQLException {
        Notifikacia n = new Notifikacia();
        n.setId(rs.getInt("id"));
        n.setPacientId(rs.getInt("pacient_id"));
        try { n.setTyp(Notifikacia.Typ.valueOf(rs.getString("typ"))); }
        catch (IllegalArgumentException ignored) { n.setTyp(Notifikacia.Typ.POTVRDENA); }
        n.setSprava(rs.getString("sprava"));
        n.setPrecitana(rs.getInt("precitana") == 1);
        String ts = rs.getString("vytvorena_at");
        if (ts != null) n.setVytvorenaAt(LocalDateTime.parse(ts.substring(0, 19).replace(" ", "T")));
        return n;
    }
}
