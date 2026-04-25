package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
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
import sk.medicore.notifikator.Notifikacia;
import sk.medicore.notifikator.Notifikator;
import sk.medicore.util.SceneManager;
import sk.medicore.util.SessionManager;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class RezervaciaPresunController {

    @FXML private Label currentDoctorLabel;
    @FXML private Label currentProcedureLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label slotsSubLabel;
    @FXML private FlowPane slotGrid;
    @FXML private Label noSlotsLabel;
    @FXML private VBox confirmSection;
    @FXML private Label confirmOldTermin;
    @FXML private Label confirmNewTermin;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private Rezervacia rezervacia;
    private Termin currentTermin;
    private Termin selectedNewTermin;
    private Button lastSelectedBtn;

    private final TerminDAO terminDAO = new TerminDAO();
    private final RezervaciaDAO rezervaciaDAO = new RezervaciaDAO();
    private final LekarDAO lekarDAO = new LekarDAO();
    private final ProceduraDAO proceduraDAO = new ProceduraDAO();

    @FXML
    public void initialize() {
        rezervacia = SessionManager.getInstance().getRezervaciaToReschedule();
        if (rezervacia == null) {
            handleBack();
            return;
        }

        currentTermin = terminDAO.findById(rezervacia.getTerminId());
        Lekar lekar = lekarDAO.findById(rezervacia.getLekarId());
        Procedura procedura = proceduraDAO.findAll().stream()
            .filter(p -> p.getId() == rezervacia.getProceduraId()).findFirst().orElse(null);

        currentDoctorLabel.setText(lekar != null ? "Dr. " + lekar.getCeleMeno() + " — " + lekar.getSpecializacia() : "");
        currentProcedureLabel.setText(procedura != null ? procedura.getNazov() : "");
        currentDateLabel.setText(currentTermin != null ? currentTermin.getDatumCas().format(DT_FMT) : "");

        if (lekar != null) {
            slotsSubLabel.setText("Dostupné termíny — Dr. " + lekar.getCeleMeno());
            loadSlots(lekar.getId());
        }
    }

    private void loadSlots(int lekarId) {
        slotGrid.getChildren().clear();
        List<Termin> slots = terminDAO.findAvailable(lekarId);

        // exclude the current slot (it shows as PUBLIKOVANY while the reservation is active — it's actually REZERVOVANY)
        // findAvailable only returns PUBLIKOVANY, so the current termin won't appear anyway
        if (slots.isEmpty()) {
            noSlotsLabel.setVisible(true);
            noSlotsLabel.setManaged(true);
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
        btn.setUserData(t);
        applySlotStyle(btn, false);
        btn.setOnAction(e -> selectSlot(btn, t));
        btn.setOnMouseEntered(e -> { if (btn != lastSelectedBtn) applySlotHoverStyle(btn); });
        btn.setOnMouseExited(e -> { if (btn != lastSelectedBtn) applySlotStyle(btn, false); });
        return btn;
    }

    private void selectSlot(Button btn, Termin t) {
        // Deselect previous
        if (lastSelectedBtn != null) {
            applySlotStyle(lastSelectedBtn, false);
        }
        selectedNewTermin = t;
        lastSelectedBtn = btn;
        applySlotStyle(btn, true);

        confirmOldTermin.setText(currentTermin != null ? currentTermin.getDatumCas().format(DT_FMT) : "");
        confirmNewTermin.setText(t.getDatumCas().format(DT_FMT));
        confirmSection.setVisible(true);
        confirmSection.setManaged(true);
    }

    @FXML
    private void handleClearSelection() {
        if (lastSelectedBtn != null) {
            applySlotStyle(lastSelectedBtn, false);
            lastSelectedBtn = null;
        }
        selectedNewTermin = null;
        confirmSection.setVisible(false);
        confirmSection.setManaged(false);
    }

    @FXML
    private void handleConfirm() {
        if (selectedNewTermin == null) return;

        // Re-check availability
        List<Termin> available = terminDAO.findAvailable(rezervacia.getLekarId());
        boolean stillAvailable = available.stream().anyMatch(t -> t.getId() == selectedNewTermin.getId());
        if (!stillAvailable) {
            confirmSection.setVisible(false);
            confirmSection.setManaged(false);
            selectedNewTermin = null;
            lastSelectedBtn = null;
            noSlotsLabel.setText("Vybraný termín bol medzičasom obsadený. Vyberte iný.");
            noSlotsLabel.setVisible(true);
            noSlotsLabel.setManaged(true);
            loadSlots(rezervacia.getLekarId());
            return;
        }

        // 1. Free the old slot
        terminDAO.updateStav(rezervacia.getTerminId(), Termin.Stav.PUBLIKOVANY);
        // 2. Update the reservation to the new slot
        rezervaciaDAO.updateTermin(rezervacia.getId(), selectedNewTermin.getId());
        // 3. Mark the new slot as taken
        terminDAO.updateStav(selectedNewTermin.getId(), Termin.Stav.REZERVOVANY);
        Notifikator.odosliNotifikaciu(rezervacia.getPacientId(), Notifikacia.Typ.PRESUNUTA);

        SessionManager.getInstance().setRezervaciaToReschedule(null);

        Stage stage = (Stage) slotGrid.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/dashboard.fxml");
    }

    @FXML
    private void handleBack() {
        SessionManager.getInstance().setRezervaciaToReschedule(null);
        Stage stage = (Stage) slotGrid.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/dashboard.fxml");
    }

    private void applySlotStyle(Button btn, boolean selected) {
        if (selected) {
            btn.setStyle("-fx-background-color: #1a9e8f; -fx-text-fill: white; -fx-border-color: #1a9e8f; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 12px; -fx-padding: 8 4; -fx-cursor: hand; -fx-alignment: CENTER;");
        } else {
            btn.setStyle("-fx-background-color: white; -fx-text-fill: #1a1a2e; -fx-border-color: #ddd; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 12px; -fx-padding: 8 4; -fx-cursor: hand; -fx-alignment: CENTER;");
        }
    }

    private void applySlotHoverStyle(Button btn) {
        btn.setStyle("-fx-background-color: #e8f5f3; -fx-text-fill: #1a9e8f; -fx-border-color: #1a9e8f; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 12px; -fx-padding: 8 4; -fx-cursor: hand; -fx-alignment: CENTER;");
    }
}
