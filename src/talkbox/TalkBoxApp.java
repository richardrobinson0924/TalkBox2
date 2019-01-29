package talkbox;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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
import java.util.Optional;

/**
 * NOT SUITABLE YET FOR PRODUCTION USE
 * <p>
 *     ___       ___       ___       ___       ___       ___       ___
 *    /\  \     /\  \     /\__\     /\__\     /\  \     /\  \     /\__\
 *    \:\  \   /::\  \   /:/  /    /:/ _/_   /::\  \   /::\  \   |::L__L
 *    /::\__\ /::\:\__\ /:/__/    /::-"\__\ /::\:\__\ /:/\:\__\ /::::\__\
 *   /:/\/__/ \/\::/  / \:\  \    \;:;-",-" \:\::/  / \:\/:/  / \;::;/__/
 *   \/__/      /:/  /   \:\__\    |:|  |    \::/  /   \::/  /   |::|__|
 *              \/__/     \/__/     \|__|     \/__/     \/__/     \/__/
 * <p>
 * The TalkBox Configuration App. Once a *.tbc file in the app via <code>File > Open</code>, a user may edit any of the buttons on the TalkBox via the context menu. More specifically, the <codez>TalkBox.getTotalNumberOfButtons()</codez> buttons may be removed, renamed, or have an audio file added to them.
 * <p>
 * Upon clicking any one of the buttons, the button plays the audio if it has any; otherwise, the user is prompted to select an audio file to use. The configuration may then be saved via <code>File > Save</code>
 * <p>
 * The backend of the app uses a FlowPane of buttons in addition to a Pagination control, wrapped together in a VBox. Upon exit, a dialog is presented to ask the user to save the file before exit, or discard its state.
 * <p>
 * Furthermore, the TalkBoxApp communicates with a TalkBoxSimulator or TalkBoxDevice via the use of TalkBoxInfo serialized objects described via *.tbc files. As of 01/27/19, TalkBoxApp is a minimum viable product.
 * <p>
 * TODO: 2019-01-27  add Context Menus to allow for options; account for edge cases
 * FIXME: 2019-01-27 get audio working
 *
 * @author Richard Robinson
 * @version 0.1
 */
public class TalkBoxApp extends Application {
	private File file;
	private TalkBoxData ts;
	private Button[] buttons;
	private Stage primaryStage;
	private VBox box;
	private MenuItem open, save;

	/**
	 * The main method to launch the application
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Initializes the app.
	 * @see #configButtons(int) the main process of the app which configures and sets the buttons and repeats for each data set in the pagination. In general, *everything* aside from global aspects of the app should be in here
	 * @see #warnBeforeExit() method to warn user before exit
	 * @see #open(ActionEvent) method to open file
	 * @see #save(ActionEvent) method to save file
	 *
	 * @param primaryStage cuz Java needs this
	 * @throws Exception just in case
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		/* Initializes app */
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		primaryStage.setTitle("TalkBox Config");
		primaryStage.setWidth(500);
		primaryStage.setHeight(400);
		primaryStage.getIcons().add(new Image(TalkBoxApp.class.getResourceAsStream("icon2.png")));

		/* Creates the outermost container, composing of a `MenuBar` and `FlowPane` */
		box = new VBox();

		/* Creates the menu bar */
		MenuBar menuBar = new MenuBar();
		menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

		/* Creates the sole menu in the menu bar, `File` */
		Menu menuFile = new Menu("File");
		menuBar.getMenus().addAll(menuFile);

		/* Adds an Open and Save action to the File menu. The latter is initially disabled. */
		open = new MenuItem("Open");
		save = new MenuItem("Save");
		save.setDisable(true);

		/* Creates main scene */
		Scene scene = new Scene(box);
		save.setOnAction(this::save);
		open.setOnAction(this::open);

		// show menu bar
		menuFile.getItems().addAll(open, save);
		box.getChildren().addAll(menuBar);

		// show window
		primaryStage.setScene(scene);
		primaryStage.show();

		warnBeforeExit();
	}

	/**
	 * Upon application close, presents a warning dialog asking the user if they wish to (a) save changes, (b) do not save changes, or (c) cancel
	 * <p>
	 * TODO: 2019-01-28  if no edits were made, do not present dialog
	 */
	private void warnBeforeExit() {
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
				save(null);
				Platform.exit();
			} else if (result.isPresent() && result.get() == noButton) {
				Platform.exit();
			} else if (result.isPresent() && result.get() == cancelButton) {
				event.consume();
			}
		});
	}

	/**
	 * Method reference to save the file (if called without reference, pass <code>null</code> as parameter
	 */
	private void save(ActionEvent event) {
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

	/**
	 * Method reference to open a file, then passes control to <code>configButtons</code>
	 *
	 * @see #configButtons(int)
	 */
	private void open(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("TalkBox Config File (.tbc)", "*.tbc"); // specifies file type
		fileChooser.getExtensionFilters().add(filter); // specifies file type

		fileChooser.setTitle("Open TalkBox File"); // specifies file prompt
		this.file = fileChooser.showOpenDialog(primaryStage); // displays file chooser window

		// adds file name to Window title
		primaryStage.setTitle("TalkBox Configurator — " + file.getName());

		try {
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream oin = new ObjectInputStream(fis);

			ts = (TalkBoxData) oin.readObject();
			buttons = new Button[ts.numberOfAudioButtons];

			Pagination pagination = new Pagination(ts.numberOfAudioSets);
			box.getChildren().add(pagination);

			pagination.setPageFactory(this::configButtons);

			save.setDisable(false);
			open.setDisable(true);
		} catch (IOException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}


	/**
	 * The main process of the app. Asynchronously continuously repeats for each page in the pagination of audio sets. Creates a FlowPane for each audio set, to use as a method reference for <code>setPageFactory()</code> method of a pagination
	 *
	 * @param page a generalized audio set
	 * @return the FlowPane created with the buttons
	 */
	private FlowPane configButtons(int page) {
		FlowPane flowPane = new FlowPane();
		flowPane.setPadding(new Insets(30, 20, 30, 20));
		flowPane.setVgap(10);
		flowPane.setHgap(10);
		flowPane.setAlignment(Pos.CENTER);

		// make the buttons
		for (int i = 0; i < ts.numberOfAudioButtons; i++) {
			buttons[i] = ts.audioFilenames[page][i] == null
					? new Button("Empty")
					: new Button(ts.audioFilenames[page][i]);

			buttons[i].setPrefSize(100, 100);
			flowPane.getChildren().add(buttons[i]);
		}

		// on button press
		for (int i = 0; i < ts.getNumberOfAudioButtons(); i++) {
			int j = i;
			buttons[i].setOnAction(event2 -> {
				if (ts.audioFilenames[page][j] == null) {
					setAudio(page, j);
				} else {
					File soundFile = new File(ts.audioFilenames[page][j]);
					Media media = new Media(soundFile.toURI().toString());
					MediaPlayer player = new MediaPlayer(media);
					player.play();
				}
				buttons[j].setText(ts.audioFilenames[page][j]);
			});
		}

		// button context menus
		for (int i = 0; i < ts.getNumberOfAudioButtons(); i++) {
			int j = i;

			ContextMenu contextMenu = new ContextMenu();
			MenuItem rename = new MenuItem("Rename");
			MenuItem remove = new MenuItem("Remove");
			MenuItem change = new MenuItem("Change");
			contextMenu.getItems().addAll(rename, remove, change);

			change.setOnAction(event -> setAudio(page, j));

			buttons[j].setContextMenu(contextMenu);

			// if button has no file, disable context menu items
			if (ts.audioFilenames[page][j] == null) {
				for (MenuItem menuItem : contextMenu.getItems())
					menuItem.setDisable(true);
			}
		}

		return flowPane;
	}

	/**
	 * Sets the audio file located at [page, j] to the file the user chooses
	 * @param page the audio set
	 * @param j the audio button
	 */
	private void setAudio(int page, int j) {
		FileChooser audioFile = new FileChooser();
		FileChooser.ExtensionFilter filter2 = new FileChooser.ExtensionFilter("Audio File", "*.mp3", "*.wav");
		audioFile.getExtensionFilters().add(filter2);

		audioFile.setTitle("Select Audio File");
		File audio = audioFile.showOpenDialog(primaryStage);
		ts.audioFilenames[page][j] = audio.getPath();
	}
}
