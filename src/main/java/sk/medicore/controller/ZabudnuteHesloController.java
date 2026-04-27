package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sk.medicore.db.dao.PouzivatelDAO;
import sk.medicore.model.Pouzivatel;
import sk.medicore.util.PasswordUtil;
import sk.medicore.util.SceneManager;

public class ZabudnuteHesloController {

    @FXML private VBox emailPane;
    @FXML private TextField emailField;
    @FXML private Label emailErrorLabel;

    @FXML private VBox passwordPane;
    @FXML private Label emailConfirmLabel;
    @FXML private PasswordField newPwField;
    @FXML private PasswordField confirmPwField;
    @FXML private Label pwErrorLabel;

    @FXML private VBox successPane;

    private final PouzivatelDAO pouzivatelDAO = new PouzivatelDAO();
    private Pouzivatel foundUser;

    @FXML
    private void handleCheckEmail() {
        String email = emailField.getText().trim();
        if (email.isBlank()) {
            showEmailError("Zadajte e-mailovú adresu.");
            return;
        }

        foundUser = pouzivatelDAO.findByEmail(email);
        if (foundUser == null) {
            showEmailError("Účet s touto e-mailovou adresou neexistuje.");
            return;
        }

        emailConfirmLabel.setText("Nastavte nové heslo pre účet: " + email);
        showPane(2);
    }

    @FXML
    private void handleSetPassword() {
        String newPw = newPwField.getText();
        String confirm = confirmPwField.getText();

        if (newPw.isBlank() || confirm.isBlank()) {
            showPwError("Vyplňte obe polia.");
            return;
        }
        String passwordError = PasswordUtil.validate(newPw);
        if (passwordError != null) {
            showPwError(passwordError);
            return;
        }
        if (!newPw.equals(confirm)) {
            showPwError("Heslá sa nezhodujú.");
            return;
        }

        pouzivatelDAO.updateHeslo(foundUser.getId(), PasswordUtil.hash(newPw));
        showPane(3);
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/prihlasenie.fxml");
    }

    @FXML
    private void handleBackToEmail() {
        newPwField.clear();
        confirmPwField.clear();
        pwErrorLabel.setVisible(false);
        pwErrorLabel.setManaged(false);
        showPane(1);
    }

    private void showPane(int pane) {
        emailPane.setVisible(pane == 1);    emailPane.setManaged(pane == 1);
        passwordPane.setVisible(pane == 2); passwordPane.setManaged(pane == 2);
        successPane.setVisible(pane == 3);  successPane.setManaged(pane == 3);
    }

    private void showEmailError(String msg) {
        emailErrorLabel.setText(msg);
        emailErrorLabel.setVisible(true);
        emailErrorLabel.setManaged(true);
    }

    private void showPwError(String msg) {
        pwErrorLabel.setText(msg);
        pwErrorLabel.setVisible(true);
        pwErrorLabel.setManaged(true);
    }
}
