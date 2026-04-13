package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sk.medicore.db.dao.TerminDAO;
import sk.medicore.model.Lekar;
import sk.medicore.model.Termin;
import sk.medicore.util.SceneManager;
import sk.medicore.util.SessionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LekarTerminyController {

    @FXML private Label sidebarMenoLabel;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> hourPicker;
    @FXML private ComboBox<String> minutePicker;
    @FXML private ComboBox<String> durationPicker;
    @FXML private Label feedbackLabel;
    @FXML private VBox terminyContainer;
    @FXML private Label emptyLabel;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TerminDAO terminDAO = new TerminDAO();
    private Lekar lekar;

    @FXML
    public void initialize() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;
        lekar = (Lekar) user;

        sidebarMenoLabel.setText("Dr. " + lekar.getCeleMeno());
        datePicker.setValue(LocalDate.now());

        for (int h = 7; h <= 18; h++) {
            hourPicker.getItems().add(String.format("%02d", h));
        }
        hourPicker.setValue("08");

        minutePicker.getItems().addAll("00", "15", "30", "45");
        minutePicker.setValue("00");

        durationPicker.getItems().addAll("15 min", "20 min", "30 min", "45 min", "60 min", "90 min");
        durationPicker.setValue("30 min");

        loadTerminy();
    }

    @FXML
    private void handleAdd() {
        feedbackLabel.setText("");
        feedbackLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #d32f2f;");

        LocalDate date = datePicker.getValue();
        if (date == null) {
            feedbackLabel.setText("Vyberte dátum.");
            return;
        }
        if (date.isBefore(LocalDate.now())) {
            feedbackLabel.setText("Dátum musí byť dnes alebo v budúcnosti.");
            return;
        }

        int hour = Integer.parseInt(hourPicker.getValue());
        int minute = Integer.parseInt(minutePicker.getValue());
        LocalDateTime datumCas = date.atTime(hour, minute);

        if (datumCas.isBefore(LocalDateTime.now())) {
            feedbackLabel.setText("Čas musí byť v budúcnosti.");
            return;
        }

        if (terminDAO.hasConflict(lekar.getId(), datumCas)) {
            feedbackLabel.setText("V tomto čase už existuje termín.");
            return;
        }

        int trvanie = Integer.parseInt(durationPicker.getValue().split(" ")[0]);
        terminDAO.insert(lekar.getId(), datumCas, trvanie);
        feedbackLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a9e8f;");
        feedbackLabel.setText("Termín pridaný.");
        loadTerminy();
    }

    private void loadTerminy() {
        terminyContainer.getChildren().clear();
        var list = terminDAO.findEnrichedByLekarId(lekar.getId());

        if (list.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }

        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        for (var info : list) {
            terminyContainer.getChildren().add(buildRow(info));
        }
    }

    private HBox buildRow(TerminDAO.TerminInfo info) {
        HBox row = new HBox(12);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);");

        Label dateLabel = new Label(info.datumCas().format(DT_FMT));
        dateLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e; -fx-min-width: 130;");

        Label durLabel = new Label(info.trvanieMin() + " min");
        durLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-min-width: 60;");

        Label stavLabel = buildStavBadge(info.stav());
        HBox.setHgrow(stavLabel, Priority.ALWAYS);

        String detail = "";
        if (info.pacientMeno() != null) {
            detail = info.pacientMeno() + " " + info.pacientPriezvisko();
            if (info.proceduraNazov() != null) detail += " — " + info.proceduraNazov();
        }
        Label detailLabel = new Label(detail);
        detailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        HBox.setHgrow(detailLabel, Priority.ALWAYS);

        row.getChildren().addAll(dateLabel, durLabel, stavLabel, detailLabel);

        if (info.stav() == Termin.Stav.DOSTUPNY) {
            Button cancelBtn = new Button("Zrušiť");
            cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d32f2f; -fx-border-color: #d32f2f; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 12px; -fx-padding: 4 12; -fx-cursor: hand;");
            cancelBtn.setOnAction(e -> {
                terminDAO.updateStav(info.terminId(), Termin.Stav.ZRUSENY);
                loadTerminy();
            });
            row.getChildren().add(cancelBtn);
        }

        return row;
    }

    private Label buildStavBadge(Termin.Stav stav) {
        Label badge = new Label();
        switch (stav) {
            case DOSTUPNY -> {
                badge.setText("Dostupný");
                badge.setStyle("-fx-background-color: #e8f5f3; -fx-text-fill: #1a9e8f; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;");
            }
            case REZERVOVANY -> {
                badge.setText("Rezervovaný");
                badge.setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #e65100; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;");
            }
            case ZRUSENY -> {
                badge.setText("Zrušený");
                badge.setStyle("-fx-background-color: #fce4ec; -fx-text-fill: #c62828; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;");
            }
        }
        return badge;
    }

    @FXML
    private void handleNavKalendar() {
        Stage stage = (Stage) sidebarMenoLabel.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/lekar-kalendar.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) sidebarMenoLabel.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/prihlasenie.fxml");
    }
}
