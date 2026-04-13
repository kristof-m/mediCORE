package sk.medicore.db.dao;

import sk.medicore.db.DatabaseManager;
import sk.medicore.model.Procedura;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProceduraDAO {

    public List<Procedura> findAll() {
        String sql = "SELECT * FROM procedury ORDER BY nazov";
        List<Procedura> list = new ArrayList<>();
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

    public List<Procedura> findByLekarId(int lekarId) {
        String sql = "SELECT p.* FROM procedury p " +
                     "JOIN lekar_procedury lp ON p.id = lp.procedura_id " +
                     "WHERE lp.lekar_id = ? ORDER BY p.nazov";
        List<Procedura> list = new ArrayList<>();
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

    private Procedura mapRow(ResultSet rs) throws SQLException {
        return new Procedura(
            rs.getInt("id"),
            rs.getString("nazov"),
            rs.getInt("trvanie_min"),
            rs.getString("popis")
        );
    }
}
