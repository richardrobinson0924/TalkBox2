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
 * The TalkBox Simulator simulates the physical device. When the app is launched, the user is prompted to either (a) create a new .tbc file or (b) open an existing file. At any point in the application, the user can launch the TalkBox configurator with the current .tbc file pre-loaded in the Configurator.
 *
 * <p>
 *     <b>Creating a new file:</b>
 * <p>
 *
 * This opens a wizard-like dialog, with the following steps:
 * <ul>
 *     <li> Asks where to save file on the disk
 *     <li> Asks how many audio buttons and swap buttons it should have
 *     <li> Once done, on backend creates a TalkBox directory in the location specified. Within the directory, there will be the .tbc file, as well as another directory entitled "Audio" to contain the audio files
 * </ul>
 *
 * Afterwards, the user will have the option of opening the Configurator with this newly created .tbc file
 *
 * <p>
 *     <b>Opening an Existing File</b>
 * <p>
 *
 * This presents a FileChooser allowing a user to select a .tbc TalkBox Configuration file. Then, an  interface will appear with the following presentation:
 * <ul>
 *     <li> All the required specifications as described on the project outline (buttons with swap buttons acting accordingly
 *     <li> There shall also exist a <code>Custom</code> button with a <code>Play</code> button beside it.
 * </ul>
 *
 * <p>
 *     <b>Using the Custom button</b>
 * <p>
 *
 * The custom button is an on-board sentence TTS generator. Upon pressing the button, each audio button transforms into a sentence Subject word (for example, "Richard", "Myself", "You"). Once the user selects the Subject, the audio buttons again transform into Verbs. This process continues for the following sentence structures: subjects, verbs, objects, tenses, and propositional meanings. After the final selection, the <code>simplenlg</code> API creates a new sentence out of the different words.
 *
 * <p>
 * The list of different options for each sentence structure will be provided in a CSV file within the directory (first column is Subject, next is Verbs, etc...), which must first be parsed by the Simulator. The user can then press <code>Play</code> to play the newly generated sentence using the Google Cloud TTS service.
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

    private static final String AUDIO_PATH = "/Audio";
    private File audioFolder;

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

        // FYI: when code appears grey (like in the next line) press Alt-Enter and intelliJ will let you convert to lambda expression :)
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
        file = fileChooser.showOpenDialog(simStage); // displays file chooser window

        // adds file name to Window title
        simStage.setTitle("TalkBox Configurator — " + file.getName());

        Try.newBuilder()
                .setDefault(this::readFile)
                .setOtherwise(() -> open(null))
                .run();

        audioFolder = new File(file.getParent().concat(AUDIO_PATH));

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
            String caption = (ts.audioList[page][i] != null)
                    ? ts.audioList[page][i].getValue()
                    : "Empty";

            buttons[i] = new Button(caption);
            buttons[i].setPrefSize(100, 100);
            flowPane.getChildren().add(buttons[i]);
        }

        // on button press
        IntStream.range(0, ts.getNumberOfAudioButtons()).forEach(i -> buttons[i].setOnAction(event2 -> {

	        File soundFile = new File(getFullPath(ts.audioList[page][i].getKey()));
            Try.newBuilder().setDefault(() -> {
                Media media = new Media(soundFile.toURI().toString());
                MediaPlayer player = new MediaPlayer(media);
	            player.play();
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

        ts.audioList = new Mapping[ts.numberOfAudioSets][ts.numberOfAudioButtons];

        for (int i = 0; i < ts.numberOfAudioSets; i++) {
            for (int j = 0; j < ts.getNumberOfAudioButtons(); j++) {
                ts.audioList[i][j] = null;
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

    private String getFullPath(String s) {
        return audioFolder.getPath().concat('/' + s);
    }
}
