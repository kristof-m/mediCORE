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

    public void autoTransitionUkoncena() {
        String sql = "UPDATE rezervacie SET stav = 'UKONCENA' " +
                     "WHERE stav = 'POTVRDENA' " +
                     "AND termin_id IN (SELECT id FROM terminy WHERE datum_cas < datetime('now','localtime'))";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Rezervacia> findByPacientId(int pacientId) {
        autoTransitionUkoncena();
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
        String sql = "SELECT * FROM rezervacie WHERE termin_id = ? AND stav IN ('POTVRDENA','UKONCENA') LIMIT 1";
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

    public record PacientInfo(
        int pacientId,
        String meno,
        String priezvisko,
        String email,
        int visitCount,
        LocalDateTime lastVisit,
        LocalDateTime nextVisit
    ) {}

    public List<PacientInfo> findPacientiByLekarId(int lekarId) {
        String sql =
            "SELECT p.id, p.meno, p.priezvisko, p.email, " +
            "COUNT(DISTINCT r.id) as visit_count, " +
            "MAX(CASE WHEN datetime(t.datum_cas) <= datetime('now') THEN t.datum_cas ELSE NULL END) as last_visit, " +
            "MIN(CASE WHEN datetime(t.datum_cas) > datetime('now') THEN t.datum_cas ELSE NULL END) as next_visit " +
            "FROM pouzivatelia p " +
            "JOIN rezervacie r ON r.pacient_id = p.id " +
            "JOIN terminy t ON r.termin_id = t.id " +
            "WHERE r.lekar_id = ? AND r.stav != 'ZRUSENA' " +
            "GROUP BY p.id, p.meno, p.priezvisko, p.email " +
            "ORDER BY MAX(CASE WHEN datetime(t.datum_cas) <= datetime('now') THEN t.datum_cas ELSE NULL END) DESC";
        List<PacientInfo> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, lekarId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String lv = rs.getString("last_visit");
                String nv = rs.getString("next_visit");
                list.add(new PacientInfo(
                    rs.getInt("id"),
                    rs.getString("meno"),
                    rs.getString("priezvisko"),
                    rs.getString("email"),
                    rs.getInt("visit_count"),
                    lv != null ? LocalDateTime.parse(lv, FMT) : null,
                    nv != null ? LocalDateTime.parse(nv, FMT) : null
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public record RezervaciaInfo(
        int rezervaciaId,
        LocalDateTime datumCas,
        int trvanieMin,
        String lekarMeno,
        String lekarPriezvisko,
        String proceduraNazov,
        Rezervacia.Stav stav
    ) {}

    public List<RezervaciaInfo> findEnrichedByPacientIdForMonth(int pacientId, int year, int month) {
        autoTransitionUkoncena();
        String sql =
            "SELECT r.id, t.datum_cas, t.trvanie_min, " +
            "l.meno AS l_meno, l.priezvisko AS l_priezvisko, " +
            "proc.nazov AS proc_nazov, r.stav " +
            "FROM rezervacie r " +
            "JOIN terminy t ON r.termin_id = t.id " +
            "JOIN pouzivatelia l ON r.lekar_id = l.id " +
            "LEFT JOIN procedury proc ON r.procedura_id = proc.id " +
            "WHERE r.pacient_id = ? " +
            "AND strftime('%Y', t.datum_cas) = ? " +
            "AND strftime('%m', t.datum_cas) = ? " +
            "ORDER BY t.datum_cas";
        List<RezervaciaInfo> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, pacientId);
            ps.setString(2, String.format("%04d", year));
            ps.setString(3, String.format("%02d", month));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new RezervaciaInfo(
                    rs.getInt("id"),
                    LocalDateTime.parse(rs.getString("datum_cas"), FMT),
                    rs.getInt("trvanie_min"),
                    rs.getString("l_meno"),
                    rs.getString("l_priezvisko"),
                    rs.getString("proc_nazov"),
                    Rezervacia.Stav.valueOf(rs.getString("stav"))
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
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
