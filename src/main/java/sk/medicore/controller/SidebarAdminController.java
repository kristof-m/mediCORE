package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import sk.medicore.util.SceneManager;
import sk.medicore.util.SessionManager;

public class SidebarAdminController {

    @FXML private Label sidebarMenoLabel;
    @FXML private Button navDashboard;
    @FXML private Button navKalendar;

    private static final String ACTIVE   = "-fx-background-color: #e8f5f3; -fx-text-fill: #1a9e8f; -fx-font-size: 13px; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 10 20; -fx-background-radius: 0; -fx-cursor: hand; -fx-border-color: transparent;";
    private static final String INACTIVE = "-fx-background-color: transparent; -fx-text-fill: #555; -fx-font-size: 13px; -fx-alignment: CENTER_LEFT; -fx-padding: 10 20; -fx-background-radius: 0; -fx-cursor: hand; -fx-border-color: transparent;";

    @FXML
    public void initialize() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            sidebarMenoLabel.setText(user.getCeleMeno());
        }
    }

    public void setActivePage(String page) {
        navDashboard.setStyle("dashboard".equals(page) ? ACTIVE : INACTIVE);
        navKalendar.setStyle("kalendar".equals(page) ? ACTIVE : INACTIVE);
    }

    @FXML private void handleNavDashboard() { navigate("/view/admin-dashboard.fxml"); }
    @FXML private void handleNavKalendar()  { navigate("/view/admin-kalendar.fxml"); }

    @FXML private void handleNavProfil() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Pripravujeme");
        info.setHeaderText(null);
        info.setContentText("Sekcia Profil bude dostupná v ďalšej verzii.");
        info.showAndWait();
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().logout();
        navigate("/view/prihlasenie.fxml");
    }

    private void navigate(String fxml) {
        Stage stage = (Stage) sidebarMenoLabel.getScene().getWindow();
        SceneManager.switchTo(stage, fxml);
    }
}
