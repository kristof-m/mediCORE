package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import sk.medicore.db.dao.RezervaciaDAO;
import sk.medicore.model.Rezervacia;
import sk.medicore.util.SessionManager;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class PatientKalendarController {

    @FXML private SidebarPacientController sidebarController;
    @FXML private Label monthLabel;
    @FXML private GridPane calGrid;
    @FXML private Label selectedDayLabel;
    @FXML private VBox sidePanelRows;
    @FXML private Label noApptsLabel;

    private YearMonth currentYearMonth;
    private int pacientId;
    private List<RezervaciaDAO.RezervaciaInfo> currentMonthData = List.of();
    private final RezervaciaDAO rezervaciaDAO = new RezervaciaDAO();

    private static final DateTimeFormatter TIME_FMT  = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("sk"));
    private static final String[] DOW = {"Po", "Ut", "St", "Št", "Pi", "So", "Ne"};

    @FXML
    public void initialize() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;
        pacientId = user.getId();
        sidebarController.setActivePage("kalendar");
        currentYearMonth = YearMonth.now();
        buildCalendar();
        showSidePanel(LocalDate.now(), filterForDate(LocalDate.now()));
    }

    @FXML
    private void handlePrev() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        buildCalendar();
        showSidePanel(currentYearMonth.atDay(1), filterForDate(currentYearMonth.atDay(1)));
    }

    @FXML
    private void handleNext() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        buildCalendar();
        showSidePanel(currentYearMonth.atDay(1), filterForDate(currentYearMonth.atDay(1)));
    }

    @FXML
    private void handleToday() {
        currentYearMonth = YearMonth.now();
        buildCalendar();
        showSidePanel(LocalDate.now(), filterForDate(LocalDate.now()));
    }

    private void buildCalendar() {
        calGrid.getChildren().clear();
        calGrid.getRowConstraints().clear();

        String raw = currentYearMonth.format(MONTH_FMT);
        monthLabel.setText(raw.substring(0, 1).toUpperCase() + raw.substring(1));

        // Load reservations for the month — stored in field so cell clicks reuse it
        currentMonthData = rezervaciaDAO.findEnrichedByPacientIdForMonth(
            pacientId, currentYearMonth.getYear(), currentYearMonth.getMonthValue());
        Map<LocalDate, List<RezervaciaDAO.RezervaciaInfo>> byDate = currentMonthData.stream()
            .collect(Collectors.groupingBy(r -> r.datumCas().toLocalDate()));

        // DOW header row
        calGrid.getRowConstraints().add(new RowConstraints(28));
        for (int col = 0; col < 7; col++) {
            Label h = new Label(DOW[col]);
            h.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #9aa0a8; -fx-alignment: CENTER; -fx-padding: 6 0;");
            h.setMaxWidth(Double.MAX_VALUE);
            GridPane.setFillWidth(h, true);
            calGrid.add(h, col, 0);
        }

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int startDow = firstOfMonth.getDayOfWeek().getValue() - 1; // Mon=0 … Sun=6
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int totalCells = (int) Math.ceil((startDow + daysInMonth) / 7.0) * 7;
        int totalRows = totalCells / 7;

        for (int r = 0; r < totalRows; r++) {
            RowConstraints rc = new RowConstraints(68);
            rc.setMinHeight(68);
            calGrid.getRowConstraints().add(rc);
        }

        for (int i = 0; i < totalCells; i++) {
            int col = i % 7;
            int gridRow = i / 7 + 1;

            LocalDate date;
            boolean muted;
            if (i < startDow) {
                date = firstOfMonth.minusDays(startDow - i);
                muted = true;
            } else if (i < startDow + daysInMonth) {
                date = currentYearMonth.atDay(i - startDow + 1);
                muted = false;
            } else {
                date = currentYearMonth.plusMonths(1).atDay(i - startDow - daysInMonth + 1);
                muted = true;
            }

            List<RezervaciaDAO.RezervaciaInfo> cellRezs = muted ? List.of() : byDate.getOrDefault(date, List.of());
            VBox cell = buildCell(date, muted, cellRezs);
            GridPane.setFillWidth(cell, true);
            GridPane.setFillHeight(cell, true);
            calGrid.add(cell, col, gridRow);
        }
    }

    private VBox buildCell(LocalDate date, boolean muted, List<RezervaciaDAO.RezervaciaInfo> rezs) {
        boolean isToday = !muted && date.equals(LocalDate.now());

        VBox cell = new VBox(3);
        cell.setPrefHeight(68);
        cell.setMinHeight(68);
        cell.setMaxWidth(Double.MAX_VALUE);

        String bg = muted ? "#fafafb" : "white";
        String border = isToday ? "#1a9e8f" : "#eef0f3";
        String extra = isToday ? " -fx-effect: dropshadow(gaussian, rgba(26,158,143,0.15), 4, 0, 0, 0);" : "";
        cell.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 7; " +
                      "-fx-border-color: " + border + "; -fx-border-radius: 7; -fx-border-width: 1; " +
                      "-fx-padding: 6 8; -fx-cursor: hand;" + extra);

        String dayColor = muted ? "#c2c6cc" : isToday ? "#1a9e8f" : "#1a1a2e";
        Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
        dayNum.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + dayColor + ";");
        cell.getChildren().add(dayNum);

        LocalDate today = LocalDate.now();
        int shown = 0;
        for (RezervaciaDAO.RezervaciaInfo rez : rezs) {
            if (shown >= 2) {
                Label more = new Label("+" + (rezs.size() - 2) + " ďalšie");
                more.setStyle("-fx-font-size: 10px; -fx-text-fill: #1a9e8f; -fx-font-weight: bold;");
                cell.getChildren().add(more);
                break;
            }
            cell.getChildren().add(buildPill(rez, today));
            shown++;
        }

        final LocalDate clickDate = date;
        final List<RezervaciaDAO.RezervaciaInfo> clickRezs = rezs;
        cell.setOnMouseClicked(e -> showSidePanel(clickDate, clickRezs));
        return cell;
    }

    private Label buildPill(RezervaciaDAO.RezervaciaInfo rez, LocalDate today) {
        String color;
        String text;
        String surname = rez.lekarPriezvisko() != null ? rez.lekarPriezvisko() : "Lekár";
        if (rez.stav() == Rezervacia.Stav.ZRUSENA) {
            color = "#9aa0a8";
            text = rez.datumCas().format(TIME_FMT) + " Dr. " + surname;
        } else if (rez.datumCas().toLocalDate().isBefore(today)) {
            color = "#388e3c";
            String proc = rez.proceduraNazov() != null ? rez.proceduraNazov() : "Termín";
            text = "✓ " + proc;
        } else {
            color = "#1a9e8f";
            text = rez.datumCas().format(TIME_FMT) + " Dr. " + surname;
        }
        Label pill = new Label(text);
        pill.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                      "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 5; -fx-background-radius: 3;");
        pill.setMaxWidth(Double.MAX_VALUE);
        return pill;
    }

    private void showSidePanel(LocalDate date, List<RezervaciaDAO.RezervaciaInfo> rezs) {
        selectedDayLabel.setText("Vybraný deň — " + formatDayLabel(date));
        sidePanelRows.getChildren().clear();

        if (rezs == null || rezs.isEmpty()) {
            noApptsLabel.setVisible(true);
            noApptsLabel.setManaged(true);
        } else {
            noApptsLabel.setVisible(false);
            noApptsLabel.setManaged(false);
            LocalDate today = LocalDate.now();
            for (RezervaciaDAO.RezervaciaInfo rez : rezs) {
                sidePanelRows.getChildren().add(buildMiniRow(rez, today));
            }
        }
    }

    private HBox buildMiniRow(RezervaciaDAO.RezervaciaInfo rez, LocalDate today) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 8 0; -fx-border-color: transparent transparent #eef0f3 transparent; -fx-border-width: 0 0 1 0;");

        Label time = new Label(rez.datumCas().format(TIME_FMT));
        time.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e; -fx-min-width: 48;");

        String meno = rez.lekarMeno() != null ? rez.lekarMeno() : "";
        String priezvisko = rez.lekarPriezvisko() != null ? rez.lekarPriezvisko() : "Lekár";
        String doctor = "Dr. " + (meno.isEmpty() ? "" : meno + " ") + priezvisko;
        String proc = rez.proceduraNazov() != null ? " · " + rez.proceduraNazov() : "";
        Label detail = new Label(doctor + proc);
        detail.setStyle("-fx-font-size: 12px; -fx-text-fill: #9aa0a8;");
        HBox.setHgrow(detail, Priority.ALWAYS);

        row.getChildren().addAll(time, detail, buildStavBadge(rez, today));
        return row;
    }

    private Label buildStavBadge(RezervaciaDAO.RezervaciaInfo rez, LocalDate today) {
        Label badge = new Label();
        if (rez.stav() == Rezervacia.Stav.ZRUSENA) {
            badge.setText("Zrušené");
            badge.setStyle("-fx-background-color: #fce4ec; -fx-text-fill: #c62828; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 4;");
        } else if (rez.datumCas().toLocalDate().isBefore(today)) {
            badge.setText("Absolvované");
            badge.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #388e3c; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 4;");
        } else {
            badge.setText("Nadchádzajúce");
            badge.setStyle("-fx-background-color: #e8f5f3; -fx-text-fill: #1a9e8f; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 4;");
        }
        return badge;
    }

    /** Filter already-loaded month data for a specific date (no DB hit). */
    private List<RezervaciaDAO.RezervaciaInfo> filterForDate(LocalDate date) {
        return currentMonthData.stream()
            .filter(r -> r.datumCas().toLocalDate().equals(date))
            .collect(Collectors.toList());
    }

    private static String formatDayLabel(LocalDate date) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", Locale.forLanguageTag("sk"));
        String s = date.format(fmt);
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
