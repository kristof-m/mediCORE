package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.Group;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import sk.medicore.db.dao.LekarDAO;
import sk.medicore.db.dao.ProceduraDAO;
import sk.medicore.db.dao.RezervaciaDAO;
import sk.medicore.db.dao.TerminDAO;
import sk.medicore.model.Lekar;
import sk.medicore.model.Procedura;
import sk.medicore.model.Rezervacia;
import sk.medicore.model.Termin;
import sk.medicore.notifikator.Notifikacia;
import sk.medicore.notifikator.Notifikator;
import sk.medicore.util.SceneManager;
import sk.medicore.util.SessionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DashboardController {

    @FXML private SidebarPacientController sidebarController;
    @FXML private Label greetingLabel;
    @FXML private Label statNadchadzajuce;
    @FXML private Label statAbsolvovane;
    @FXML private Label statZrusene;
    @FXML private Pane iconStatNadc;
    @FXML private Pane iconStatAbs;
    @FXML private Pane iconStatZrus;
    @FXML private VBox rezervacieContainer;
    @FXML private Label upcomingCountLabel;
    @FXML private VBox emptyLabel;
    @FXML private VBox notifikacieArea;

    private final RezervaciaDAO rezervaciaDAO = new RezervaciaDAO();
    private final LekarDAO lekarDAO = new LekarDAO();
    private final ProceduraDAO proceduraDAO = new ProceduraDAO();
    private final TerminDAO terminDAO = new TerminDAO();

    @FXML
    public void initialize() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        greetingLabel.setText(getGreeting() + ", " + user.getMeno() + "!");
        sidebarController.setActivePage("dashboard");

        fillIcon(iconStatNadc, "M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2Z M16 2v4 M8 2v4 M3 10h18", "#1a9e8f");
        fillIcon(iconStatAbs,  "M20 6 9 17 4 12", "#388e3c");
        fillIcon(iconStatZrus, "M12 22C6.48 22 2 17.52 2 12S6.48 2 12 2s10 4.48 10 10-4.48 10-10 10zm3-13l-6 6M9 9l6 6", "#8a8f98");

        loadStats(user.getId());
        loadUpcoming(user.getId());
        loadNotifikacie(user.getId());
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

    private void loadNotifikacie(int pacientId) {
        var neprecitane = Notifikator.getNeprecitane(pacientId);
        notifikacieArea.getChildren().clear();
        if (neprecitane.isEmpty()) {
            notifikacieArea.setVisible(false);
            notifikacieArea.setManaged(false);
            return;
        }

        for (Notifikacia n : neprecitane) {
            String bg = "#e8f5f3", accent = "#1a9e8f";
            if (n.getTyp() == Notifikacia.Typ.ZRUSENA)   { bg = "#fce4ec"; accent = "#c62828"; }
            if (n.getTyp() == Notifikacia.Typ.PRESUNUTA) { bg = "#fff3e0"; accent = "#f57c00"; }

            HBox row = new HBox(10);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 6; -fx-padding: 10 14;");

            Pane icon = makeBellIcon(accent);

            Label msg = new Label(n.getSprava());
            msg.setStyle("-fx-font-size: 13px; -fx-text-fill: " + accent + "; -fx-font-weight: bold;");
            HBox.setHgrow(msg, Priority.ALWAYS);

            row.getChildren().addAll(icon, msg);
            notifikacieArea.getChildren().add(row);
        }

        Button dismiss = new Button("Zatvoriť notifikácie");
        dismiss.setStyle("-fx-background-color: transparent; -fx-text-fill: #888; -fx-font-size: 12px; -fx-cursor: hand; -fx-border-color: transparent;");
        dismiss.setOnAction(e -> {
            Notifikator.oznacVsetkyPrecitane(pacientId);
            notifikacieArea.setVisible(false);
            notifikacieArea.setManaged(false);
        });
        notifikacieArea.getChildren().add(dismiss);

        notifikacieArea.setVisible(true);
        notifikacieArea.setManaged(true);
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
        Lekar lekar = lekarDAO.findById(r.getLekarId());
        Termin termin = terminDAO.findById(r.getTerminId());

        if (termin != null && termin.getDatumCas().isBefore(LocalDateTime.now().plusHours(24))) {
            Alert blocked = new Alert(Alert.AlertType.WARNING);
            blocked.setTitle("Zrušenie nie je možné");
            blocked.setHeaderText("Rezerváciu nie je možné zrušiť menej ako 24 hodín pred termínom.");
            blocked.setContentText("V prípade potreby kontaktujte recepciu.");
            blocked.showAndWait();
            return;
        }

        String info = lekar != null ? "Dr. " + lekar.getCeleMeno() : "Neznámy lekár";
        if (termin != null) {
            info += " — " + DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(termin.getDatumCas());
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Zrušenie rezervácie");
        alert.setHeaderText("Naozaj chcete zrušiť rezerváciu?");
        alert.setContentText(info + "\n\nTáto akcia sa nedá vrátiť späť.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            rezervaciaDAO.updateStav(r.getId(), Rezervacia.Stav.ZRUSENA);
            terminDAO.updateStav(r.getTerminId(), Termin.Stav.PUBLIKOVANY);
            Notifikator.odosliNotifikaciu(r.getPacientId(), Notifikacia.Typ.ZRUSENA);
            int uid = SessionManager.getInstance().getCurrentUser().getId();
            loadStats(uid);
            loadUpcoming(uid);
            loadNotifikacie(uid);
        }
    }

    private LocalDateTime getTerminTime(int terminId) {
        Termin t = terminDAO.findById(terminId);
        return t != null ? t.getDatumCas() : LocalDateTime.MIN;
    }

    @FXML
    private void handleNavRezervovat() {
        Stage stage = (Stage) greetingLabel.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/rezervacia-wizard.fxml");
    }

    private String getGreeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) return "Dobré ráno";
        if (hour < 18) return "Dobrý deň";
        return "Dobrý večer";
    }

    private static void fillIcon(Pane tile, String pathData, String strokeColor) {
        tile.getChildren().clear();
        SVGPath svg = new SVGPath();
        svg.setContent(pathData);
        svg.setFill(Color.TRANSPARENT);
        svg.setStroke(Color.web(strokeColor));
        svg.setStrokeWidth(2.0);
        svg.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        svg.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        svg.getTransforms().add(new Scale(17.0 / 24, 17.0 / 24, 0, 0));
        StackPane center = new StackPane(new Group(svg));
        center.setMinSize(34, 34);
        center.setMaxSize(34, 34);
        tile.getChildren().add(center);
    }

    private static Pane makeBellIcon(String strokeColor) {
        SVGPath svg = new SVGPath();
        svg.setContent("M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9 M13.73 21a2 2 0 0 1-3.46 0");
        svg.setFill(Color.TRANSPARENT);
        svg.setStroke(Color.web(strokeColor));
        svg.setStrokeWidth(2.0);
        svg.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        svg.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        svg.getTransforms().add(new Scale(16.0 / 24, 16.0 / 24, 0, 0));
        StackPane pane = new StackPane(new Group(svg));
        pane.setMinSize(16, 16);
        pane.setMaxSize(16, 16);
        return pane;
    }

}
