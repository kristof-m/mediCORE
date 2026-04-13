package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sk.medicore.db.DatabaseManager;
import sk.medicore.model.Lekar;
import sk.medicore.util.SceneManager;
import sk.medicore.util.SessionManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LekarDashboardController {

    @FXML private Label greetingLabel;
    @FXML private Label todayLabel;
    @FXML private Label sidebarMenoLabel;
    @FXML private Label statDnes;
    @FXML private Label statPacienti;
    @FXML private VBox todayContainer;
    @FXML private Label emptyLabel;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private record AppointmentInfo(
        String patientName,
        String proceduraNazov,
        String cas,
        String miestnost
    ) {}

    @FXML
    public void initialize() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        Lekar lekar = (Lekar) user;

        sidebarMenoLabel.setText("Dr. " + lekar.getCeleMeno());
        greetingLabel.setText(getGreeting() + ", Dr. " + lekar.getPriezvisko() + "!");

        LocalDate today = LocalDate.now();
        String dayName = today.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("sk"));
        todayLabel.setText(dayName.substring(0, 1).toUpperCase() + dayName.substring(1) + ", "
            + today.format(DateTimeFormatter.ofPattern("d. MMMM yyyy", new Locale("sk"))));

        loadStats(lekar.getId());
        loadTodayAppointments(lekar.getId());
    }

    private void loadStats(int lekarId) {
        int todayCount = 0;
        int patientCount = 0;

        String sqlToday = "SELECT COUNT(*) FROM rezervacie r " +
                          "JOIN terminy t ON r.termin_id = t.id " +
                          "WHERE r.lekar_id = ? AND DATE(t.datum_cas) = DATE('now','localtime') AND r.stav = 'POTVRDENA'";
        String sqlPatients = "SELECT COUNT(DISTINCT pacient_id) FROM rezervacie WHERE lekar_id = ? AND stav != 'ZRUSENA'";

        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sqlToday)) {
            ps.setInt(1, lekarId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) todayCount = rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }

        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sqlPatients)) {
            ps.setInt(1, lekarId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) patientCount = rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }

        statDnes.setText(String.valueOf(todayCount));
        statPacienti.setText(String.valueOf(patientCount));
    }

    private void loadTodayAppointments(int lekarId) {
        List<AppointmentInfo> items = fetchTodayAppointments(lekarId);
        todayContainer.getChildren().clear();

        if (items.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }

        for (AppointmentInfo a : items) {
            todayContainer.getChildren().add(buildCard(a));
        }
    }

    private List<AppointmentInfo> fetchTodayAppointments(int lekarId) {
        String sql = "SELECT p.meno, p.priezvisko, proc.nazov AS proc_nazov, " +
                     "t.datum_cas, prac.miestnost, prac.budova, prac.poschodie " +
                     "FROM rezervacie r " +
                     "JOIN pouzivatelia p ON r.pacient_id = p.id " +
                     "JOIN procedury proc ON r.procedura_id = proc.id " +
                     "JOIN terminy t ON r.termin_id = t.id " +
                     "LEFT JOIN lekari l ON r.lekar_id = l.id " +
                     "LEFT JOIN pracoviska prac ON l.pracovisko_id = prac.id " +
                     "WHERE r.lekar_id = ? " +
                     "AND DATE(t.datum_cas) = DATE('now','localtime') " +
                     "AND r.stav = 'POTVRDENA' " +
                     "ORDER BY t.datum_cas";

        List<AppointmentInfo> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, lekarId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String patientName = rs.getString("meno") + " " + rs.getString("priezvisko");
                String procedura = rs.getString("proc_nazov");
                String cas = LocalDateTime.parse(rs.getString("datum_cas"), DT_FMT).format(TIME_FMT);
                String miestnost = buildLocation(rs.getString("budova"), rs.getString("poschodie"), rs.getString("miestnost"));
                list.add(new AppointmentInfo(patientName, procedura, cas, miestnost));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private VBox buildCard(AppointmentInfo a) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        // Avatar + patient info
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
        Label procLabel = new Label(a.proceduraNazov());
        procLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a9e8f;");
        info.getChildren().addAll(nameLabel, procLabel);

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
        if (poschodie != null) sb.append(", Poschodie ").append(poschodie);
        if (miestnost != null) sb.append(" / Izba ").append(miestnost);
        return sb.toString();
    }

    @FXML
    private void handleNavKalendar() {
        Stage stage = (Stage) greetingLabel.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/lekar-kalendar.fxml");
    }

    @FXML
    private void handleNavTerminy() {
        Stage stage = (Stage) greetingLabel.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/lekar-terminy.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) greetingLabel.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/prihlasenie.fxml");
    }

    private String getGreeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) return "Dobré ráno";
        if (hour < 18) return "Dobrý deň";
        return "Dobrý večer";
    }
}
