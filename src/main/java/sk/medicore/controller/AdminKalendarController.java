package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import sk.medicore.db.dao.LekarDAO;
import sk.medicore.db.dao.RezervaciaDAO;
import sk.medicore.db.dao.TerminDAO;
import sk.medicore.model.Lekar;
import sk.medicore.model.Rezervacia;
import sk.medicore.model.Termin;
import sk.medicore.util.DateUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminKalendarController {

    @FXML private SidebarAdminController sidebarController;
    @FXML private Label weekRangeLabel;
    @FXML private VBox weekContainer;
    @FXML private ComboBox<Lekar> lekarCombo;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final TerminDAO terminDAO       = new TerminDAO();
    private final RezervaciaDAO rezervaciaDAO = new RezervaciaDAO();
    private final LekarDAO lekarDAO         = new LekarDAO();

    private Lekar selectedLekar;
    private LocalDate weekStart;

    @FXML
    public void initialize() {
        sidebarController.setActivePage("kalendar");
        weekStart = LocalDate.now().with(DayOfWeek.MONDAY);

        List<Lekar> lekari = lekarDAO.findAll();
        lekarCombo.getItems().setAll(lekari);
        lekarCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Lekar l)    { return l == null ? "" : "Dr. " + l.getCeleMeno(); }
            @Override public Lekar fromString(String s)  { return null; }
        });

        if (!lekari.isEmpty()) {
            lekarCombo.setValue(lekari.get(0));
            selectedLekar = lekari.get(0);
            loadWeek();
        } else {
            updateWeekLabel();
        }
    }

    @FXML
    private void handleLekarSelected() {
        Lekar chosen = lekarCombo.getValue();
        if (chosen == null || (selectedLekar != null && chosen.getId() == selectedLekar.getId())) return;
        selectedLekar = chosen;
        loadWeek();
    }

    @FXML
    private void handlePrevWeek() {
        weekStart = weekStart.minusWeeks(1);
        loadWeek();
    }

    @FXML
    private void handleNextWeek() {
        weekStart = weekStart.plusWeeks(1);
        loadWeek();
    }

    @FXML
    private void handleToday() {
        weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        loadWeek();
    }

    private void loadWeek() {
        updateWeekLabel();
        weekContainer.getChildren().clear();

        if (selectedLekar == null) return;

        LocalDate weekEnd = weekStart.plusDays(6);
        List<TerminDAO.TerminInfo> all = terminDAO.findEnrichedForWeek(selectedLekar.getId(), weekStart, weekEnd);

        if (all.isEmpty()) {
            Label msg = new Label("Tento týždeň " + "Dr. " + selectedLekar.getCeleMeno() + " nemá žiadne termíny.");
            msg.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaa; -fx-padding: 16 0;");
            weekContainer.getChildren().add(msg);
            return;
        }

        Map<LocalDate, List<TerminDAO.TerminInfo>> byDay = all.stream()
            .collect(Collectors.groupingBy(t -> t.datumCas().toLocalDate()));

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            List<TerminDAO.TerminInfo> dayItems = byDay.get(day);
            if (dayItems == null || dayItems.isEmpty()) continue;
            weekContainer.getChildren().add(buildDaySection(day, dayItems));
        }
    }

    private void updateWeekLabel() {
        LocalDate weekEnd = weekStart.plusDays(6);
        weekRangeLabel.setText(DateUtil.formatDate(weekStart) + " — " + DateUtil.formatDate(weekEnd));
    }

    private VBox buildDaySection(LocalDate day, List<TerminDAO.TerminInfo> items) {
        VBox section = new VBox(8);
        Label dayLabel = new Label(DateUtil.formatDayHeading(day));
        dayLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e; -fx-padding: 0 0 4 0;");
        section.getChildren().add(dayLabel);
        for (var info : items) {
            section.getChildren().add(buildTerminCard(info));
        }
        return section;
    }

    private HBox buildTerminCard(TerminDAO.TerminInfo info) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);

        boolean isRezerv = info.stav() == Termin.Stav.REZERVOVANY;
        String bg     = isRezerv ? "#fff8f0" : "white";
        String border = isRezerv ? "#ffcc80" : "#e8f5f3";
        card.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 8; -fx-padding: 12 16; " +
                      "-fx-border-color: " + border + "; -fx-border-radius: 8; -fx-border-width: 1.5;");

        Label timeLabel = new Label(info.datumCas().format(TIME_FMT));
        timeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e; -fx-min-width: 50;");

        Label durLabel = new Label(info.trvanieMin() + " min");
        durLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-min-width: 55;");

        VBox middle = new VBox(2);
        HBox.setHgrow(middle, Priority.ALWAYS);

        if (isRezerv && info.pacientMeno() != null) {
            Label patLabel = new Label(info.pacientMeno() + " " + info.pacientPriezvisko());
            patLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
            middle.getChildren().add(patLabel);
            if (info.proceduraNazov() != null) {
                Label procLabel = new Label(info.proceduraNazov());
                procLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e65100;");
                middle.getChildren().add(procLabel);
            }
        } else {
            Label freeLabel = new Label("Voľný termín");
            freeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #1a9e8f;");
            middle.getChildren().add(freeLabel);
        }

        Label stavBadge = buildStavBadge(info.stav());
        card.getChildren().addAll(timeLabel, durLabel, middle, stavBadge);

        if (isRezerv) {
            Button cancelBtn = new Button("Zrušiť");
            cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d32f2f; -fx-border-color: #d32f2f; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 12px; -fx-padding: 4 12; -fx-cursor: hand;");
            cancelBtn.setOnAction(e -> handleCancelRezerv(info));
            card.getChildren().add(cancelBtn);
        }

        return card;
    }

    private void handleCancelRezerv(TerminDAO.TerminInfo info) {
        if (info.rezervaciaId() == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Zrušiť rezerváciu");
        confirm.setHeaderText("Naozaj chcete zrušiť rezerváciu?");
        confirm.setContentText("Rezervácia pacienta " + info.pacientMeno() + " " + info.pacientPriezvisko()
            + " bude zrušená. Termín zostane dostupný.");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                rezervaciaDAO.updateStav(info.rezervaciaId(), Rezervacia.Stav.ZRUSENA);
                terminDAO.updateStav(info.terminId(), Termin.Stav.PUBLIKOVANY);
                loadWeek();
            }
        });
    }

    private Label buildStavBadge(Termin.Stav stav) {
        Label badge = new Label();
        switch (stav) {
            case PUBLIKOVANY  -> {
                badge.setText("Publikovaný");
                badge.setStyle("-fx-background-color: #e8f5f3; -fx-text-fill: #1a9e8f; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;");
            }
            case REZERVOVANY -> {
                badge.setText("Rezervovaný");
                badge.setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #e65100; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;");
            }
            case UKONCENY -> {
                badge.setText("Ukončený");
                badge.setStyle("-fx-background-color: #ede7f6; -fx-text-fill: #4527a0; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;");
            }
            case ZRUSENY -> {
                badge.setText("Zrušený");
                badge.setStyle("-fx-background-color: #fce4ec; -fx-text-fill: #c62828; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;");
            }
        }
        return badge;
    }
}
