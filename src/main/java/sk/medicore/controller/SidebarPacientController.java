package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import sk.medicore.util.SceneManager;
import sk.medicore.util.SessionManager;

public class SidebarPacientController {

    @FXML private Label sidebarMenoLabel;
    @FXML private Label sidebarTypLabel;
    @FXML private Button navDashboard;
    @FXML private Button navRezervacje;
    @FXML private Button navRezervovat;
    @FXML private Button navKalendar;
    @FXML private Button navProfil;

    private static final String ACTIVE   = "-fx-background-color: #e8f5f3; -fx-text-fill: #1a9e8f; -fx-font-size: 13px; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 10 20; -fx-background-radius: 0; -fx-cursor: hand; -fx-border-color: transparent;";
    private static final String INACTIVE = "-fx-background-color: transparent; -fx-text-fill: #555; -fx-font-size: 13px; -fx-alignment: CENTER_LEFT; -fx-padding: 10 20; -fx-background-radius: 0; -fx-cursor: hand; -fx-border-color: transparent;";

    @FXML
    public void initialize() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            sidebarMenoLabel.setText(user.getCeleMeno());
            sidebarTypLabel.setText(formatTyp(user.getTyp()));
        }
    }

    /** Call from parent controller after its own initialize() to highlight the current page. */
    public void setActivePage(String page) {
        navDashboard.setStyle("dashboard".equals(page) ? ACTIVE : INACTIVE);
        navRezervacje.setStyle("rezervacje".equals(page) ? ACTIVE : INACTIVE);
        navRezervovat.setStyle("rezervovat".equals(page) ? ACTIVE : INACTIVE);
        navKalendar.setStyle("kalendar".equals(page) ? ACTIVE : INACTIVE);
        navProfil.setStyle("profil".equals(page) ? ACTIVE : INACTIVE);
    }

    @FXML private void handleNavDashboard()  { navigate("/view/dashboard.fxml"); }
    @FXML private void handleNavRezervacje() { navigate("/view/moje-rezervacie.fxml"); }
    @FXML private void handleNavRezervovat() { navigate("/view/rezervacia-wizard.fxml"); }
    @FXML private void handleNavKalendar()   { navigate("/view/patient-kalendar.fxml"); }
    @FXML private void handleNavProfil()     { navigate("/view/profil.fxml"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        navigate("/view/prihlasenie.fxml");
    }

    private void navigate(String fxml) {
        Stage stage = (Stage) sidebarMenoLabel.getScene().getWindow();
        SceneManager.switchTo(stage, fxml);
    }

    private String formatTyp(String typ) {
        return switch (typ) {
            case "LEKAR" -> "Lekár";
            case "ADMIN" -> "Administrátor";
            default -> "Pacient";
        };
    }
}
