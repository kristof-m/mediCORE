package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sk.medicore.db.dao.PouzivatelDAO;
import sk.medicore.db.dao.RezervaciaDAO;
import sk.medicore.model.Lekar;
import sk.medicore.util.PasswordUtil;
import sk.medicore.util.SceneManager;
import sk.medicore.util.SessionManager;

import java.util.List;

public class LekarProfilController {

    @FXML private SidebarLekarController sidebarController;

    @FXML private Label bigAvatarLabel;
    @FXML private Label headerNameLabel;
    @FXML private Label headerEmailLabel;
    @FXML private Label headerRoleLabel;
    @FXML private Label statTerminyLabel;
    @FXML private Label statPacientiLabel;

    @FXML private TextField menoField;
    @FXML private TextField priezviskoField;
    @FXML private TextField emailField;
    @FXML private TextField specializaciaField;

    @FXML private PasswordField currentPwField;
    @FXML private PasswordField newPwField;
    @FXML private Label pwFeedbackLabel;
    @FXML private Label saveFeedbackLabel;

    private final PouzivatelDAO pouzivatelDAO = new PouzivatelDAO();
    private final RezervaciaDAO rezervaciaDAO = new RezervaciaDAO();

    private Lekar lekar;

    @FXML
    public void initialize() {
        sidebarController.setActivePage("profil");

        lekar = (Lekar) SessionManager.getInstance().getCurrentUser();

        String m = lekar.getMeno() != null ? lekar.getMeno() : "";
        String p = lekar.getPriezvisko() != null ? lekar.getPriezvisko() : "";
        String initials = (m.isEmpty() ? "?" : m.substring(0, 1).toUpperCase())
                        + (p.isEmpty() ? "?" : p.substring(0, 1).toUpperCase());

        bigAvatarLabel.setText(initials);
        headerNameLabel.setText("Dr. " + lekar.getCeleMeno());
        headerEmailLabel.setText(lekar.getEmail() != null ? lekar.getEmail() : "");
        headerRoleLabel.setText("Lekár · " + (lekar.getSpecializacia() != null ? lekar.getSpecializacia() : ""));

        menoField.setText(m);
        priezviskoField.setText(p);
        emailField.setText(lekar.getEmail() != null ? lekar.getEmail() : "");
        specializaciaField.setText(lekar.getSpecializacia() != null ? lekar.getSpecializacia() : "");

        List<RezervaciaDAO.PacientInfo> pacienti = rezervaciaDAO.findPacientiByLekarId(lekar.getId());
        statPacientiLabel.setText(String.valueOf(pacienti.size()));
        int totalTerminy = pacienti.stream().mapToInt(RezervaciaDAO.PacientInfo::visitCount).sum();
        statTerminyLabel.setText(String.valueOf(totalTerminy));
    }

    @FXML
    private void handleSave() {
        pwFeedbackLabel.setVisible(false);
        pwFeedbackLabel.setManaged(false);
        saveFeedbackLabel.setVisible(false);
        saveFeedbackLabel.setManaged(false);

        // Validate password change first, before any DB writes
        if (!newPwField.getText().isBlank()) {
            if (!PasswordUtil.verify(currentPwField.getText(), lekar.getHesloHash())) {
                pwFeedbackLabel.setText("Nesprávne súčasné heslo.");
                pwFeedbackLabel.setVisible(true);
                pwFeedbackLabel.setManaged(true);
                return;
            }
        }

        lekar.setMeno(menoField.getText().trim());
        lekar.setPriezvisko(priezviskoField.getText().trim());
        lekar.setEmail(emailField.getText().trim());
        pouzivatelDAO.updateProfil(lekar.getId(), lekar.getMeno(), lekar.getPriezvisko(), lekar.getEmail());

        if (!newPwField.getText().isBlank()) {
            String newHash = PasswordUtil.hash(newPwField.getText());
            pouzivatelDAO.updateHeslo(lekar.getId(), newHash);
            lekar.setHesloHash(newHash);
            currentPwField.clear();
            newPwField.clear();
        }

        String newInitials = initial(lekar.getMeno()) + initial(lekar.getPriezvisko());
        bigAvatarLabel.setText(newInitials);
        headerNameLabel.setText("Dr. " + lekar.getCeleMeno());
        headerEmailLabel.setText(lekar.getEmail() != null ? lekar.getEmail() : "");

        saveFeedbackLabel.setText("Zmeny boli uložené.");
        saveFeedbackLabel.setVisible(true);
        saveFeedbackLabel.setManaged(true);
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) menoField.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/lekar-dashboard.fxml");
    }

    private static String initial(String s) {
        return (s != null && !s.isEmpty()) ? s.substring(0, 1).toUpperCase() : "?";
    }
}
