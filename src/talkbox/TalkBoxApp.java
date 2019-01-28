package talkbox;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import javax.swing.*;
import java.io.*;
import java.util.Optional;

public class TalkBoxApp extends Application {
    private File file;
    private TalkBoxData ts;
    private Button[] buttons;

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

        /* Creates main scene */
        Scene scene = new Scene(box);
        save.setOnAction(event -> save());

        /* Configures the open action to open a file/ If successful, continue to `action` method */
        open.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("TalkBox Config File (.tbc)", "*.tbc"); // specifies file type
            fileChooser.getExtensionFilters().add(filter); // specifies file type

            fileChooser.setTitle("Open TalkBox File"); // specifies file prompt
            this.file = fileChooser.showOpenDialog(primaryStage); // displays file chooser window

            // gets the name of the file
            String str = file.toString()
                    .substring(file.toString().lastIndexOf('/'))
                    .substring(1);

            // adds file name to Window title
            primaryStage.setTitle("TalkBox Configurator – " + str);

            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream oin = new ObjectInputStream(fis);

                ts = (TalkBoxData) oin.readObject();
                buttons = new Button[ts.numberOfAudioButtons];

                Pagination pagination = new Pagination(ts.numberOfAudioSets);
                int page = pagination.getCurrentPageIndex();
                box.getChildren().add(pagination);

                pagination.setPageFactory(this::configButtons);

                save.setDisable(false);
                open.setDisable(true);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
        });

        // show menu bar
        menuFile.getItems().addAll(open, save);
        box.getChildren().addAll(menuBar);

        // show window
        primaryStage.setScene(scene);
        primaryStage.show();

        warnBeforeExit(primaryStage);
    }

    private void warnBeforeExit(Stage primaryStage) {
        primaryStage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Exit");
            alert.setHeaderText("Save File?");
            alert.setContentText("Please choose an option.");

            ButtonType yesButton = new ButtonType("Yes");
            ButtonType noButton = new ButtonType("No");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == yesButton) {
                event.consume();
                save();
                Platform.exit();
            } else if (result.isPresent() && result.get() == noButton) {
                Platform.exit();
            } else if (result.isPresent() && result.get() == cancelButton) {
                event.consume();
            }
        });
    }

    private void save() {
        try {
            FileOutputStream fos = new FileOutputStream(file.toString());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(ts);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FlowPane configButtons(int page) {
        FlowPane flowPane = new FlowPane();
        flowPane.setPadding(new Insets(30, 20, 30, 20));
        flowPane.setVgap(10);
        flowPane.setHgap(10);
        flowPane.setAlignment(Pos.CENTER);

        for (int i = 0; i < ts.numberOfAudioButtons; i++) {
            buttons[i] = ts.audioFilenames[page][i] == null
                    ? new Button("Empty")
                    : new Button(ts.audioFilenames[page][i]);

            buttons[i].setPrefSize(100, 100);
            flowPane.getChildren().add(buttons[i]);
        }

        for (int i = 0; i < ts.getNumberOfAudioButtons(); i++) {
            int j = i;
            buttons[i].setOnAction(event2 -> {
                if (ts.audioFilenames[page][j] == null) {
                    FileChooser audioFile = new FileChooser();
                    FileChooser.ExtensionFilter filter2 = new FileChooser.ExtensionFilter("Audio File", "*.mp3", "*.wav");
                    audioFile.getExtensionFilters().add(filter2);

                    audioFile.setTitle("Select Audio File");
                    File audio = audioFile.showOpenDialog(null);
                    ts.audioFilenames[page][j] = audio.getPath();
                } else {
                    File soundFile = new File(ts.audioFilenames[page][j]);
                    Media media = new Media(soundFile.toURI().toString());
                    MediaPlayer player = new MediaPlayer(media);
                    player.play();
                }
                buttons[j].setText(ts.audioFilenames[page][j]);
            });
        }
        return flowPane;
    }

}
