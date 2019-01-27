package talkbox;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.*;

public class TalkBoxApp extends Application {
    private File file;

    @Override
    public void start(Stage primaryStage) throws Exception {
        /* Initializes app */
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        primaryStage.setTitle("TalkBox Config");
        primaryStage.setWidth(500);
        primaryStage.setHeight(400);
        primaryStage.getIcons().add(new Image(TalkBoxApp.class.getResourceAsStream("icon2.png")));


        /* Creates the outermost container, composing of a `MenuBar` and `FlowPane` */
        VBox box = new VBox();

        /* Creates the menu bar */
        MenuBar menuBar = new MenuBar();
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

        /* Creates the sole menu in the menu bar, `File` */
        Menu menuFile = new Menu("File");
        menuBar.getMenus().addAll(menuFile);

        /* Adds an Open and Save action to the File menu. The latter is initially disabled. */
        MenuItem open = new MenuItem("Open");
        MenuItem save = new MenuItem("Save");
        save.setDisable(true);

        /* Initializes the FlowPane */
        FlowPane flow = new FlowPane();
        flow.setPadding(new Insets(5, 0, 5, 0));
        flow.setVgap(4);
        flow.setHgap(4);

        Scene scene = new Scene(box);

        /* Configures the open action to open a file/ If successful, continue to `action` method */
        open.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("TalkBox Config File (.tbc)", "*.tbc");
            fileChooser.getExtensionFilters().add(filter);

            fileChooser.setTitle("Open TalkBox File");
            this.file = fileChooser.showOpenDialog(primaryStage);

            String str = file.toString()
                    .substring(file.toString().lastIndexOf('/'))
                    .substring(1);

            primaryStage.setTitle("TalkBox Configurator – " + str);

            try {
                action(flow, box);
                save.setDisable(false);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
        });

        menuFile.getItems().addAll(open, save);

        box.getChildren().addAll(menuBar, flow);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void action(FlowPane flow, Pane box) throws IOException, ClassNotFoundException {

        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream oin = new ObjectInputStream(fis);

        TalkBoxData ts = (TalkBoxData) oin.readObject();
        Button[] buttons = new Button[ts.numberOfAudioButtons];

        Pagination pagination = new Pagination(ts.numberOfAudioSets);
        box.getChildren().add(pagination);

        for (int i = 0; i < ts.numberOfAudioButtons; i++) {
            buttons[i] = new Button("" + i);
            flow.getChildren().add(buttons[i]);
        }
    }
}
