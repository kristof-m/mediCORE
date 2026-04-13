package sk.medicore.db.dao;

import sk.medicore.db.DatabaseManager;
import sk.medicore.model.Rezervacia;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RezervaciaDAO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Rezervacia> findByPacientId(int pacientId) {
        String sql = "SELECT * FROM rezervacie WHERE pacient_id = ? ORDER BY vytvorena_at DESC";
        List<Rezervacia> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, pacientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public Rezervacia findById(int id) {
        String sql = "SELECT * FROM rezervacie WHERE id = ?";
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

    public void insert(Rezervacia r) {
        String sql = "INSERT INTO rezervacie (pacient_id, lekar_id, termin_id, procedura_id, stav) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getPacientId());
            ps.setInt(2, r.getLekarId());
            ps.setInt(3, r.getTerminId());
            ps.setInt(4, r.getProceduraId());
            ps.setString(5, r.getStav().name());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                r.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateTermin(int rezervaciaId, int newTerminId) {
        String sql = "UPDATE rezervacie SET termin_id = ?, stav = 'POTVRDENA' WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, newTerminId);
            ps.setInt(2, rezervaciaId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Rezervacia> findByLekarId(int lekarId) {
        String sql = "SELECT * FROM rezervacie WHERE lekar_id = ? AND stav = 'POTVRDENA' ORDER BY vytvorena_at DESC";
        List<Rezervacia> list = new ArrayList<>();
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

    public Rezervacia findByTerminId(int terminId) {
        String sql = "SELECT * FROM rezervacie WHERE termin_id = ? AND stav = 'POTVRDENA' LIMIT 1";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, terminId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void updateStav(int id, Rezervacia.Stav stav) {
        String sql = "UPDATE rezervacie SET stav = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, stav.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Rezervacia mapRow(ResultSet rs) throws SQLException {
        Rezervacia r = new Rezervacia();
        r.setId(rs.getInt("id"));
        r.setPacientId(rs.getInt("pacient_id"));
        r.setLekarId(rs.getInt("lekar_id"));
        r.setTerminId(rs.getInt("termin_id"));
        r.setProceduraId(rs.getInt("procedura_id"));
        r.setStav(Rezervacia.Stav.valueOf(rs.getString("stav")));
        String ts = rs.getString("vytvorena_at");
        if (ts != null) {
            r.setVytvorenaAt(LocalDateTime.parse(ts, FMT));
        }
        return r;
    }
}
