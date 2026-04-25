package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import sk.medicore.db.DatabaseManager;
import sk.medicore.util.DateUtil;
import sk.medicore.util.SessionManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardController {

    @FXML private SidebarAdminController sidebarController;
    @FXML private Label todayLabel;
    @FXML private Label statDnes;
    @FXML private Label statPacienti;
    @FXML private Label statLekari;
    @FXML private VBox todayContainer;
    @FXML private Label emptyLabel;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private record AppointmentInfo(
        String patientName,
        String doctorName,
        String proceduraNazov,
        String cas,
        String miestnost
    ) {}

    @FXML
    public void initialize() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        sidebarController.setActivePage("dashboard");
        todayLabel.setText(DateUtil.formatDayHeading(LocalDate.now()) + " · celoklinický prehľad");

        loadStats();
        loadTodayAppointments();
    }

    private void loadStats() {
        int todayCount = 0, patientCount = 0, lekarCount = 0;

        String sqlToday = "SELECT COUNT(*) FROM rezervacie r " +
                          "JOIN terminy t ON r.termin_id = t.id " +
                          "WHERE DATE(t.datum_cas) = DATE('now','localtime') AND r.stav IN ('POTVRDENA','UKONCENA')";
        String sqlPatients = "SELECT COUNT(DISTINCT pacient_id) FROM rezervacie WHERE stav != 'ZRUSENA'";
        String sqlLekari   = "SELECT COUNT(*) FROM lekari";

        try (var ps = DatabaseManager.getConnection().prepareStatement(sqlToday);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) todayCount = rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }

        try (var ps = DatabaseManager.getConnection().prepareStatement(sqlPatients);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) patientCount = rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }

        try (var ps = DatabaseManager.getConnection().prepareStatement(sqlLekari);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) lekarCount = rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }

        statDnes.setText(String.valueOf(todayCount));
        statPacienti.setText(String.valueOf(patientCount));
        statLekari.setText(String.valueOf(lekarCount));
    }

    private void loadTodayAppointments() {
        List<AppointmentInfo> items = fetchTodayAppointments();
        todayContainer.getChildren().clear();
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        if (items.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }

        for (AppointmentInfo a : items) {
            todayContainer.getChildren().add(buildCard(a));
        }
    }

    private List<AppointmentInfo> fetchTodayAppointments() {
        String sql = "SELECT p.meno AS p_meno, p.priezvisko AS p_priezvisko, " +
                     "lu.meno AS l_meno, lu.priezvisko AS l_priezvisko, " +
                     "proc.nazov AS proc_nazov, t.datum_cas, " +
                     "prac.miestnost, prac.budova, prac.poschodie " +
                     "FROM rezervacie r " +
                     "JOIN pouzivatelia p  ON r.pacient_id = p.id " +
                     "JOIN pouzivatelia lu ON r.lekar_id   = lu.id " +
                     "JOIN procedury proc  ON r.procedura_id = proc.id " +
                     "JOIN terminy t       ON r.termin_id  = t.id " +
                     "LEFT JOIN lekari l   ON r.lekar_id   = l.id " +
                     "LEFT JOIN pracoviska prac ON l.pracovisko_id = prac.id " +
                     "WHERE DATE(t.datum_cas) = DATE('now','localtime') " +
                     "AND r.stav IN ('POTVRDENA','UKONCENA') " +
                     "ORDER BY t.datum_cas";

        List<AppointmentInfo> list = new ArrayList<>();
        try (var ps = DatabaseManager.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String patient  = rs.getString("p_meno") + " " + rs.getString("p_priezvisko");
                String doctor   = "Dr. " + rs.getString("l_meno") + " " + rs.getString("l_priezvisko");
                String proc     = rs.getString("proc_nazov");
                String cas      = LocalDateTime.parse(rs.getString("datum_cas"), DT_FMT).format(TIME_FMT);
                String location = buildLocation(rs.getString("budova"), rs.getString("poschodie"), rs.getString("miestnost"));
                list.add(new AppointmentInfo(patient, doctor, proc, cas, location));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private VBox buildCard(AppointmentInfo a) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        HBox topRow = new HBox(12);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String initials = a.patientName().contains(" ")
            ? "" + a.patientName().charAt(0) + a.patientName().charAt(a.patientName().indexOf(' ') + 1)
            : a.patientName().substring(0, 1);
        Label avatar = new Label(initials.toUpperCase());
        avatar.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40; -fx-background-radius: 20; -fx-alignment: CENTER;");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label nameLabel = new Label(a.patientName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        Label doctorLabel = new Label(a.doctorName());
        doctorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label procLabel = new Label(a.proceduraNazov());
        procLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a9e8f;");
        info.getChildren().addAll(nameLabel, doctorLabel, procLabel);

        Label timeBadge = new Label(a.cas());
        timeBadge.setStyle("-fx-background-color: #e8f5f3; -fx-text-fill: #1a9e8f; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 6;");

        topRow.getChildren().addAll(avatar, info, timeBadge);
        card.getChildren().add(topRow);

        if (!a.miestnost().isBlank()) {
            Label locationLabel = new Label("Miesto: " + a.miestnost());
            locationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
            card.getChildren().add(locationLabel);
        }

        return card;
    }

    private String buildLocation(String budova, String poschodie, String miestnost) {
        StringBuilder sb = new StringBuilder();
        if (budova != null) sb.append(budova);
        if (poschodie != null) { if (sb.length() > 0) sb.append(", "); sb.append("Poschodie ").append(poschodie); }
        if (miestnost != null) { if (sb.length() > 0) sb.append(" / "); sb.append("Izba ").append(miestnost); }
        return sb.toString();
    }

    private String getGreeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) return "Dobré ráno";
        if (hour < 18) return "Dobrý deň";
        return "Dobrý večer";
    }
}
