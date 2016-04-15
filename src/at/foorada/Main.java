package at.foorada;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    private static File initialFolder;

    @Override
    public void start(Stage primaryStage) throws Exception{
        final FXMLLoader loader = new FXMLLoader(
                getClass().getResource("simpleRenamer.fxml")
        );
        Parent root = loader.load();

        Controller ctrl = loader.getController();
        ctrl.checkOptions();
        ctrl.prepareTable();

        if(initialFolder != null)
            ctrl.loadFileList(initialFolder);

        primaryStage.getIcons().add(new Image("at/foorada/icon/icon.png"));
        primaryStage.setTitle("Simple Renamer by foorada");
        primaryStage.setScene(new Scene(root, 475, 550));
        primaryStage.setMinWidth(475);
        primaryStage.setMinHeight(550);
        primaryStage.show();
    }


    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                File init = new File(args[0]);
                if (init.exists() && init.isDirectory())
                    initialFolder = init;
            }
        } catch(NullPointerException npe) {
            initialFolder = null;
        }

        launch(args);
    }
}
