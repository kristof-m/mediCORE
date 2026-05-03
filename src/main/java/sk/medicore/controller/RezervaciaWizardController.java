package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.Group;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.ScrollPane;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RezervaciaWizardController {

    @FXML private Label step1Label;
    @FXML private Label step2Label;
    @FXML private Label step3Label;
    @FXML private Label step4Label;

    @FXML private ScrollPane step1Pane;
    @FXML private ScrollPane step2Pane;
    @FXML private ScrollPane step3Pane;
    @FXML private VBox step4Pane;

    @FXML private Pane wizardSearchIcon;
    @FXML private TextField searchField;
    @FXML private FlowPane categoryChips;
    @FXML private VBox procedureGrid;
    @FXML private Label step2SubLabel;
    @FXML private VBox doctorList;
    @FXML private Label step3SubLabel;
    @FXML private VBox slotGrid;

    @FXML private Label confirmProcedura;
    @FXML private Label confirmLekar;
    @FXML private Label confirmTermin;
    @FXML private Label confirmTrvanie;

    private int currentStep = 1;
    private boolean prefilledMode = false;
    private String activeCategory = null;
    private List<Procedura> allProcedures;
    private Procedura selectedProcedura;
    private Lekar selectedLekar;
    private Termin selectedTermin;

    private final ProceduraDAO proceduraDAO = new ProceduraDAO();
    private final LekarDAO lekarDAO = new LekarDAO();
    private final TerminDAO terminDAO = new TerminDAO();
    private final RezervaciaDAO rezervaciaDAO = new RezervaciaDAO();

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final String CHIP_NORMAL = "-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e2e5e9; -fx-border-radius: 8; -fx-border-width: 1; -fx-padding: 9 14; -fx-cursor: hand;";
    private static final String CHIP_HOVER  = "-fx-background-color: #1a9e8f; -fx-background-radius: 8; -fx-border-color: #1a9e8f; -fx-border-radius: 8; -fx-border-width: 1; -fx-padding: 9 14; -fx-cursor: hand;";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private static final double CARD_HEIGHT = 170;
    private static final String CARD_STYLE_DEFAULT =
        "-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 13 14;" +
        "-fx-border-color: #eef0f3; -fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;";
    private static final String CARD_STYLE_HOVER =
        "-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 13 14;" +
        "-fx-border-color: #1a9e8f; -fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;" +
        "-fx-effect: dropshadow(gaussian, rgba(26,158,143,0.08), 8, 0, 0, 2);";
    private static final String CLOCK_ICON = "M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z M12 6v6l4 2";
    private static final String DEFAULT_ICON = "M22 12h-4l-3 9L9 3l-3 9H2";
    private static final Map<String, String> CAT_ICONS;
    static {
        CAT_ICONS = new java.util.HashMap<>();
        CAT_ICONS.put("Kardiológia",    "M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z");
        CAT_ICONS.put("Dermatológia",   "M12 22C6.477 22 2 17.523 2 12S6.477 2 12 2s10 4.477 10 10-4.477 10-10 10zm-1-5h2v2h-2v-2zm0-8h2v6h-2V9z");
        CAT_ICONS.put("Všeobecná prax", "M22 12h-4l-3 9L9 3l-3 9H2");
        CAT_ICONS.put("Oftalmológia",   "M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z M12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6z");
        CAT_ICONS.put("Laboratórium",   "M9 3h6v2H9V3zm0 2v4L5 17a1 1 0 0 0 .894 1.447h12.212A1 1 0 0 0 19 17l-4-8V5H9z");
        CAT_ICONS.put("Neurológia",     "M22 12h-4l-3 9L9 3l-3 9H2");
        CAT_ICONS.put("Ortopédia",      "M12 2a10 10 0 1 0 0 20A10 10 0 0 0 12 2zm-2 14.5v-9l6 4.5-6 4.5z");
        CAT_ICONS.put("Chirurgia",      "M18 8h1a4 4 0 0 1 0 8h-1 M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z M6 1v3 M10 1v3 M14 1v3");
        CAT_ICONS.put("Pediatria",      "M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2 M9 3a4 4 0 1 1 0 8 4 4 0 0 1 0-8z");
        CAT_ICONS.put("Onkológia",      "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z");
    }

    @FXML
    public void initialize() {
        Lekar prelekar = SessionManager.getInstance().getPreselectedLekar();
        Termin pretermin = SessionManager.getInstance().getPreselectedTermin();
        Procedura preprocedura = SessionManager.getInstance().getPreselectedProcedura();

        fillIconPane(wizardSearchIcon, "M21 21l-4.35-4.35 M17 11A6 6 0 1 1 5 11a6 6 0 0 1 12 0z", "#9aa0a8", 12.0 / 24, 16);

        if (prelekar != null && pretermin != null && preprocedura != null) {
            prefilledMode = true;
            selectedLekar = prelekar;
            selectedTermin = pretermin;
            selectedProcedura = preprocedura;
            SessionManager.getInstance().clearPreselectedBooking();
            goToStep(4);
            confirmProcedura.setText(preprocedura.getNazov());
            confirmLekar.setText("Dr. " + prelekar.getCeleMeno() + " — " + prelekar.getSpecializacia());
            confirmTermin.setText(pretermin.getDatumCas().format(DT_FMT));
            confirmTrvanie.setText(pretermin.getTrvanieMin() + " minút");
        } else {
            loadProcedures();
        }
    }

    private void loadProcedures() {
        allProcedures = proceduraDAO.findAll();
        buildCategoryChips();
        applyFilter();

        searchField.textProperty().addListener((obs, old, val) -> applyFilter());
    }

    private void buildCategoryChips() {
        categoryChips.getChildren().clear();
        java.util.Set<String> cats = new java.util.LinkedHashSet<>();
        cats.add("Všetky");
        for (Procedura p : allProcedures) {
            if (p.getKategoria() != null && !p.getKategoria().isBlank()) cats.add(p.getKategoria());
        }
        for (String cat : cats) {
            Button chip = new Button(cat);
            boolean isAll = "Všetky".equals(cat);
            chip.setStyle(isAll && activeCategory == null
                ? "-fx-background-color: #1a9e8f; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 5 10; -fx-background-radius: 14; -fx-cursor: hand; -fx-border-color: transparent;"
                : "-fx-background-color: white; -fx-text-fill: #9aa0a8; -fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 5 10; -fx-background-radius: 14; -fx-cursor: hand; -fx-border-color: #eef0f3; -fx-border-width: 1;");
            chip.setOnAction(e -> {
                activeCategory = isAll ? null : cat;
                buildCategoryChips();
                applyFilter();
            });
            categoryChips.getChildren().add(chip);
        }
    }

    private void applyFilter() {
        String query = searchField != null ? searchField.getText() : "";
        procedureGrid.getChildren().clear();

        List<VBox> cards = new ArrayList<>();
        for (Procedura p : allProcedures) {
            boolean matchesCat = activeCategory == null || activeCategory.equals(p.getKategoria());
            boolean matchesSearch = query.isBlank()
                || p.getNazov().toLowerCase().contains(query.toLowerCase())
                || (p.getPopis() != null && p.getPopis().toLowerCase().contains(query.toLowerCase()));
            if (matchesCat && matchesSearch) cards.add(buildProcedureCard(p));
        }

        for (int i = 0; i < cards.size(); i += 3) {
            HBox row = new HBox(10);
            int end = Math.min(i + 3, cards.size());
            for (int j = i; j < end; j++) {
                HBox.setHgrow(cards.get(j), Priority.ALWAYS);
                row.getChildren().add(cards.get(j));
            }
            for (int k = end - i; k < 3; k++) {
                Region filler = new Region();
                HBox.setHgrow(filler, Priority.ALWAYS);
                row.getChildren().add(filler);
            }
            procedureGrid.getChildren().add(row);
        }
    }

    private VBox buildProcedureCard(Procedura p) {
        VBox card = new VBox(8);
        card.setMinWidth(0);
        card.setPrefWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPrefHeight(CARD_HEIGHT);
        card.setMinHeight(CARD_HEIGHT);
        card.setMaxHeight(CARD_HEIGHT);
        card.setStyle(CARD_STYLE_DEFAULT);

        HBox top = new HBox(8);
        top.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Pane iconTile = makeCategoryIconTile(p.getKategoria());
        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);
        Label catTag = new Label(p.getKategoria() != null ? p.getKategoria() : "");
        catTag.setStyle("-fx-font-size: 9.5px; -fx-font-weight: 600; -fx-text-fill: #9aa0a8;" +
                        "-fx-background-color: #f1f2f4; -fx-background-radius: 4; -fx-padding: 3 7;");
        boolean hasKat = p.getKategoria() != null && !p.getKategoria().isBlank();
        catTag.setVisible(hasKat);
        catTag.setManaged(hasKat);
        top.getChildren().addAll(iconTile, topSpacer, catTag);

        Label name = new Label(p.getNazov());
        name.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        name.setWrapText(true);
        name.setMaxHeight(36);

        Label desc = new Label(p.getPopis() != null ? p.getPopis() : "");
        desc.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #9aa0a8; -fx-line-spacing: 1;");
        desc.setWrapText(true);
        desc.setMinHeight(0);
        VBox.setVgrow(desc, Priority.ALWAYS);

        HBox foot = new HBox(5);
        foot.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        foot.setStyle("-fx-border-color: #eef0f3 transparent transparent transparent; -fx-border-width: 1 0 0 0; -fx-padding: 8 0 0 0;");
        Pane clockIcon = makeSmallSvgIcon(CLOCK_ICON);
        Label dur = new Label(p.getTrvanieMin() + " min");
        dur.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #9aa0a8;");
        Region footSpacer = new Region();
        HBox.setHgrow(footSpacer, Priority.ALWAYS);
        Label sel = new Label("Vybrať →");
        sel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #1a9e8f; -fx-cursor: hand;");
        foot.getChildren().addAll(clockIcon, dur, footSpacer, sel);

        card.getChildren().addAll(top, name, desc, foot);
        card.setOnMouseClicked(e -> selectProcedura(p));
        card.setOnMouseEntered(e -> card.setStyle(CARD_STYLE_HOVER));
        card.setOnMouseExited(e -> card.setStyle(CARD_STYLE_DEFAULT));
        return card;
    }

    private Pane makeCategoryIconTile(String kategoria) {
        SVGPath svg = new SVGPath();
        svg.setContent(CAT_ICONS.getOrDefault(kategoria, DEFAULT_ICON));
        svg.setFill(Color.TRANSPARENT);
        svg.setStroke(Color.web("#1a9e8f"));
        svg.setStrokeWidth(1.5);
        svg.getTransforms().add(new Scale(16.0 / 24, 16.0 / 24, 0, 0));

        StackPane tile = new StackPane(new Group(svg));
        tile.setMinSize(32, 32);
        tile.setMaxSize(32, 32);
        tile.setPrefSize(32, 32);
        tile.setStyle("-fx-background-color: #e8f5f3; -fx-background-radius: 7;");
        return tile;
    }

    private static void fillIconPane(Pane tile, String pathData, String strokeColor, double scale, double size) {
        tile.getChildren().clear();
        SVGPath svg = new SVGPath();
        svg.setContent(pathData);
        svg.setFill(Color.TRANSPARENT);
        svg.setStroke(Color.web(strokeColor));
        svg.setStrokeWidth(1.5);
        svg.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        svg.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        svg.getTransforms().add(new Scale(scale, scale, 0, 0));
        StackPane center = new StackPane(new Group(svg));
        center.setMinSize(size, size);
        center.setMaxSize(size, size);
        tile.getChildren().add(center);
    }

    private Pane makeSmallSvgIcon(String pathData) {
        SVGPath svg = new SVGPath();
        svg.setContent(pathData);
        svg.setFill(Color.TRANSPARENT);
        svg.setStroke(Color.web("#9aa0a8"));
        svg.setStrokeWidth(1.5);
        svg.getTransforms().add(new Scale(12.0 / 24, 12.0 / 24, 0, 0));

        StackPane tile = new StackPane(new Group(svg));
        tile.setMinSize(12, 12);
        tile.setMaxSize(12, 12);
        tile.setPrefSize(12, 12);
        return tile;
    }

    private void selectProcedura(Procedura p) {
        this.selectedProcedura = p;
        goToStep(2);
        step2SubLabel.setText("Dostupní lekári pre: " + p.getNazov());
        loadDoctors(p.getId());
    }

    private void loadDoctors(int proceduraId) {
        doctorList.getChildren().clear();
        List<Lekar> lekari = lekarDAO.findByProceduraId(proceduraId);

        if (lekari.isEmpty()) {
            Label empty = new Label("Pre túto procedúru nie sú dostupní žiadni lekári.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaa;");
            doctorList.getChildren().add(empty);
            return;
        }

        for (Lekar l : lekari) {
            doctorList.getChildren().add(buildDoctorCard(l));
        }
    }

    private HBox buildDoctorCard(Lekar l) {
        HBox card = new HBox(14);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        Label avatar = new Label(l.getMeno().substring(0, 1) + l.getPriezvisko().substring(0, 1));
        avatar.setStyle("-fx-background-color: #1a9e8f; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 44; -fx-min-height: 44; -fx-max-width: 44; -fx-max-height: 44; -fx-background-radius: 22; -fx-alignment: CENTER;");

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label("Dr. " + l.getCeleMeno());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        Label spec = new Label(l.getSpecializacia());
        spec.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        info.getChildren().addAll(name, spec);

        Button select = new Button("Vybrať");
        select.setStyle("-fx-background-color: #1a9e8f; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 7 16; -fx-background-radius: 5; -fx-cursor: hand;");
        select.setOnAction(e -> selectLekar(l));

        card.getChildren().addAll(avatar, info, select);
        return card;
    }

    private void selectLekar(Lekar l) {
        this.selectedLekar = l;
        goToStep(3);
        step3SubLabel.setText("Dostupné termíny — Dr. " + l.getCeleMeno());
        loadSlots(l.getId());
    }

    private void loadSlots(int lekarId) {
        slotGrid.getChildren().clear();
        List<Termin> slots = terminDAO.findAvailable(lekarId);

        if (slots.isEmpty()) {
            Label empty = new Label("Žiadne dostupné termíny pre tohto lekára.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #9aa0a8; -fx-padding: 24 0;");
            slotGrid.getChildren().add(empty);
            return;
        }

        // Group slots by calendar date (sorted by date)
        LinkedHashMap<LocalDate, List<Termin>> byDay = new LinkedHashMap<>();
        for (Termin t : slots) {
            byDay.computeIfAbsent(t.getDatumCas().toLocalDate(), k -> new ArrayList<>()).add(t);
        }

        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", new Locale("sk"));

        for (Map.Entry<LocalDate, List<Termin>> entry : byDay.entrySet()) {
            VBox section = new VBox(10);
            section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                             "-fx-border-color: #eef0f3; -fx-border-radius: 10; -fx-border-width: 1; " +
                             "-fx-padding: 14 16;");

            String dayStr = entry.getKey().format(dayFmt);
            dayStr = dayStr.substring(0, 1).toUpperCase() + dayStr.substring(1);
            Label dayLabel = new Label(dayStr);
            dayLabel.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");

            FlowPane chips = new FlowPane(8, 8);
            for (Termin t : entry.getValue()) {
                chips.getChildren().add(buildSlotChip(t));
            }

            section.getChildren().addAll(dayLabel, chips);
            slotGrid.getChildren().add(section);
        }
    }

    private VBox buildSlotChip(Termin t) {
        Label timeLbl = new Label(t.getDatumCas().format(TIME_FMT));
        timeLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");

        Label durLbl = new Label(t.getTrvanieMin() + " min");
        durLbl.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #9aa0a8;");

        VBox chip = new VBox(2);
        chip.setAlignment(javafx.geometry.Pos.CENTER);
        chip.setMinWidth(72);
        chip.setPrefWidth(72);
        chip.setMaxWidth(72);
        chip.setStyle(CHIP_NORMAL);
        chip.getChildren().addAll(timeLbl, durLbl);
        chip.setCursor(javafx.scene.Cursor.HAND);

        chip.setOnMouseClicked(e -> selectTermin(t));
        chip.setOnMouseEntered(e -> {
            chip.setStyle(CHIP_HOVER);
            timeLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
            durLbl.setStyle("-fx-font-size: 10.5px; -fx-text-fill: rgba(255,255,255,0.75);");
        });
        chip.setOnMouseExited(e -> {
            chip.setStyle(CHIP_NORMAL);
            timeLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
            durLbl.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #9aa0a8;");
        });

        return chip;
    }

    private void selectTermin(Termin t) {
        this.selectedTermin = t;
        goToStep(4);
        confirmProcedura.setText(selectedProcedura.getNazov());
        confirmLekar.setText("Dr. " + selectedLekar.getCeleMeno() + " — " + selectedLekar.getSpecializacia());
        confirmTermin.setText(t.getDatumCas().format(DT_FMT));
        confirmTrvanie.setText(t.getTrvanieMin() + " minút");
    }

    @FXML
    private void handleConfirm() {
        // Double-check the slot is still available
        List<Termin> available = terminDAO.findAvailable(selectedLekar.getId());
        boolean stillAvailable = available.stream().anyMatch(t -> t.getId() == selectedTermin.getId());

        if (!stillAvailable) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Termín nedostupný");
            alert.setHeaderText("Tento termín bol medzičasom obsadený.");
            alert.setContentText("Prosím, vyberte iný termín.");
            alert.showAndWait();
            goToStep(3);
            loadSlots(selectedLekar.getId());
            return;
        }

        int pacientId = SessionManager.getInstance().getCurrentUser().getId();

        Rezervacia r = new Rezervacia();
        r.setPacientId(pacientId);
        r.setLekarId(selectedLekar.getId());
        r.setTerminId(selectedTermin.getId());
        r.setProceduraId(selectedProcedura.getId());
        r.setStav(Rezervacia.Stav.POTVRDENA);

        rezervaciaDAO.insert(r);
        terminDAO.updateStav(selectedTermin.getId(), Termin.Stav.REZERVOVANY);
        Notifikator.odosliNotifikaciu(pacientId, Notifikacia.Typ.POTVRDENA);

        Stage stage = (Stage) step1Pane.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/dashboard.fxml");
    }

    @FXML
    private void handleBack() {
        if (prefilledMode) {
            Stage stage = (Stage) step1Pane.getScene().getWindow();
            SceneManager.switchTo(stage, "/view/patient-kalendar.fxml");
            return;
        }
        if (currentStep > 1) {
            goToStep(currentStep - 1);
        } else {
            Stage stage = (Stage) step1Pane.getScene().getWindow();
            SceneManager.switchTo(stage, "/view/dashboard.fxml");
        }
    }

    private void goToStep(int step) {
        currentStep = step;
        step1Pane.setVisible(step == 1); step1Pane.setManaged(step == 1);
        step2Pane.setVisible(step == 2); step2Pane.setManaged(step == 2);
        step3Pane.setVisible(step == 3); step3Pane.setManaged(step == 3);
        step4Pane.setVisible(step == 4); step4Pane.setManaged(step == 4);

        updateStepLabels();
    }

    private void updateStepLabels() {
        setStepStyle(step1Label, 1, currentStep == 1, currentStep > 1);
        setStepStyle(step2Label, 2, currentStep == 2, currentStep > 2);
        setStepStyle(step3Label, 3, currentStep == 3, currentStep > 3);
        setStepStyle(step4Label, 4, currentStep == 4, false);
    }

    private static final String CIRCLE_BASE = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-min-width: 26; -fx-min-height: 26; -fx-max-width: 26; -fx-max-height: 26; -fx-border-radius: 13; -fx-background-radius: 13; -fx-border-width: 1.5;";

    private void setStepStyle(Label label, int stepNum, boolean active, boolean done) {
        if (done) {
            label.setText("✓");
            label.setStyle(CIRCLE_BASE + "-fx-background-color: #1a9e8f; -fx-border-color: #1a9e8f; -fx-text-fill: white;");
        } else if (active) {
            label.setText(String.valueOf(stepNum));
            label.setStyle(CIRCLE_BASE + "-fx-background-color: #1a9e8f; -fx-border-color: #1a9e8f; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(26,158,143,0.3), 6, 0, 0, 0);");
        } else {
            label.setText(String.valueOf(stepNum));
            label.setStyle(CIRCLE_BASE + "-fx-background-color: white; -fx-border-color: #d0d3d9; -fx-text-fill: #9aa0a8;");
        }
    }
}
