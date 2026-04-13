package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sk.medicore.db.dao.LekarDAO;
import sk.medicore.db.dao.ProceduraDAO;
import sk.medicore.db.dao.TerminDAO;
import sk.medicore.model.Lekar;
import sk.medicore.model.Procedura;
import sk.medicore.model.Termin;
import sk.medicore.util.SceneManager;
import sk.medicore.util.SessionManager;

import sk.medicore.util.DateUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PatientKalendarController {

    @FXML private SidebarPacientController sidebarController;
    @FXML private ComboBox<Lekar> lekarCombo;
    @FXML private VBox dniContainer;
    @FXML private Label emptyLabel;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final LekarDAO lekarDAO = new LekarDAO();
    private final TerminDAO terminDAO = new TerminDAO();
    private final ProceduraDAO proceduraDAO = new ProceduraDAO();

    @FXML
    public void initialize() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        sidebarController.setActivePage("kalendar");

        lekarCombo.setCellFactory(lv -> new LekarCell());
        lekarCombo.setButtonCell(new LekarCell());

        List<Lekar> lekari = lekarDAO.findAll();
        lekarCombo.getItems().addAll(lekari);
    }

    @FXML
    private void handleLekarSelected() {
        Lekar selected = lekarCombo.getValue();
        if (selected == null) return;

        dniContainer.getChildren().clear();
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(30);
        List<TerminDAO.TerminInfo> terminy = terminDAO.findEnrichedForWeek(selected.getId(), from, to);

        if (terminy.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }

        List<Procedura> doctorProcs = proceduraDAO.findByLekarId(selected.getId());

        Map<LocalDate, List<TerminDAO.TerminInfo>> byDay = terminy.stream()
            .collect(Collectors.groupingBy(t -> t.datumCas().toLocalDate()));

        byDay.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> dniContainer.getChildren().add(buildDaySection(e.getKey(), e.getValue(), selected, doctorProcs)));
    }

    private VBox buildDaySection(LocalDate day, List<TerminDAO.TerminInfo> items, Lekar lekar, List<Procedura> procs) {
        VBox section = new VBox(8);

        String dayStr = DateUtil.formatDayHeading(day);

        Label dayLabel = new Label(dayStr);
        dayLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e; -fx-padding: 0 0 4 0;");
        section.getChildren().add(dayLabel);

        for (var info : items) {
            section.getChildren().add(buildSlotRow(info, lekar, procs));
        }
        return section;
    }

    private HBox buildSlotRow(TerminDAO.TerminInfo info, Lekar lekar, List<Procedura> procs) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        boolean dostupny = info.stav() == Termin.Stav.DOSTUPNY;
        String bg = dostupny ? "white" : "#fff8f0";
        String border = dostupny ? "#e8f5f3" : "#ffcc80";
        row.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 8; -fx-padding: 12 16; " +
                     "-fx-border-color: " + border + "; -fx-border-radius: 8; -fx-border-width: 1.5;");

        Label timeLabel = new Label(info.datumCas().format(TIME_FMT));
        timeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e; -fx-min-width: 50;");

        Label durLabel = new Label(info.trvanieMin() + " min");
        durLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-min-width: 55;");

        Label stavBadge = buildStavBadge(info.stav());

        // Spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(timeLabel, durLabel, stavBadge, spacer);

        if (dostupny) {
            if (procs.isEmpty()) {
                // fallback: no procedures configured for this doctor
                Button btn = new Button("Rezervovať");
                btn.setStyle("-fx-background-color: #1a9e8f; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 14; -fx-background-radius: 5; -fx-cursor: hand;");
                btn.setOnAction(e -> navigateToWizard(lekar, toTermin(info), null));
                row.getChildren().add(btn);
            } else if (procs.size() == 1) {
                Button btn = new Button("Rezervovať");
                btn.setStyle("-fx-background-color: #1a9e8f; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 14; -fx-background-radius: 5; -fx-cursor: hand;");
                btn.setOnAction(e -> navigateToWizard(lekar, toTermin(info), procs.get(0)));
                row.getChildren().add(btn);
            } else {
                ComboBox<Procedura> procCombo = new ComboBox<>();
                procCombo.setCellFactory(lv -> new ProceduraCell());
                procCombo.setButtonCell(new ProceduraCell());
                procCombo.getItems().addAll(procs);
                procCombo.setPromptText("Vybrať procedúru");
                procCombo.setPrefWidth(180);

                Button btn = new Button("Rezervovať");
                btn.setStyle("-fx-background-color: #1a9e8f; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 14; -fx-background-radius: 5; -fx-cursor: hand;");
                btn.setOnAction(e -> {
                    Procedura chosen = procCombo.getValue();
                    if (chosen == null) {
                        procCombo.setStyle("-fx-border-color: #d32f2f; -fx-border-radius: 4;");
                        return;
                    }
                    navigateToWizard(lekar, toTermin(info), chosen);
                });
                row.getChildren().addAll(procCombo, btn);
            }
        }

        return row;
    }

    private void navigateToWizard(Lekar lekar, Termin termin, Procedura procedura) {
        termin.setLekarId(lekar.getId());
        if (procedura != null) {
            SessionManager.getInstance().setPreselectedBooking(lekar, termin, procedura);
        }
        Stage stage = (Stage) dniContainer.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/rezervacia-wizard.fxml");
    }

    private Termin toTermin(TerminDAO.TerminInfo info) {
        Termin t = new Termin();
        t.setId(info.terminId());
        t.setDatumCas(info.datumCas());
        t.setTrvanieMin(info.trvanieMin());
        t.setStav(info.stav());
        return t;
    }

    private Label buildStavBadge(Termin.Stav stav) {
        Label badge = new Label();
        if (stav == Termin.Stav.DOSTUPNY) {
            badge.setText("Dostupný");
            badge.setStyle("-fx-background-color: #e8f5f3; -fx-text-fill: #1a9e8f; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;");
        } else {
            badge.setText("Rezervovaný");
            badge.setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #e65100; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;");
        }
        return badge;
    }

    // --- Cell factories ---

    private static class LekarCell extends ListCell<Lekar> {
        @Override
        protected void updateItem(Lekar l, boolean empty) {
            super.updateItem(l, empty);
            setText(empty || l == null ? null : "Dr. " + l.getCeleMeno() + " — " + l.getSpecializacia());
        }
    }

    private static class ProceduraCell extends ListCell<Procedura> {
        @Override
        protected void updateItem(Procedura p, boolean empty) {
            super.updateItem(p, empty);
            setText(empty || p == null ? null : p.getNazov() + " (" + p.getTrvanieMin() + " min)");
        }
    }

}
