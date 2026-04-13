package sk.medicore.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    public static void switchTo(Stage stage, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            boolean wasMaximized = stage.isMaximized();
            Scene scene = new Scene(root, 900, 650);
            stage.setScene(scene);
            if (wasMaximized) stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Nepodarilo sa načítať obrazovku: " + fxmlPath, e);
        }
    }
}
