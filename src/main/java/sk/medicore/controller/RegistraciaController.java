package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sk.medicore.db.dao.PouzivatelDAO;
import sk.medicore.model.Pacient;
import sk.medicore.util.PasswordUtil;
import sk.medicore.util.SceneManager;

public class RegistraciaController {

    @FXML private TextField menoField;
    @FXML private TextField priezviskoField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private DatePicker datumNarodeniaField;
    @FXML private PasswordField passwordConfirmField;
    @FXML private Label errorLabel;
    @FXML private Label emailHintLabel;

    private final PouzivatelDAO pouzivatelDAO = new PouzivatelDAO();

    @FXML
    private void handleRegistracia() {
        String meno = menoField.getText().trim();
        String priezvisko = priezviskoField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String passwordConfirm = passwordConfirmField.getText();

        if (meno.isEmpty() || priezvisko.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Vyplňte všetky povinné polia.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showError("Zadajte platný e-mail.");
            return;
        }

        String passwordError = PasswordUtil.validate(password);
        if (passwordError != null) {
            showError(passwordError);
            return;
        }

        if (!password.equals(passwordConfirm)) {
            showError("Heslá sa nezhodujú.");
            return;
        }

        if (pouzivatelDAO.emailExists(email)) {
            showError("Účet s týmto e-mailom už existuje. Skúste sa prihlásiť.");
            return;
        }

        if (datumNarodeniaField.getValue() == null) {
            showError("Zadajte dátum narodenia.");
            return;
        }

        java.time.LocalDate dob = datumNarodeniaField.getValue();
        if (dob.isAfter(java.time.LocalDate.now())) {
            showError("Dátum narodenia nemôže byť v budúcnosti.");
            return;
        }
        if (java.time.Period.between(dob, java.time.LocalDate.now()).getYears() < 18) {
            showError("Musíte mať aspoň 18 rokov.");
            return;
        }

        Pacient pacient = new Pacient();
        pacient.setMeno(meno);
        pacient.setPriezvisko(priezvisko);
        pacient.setEmail(email);
        pacient.setHesloHash(PasswordUtil.hash(password));
        pacient.setDatumNarodenia(datumNarodeniaField.getValue());

        pouzivatelDAO.insert(pacient);

        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/prihlasenie.fxml");
    }

    @FXML
    private void handleGoToPrihlasenie() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/prihlasenie.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
