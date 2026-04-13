package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sk.medicore.db.dao.LekarDAO;
import sk.medicore.db.dao.ProceduraDAO;
import sk.medicore.db.dao.RezervaciaDAO;
import sk.medicore.db.dao.TerminDAO;
import sk.medicore.model.Lekar;
import sk.medicore.model.Procedura;
import sk.medicore.model.Rezervacia;
import sk.medicore.model.Termin;
import sk.medicore.util.SceneManager;
import sk.medicore.util.SessionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DashboardController {

    @FXML private Label greetingLabel;
    @FXML private Label sidebarMenoLabel;
    @FXML private Label sidebarTypLabel;
    @FXML private Label statNadchadzajuce;
    @FXML private Label statAbsolvovane;
    @FXML private Label statZrusene;
    @FXML private VBox rezervacieContainer;
    @FXML private Label upcomingCountLabel;
    @FXML private Label emptyLabel;

    private final RezervaciaDAO rezervaciaDAO = new RezervaciaDAO();
    private final LekarDAO lekarDAO = new LekarDAO();
    private final ProceduraDAO proceduraDAO = new ProceduraDAO();
    private final TerminDAO terminDAO = new TerminDAO();

    @FXML
    public void initialize() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        String greeting = getGreeting() + ", " + user.getMeno() + "!";
        greetingLabel.setText(greeting);
        sidebarMenoLabel.setText(user.getCeleMeno());
        sidebarTypLabel.setText(formatTyp(user.getTyp()));

        loadStats(user.getId());
        loadUpcoming(user.getId());
    }

    private void loadStats(int pacientId) {
        List<Rezervacia> all = rezervaciaDAO.findByPacientId(pacientId);
        LocalDateTime now = LocalDateTime.now();

        long completed = all.stream().filter(r ->
            r.getStav() == Rezervacia.Stav.POTVRDENA &&
            getTerminTime(r.getTerminId()).isBefore(now)).count();
        long upcoming2 = all.stream().filter(r ->
            r.getStav() == Rezervacia.Stav.POTVRDENA &&
            getTerminTime(r.getTerminId()).isAfter(now)).count();
        long cancelled = all.stream().filter(r ->
            r.getStav() == Rezervacia.Stav.ZRUSENA).count();

        statNadchadzajuce.setText(String.valueOf(upcoming2));
        statAbsolvovane.setText(String.valueOf(completed));
        statZrusene.setText(String.valueOf(cancelled));
    }

    private void loadUpcoming(int pacientId) {
        List<Rezervacia> all = rezervaciaDAO.findByPacientId(pacientId);
        LocalDateTime now = LocalDateTime.now();

        List<Rezervacia> upcoming = all.stream()
            .filter(r -> r.getStav() == Rezervacia.Stav.POTVRDENA)
            .filter(r -> getTerminTime(r.getTerminId()).isAfter(now))
            .toList();

        rezervacieContainer.getChildren().clear();

        if (upcoming.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            upcomingCountLabel.setText("");
            return;
        }

        upcomingCountLabel.setText(upcoming.size() + " rezervácií");
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        for (Rezervacia r : upcoming) {
            rezervacieContainer.getChildren().add(buildRezervaciiaCard(r));
        }
    }

    private VBox buildRezervaciiaCard(Rezervacia r) {
        Lekar lekar = lekarDAO.findById(r.getLekarId());
        Procedura procedura = proceduraDAO.findAll().stream()
            .filter(p -> p.getId() == r.getProceduraId()).findFirst().orElse(null);
        Termin termin = terminDAO.findById(r.getTerminId());

        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        // Top row: doctor info + status badge
        HBox topRow = new HBox(12);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Avatar circle
        Label avatar = new Label(lekar != null ? lekar.getMeno().substring(0, 1) + lekar.getPriezvisko().substring(0, 1) : "??");
        avatar.setStyle("-fx-background-color: #1a9e8f; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40; -fx-background-radius: 20; -fx-alignment: CENTER;");

        VBox doctorInfo = new VBox(2);
        HBox.setHgrow(doctorInfo, Priority.ALWAYS);
        Label doctorName = new Label(lekar != null ? "Dr. " + lekar.getCeleMeno() : "Neznámy lekár");
        doctorName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        Label specialty = new Label(lekar != null ? lekar.getSpecializacia() : "");
        specialty.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label procedureName = new Label(procedura != null ? procedura.getNazov() : "");
        procedureName.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a9e8f;");
        doctorInfo.getChildren().addAll(doctorName, specialty, procedureName);

        Label badge = new Label("Nadchádzajúce");
        badge.setStyle("-fx-background-color: #e8f5f3; -fx-text-fill: #1a9e8f; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4;");

        topRow.getChildren().addAll(avatar, doctorInfo, badge);

        // Detail row
        HBox detailRow = new HBox(24);
        detailRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        if (termin != null) {
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
            detailRow.getChildren().addAll(
                makeDetailLabel("Dátum: ", termin.getDatumCas().format(dateFmt)),
                makeDetailLabel("Čas: ", termin.getDatumCas().format(timeFmt))
            );
        }
        if (lekar != null) {
            String location = lekarDAO.getLocation(lekar.getId());
            if (!location.isBlank()) {
                detailRow.getChildren().add(makeDetailLabel("Miesto: ", location));
            }
        }

        // Action buttons
        HBox btnRow = new HBox(10);
        btnRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Button reschedule = new Button("Presunúť");
        reschedule.setStyle("-fx-background-color: transparent; -fx-text-fill: #1a9e8f; -fx-border-color: #1a9e8f; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 14; -fx-cursor: hand;");
        reschedule.setOnAction(e -> {
            SessionManager.getInstance().setRezervaciaToReschedule(r);
            Stage stage = (Stage) reschedule.getScene().getWindow();
            SceneManager.switchTo(stage, "/view/rezervacia-presun.fxml");
        });

        Button cancel = new Button("Zrušiť");
        cancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #d32f2f; -fx-border-color: #d32f2f; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 14; -fx-cursor: hand;");
        cancel.setOnAction(e -> handleCancel(r));

        btnRow.getChildren().addAll(reschedule, cancel);

        card.getChildren().addAll(topRow, detailRow, btnRow);
        return card;
    }

    private HBox makeDetailLabel(String key, String value) {
        HBox box = new HBox(4);
        Label k = new Label(key);
        k.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #444;");
        box.getChildren().addAll(k, v);
        return box;
    }

    private void handleCancel(Rezervacia r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Zrušenie rezervácie");
        alert.setHeaderText("Naozaj chcete zrušiť rezerváciu?");
        alert.setContentText("Táto akcia sa nedá vrátiť späť.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            rezervaciaDAO.updateStav(r.getId(), Rezervacia.Stav.ZRUSENA);
            terminDAO.updateStav(r.getTerminId(), Termin.Stav.DOSTUPNY);
            loadStats(SessionManager.getInstance().getCurrentUser().getId());
            loadUpcoming(SessionManager.getInstance().getCurrentUser().getId());
        }
    }

    private LocalDateTime getTerminTime(int terminId) {
        Termin t = terminDAO.findById(terminId);
        return t != null ? t.getDatumCas() : LocalDateTime.MIN;
    }

    @FXML private void handleNavDashboard() {}

    @FXML
    private void handleNavRezervacje() {
        Stage stage = (Stage) greetingLabel.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/moje-rezervacie.fxml");
    }

    @FXML
    private void handleNavRezervovat() {
        Stage stage = (Stage) greetingLabel.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/rezervacia-wizard.fxml");
    }

    @FXML
    private void handleNavKalendar() {
        Stage stage = (Stage) greetingLabel.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/patient-kalendar.fxml");
    }

    @FXML
    private void handleNavProfil() {
        Stage stage = (Stage) greetingLabel.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/profil.fxml");
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

    private String formatTyp(String typ) {
        return switch (typ) {
            case "LEKAR" -> "Lekár";
            case "ADMIN" -> "Administrátor";
            default -> "Pacient";
        };
    }
}
