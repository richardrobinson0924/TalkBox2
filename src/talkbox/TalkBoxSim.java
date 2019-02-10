package talkbox;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Pagination;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.*;
import java.util.stream.IntStream;

/**
 * @apiNote DO NOT use <code>audioFilenames[i][j]</code> field to retrieve filenames. Instead, use <code>getPath(i, j)</code>. This is because each filename in the matrix is a pseudomap, of form <code>"actualFilename|alias"</code>.
 */

public class TalkBoxSim extends Application {
    // instructions:
    // you wanna ask the user if they wanna make a new tbc file or open an existing one. (2 buttons)
    // when the .tbc file is created, it opens (or when the existing file is opened) and a simple interface of the buttons

    private File file;
    private TalkBoxData ts;
    private Button[] buttons;
    private Stage simStage;
    private VBox box;
    private Scene scene;

    public static void main(String... args) {
        launch(args);
    }

    public void start(Stage simStage) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        FlowPane onStartPane = new FlowPane();
        onStartPane.setPadding(new Insets(30, 20, 30, 20));
        onStartPane.setVgap(10);
        onStartPane.setHgap(10);
        onStartPane.setAlignment(Pos.CENTER);

        /* Sets the UI */
        this.simStage = simStage;
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        /* Creates the outermost container, composing of a `FlowPane` */
        box = new VBox();

        /* Sets window size and title */
        simStage.setTitle("TalkBox Simulator");
        simStage.setWidth(500);
        simStage.setHeight(400);
        simStage.getIcons().add(new Image(TalkBoxApp.class.getResourceAsStream("icon2.png")));

        /* Added the Creating a new file button */
        Button newFileBtn = new Button("Create a New File");
        newFileBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

            }
        });
        newFileBtn.setPrefSize(100,100);
        newFileBtn.setWrapText(true);
        newFileBtn.setAlignment(Pos.CENTER);

        /* Added the Open an Existing File */
        Button openExistFileBtn = new Button("Open an Existing File");
        openExistFileBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                open(null);
                Scene newTBCscene = new Scene(box);
                simStage.setScene(newTBCscene);
            }
        });
        openExistFileBtn.setPrefSize(100,100);
        openExistFileBtn.setWrapText(true);
        openExistFileBtn.setAlignment(Pos.CENTER);

        /* add the two buttons on the pane */
        onStartPane.getChildren().add(newFileBtn);
        onStartPane.getChildren().add(openExistFileBtn);

        /* Creates main scene */
        scene = new Scene(onStartPane);

        // show window
        simStage.setScene(scene);
        simStage.show();

        //open(null);
    }

    private void open(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("TalkBox Config File (.tbc)", "*.tbc"); // specifies file type
        fileChooser.getExtensionFilters().add(filter); // specifies file type

        fileChooser.setTitle("Open TalkBox File"); // specifies file prompt
        this.file = fileChooser.showOpenDialog(simStage); // displays file chooser window

        // adds file name to Window title
        simStage.setTitle("TalkBox Simulator — " + file.getName());

        Try.newBuilder()
                .setDefault(this::readFile)
                .setOtherwise(() -> open(null))
                .run();

        buttons = new Button[ts.numberOfAudioButtons];

        Pagination pagination = new Pagination(ts.numberOfAudioSets);
        box.getChildren().add(pagination);

        pagination.setPageFactory(this::configButtons);
    }

    private FlowPane configButtons(int page) {
        FlowPane flowPane = new FlowPane();
        flowPane.setPadding(new Insets(30, 20, 30, 20));
        flowPane.setVgap(10);
        flowPane.setHgap(10);
        flowPane.setAlignment(Pos.CENTER);

        // make the buttons
        for (int i = 0; i < ts.numberOfAudioButtons; i++) {
            String caption = (ts.audioFilenames[page][i] == null)
                    ? "Empty"
                    : ts.getAlias(page, i);

            buttons[i] = new Button(caption);
            buttons[i].setPrefSize(100, 100);
            flowPane.getChildren().add(buttons[i]);
        }

        // on button press
        IntStream.range(0, ts.getNumberOfAudioButtons()).forEach(i -> buttons[i].setOnAction(event2 -> {

            File soundFile = new File(ts.getPath(page, i));
            Try.newBuilder().setDefault(() -> {
                Media media = new Media(soundFile.toURI().toString());
                MediaPlayer player = new MediaPlayer(media);
            }).run();
        }));

        return flowPane;
    }

    public void createNewTBC() throws IOException {
        FileOutputStream fos = new FileOutputStream("test.tbc");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        TalkBoxData ts = new TalkBoxData();
        ts.numberOfAudioButtons = 5;
        ts.numberOfAudioSets = 8;
        // testing to see the branch

        ts.audioFilenames = new String[8][5];

        for (int i = 0; i < ts.numberOfAudioSets; i++) {
            for (int j = 0; j < ts.getNumberOfAudioButtons(); j++) {
                ts.audioFilenames[i][j] = null;
            }
        }
        oos.writeObject(ts);
        oos.flush();
        oos.close();
    }

    private void readFile() throws Exception {
        FileInputStream fis;
        ObjectInputStream oin;

        fis = new FileInputStream(file);
        oin = new ObjectInputStream(fis);

        ts = (TalkBoxData) oin.readObject();
    }
}
