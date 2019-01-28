package talkbox;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.*;

public class TalkBoxApp extends Application {
    private File file;
    private TalkBoxData ts;

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
        flow.setPadding(new Insets(30, 20, 30, 20));
        flow.setVgap(10);
        flow.setHgap(10);
        flow.setAlignment(Pos.CENTER);

        /* Creates main scene */
        Scene scene = new Scene(box);

        save.setOnAction(event -> {
            try {
                FileOutputStream fos = new FileOutputStream(file.toString());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(ts);
                oos.flush();
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

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
                Button[] buttons = new Button[ts.numberOfAudioButtons];

                Pagination pagination = new Pagination(ts.numberOfAudioSets);
                int page = pagination.getCurrentPageIndex();
                box.getChildren().add(pagination);

                for (int i = 0; i < ts.numberOfAudioButtons; i++) {
                    buttons[i] = ts.audioFilenames[page][i] == null
                            ? new Button("Empty")
                            : new Button(ts.audioFilenames[page][i]);

                    buttons[i].setPrefSize(100, 100);
                    flow.getChildren().add(buttons[i]);
                }

                for (int i = 0; i < ts.getNumberOfAudioButtons(); i++) {
                    int j = i;
                    buttons[i].setOnAction(event2 -> {
                        if (ts.audioFilenames[page][j] == null) {
                            FileChooser audioFile = new FileChooser();
                            FileChooser.ExtensionFilter filter2 = new FileChooser.ExtensionFilter("Audio File", "*.mp3", "*.wav");
                            audioFile.getExtensionFilters().add(filter2);

                            audioFile.setTitle("Select Audio File");
                            File audio = audioFile.showOpenDialog(primaryStage);
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
                save.setDisable(false);
                open.setDisable(true);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
        });

        // show menu bar
        menuFile.getItems().addAll(open, save);
        box.getChildren().addAll(menuBar, flow);

        // show window
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
