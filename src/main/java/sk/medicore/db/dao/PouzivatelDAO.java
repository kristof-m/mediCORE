package sk.medicore.db.dao;

import sk.medicore.db.DatabaseManager;
import sk.medicore.model.*;

import java.sql.*;

public class PouzivatelDAO {

    public Pouzivatel findByEmail(String email) {
        String sql = "SELECT p.*, l.specializacia, l.pracovisko_id FROM pouzivatelia p " +
                     "LEFT JOIN lekari l ON p.id = l.id WHERE p.email = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM pouzivatelia WHERE email = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insert(Pouzivatel p) {
        String sql = "INSERT INTO pouzivatelia (meno, priezvisko, email, heslo_hash, typ) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getMeno());
            ps.setString(2, p.getPriezvisko());
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getHesloHash());
            ps.setString(5, p.getTyp());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                p.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateProfil(int userId, String meno, String priezvisko, String email) {
        String sql = "UPDATE pouzivatelia SET meno = ?, priezvisko = ?, email = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, meno);
            ps.setString(2, priezvisko);
            ps.setString(3, email);
            ps.setInt(4, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int userId) {
        String cancelFuture = "UPDATE rezervacie SET stav = 'ZRUSENA' WHERE pacient_id = ? AND stav = 'POTVRDENA'";
        String deleteUser   = "DELETE FROM pouzivatelia WHERE id = ?";
        try (PreparedStatement ps1 = DatabaseManager.getConnection().prepareStatement(cancelFuture);
             PreparedStatement ps2 = DatabaseManager.getConnection().prepareStatement(deleteUser)) {
            ps1.setInt(1, userId);
            ps1.executeUpdate();
            ps2.setInt(1, userId);
            ps2.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateHeslo(int userId, String newHesloHash) {
        String sql = "UPDATE pouzivatelia SET heslo_hash = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, newHesloHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Pouzivatel mapRow(ResultSet rs) throws SQLException {
        String typ = rs.getString("typ");
        Pouzivatel p;
        switch (typ) {
            case "LEKAR" -> {
                Lekar l = new Lekar();
                l.setSpecializacia(rs.getString("specializacia"));
                l.setPracoviskoId(rs.getInt("pracovisko_id"));
                p = l;
            }
            case "ADMIN" -> p = new AdministrativnyPracovnik();
            default -> p = new Pacient();
        }
        p.setId(rs.getInt("id"));
        p.setMeno(rs.getString("meno"));
        p.setPriezvisko(rs.getString("priezvisko"));
        p.setEmail(rs.getString("email"));
        p.setHesloHash(rs.getString("heslo_hash"));
        p.setTyp(typ);
        return p;
    }
}
