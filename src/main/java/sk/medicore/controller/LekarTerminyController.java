package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import sk.medicore.db.dao.RezervaciaDAO;
import sk.medicore.db.dao.TerminDAO;
import sk.medicore.service.Kalendar;
import sk.medicore.model.Lekar;
import sk.medicore.model.Rezervacia;
import sk.medicore.model.Termin;
import sk.medicore.notifikator.Notifikacia;
import sk.medicore.notifikator.Notifikator;
import sk.medicore.util.SessionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class LekarTerminyController {

    @FXML private SidebarLekarController sidebarController;
    @FXML private Label formTitle;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> hourPicker;
    @FXML private ComboBox<String> minutePicker;
    @FXML private ComboBox<String> durationPicker;
    @FXML private Button submitBtn;
    @FXML private Button cancelEditBtn;
    @FXML private Label feedbackLabel;
    @FXML private VBox terminyContainer;
    @FXML private Label emptyLabel;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TerminDAO terminDAO = new TerminDAO();
    private final RezervaciaDAO rezervaciaDAO = new RezervaciaDAO();
    private Lekar lekar;
    private Kalendar kalendar;
    private Integer editingTerminId = null;

    @FXML
    public void initialize() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;
        lekar = (Lekar) user;
        kalendar = new Kalendar(lekar.getId());

        sidebarController.setActivePage("terminy");
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
        if (date == null) { feedbackLabel.setText("Vyberte dátum."); return; }
        if (date.isBefore(LocalDate.now())) { feedbackLabel.setText("Dátum musí byť dnes alebo v budúcnosti."); return; }

        int hour = Integer.parseInt(hourPicker.getValue());
        int minute = Integer.parseInt(minutePicker.getValue());
        LocalDateTime datumCas = date.atTime(hour, minute);

        if (datumCas.isBefore(LocalDateTime.now())) { feedbackLabel.setText("Čas musí byť v budúcnosti."); return; }

        if (editingTerminId != null) {
            saveEdit(datumCas);
            return;
        }

        if (kalendar.skontrolujKonflikt(datumCas)) {
            feedbackLabel.setText("V tomto čase už existuje termín.");
            return;
        }

        int trvanie = Integer.parseInt(durationPicker.getValue().split(" ")[0]);
        kalendar.pridajTermin(datumCas, trvanie);
        feedbackLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a9e8f;");
        feedbackLabel.setText("Termín pridaný.");
        loadTerminy();
    }

    private void saveEdit(LocalDateTime novyDatumCas) {
        if (kalendar.skontrolujKonflikt(novyDatumCas, editingTerminId)) {
            feedbackLabel.setText("V tomto čase už existuje iný termín.");
            return;
        }

        Rezervacia rezervacia = rezervaciaDAO.findByTerminId(editingTerminId);
        if (rezervacia != null) {
            Alert warning = new Alert(Alert.AlertType.CONFIRMATION);
            warning.setTitle("Termín má rezerváciu");
            warning.setHeaderText("Tento termín má aktívnu rezerváciu pacienta.");
            warning.setContentText("Pacient bude o zmene upozornený. Pokračovať?");
            Optional<ButtonType> result = warning.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) return;
        }

        kalendar.aktualizujTermin(editingTerminId, novyDatumCas);

        if (rezervacia != null) {
            Notifikator.odosliNotifikaciu(rezervacia.getPacientId(), Notifikacia.Typ.ZMENENY);
        }

        resetForm();
        feedbackLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a9e8f;");
        feedbackLabel.setText("Termín aktualizovaný.");
        loadTerminy();
    }

    @FXML
    private void handleCancelEdit() {
        resetForm();
        feedbackLabel.setText("");
    }

    private void resetForm() {
        editingTerminId = null;
        formTitle.setText("Pridať nový termín");
        submitBtn.setText("Pridať termín");
        cancelEditBtn.setVisible(false);
        cancelEditBtn.setManaged(false);
        datePicker.setValue(LocalDate.now());
        hourPicker.setValue("08");
        minutePicker.setValue("00");
        durationPicker.setValue("30 min");
    }

    private void startEdit(TerminDAO.TerminInfo info) {
        editingTerminId = info.terminId();
        formTitle.setText("Upraviť termín");
        submitBtn.setText("Uložiť zmenu");
        cancelEditBtn.setVisible(true);
        cancelEditBtn.setManaged(true);
        feedbackLabel.setText("");

        datePicker.setValue(info.datumCas().toLocalDate());
        hourPicker.setValue(String.format("%02d", info.datumCas().getHour()));
        String minuteStr = String.format("%02d", (info.datumCas().getMinute() / 15) * 15);
        minutePicker.setValue(minutePicker.getItems().contains(minuteStr) ? minuteStr : "00");
        String durStr = info.trvanieMin() + " min";
        durationPicker.setValue(durationPicker.getItems().contains(durStr) ? durStr : "30 min");
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

        if (info.stav() == Termin.Stav.DOSTUPNY || info.stav() == Termin.Stav.REZERVOVANY) {
            Button editBtn = new Button("Upraviť");
            editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1a9e8f; -fx-border-color: #1a9e8f; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 12px; -fx-padding: 4 12; -fx-cursor: hand;");
            editBtn.setOnAction(e -> startEdit(info));
            row.getChildren().add(editBtn);
        }

        if (info.stav() == Termin.Stav.DOSTUPNY) {
            Button cancelBtn = new Button("Zrušiť");
            cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d32f2f; -fx-border-color: #d32f2f; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 12px; -fx-padding: 4 12; -fx-cursor: hand;");
            cancelBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Zrušiť termín");
                confirm.setHeaderText("Naozaj chcete zrušiť tento termín?");
                confirm.setContentText(info.datumCas().format(DT_FMT) + " (" + info.trvanieMin() + " min)");
                confirm.showAndWait().ifPresent(result -> {
                    if (result == ButtonType.OK) {
                        terminDAO.updateStav(info.terminId(), Termin.Stav.ZRUSENY);
                        if (editingTerminId != null && editingTerminId == info.terminId()) resetForm();
                        loadTerminy();
                    }
                });
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
}
