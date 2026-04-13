package sk.medicore.db.dao;

import sk.medicore.db.DatabaseManager;
import sk.medicore.model.Lekar;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LekarDAO {

    public List<Lekar> findAll() {
        String sql = "SELECT p.*, l.specializacia, l.pracovisko_id FROM pouzivatelia p " +
                     "JOIN lekari l ON p.id = l.id ORDER BY p.priezvisko";
        return query(sql);
    }

    public List<Lekar> findByProceduraId(int proceduraId) {
        String sql = "SELECT p.*, l.specializacia, l.pracovisko_id FROM pouzivatelia p " +
                     "JOIN lekari l ON p.id = l.id " +
                     "JOIN lekar_procedury lp ON l.id = lp.lekar_id " +
                     "WHERE lp.procedura_id = ? ORDER BY p.priezvisko";
        List<Lekar> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, proceduraId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public String getLocation(int lekarId) {
        String sql = "SELECT pr.budova, pr.poschodie, pr.miestnost FROM pracoviska pr " +
                     "JOIN lekari l ON pr.id = l.pracovisko_id WHERE l.id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, lekarId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String budova = rs.getString("budova");
                String poschodie = rs.getString("poschodie");
                String miestnost = rs.getString("miestnost");
                StringBuilder sb = new StringBuilder();
                if (budova != null) sb.append(budova);
                if (poschodie != null) sb.append(", Poschodie ").append(poschodie);
                if (miestnost != null) sb.append(" / Izba ").append(miestnost);
                return sb.toString();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    public Lekar findById(int id) {
        String sql = "SELECT p.*, l.specializacia, l.pracovisko_id FROM pouzivatelia p " +
                     "JOIN lekari l ON p.id = l.id WHERE p.id = ?";
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

    private List<Lekar> query(String sql) {
        List<Lekar> list = new ArrayList<>();
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private Lekar mapRow(ResultSet rs) throws SQLException {
        Lekar l = new Lekar();
        l.setId(rs.getInt("id"));
        l.setMeno(rs.getString("meno"));
        l.setPriezvisko(rs.getString("priezvisko"));
        l.setEmail(rs.getString("email"));
        l.setHesloHash(rs.getString("heslo_hash"));
        l.setTyp(rs.getString("typ"));
        l.setSpecializacia(rs.getString("specializacia"));
        l.setPracoviskoId(rs.getInt("pracovisko_id"));
        return l;
    }
}
