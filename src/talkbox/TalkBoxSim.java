package talkbox;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.sound.sampled.AudioInputStream;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
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
	private final int MINOR_BUTTON_HEIGHT = 20;
	private final int MINOR_BUTTON_WIDTH = 85;
	private boolean canContinue = false;

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
		simStage.setResizable(false);
		simStage.getIcons().add(new Image(TalkBoxApp.class.getResourceAsStream("/Resources/icon2.png")));

		/* Added the Creating a new file button */
		Button newFileBtn = new Button("Create a New File");

		// FYI: when code appears grey (like in the next line) press Alt-Enter and intelliJ will let you convert to lambda expression :)
		newFileBtn.setOnAction(event -> {
			try {
				createNewTBC();
				simStage.setTitle("TalkBox Configurator — " + file.getName());

				buttons = new Button[ts.numberOfAudioButtons];

				Pagination pagination = new Pagination(ts.numberOfAudioSets);
				box.getChildren().add(pagination);

				pagination.setPageFactory(TalkBoxSim.this::configButtons);

				Scene newTBCscene = new Scene(box);
				simStage.setScene(newTBCscene);

			} catch (IOException e) {
				e.printStackTrace();
			}

		});
		newFileBtn.setPrefSize(100, 100);
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
		openExistFileBtn.setPrefSize(100, 100);
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
		File workingDirectory = new File(System.getProperty("user.dir"));
		fileChooser.setInitialDirectory(workingDirectory);
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
			String caption = (ts.database[page][i] != null)
					? ts.database[page][i].getValue()
					: "Empty";

			buttons[i] = new Button(caption);
			buttons[i].setPrefSize(100, 100);
			flowPane.getChildren().add(buttons[i]);
		}

		// on button press
		IntStream.range(0, ts.getNumberOfAudioButtons()).forEach(i -> buttons[i].setOnAction(event2 -> {

			//File soundFile = new File(getFullPath(ts.audioList[page][i].getKey()));
//			Try.newBuilder().setDefault(() -> {
//				Media media = new Media(soundFile.toURI().toString());
//				MediaPlayer player = new MediaPlayer(media);
//				player.play();
//			}).run();
		}));

		return flowPane;
	}

	public void createNewTBC() throws IOException {
		// File testTBC = new File("/Users/richardrobinson/Desktop/MyTalkBox/config.tbc");

		// whenever a new file is created, it replaces the test.tbc file here

        openWizardDialog();

        if (!canContinue) {
        	return;
		}

		File testTBC = new File("test.tbc");
		FileOutputStream fos = new FileOutputStream(testTBC);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		TalkBoxData ts = new TalkBoxData();
		ts.numberOfAudioButtons = 5;
		ts.numberOfAudioSets = 8;


		ts.database = new TalkBoxData.AudioPair[ts.numberOfAudioSets][ts.numberOfAudioButtons];

		for (int i = 0; i < ts.numberOfAudioSets; i++) {
			for (int j = 0; j < ts.getNumberOfAudioButtons(); j++) {
				ts.database[i][j] = null;
			}
		}

		List<String> list = new ArrayList<>();
		list.add("hi");

		List<String> list1 = new ArrayList<>();
		list1.add("bye");

		List<String> list2 = new ArrayList<>();
		list2.add("sigh");

		List<List<String>> master = new ArrayList<>();
		master.add(list);
		master.add(list1);
		master.add(list2);

		ts.customWords = master;

		oos.writeObject(ts);
		oos.flush();
		oos.close();

		file = testTBC;
		this.ts = ts;
	}

	private void openWizardDialog() {
	    // opens up a pop-up dialog with a wizard-like interface using a stage. Uses a Vbox (children added vertically), which
        // multiple flow panes are added to it and then the Vbox is added to the scene
	    // creates a dialog after the create new talkbox is pressed
		// uses a canContinue boolean variable to decide whether the program is ready to move on to the next stage

        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(simStage);
        dialog.setTitle("New TalkBox");
        dialog.setResizable(false);
        dialog.getIcons().add(new Image(TalkBoxApp.class.getResourceAsStream("/Resources/icon2.png")));

        // 0th pane
		FlowPane dialogPane0 = new FlowPane();
		dialogPane0.setPrefSize(500,25);
		dialogPane0.setPadding(new Insets(10, 20, 10, 35));
		dialogPane0.setAlignment(Pos.CENTER_LEFT);

        // first pane
        FlowPane dialogPane1 = new FlowPane();
        dialogPane1.setPrefSize(500,50);
        dialogPane1.setPadding(new Insets(0, 20, 10, 35));
		dialogPane1.setHgap(10);
        dialogPane1.setAlignment(Pos.CENTER_LEFT);

        // second pane
        FlowPane dialogPane2 = new FlowPane();
        dialogPane2.setPrefSize(500,50);
        dialogPane2.setPadding(new Insets(10, 20, 10, 35));
		dialogPane2.setHgap(10);
        dialogPane2.setAlignment(Pos.CENTER);

		// third pane
		FlowPane dialogPane3 = new FlowPane();
		dialogPane3.setPrefSize(500,50);
		dialogPane3.setPadding(new Insets(10, 20, 10, 35));
		dialogPane3.setHgap(30);
		dialogPane3.setAlignment(Pos.CENTER);

		// fourth pane
		FlowPane dialogPane4 = new FlowPane();
		dialogPane4.setPrefSize(500,50);
		dialogPane4.setPadding(new Insets(10, 20, 0, 35));
		dialogPane4.setHgap(10);
		dialogPane4.setAlignment(Pos.BOTTOM_RIGHT);


		// The following "nodes" are to be added to the 0th pane
		Label enterNameLbl = new Label();
		enterNameLbl.setText("Creating a New TalkBox\n\tEnter a TalkBox name.");

        // The following "nodes" are to be added to the first pane
        Label nameLbl = new Label();
        nameLbl.setText("TextBox file name: ");

		TextField nameTxtField = new TextField();
		nameTxtField.setPrefWidth(300);

        // The following "nodes are to be added to the second pane"
		Label locationLbl = new Label();
		locationLbl.setText("Location:");

		TextField locationTxtField = new TextField();
		locationTxtField.setEditable(false);
		locationTxtField.setPrefWidth(300);
		locationTxtField.setMouseTransparent(true);

		Button browseBtn = new Button();
		browseBtn.setPrefSize(MINOR_BUTTON_WIDTH, MINOR_BUTTON_HEIGHT);
		browseBtn.setText("Browse...");
		browseBtn.setWrapText(true);
		browseBtn.setAlignment(Pos.CENTER);

		browseBtn.setOnAction(event -> {
			// input action after browse button is clicked here
			DirectoryChooser dir = new DirectoryChooser();
			dir.setTitle("Browse for a folder");
			File defaultDirectory = new File(System.getProperty("user.dir"));
			dir.setInitialDirectory(defaultDirectory);
			File selectedDirectory = dir.showDialog(dialog);

			locationTxtField.setText(selectedDirectory.getPath());
		});

		// The following "nodes are to be added to the third pane"
		Label numBtnsLbl = new Label();
		numBtnsLbl.setText("Number of Buttons: ");

		Label numSetsLbl = new Label();
		numSetsLbl.setText("Number of Sets: ");

		String [] numBtnsChoices = {"1","2","3","4","5"};
		String [] numSetsChoices = {"1","2","3","4","5","6","7","8"};
		ComboBox <String> numBtnsComboBox = new ComboBox<String>(FXCollections.observableArrayList(numBtnsChoices));
		ComboBox <String> numSetsComboBox = new ComboBox<String>(FXCollections.observableArrayList(numSetsChoices));

		// how to get a selected item from the combobox
		//numBtnsComboBox.getSelectionModel().getSelectedItem().toString();


		// The following "nodes are to be added to the fourth pane"
        Button finishBtn = new Button();
        finishBtn.setPrefSize(MINOR_BUTTON_WIDTH, MINOR_BUTTON_HEIGHT);
        finishBtn.setText("Finish");
        finishBtn.setWrapText(true);
        finishBtn.setAlignment(Pos.CENTER);

        finishBtn.setOnAction(event -> {
        	// input action after finish button is clicked here
			try {
				int numBtns = Integer.parseInt(numBtnsComboBox.getSelectionModel().getSelectedItem().toString());
				int numSets = Integer.parseInt(numSetsComboBox.getSelectionModel().getSelectedItem().toString());
				String selectedDir = locationTxtField.getText();
				String talkBoxName = nameTxtField.getText().trim();
				if (selectedDir == null) {
					throw new Exception();
				}


				dialog.close();
			}
			catch (Exception e) {

			}
		});

        Button cancelBtn = new Button();
        cancelBtn.setPrefSize(MINOR_BUTTON_WIDTH, MINOR_BUTTON_HEIGHT);
        cancelBtn.setText("Cancel");
        cancelBtn.setWrapText(true);
        cancelBtn.setAlignment(Pos.CENTER);
        cancelBtn.setOnAction(event -> {
        	// input action after cancel button is clicked here
        	dialog.close();
		});

		// add all the panes to a Vbox
        VBox dialogVbox = new VBox(20);
        dialogPane0.getChildren().add(enterNameLbl);
        dialogPane1.getChildren().addAll(nameLbl, nameTxtField);
        dialogPane2.getChildren().addAll(locationLbl, locationTxtField, browseBtn);
        dialogPane3.getChildren().addAll(numBtnsLbl,numBtnsComboBox,numSetsLbl,numSetsComboBox);
        dialogPane4.getChildren().addAll(finishBtn,cancelBtn);

		// add the Vbox to the dialog scene and show it
        dialogVbox.getChildren().addAll(dialogPane0,dialogPane1,dialogPane2,dialogPane3,dialogPane4);
        Scene dialogScene = new Scene(dialogVbox);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
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
