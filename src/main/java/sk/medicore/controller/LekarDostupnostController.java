package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import sk.medicore.db.dao.TerminDAO;
import sk.medicore.model.Lekar;
import sk.medicore.model.Termin;
import sk.medicore.util.SessionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class LekarDostupnostController {

    @FXML private SidebarLekarController sidebarController;

    @FXML private Label weekLabel;
    @FXML private Label weekSubLabel;
    @FXML private GridPane headerGrid;
    @FXML private GridPane bodyGrid;

    @FXML private Label statVolneLabel;
    @FXML private Label statObsLabel;
    @FXML private Pane iconObsadene;

    @FXML private DatePicker quickDatePicker;
    @FXML private ComboBox<String> quickFromPicker;
    @FXML private ComboBox<String> quickToPicker;
    @FXML private ComboBox<Integer> quickDurationPicker;
    @FXML private Label addFeedbackLabel;

    private final TerminDAO terminDAO = new TerminDAO();
    private Lekar lekar;
    private LocalDate weekStart; // Monday of displayed week

    private static final DateTimeFormatter LBL_FMT = DateTimeFormatter.ofPattern("d. MMMM yyyy", new Locale("sk"));
    private static final int HOUR_START = 7;
    private static final int HOUR_END   = 18;

    @FXML
    public void initialize() {
        sidebarController.setActivePage("terminy");

        fillIconPane(iconObsadene, "M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2 M12 3a4 4 0 1 1 0 8 4 4 0 0 1 0-8z", "#92400e", 10.0 / 24, 14);

        lekar = (Lekar) SessionManager.getInstance().getCurrentUser();

        LocalDate today = LocalDate.now();
        weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);

        setupQuickAddControls();
        buildWeekView();
    }

    private void setupQuickAddControls() {
        quickFromPicker.getItems().clear();
        quickToPicker.getItems().clear();
        for (int h = HOUR_START; h <= HOUR_END; h++) {
            quickFromPicker.getItems().add(String.format("%02d:00", h));
            quickToPicker.getItems().add(String.format("%02d:00", h));
        }
        quickFromPicker.setValue("08:00");
        quickToPicker.setValue("16:00");
        quickDurationPicker.getItems().addAll(15, 20, 30, 45, 60, 90);
        quickDurationPicker.setValue(30);
        quickDatePicker.setValue(LocalDate.now());
    }

    private void buildWeekView() {
        LocalDate weekEnd = weekStart.plusDays(6);
        weekLabel.setText(weekStart.format(DateTimeFormatter.ofPattern("d.", new Locale("sk"))) + " – " +
                          weekEnd.format(LBL_FMT));

        List<TerminDAO.TerminInfo> slots = terminDAO.findEnrichedForWeek(lekar.getId(), weekStart, weekEnd);

        long volne = slots.stream().filter(s -> s.stav() == Termin.Stav.DOSTUPNY).count();
        long obs   = slots.stream().filter(s -> s.stav() == Termin.Stav.REZERVOVANY).count();
        statVolneLabel.setText(String.valueOf(volne));
        statObsLabel.setText(String.valueOf(obs));
        weekSubLabel.setText("Týždeň · " + volne + " voľných · " + obs + " obsadených");

        // Build header — clear all day columns, keep column-0 time label
        headerGrid.getChildren().removeIf(n -> {
            Integer col = GridPane.getColumnIndex(n);
            return col == null || col > 0;
        });
        for (int d = 0; d < 7; d++) {
            LocalDate day = weekStart.plusDays(d);
            VBox cell = new VBox(1);
            cell.setAlignment(Pos.CENTER);
            cell.setStyle("-fx-padding: 6 4;");
            String dow = day.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("sk"));
            Label dowLbl = new Label(dow.toUpperCase());
            boolean isToday = day.equals(LocalDate.now());
            dowLbl.setStyle("-fx-font-size: 9.5px; -fx-font-weight: 600; -fx-text-fill: " + (isToday ? "#1a9e8f" : "#9aa0a8") + ";");
            Label numLbl = new Label(String.valueOf(day.getDayOfMonth()));
            numLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + (isToday ? "#1a9e8f" : "#1a1a2e") + ";");
            cell.getChildren().addAll(dowLbl, numLbl);
            GridPane.setColumnIndex(cell, d + 1);
            GridPane.setRowIndex(cell, 0);
            headerGrid.getChildren().add(cell);
        }

        // Build body grid rows (one row per hour)
        bodyGrid.getChildren().clear();
        bodyGrid.getRowConstraints().clear();
        int row = 0;
        for (int h = HOUR_START; h < HOUR_END; h++) {
            RowConstraints rc = new RowConstraints(36);
            bodyGrid.getRowConstraints().add(rc);

            Label timeLbl = new Label(String.format("%02d:00", h));
            timeLbl.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #9aa0a8; -fx-font-weight: 500; -fx-padding: 4 4 0 4;");
            timeLbl.setAlignment(Pos.TOP_RIGHT);
            GridPane.setColumnIndex(timeLbl, 0);
            GridPane.setRowIndex(timeLbl, row);
            bodyGrid.getChildren().add(timeLbl);

            for (int d = 0; d < 7; d++) {
                LocalDate day = weekStart.plusDays(d);
                boolean isWeekend = d >= 5;

                final int hFinal = h;
                TerminDAO.TerminInfo slot = slots.stream()
                    .filter(s -> s.datumCas().toLocalDate().equals(day) && s.datumCas().getHour() == hFinal)
                    .findFirst().orElse(null);

                Pane cell = buildBodyCell(slot, isWeekend);
                GridPane.setColumnIndex(cell, d + 1);
                GridPane.setRowIndex(cell, row);
                bodyGrid.getChildren().add(cell);
            }
            row++;
        }
    }

    private Pane buildBodyCell(TerminDAO.TerminInfo slot, boolean isWeekend) {
        Pane cell = new Pane();
        cell.setMinHeight(36);
        cell.setPrefHeight(36);
        cell.setStyle("-fx-border-color: #f1f2f4 #f1f2f4 transparent transparent; -fx-border-width: 1 1 0 0;" +
                      (isWeekend ? "-fx-background-color: #fafafa;" : ""));

        if (slot != null) {
            Label chip = new Label();
            chip.setWrapText(false);
            chip.setMaxWidth(Double.MAX_VALUE);

            if (slot.stav() == Termin.Stav.REZERVOVANY && slot.pacientMeno() != null) {
                chip.setText(slot.datumCas().format(DateTimeFormatter.ofPattern("HH:mm")) +
                             " · " + slot.pacientMeno().charAt(0) + ". " + slot.pacientPriezvisko());
                chip.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e;" +
                              "-fx-font-size: 9px; -fx-font-weight: 600; -fx-padding: 3 5;" +
                              "-fx-background-radius: 4;" +
                              "-fx-border-color: #d97706; -fx-border-width: 0 0 0 2; -fx-border-radius: 0 4 4 0;");
            } else {
                chip.setText(slot.datumCas().format(DateTimeFormatter.ofPattern("HH:mm")) + " · Voľné");
                chip.setStyle("-fx-background-color: #e8f5f3; -fx-text-fill: #1a9e8f;" +
                              "-fx-font-size: 9px; -fx-font-weight: 600; -fx-padding: 3 5;" +
                              "-fx-background-radius: 4;" +
                              "-fx-border-color: #1a9e8f; -fx-border-width: 0 0 0 2; -fx-border-radius: 0 4 4 0;");
            }
            chip.layoutXProperty().set(2);
            chip.layoutYProperty().set(2);
            chip.prefWidthProperty().bind(cell.widthProperty().subtract(4));
            cell.getChildren().add(chip);
        } else if (!isWeekend) {
            cell.setOnMouseEntered(e -> cell.setStyle("-fx-background-color: #e8f5f3;" +
                "-fx-border-color: #f1f2f4 #f1f2f4 transparent transparent; -fx-border-width: 1 1 0 0;"));
            cell.setOnMouseExited(e -> cell.setStyle("-fx-border-color: #f1f2f4 #f1f2f4 transparent transparent; -fx-border-width: 1 1 0 0;"));
        }

        return cell;
    }

    @FXML private void handlePrevWeek() { weekStart = weekStart.minusWeeks(1); buildWeekView(); }
    @FXML private void handleNextWeek() { weekStart = weekStart.plusWeeks(1); buildWeekView(); }
    @FXML private void handleToday() {
        LocalDate today = LocalDate.now();
        weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        buildWeekView();
    }

    @FXML
    private void handleQuickAdd() {
        addFeedbackLabel.setVisible(false);
        addFeedbackLabel.setManaged(false);

        LocalDate date = quickDatePicker.getValue();
        String from = quickFromPicker.getValue();
        String to   = quickToPicker.getValue();
        Integer dur = quickDurationPicker.getValue();

        if (date == null || from == null || to == null || dur == null) {
            showFeedback("Vyplňte všetky polia.", "#d32f2f");
            return;
        }

        int fromH = Integer.parseInt(from.split(":")[0]);
        int toH   = Integer.parseInt(to.split(":")[0]);
        if (toH <= fromH) { showFeedback("Čas 'Do' musí byť po 'Od'.", "#d32f2f"); return; }

        int stepMinutes = Math.max(dur, 60); // never create overlapping slots
        int added = 0;
        for (int min = fromH * 60; min + dur <= toH * 60; min += stepMinutes) {
            LocalDateTime dt = date.atTime(min / 60, min % 60);
            if (!terminDAO.hasConflict(lekar.getId(), dt)) {
                terminDAO.insert(lekar.getId(), dt, dur);
                added++;
            }
        }

        showFeedback("Pridaných " + added + " termínov.", "#1a9e8f");

        LocalDate newWeekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
        weekStart = newWeekStart;
        buildWeekView();
    }

    private void showFeedback(String msg, String color) {
        addFeedbackLabel.setText(msg);
        addFeedbackLabel.setStyle("-fx-font-size: 11.5px; -fx-text-fill: " + color + "; -fx-wrap-text: true;");
        addFeedbackLabel.setVisible(true);
        addFeedbackLabel.setManaged(true);
    }

    private static void fillIconPane(Pane tile, String pathData, String strokeColor, double scale, double size) {
        tile.getChildren().clear();
        SVGPath svg = new SVGPath();
        svg.setContent(pathData);
        svg.setFill(Color.TRANSPARENT);
        svg.setStroke(Color.web(strokeColor));
        svg.setStrokeWidth(1.8);
        svg.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        svg.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        svg.getTransforms().add(new Scale(scale, scale, 0, 0));
        StackPane center = new StackPane(new Group(svg));
        center.setMinSize(size, size);
        center.setMaxSize(size, size);
        tile.getChildren().add(center);
    }
}
