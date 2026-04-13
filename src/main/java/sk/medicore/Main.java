package sk.medicore;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sk.medicore.db.DatabaseManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseManager.init();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/prihlasenie.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("MediCORE — Zdravotná rezervačná platforma");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);
        primaryStage.setScene(new Scene(root, 900, 650));
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
