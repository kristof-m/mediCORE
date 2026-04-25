package sk.medicore.db.dao;

import sk.medicore.db.DatabaseManager;
import sk.medicore.model.Termin;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TerminDAO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Enriched view of a termin joined with its active reservation (if any). */
    public record TerminInfo(
        int terminId,
        LocalDateTime datumCas,
        int trvanieMin,
        Termin.Stav stav,
        Integer rezervaciaId,      // null if PUBLIKOVANY/ZRUSENY/UKONCENY
        String pacientMeno,        // null if no reservation
        String pacientPriezvisko,  // null if no reservation
        String proceduraNazov      // null if no reservation
    ) {}

    public List<TerminInfo> findEnrichedByLekarId(int lekarId) {
        autoTransitionUkonceny();
        String sql = "SELECT t.id, t.datum_cas, t.trvanie_min, t.stav, " +
                     "r.id AS rez_id, p.meno, p.priezvisko, proc.nazov AS proc_nazov " +
                     "FROM terminy t " +
                     "LEFT JOIN rezervacie r ON t.id = r.termin_id AND r.stav IN ('POTVRDENA','UKONCENA') " +
                     "LEFT JOIN pouzivatelia p ON r.pacient_id = p.id " +
                     "LEFT JOIN procedury proc ON r.procedura_id = proc.id " +
                     "WHERE t.lekar_id = ? ORDER BY t.datum_cas DESC";
        return queryEnriched(sql, lekarId, null, null);
    }

    public void autoTransitionUkonceny() {
        String sql = "UPDATE terminy SET stav = 'UKONCENY' WHERE stav IN ('REZERVOVANY','PUBLIKOVANY') AND datum_cas < datetime('now','localtime')";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TerminInfo> findEnrichedForWeek(int lekarId, LocalDate from, LocalDate to) {
        autoTransitionUkonceny();
        String sql = "SELECT t.id, t.datum_cas, t.trvanie_min, t.stav, " +
                     "r.id AS rez_id, p.meno, p.priezvisko, proc.nazov AS proc_nazov " +
                     "FROM terminy t " +
                     "LEFT JOIN rezervacie r ON t.id = r.termin_id AND r.stav IN ('POTVRDENA','UKONCENA') " +
                     "LEFT JOIN pouzivatelia p ON r.pacient_id = p.id " +
                     "LEFT JOIN procedury proc ON r.procedura_id = proc.id " +
                     "WHERE t.lekar_id = ? AND DATE(t.datum_cas) >= ? AND DATE(t.datum_cas) <= ? " +
                     "AND t.stav != 'ZRUSENY' ORDER BY t.datum_cas";
        return queryEnriched(sql, lekarId, from.toString(), to.toString());
    }

    public boolean hasConflict(int lekarId, LocalDateTime datumCas) {
        String sql = "SELECT COUNT(*) FROM terminy WHERE lekar_id = ? AND datum_cas = ? AND stav != 'ZRUSENY'";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, lekarId);
            ps.setString(2, datumCas.format(FMT));
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasConflictExcluding(int lekarId, LocalDateTime datumCas, int excludeTerminId) {
        String sql = "SELECT COUNT(*) FROM terminy WHERE lekar_id = ? AND datum_cas = ? AND stav != 'ZRUSENY' AND id != ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, lekarId);
            ps.setString(2, datumCas.format(FMT));
            ps.setInt(3, excludeTerminId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateDatumCas(int id, LocalDateTime novyDatumCas) {
        String sql = "UPDATE terminy SET datum_cas = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, novyDatumCas.format(FMT));
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insert(int lekarId, LocalDateTime datumCas, int trvanieMin) {
        String sql = "INSERT INTO terminy (lekar_id, datum_cas, trvanie_min, stav) VALUES (?, ?, ?, 'PUBLIKOVANY')";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, lekarId);
            ps.setString(2, datumCas.format(FMT));
            ps.setInt(3, trvanieMin);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<TerminInfo> queryEnriched(String sql, int lekarId, String from, String to) {
        List<TerminInfo> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, lekarId);
            if (from != null) { ps.setString(2, from); ps.setString(3, to); }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int rezId = rs.getInt("rez_id");
                Integer rezervaciaId = rs.wasNull() ? null : rezId;
                list.add(new TerminInfo(
                    rs.getInt("id"),
                    LocalDateTime.parse(rs.getString("datum_cas"), FMT),
                    rs.getInt("trvanie_min"),
                    Termin.Stav.valueOf(rs.getString("stav")),
                    rezervaciaId,
                    rs.getString("meno"),
                    rs.getString("priezvisko"),
                    rs.getString("proc_nazov")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<Termin> findByLekarId(int lekarId) {
        String sql = "SELECT * FROM terminy WHERE lekar_id = ? ORDER BY datum_cas";
        return query(sql, lekarId);
    }

    public List<Termin> findAvailable(int lekarId) {
        String sql = "SELECT * FROM terminy WHERE lekar_id = ? AND stav = 'PUBLIKOVANY' AND datum_cas > datetime('now','localtime') ORDER BY datum_cas";
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
