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
import java.util.stream.IntStream;

/**
 * NOT SUITABLE YET FOR PRODUCTION USE
 * <p>
 * The TalkBox Configuration App. Once a *.tbc file in the app via <code>File > Open</code>, a user may edit any of the buttons on the TalkBox via the context menu. More specifically, the <code>TalkBox.getTotalNumberOfButtons()</code> buttons may be removed, renamed, or have an audio file added to them.
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
 * @author EECS 2311 W2019 Z, Group 2
 * @version 0.1
 */
public class TalkBoxApp extends Application {
	private File file;
	private TalkBoxData ts;
	private Button[] buttons;
	private Stage primaryStage;
	private VBox box;
	private MenuItem open, save;

	private static final String DELIM = "\\|";

	/* DO NOT modify this field directly. Instead, use the `setIsChanged()` method */
	private boolean fileIsChanged = false;

	/**
	 * The main method to launch the application
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Initializes the app.
	 *
	 * @param primaryStage cuz Java needs this
	 * @throws Exception just in case
	 * @see #configButtons(int) the main process of the app which configures and sets the buttons and repeats for each data set in the pagination. In general, *everything* aside from global aspects of the app should be in here
	 * @see #warnBeforeExit() method to warn user before exit
	 * @see #open(ActionEvent) method to open file
	 * @see #save(ActionEvent) method to save file
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
		Menu menuHelp = new Menu("Help");
		menuBar.getMenus().addAll(menuFile, menuHelp);

		/* Adds an Open and Save action to the File menu. The latter is initially disabled. */
		open = new MenuItem("Open");
		save = new MenuItem("Save");
		MenuItem newAudio = new MenuItem("Launch TTS Wizard");
		save.setDisable(true);

		MenuItem about = new MenuItem("About");
		MenuItem help = new MenuItem("Help");

		/* Creates main scene */
		Scene scene = new Scene(box);
		save.setOnAction(this::save);
		open.setOnAction(this::open);

		newAudio.setOnAction(event -> TTSWizard.launch(primaryStage));

		about.setOnAction(this::about);
		help.setOnAction(this::help);

		// show menu bar
		menuFile.getItems().addAll(open, save, newAudio);
		menuHelp.getItems().addAll(about, help);

		box.getChildren().addAll(menuBar);

		// show window
		primaryStage.setScene(scene);
		primaryStage.show();

		open(null);
		warnBeforeExit();
	}

	private void help(ActionEvent event) {
	}

	private void about(ActionEvent event) {
	}

	/**
	 * Upon application close, presents a warning dialog asking the user if they wish to (a) save changes, (b) do not save changes, or (c) cancel
	 */
	private void warnBeforeExit() {
		primaryStage.setOnCloseRequest(event -> {
			if (!fileIsChanged) return;

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
			setIsChanged(false);
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
			String caption = (ts.audioFilenames[page][i] == null)
					? "Empty"
					: ts.getAlias(page, i);

			buttons[i] = new Button(caption);
			buttons[i].setPrefSize(100, 100);
			flowPane.getChildren().add(buttons[i]);
		}

		// on button press
		IntStream.range(0, ts.getNumberOfAudioButtons()).forEach(i -> buttons[i].setOnAction(event2 -> {
			boolean added = false;

			if (ts.audioFilenames[page][i] == null) {
				setAudio(null, page, i);
				String caption = ts.getAlias(page, i);

				buttons[i].setText(caption);
				makeContextMenu(page, i);
				added = true;
			}

			File soundFile = new File(ts.getPath(page, i));
			Media media = new Media(soundFile.toURI().toString());
			MediaPlayer player = new MediaPlayer(media);
			if (!added) player.play();
		}));

		// button context menus
		IntStream.range(0, ts.getNumberOfAudioButtons()).forEach(i -> makeContextMenu(page, i));

		return flowPane;
	}

	/**
	 * Generates the context menu for each button. If button has no file, disable menu items.
	 *
	 * @param page the audio set
	 * @param j    the audio button
	 */
	private void makeContextMenu(int page, int j) {
		ContextMenu contextMenu = new ContextMenu();

		MenuItem rename = new MenuItem("Rename");
		MenuItem remove = new MenuItem("Remove");
		MenuItem change = new MenuItem("Change");
		contextMenu.getItems().addAll(rename, remove, change);

		change.setOnAction(event -> {
			setAudio(contextMenu, page, j);
			buttons[j].setText(ts.getAlias(page, j));
		});

		rename.setOnAction(event -> changeName(page, j));
		remove.setOnAction(event -> remove(page, j));

		buttons[j].setContextMenu(contextMenu);

		// if button has no file, disable context menu items
		if (ts.audioFilenames[page][j] == null) {
			contextMenu.getItems().forEach(menuItem -> menuItem.setDisable(true));
		}
	}

	/**
	 * Removes an audio file
	 *
	 * @param page the audio set
	 * @param j    the audio button
	 */
	private void remove(int page, int j) {
		ts.audioFilenames[page][j] = null;
		buttons[j].setText("Empty");
		setIsChanged(true);
	}

	/**
	 * Displays an input dialog box to change the name of the button text
	 *
	 * @param j the button whose text to change
	 */
	private void changeName(int page, int j) {
		TextInputDialog dialog = new TextInputDialog(buttons[j].getText());
		dialog.setTitle("Change Button Name");
		dialog.setHeaderText("Change Button Name");
		dialog.setContentText("Please enter the new name:");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(name -> {
			buttons[j].setText(name);
			String path = ts.getPath(page, j);
			ts.getAudioFileNames()[page][j] = path + DELIM + name;
			setIsChanged(true);
		});
	}

	/**
	 * Sets the audio file located at [page, j] to the file the user chooses
	 *
	 * @param page the audio set
	 * @param j    the audio button
	 */
	private void setAudio(ContextMenu contextMenu, int page, int j) {
		FileChooser audioFile = new FileChooser();
		FileChooser.ExtensionFilter filter2 = new FileChooser.ExtensionFilter("Audio File", "*.mp3", "*.wav");
		audioFile.getExtensionFilters().add(filter2);

		audioFile.setTitle("Select Audio File");
		File audio = audioFile.showOpenDialog(primaryStage);

		if (audio != null) {
			ts.audioFilenames[page][j] = audio.getPath() + DELIM + audio.getName();
			setIsChanged(true);
			if (contextMenu != null) contextMenu.getItems().forEach(menuItem -> menuItem.setDisable(false));
		}
	}

	/**
	 * An intermediary method to change the state of <code>fileIsChanged</code>. If a file is saved, disable the Save menu item, and don't warn upon application exit. If a file is edited, add <code>(Edited)</code> to the title bar
	 *
	 * @param isChanged set to true if a property is modified; set to false upon file save
	 */
	private void setIsChanged(boolean isChanged) {
		if (fileIsChanged == isChanged) return;
		fileIsChanged = isChanged;

		if (isChanged) {
			save.setDisable(false);
			primaryStage.setTitle("TalkBox Configurator — " + file.getName() + " (Edited)");
		} else {
			save.setDisable(true);
			primaryStage.setTitle("TalkBox Configurator — " + file.getName());
		}
	}
}
