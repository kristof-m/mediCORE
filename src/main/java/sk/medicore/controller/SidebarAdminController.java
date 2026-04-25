package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import sk.medicore.util.SceneManager;
import sk.medicore.util.SessionManager;

public class SidebarAdminController {

    @FXML private Label sidebarAvatarLabel;
    @FXML private Label sidebarMenoLabel;
    @FXML private Button navDashboard;
    @FXML private Button navKalendar;
    @FXML private Button navProfil;
    @FXML private Button logoutBtn;

    private static final String ACTIVE   = "-fx-background-color: #e8f5f3; -fx-text-fill: #1a9e8f; -fx-font-size: 13px; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 9 12; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: transparent;";
    private static final String INACTIVE = "-fx-background-color: transparent; -fx-text-fill: #555; -fx-font-size: 13px; -fx-alignment: CENTER_LEFT; -fx-padding: 9 12; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: transparent;";

    private static final String IC_DASHBOARD = "M3 3h7v9H3Z M14 3h7v5h-7Z M14 12h7v9h-7Z M3 16h7v5H3Z";
    private static final String IC_CALENDAR  = "M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2Z M16 2v4 M8 2v4 M3 10h18";
    private static final String IC_USER      = "M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2 M12 3a4 4 0 1 1 0 8 4 4 0 0 1 0-8Z";
    private static final String IC_LOGOUT    = "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4 M16 17l5-5-5-5 M21 12H9";

    @FXML
    public void initialize() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            String m = user.getMeno() != null ? user.getMeno() : "";
            String p = user.getPriezvisko() != null ? user.getPriezvisko() : "";
            String initials = (m.isEmpty() ? "?" : m.substring(0, 1).toUpperCase())
                + (p.isEmpty() ? "?" : p.substring(0, 1).toUpperCase());
            sidebarAvatarLabel.setText(initials);
            sidebarMenoLabel.setText(user.getCeleMeno());
        }

        navDashboard.setGraphic(makeIcon(IC_DASHBOARD, "#555")); navDashboard.setGraphicTextGap(8);
        navKalendar.setGraphic(makeIcon(IC_CALENDAR, "#555"));   navKalendar.setGraphicTextGap(8);
        navProfil.setGraphic(makeIcon(IC_USER, "#555"));         navProfil.setGraphicTextGap(8);
        logoutBtn.setGraphic(makeIcon(IC_LOGOUT, "#d32f2f"));    logoutBtn.setGraphicTextGap(8);
    }

    public void setActivePage(String page) {
        setBtn(navDashboard, "dashboard".equals(page));
        setBtn(navKalendar, "kalendar".equals(page));
        setBtn(navProfil, "profil".equals(page));
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

    private void setBtn(Button btn, boolean active) {
        btn.setStyle(active ? ACTIVE : INACTIVE);
        updateIconColor(btn, active ? "#1a9e8f" : "#555");
    }

    private static void updateIconColor(Button btn, String color) {
        if (btn.getGraphic() instanceof Pane pane && !pane.getChildren().isEmpty()
                && pane.getChildren().get(0) instanceof SVGPath svg) {
            svg.setStyle("-fx-fill: transparent; -fx-stroke: " + color + "; -fx-stroke-width: 1.5; -fx-stroke-line-cap: round; -fx-stroke-line-join: round;");
        }
    }

    private static Node makeIcon(String content, String color) {
        SVGPath svg = new SVGPath();
        svg.setContent(content);
        svg.setStyle("-fx-fill: transparent; -fx-stroke: " + color + "; -fx-stroke-width: 1.5; -fx-stroke-line-cap: round; -fx-stroke-line-join: round;");
        svg.getTransforms().add(new Scale(16.0 / 24, 16.0 / 24, 0, 0));
        Pane pane = new Pane(svg);
        pane.setPrefSize(16, 16);
        pane.setMinSize(16, 16);
        pane.setMaxSize(16, 16);
        return pane;
    }

    private void navigate(String fxml) {
        Stage stage = (Stage) sidebarMenoLabel.getScene().getWindow();
        SceneManager.switchTo(stage, fxml);
    }
}
