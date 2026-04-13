package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.ScrollPane;
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

import java.time.format.DateTimeFormatter;
import java.util.List;

public class RezervaciaWizardController {

    @FXML private Label step1Label;
    @FXML private Label step2Label;
    @FXML private Label step3Label;
    @FXML private Label step4Label;

    @FXML private ScrollPane step1Pane;
    @FXML private ScrollPane step2Pane;
    @FXML private ScrollPane step3Pane;
    @FXML private VBox step4Pane;

    @FXML private FlowPane procedureGrid;
    @FXML private Label step2SubLabel;
    @FXML private VBox doctorList;
    @FXML private Label step3SubLabel;
    @FXML private FlowPane slotGrid;

    @FXML private Label confirmProcedura;
    @FXML private Label confirmLekar;
    @FXML private Label confirmTermin;
    @FXML private Label confirmTrvanie;

    private int currentStep = 1;
    private boolean prefilledMode = false;
    private Procedura selectedProcedura;
    private Lekar selectedLekar;
    private Termin selectedTermin;

    private final ProceduraDAO proceduraDAO = new ProceduraDAO();
    private final LekarDAO lekarDAO = new LekarDAO();
    private final TerminDAO terminDAO = new TerminDAO();
    private final RezervaciaDAO rezervaciaDAO = new RezervaciaDAO();

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    public void initialize() {
        Lekar prelekar = SessionManager.getInstance().getPreselectedLekar();
        Termin pretermin = SessionManager.getInstance().getPreselectedTermin();
        Procedura preprocedura = SessionManager.getInstance().getPreselectedProcedura();

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
        procedureGrid.getChildren().clear();
        List<Procedura> all = proceduraDAO.findAll();
        for (Procedura p : all) {
            procedureGrid.getChildren().add(buildProcedureCard(p));
        }
    }

    private VBox buildProcedureCard(Procedura p) {
        VBox card = new VBox(8);
        card.setPrefWidth(240);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 6, 0, 0, 2); -fx-cursor: hand;");

        Label name = new Label(p.getNazov());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        name.setWrapText(true);

        Label desc = new Label(p.getPopis() != null ? p.getPopis() : "");
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        desc.setWrapText(true);

        Label duration = new Label("Trvanie: " + p.getTrvanieMin() + " min");
        duration.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a9e8f;");

        Button select = new Button("Vybrať →");
        select.setStyle("-fx-background-color: transparent; -fx-text-fill: #1a9e8f; -fx-font-size: 12px; -fx-cursor: hand; -fx-border-color: transparent;");
        select.setOnAction(e -> selectProcedura(p));

        card.getChildren().addAll(name, desc, duration, select);
        card.setOnMouseClicked(e -> selectProcedura(p));
        return card;
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
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaa;");
            slotGrid.getChildren().add(empty);
            return;
        }

        for (Termin t : slots) {
            slotGrid.getChildren().add(buildSlotButton(t));
        }
    }

    private Button buildSlotButton(Termin t) {
        String label = t.getDatumCas().format(DateTimeFormatter.ofPattern("dd.MM\nHH:mm"));
        Button btn = new Button(label);
        btn.setPrefWidth(90);
        btn.setStyle("-fx-background-color: white; -fx-text-fill: #1a1a2e; -fx-border-color: #ddd; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 12px; -fx-padding: 8 4; -fx-cursor: hand; -fx-alignment: CENTER;");
        btn.setOnAction(e -> selectTermin(t));
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #e8f5f3; -fx-text-fill: #1a9e8f; -fx-border-color: #1a9e8f; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 12px; -fx-padding: 8 4; -fx-cursor: hand; -fx-alignment: CENTER;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: white; -fx-text-fill: #1a1a2e; -fx-border-color: #ddd; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 12px; -fx-padding: 8 4; -fx-cursor: hand; -fx-alignment: CENTER;"));
        return btn;
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
        setStepStyle(step1Label, currentStep == 1, currentStep > 1);
        setStepStyle(step2Label, currentStep == 2, currentStep > 2);
        setStepStyle(step3Label, currentStep == 3, currentStep > 3);
        setStepStyle(step4Label, currentStep == 4, false);
    }

    private void setStepStyle(Label label, boolean active, boolean done) {
        if (active) {
            label.setStyle("-fx-background-color: #1a9e8f; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 4 12; -fx-background-radius: 12;");
        } else if (done) {
            label.setStyle("-fx-background-color: #b2dfdb; -fx-text-fill: #1a9e8f; -fx-font-size: 12px; -fx-padding: 4 12; -fx-background-radius: 12;");
        } else {
            label.setStyle("-fx-background-color: #ddd; -fx-text-fill: #888; -fx-font-size: 12px; -fx-padding: 4 12; -fx-background-radius: 12;");
        }
    }
}
