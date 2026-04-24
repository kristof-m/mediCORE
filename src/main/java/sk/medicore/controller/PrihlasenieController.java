package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sk.medicore.db.dao.PouzivatelDAO;
import sk.medicore.model.Pouzivatel;
import sk.medicore.util.PasswordUtil;
import sk.medicore.util.SceneManager;
import sk.medicore.util.SessionManager;

public class PrihlasenieController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final PouzivatelDAO pouzivatelDAO = new PouzivatelDAO();

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Vyplňte e-mail aj heslo.");
            return;
        }

        Pouzivatel user = pouzivatelDAO.findByEmail(email);
        if (user == null || !PasswordUtil.verify(password, user.getHesloHash())) {
            showError("Nesprávny e-mail alebo heslo.");
            return;
        }

        SessionManager.getInstance().setCurrentUser(user);
        Stage stage = (Stage) emailField.getScene().getWindow();
        String nextScene = switch (user.getTyp()) {
            case "LEKAR" -> "/view/lekar-dashboard.fxml";
            case "ADMIN" -> "/view/admin-dashboard.fxml";
            default      -> "/view/dashboard.fxml";
        };
        SceneManager.switchTo(stage, nextScene);
    }

    @FXML
    private void handleForgotPassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Obnovenie hesla");
        dialog.setHeaderText("Zadajte vašu e-mailovú adresu");
        dialog.setContentText("E-mail:");

        dialog.showAndWait().ifPresent(email -> {
            String trimmed = email.trim();
            if (trimmed.isEmpty()) return;

            if (pouzivatelDAO.findByEmail(trimmed) == null) {
                showError("Účet s touto e-mailovou adresou neexistuje.");
                return;
            }

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("E-mail odoslaný");
            info.setHeaderText(null);
            info.setContentText("Odkaz na obnovenie hesla bol odoslaný na adresu: " + trimmed);
            info.showAndWait();
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        });
    }

    @FXML
    private void handleGoToRegistracia() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/registracia.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
