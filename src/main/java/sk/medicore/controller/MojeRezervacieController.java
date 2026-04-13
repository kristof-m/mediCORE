package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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

public class MojeRezervacieController {

    @FXML private SidebarPacientController sidebarController;
    @FXML private VBox upcomingContainer;
    @FXML private VBox pastContainer;
    @FXML private VBox cancelledContainer;

    private final RezervaciaDAO rezervaciaDAO = new RezervaciaDAO();
    private final LekarDAO lekarDAO = new LekarDAO();
    private final ProceduraDAO proceduraDAO = new ProceduraDAO();
    private final TerminDAO terminDAO = new TerminDAO();

    @FXML
    public void initialize() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        sidebarController.setActivePage("rezervacje");
        loadRezervacje(user.getId());
    }

    private void loadRezervacje(int pacientId) {
        List<Rezervacia> all = rezervaciaDAO.findByPacientId(pacientId);
        LocalDateTime now = LocalDateTime.now();

        upcomingContainer.getChildren().clear();
        pastContainer.getChildren().clear();
        cancelledContainer.getChildren().clear();

        for (Rezervacia r : all) {
            LocalDateTime terminTime = getTerminTime(r.getTerminId());
            VBox card;
            if (r.getStav() == Rezervacia.Stav.ZRUSENA) {
                card = buildCard(r, "Zrušená", "#757575", "#f5f5f5", false);
                cancelledContainer.getChildren().add(card);
            } else if (terminTime.isBefore(now)) {
                card = buildCard(r, "Absolvovaná", "#388e3c", "#e8f5e9", false, true);
                pastContainer.getChildren().add(card);
            } else {
                card = buildCard(r, "Nadchádzajúca", "#1a9e8f", "#e8f5f3", true);
                upcomingContainer.getChildren().add(card);
            }
        }

        if (upcomingContainer.getChildren().isEmpty()) {
            upcomingContainer.getChildren().add(emptyLabel("Žiadne nadchádzajúce rezervácie.", true));
        }
        if (pastContainer.getChildren().isEmpty()) {
            pastContainer.getChildren().add(emptyLabel("Žiadne minulé rezervácie.", false));
        }
        if (cancelledContainer.getChildren().isEmpty()) {
            cancelledContainer.getChildren().add(emptyLabel("Žiadne zrušené rezervácie.", false));
        }
    }

    private VBox buildCard(Rezervacia r, String statusText, String statusColor, String statusBg, boolean showCancel) {
        return buildCard(r, statusText, statusColor, statusBg, showCancel, false);
    }

    private VBox buildCard(Rezervacia r, String statusText, String statusColor, String statusBg, boolean showCancel, boolean showBookAgain) {
        Lekar lekar = lekarDAO.findById(r.getLekarId());
        Procedura procedura = proceduraDAO.findAll().stream()
            .filter(p -> p.getId() == r.getProceduraId()).findFirst().orElse(null);
        Termin termin = terminDAO.findById(r.getTerminId());

        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        // Top row
        HBox topRow = new HBox(12);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label avatar = new Label(lekar != null ? lekar.getMeno().substring(0, 1) + lekar.getPriezvisko().substring(0, 1) : "??");
        avatar.setStyle("-fx-background-color: #1a9e8f; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40; -fx-background-radius: 20; -fx-alignment: CENTER;");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label doctorName = new Label(lekar != null ? "Dr. " + lekar.getCeleMeno() : "Neznámy lekár");
        doctorName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        Label specialty = new Label(lekar != null ? lekar.getSpecializacia() : "");
        specialty.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label procLabel = new Label(procedura != null ? procedura.getNazov() : "");
        procLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a9e8f;");
        info.getChildren().addAll(doctorName, specialty, procLabel);

        Label badge = new Label(statusText);
        badge.setStyle("-fx-background-color: " + statusBg + "; -fx-text-fill: " + statusColor + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4;");

        topRow.getChildren().addAll(avatar, info, badge);

        // Details
        HBox detailRow = new HBox(24);
        if (termin != null) {
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
            detailRow.getChildren().addAll(
                makeDetail("Dátum:", termin.getDatumCas().format(dateFmt)),
                makeDetail("Čas:", termin.getDatumCas().format(timeFmt))
            );
        }
        if (lekar != null) {
            String location = lekarDAO.getLocation(lekar.getId());
            if (!location.isBlank()) {
                detailRow.getChildren().add(makeDetail("Miesto:", location));
            }
        }

        card.getChildren().addAll(topRow, detailRow);

        if (showCancel || showBookAgain) {
            HBox btnRow = new HBox(10);
            btnRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            if (showBookAgain) {
                Button bookAgain = new Button("Rezervovať znovu");
                bookAgain.setStyle("-fx-background-color: #1a9e8f; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 14; -fx-cursor: hand;");
                bookAgain.setOnAction(e -> {
                    Stage stage = (Stage) bookAgain.getScene().getWindow();
                    SceneManager.switchTo(stage, "/view/rezervacia-wizard.fxml");
                });
                btnRow.getChildren().add(bookAgain);
            }
            if (showCancel) {
                Button rescheduleBtn = new Button("Presunúť");
                rescheduleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1a9e8f; -fx-border-color: #1a9e8f; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 14; -fx-cursor: hand;");
                rescheduleBtn.setOnAction(e -> {
                    SessionManager.getInstance().setRezervaciaToReschedule(r);
                    Stage stage = (Stage) rescheduleBtn.getScene().getWindow();
                    SceneManager.switchTo(stage, "/view/rezervacia-presun.fxml");
                });
                Button cancelBtn = new Button("Zrušiť");
                cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d32f2f; -fx-border-color: #d32f2f; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 14; -fx-cursor: hand;");
                cancelBtn.setOnAction(e -> handleCancel(r));
                btnRow.getChildren().addAll(rescheduleBtn, cancelBtn);
            }
            card.getChildren().add(btnRow);
        }

        return card;
    }

    private HBox makeDetail(String key, String value) {
        HBox box = new HBox(4);
        Label k = new Label(key);
        k.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #444;");
        box.getChildren().addAll(k, v);
        return box;
    }

    private javafx.scene.Node emptyLabel(String text, boolean showBookButton) {
        VBox box = new VBox(12);
        box.setStyle("-fx-padding: 20 0;");
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaa;");
        box.getChildren().add(l);
        if (showBookButton) {
            Button btn = new Button("+ Rezervovať termín");
            btn.setStyle("-fx-background-color: #1a9e8f; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
            btn.setOnAction(e -> handleNavRezervovat());
            box.getChildren().add(btn);
        }
        return box;
    }

    private void handleCancel(Rezervacia r) {
        Lekar lekar = lekarDAO.findById(r.getLekarId());
        Termin termin = terminDAO.findById(r.getTerminId());
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
            terminDAO.updateStav(r.getTerminId(), Termin.Stav.DOSTUPNY);
            loadRezervacje(SessionManager.getInstance().getCurrentUser().getId());
        }
    }

    private LocalDateTime getTerminTime(int terminId) {
        Termin t = terminDAO.findById(terminId);
        return t != null ? t.getDatumCas() : LocalDateTime.MIN;
    }

    @FXML private void handleNavRezervovat() {
        Stage stage = (Stage) upcomingContainer.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/rezervacia-wizard.fxml");
    }
}
