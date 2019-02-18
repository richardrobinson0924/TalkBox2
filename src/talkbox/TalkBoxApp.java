package talkbox;

import com.sun.media.sound.WaveFileWriter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.stream.IntStream;

import static talkbox.TalkBoxData.*;

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
 *
 * @author EECS 2311 W2019 Z, Group 2
 * @version 0.1
 */
public class TalkBoxApp extends Application {
	private TalkBoxData ts;
	private Button[] buttons;
	private File audioFolder;

	private static File file;
	private static Stage primaryStage;
	private static MenuItem save;

	/* DO NOT modify this field directly. Instead, use the `setIsChanged()` method */
	private static boolean fileIsChanged = false;

	private final static int GRAPHIC_SIZE = 55;
	private final static int BUTTON_SIZE = 100;
	private final static Image GRAPHIC = new Image(TalkBoxApp.class.getResource("/Resources/button_graphic.png").toString());
	private final static String AUDIO_PATH = "/Audio";

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
	 * @see #configButtons(int) the main process of the app which configures and sets the buttons and repeats for each data set in the pagination. In general, *everything* aside from global aspects of the app should be in here
	 * @see #warnBeforeExit() method to warn user before exit
	 * @see #open(VBox, MenuItem, Scene) method to open file
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		TalkBoxApp.primaryStage = primaryStage;
		Try.setFailSafe(TalkBoxApp::setFailSafe);

		/* Sets the UI */
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		/* Sets window size and title */
		primaryStage.setTitle("TalkBox Config");
		primaryStage.setWidth(500);
		primaryStage.setHeight(400);
		primaryStage.getIcons().add(new Image(TalkBoxApp.class.getResourceAsStream("/Resources/icon2.png")));

		/* Creates the outermost container, composing of a `MenuBar` and `FlowPane` */
		final VBox box = new VBox();

		/* Creates the menu bar */
		final MenuBar menuBar = new MenuBar();
		menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

		/* Creates the sole menu in the menu bar, `File` */
		final Menu menuFile = new Menu("File");
		final Menu menuHelp = new Menu("Help");
		final Menu menuView = new Menu("View");
		menuBar.getMenus().addAll(menuFile, menuView, menuHelp);

		/* Adds an Open and Save action to the File menu. The latter is initially disabled. */
		final MenuItem open = new MenuItem("Open");
		save = new MenuItem("Save");
		save.setDisable(true);

		/* Creates about and help menus */
		final MenuItem about = new MenuItem("About");
		final MenuItem help = new MenuItem("Help");

		final MenuItem custom = new MenuItem("Custom Phrase List");

		/* Creates main scene */
		final Scene scene = new Scene(box);

		/* Configures the `save` action, which attempts to execute the `save()` method */
		save.setOnAction(e -> Try.newBuilder()
				.setDefault(this::save)
				.run());

		/* Configures the `open` action, which attempts to execute the `open()` method */
		open.setOnAction((event) -> open(box, open, scene));

		about.setOnAction(this::about);
		help.setOnAction(this::help);

		// show menu bar
		menuFile.getItems().addAll(open, save);
		menuHelp.getItems().addAll(about, help);
		menuView.getItems().add(custom);

		custom.setOnAction(event -> {
			final CustomDataView c = new CustomDataView(ts, primaryStage);
			Try.newBuilder()
					.setDefault(() -> c.start(new Stage()))
					.setOtherwise(event::consume)
					.run();
		});

		box.getChildren().addAll(menuBar);

		// show window
		primaryStage.setScene(scene);
		primaryStage.show();

		/* start app by opening a file with `open()` */
		open(box, open, scene);

		/* Upon exit, call method to prompt user to save */
		warnBeforeExit();
	}

	/**
	 * The method that is called whenever an exception is thrown. When an error occurs, an error dialog appears presenting the error and the exception's stacktrace, and consumes the excepted action.
	 *
	 * @param ex the exception that is thrown
	 */
	static void setFailSafe(Exception ex) {
		final Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("An Error has Occurred");
		alert.setHeaderText(alert.getTitle());
		alert.setContentText(ex.getMessage());

		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		final String exceptionText = sw.toString();

		final Label label = new Label("Full error message:");

		final TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		final GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		alert.getDialogPane().setExpandableContent(expContent);
		alert.showAndWait();
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

			final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Confirm Exit");
			alert.setHeaderText("Save File?");
			alert.setContentText("Please choose an option.");

			final ButtonType yesButton = new ButtonType("Yes");
			final ButtonType noButton = new ButtonType("No");
			final ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

			alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

			final Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == yesButton) {
				event.consume();
				Try.newBuilder()
						.setDefault(this::save)
						.run();
				Platform.exit();
			} else if (result.isPresent() && result.get() == noButton) {
				Platform.exit();
			} else if (result.isPresent() && result.get() == cancelButton) {
				event.consume();
			}
		});
	}

	/**
	 * Method reference to open a file, then passes control to <code>configButtons</code>
	 *
	 * @see #configButtons(int)
	 */
	private void open(VBox box, MenuItem openMenu, Scene scene) {
		final FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("TalkBox Config File (.tbc)", "*.tbc"); // specifies file type
		fileChooser.getExtensionFilters().add(filter); // specifies file type

		fileChooser.setTitle("Open TalkBox File"); // specifies file prompt
		file = fileChooser.showOpenDialog(primaryStage); // displays file chooser window
		audioFolder = new File(file.getParent().concat(AUDIO_PATH));

		// adds file name to Window title
		primaryStage.setTitle("TalkBox Configurator — " + file.getName());

		Try.newBuilder()
				.setDefault(this::readFile)
				.setOtherwise(() -> open(box, openMenu, scene))
				.run();

		buttons = new Button[ts.numberOfAudioButtons];

		scene.setOnKeyTyped(e -> {
			final int index = Integer.parseInt(e.getCharacter()) - 1;
			if (index < ts.getNumberOfAudioButtons())
				buttons[index].fire();
		});

		final Pagination pagination = new Pagination(ts.numberOfAudioSets);
		box.getChildren().add(pagination);

		pagination.setPageFactory(this::configButtons);

		openMenu.setDisable(true);
	}

	/**
	 * The main process of the app. Asynchronously continuously repeats for each page in the pagination of audio sets. Creates a FlowPane for each audio set, to use as a method reference for <code>setPageFactory()</code> method of a pagination
	 *
	 * @param page a generalized audio set
	 * @return the FlowPane created with the buttons
	 */
	private FlowPane configButtons(int page) {
		final FlowPane flowPane = new FlowPane();
		flowPane.setPadding(new Insets(30, 20, 30, 20));
		flowPane.setVgap(10);
		flowPane.setHgap(10);
		flowPane.setAlignment(Pos.CENTER);

		for (int i = 0; i < ts.numberOfAudioButtons; i++) {
			final String caption = (ts.database[page][i] != null)
					? ts.database[page][i].getValue()
					: "Empty";

			buttons[i] = new Button(caption);

			buttons[i].setContentDisplay(ContentDisplay.TOP);
			buttons[i].setPrefSize(BUTTON_SIZE, BUTTON_SIZE);

			flowPane.getChildren().add(buttons[i]);

			setButtonAction(page, i);
			makeContextMenu(page, i);
		}

		setDragAndDrop(page);

		IntStream.range(0, ts.getNumberOfAudioButtons())
				.filter(i -> ts.database[page][i] != null)
				.forEach(this::setGraphic);

		return flowPane;
	}

	private void setButtonAction(int page, int i) {
		buttons[i].setOnAction(event2 -> {
			boolean added = false;

			if (ts.database[page][i] == null) {
				final AudioInputStream audio = TTSWizard.launch(primaryStage);
				if (audio == null) return;

				final WaveFileWriter w = new WaveFileWriter();
				final File f = new File(getFullPath("Audio_" + i + ".wav"));

				Try.newBuilder()
						.setDefault(() -> w.write(audio, AudioFileFormat.Type.WAVE, f))
						.run();

				ts.database[page][i] = new AudioPair(f, f.getName());

				buttons[i].setText(f.getName());
				makeContextMenu(page, i);

				setGraphic(i);
				setIsChanged(true);
				added = true;
			}

			if (ts.database[page][i] != null) {
				final File soundFile = ts.database[page][i].getKey();
				boolean finalAdded = added;

				Try.newBuilder().setDefault(() -> {
					final Media media = new Media(soundFile.toURI().toString());
					final MediaPlayer player = new MediaPlayer(media);
					if (!finalAdded) player.play();
				}).setOtherwise(() -> remove(page, i)).run();
			}
		});
	}

	/**
	 * Configures the drag and drop operation to allow a user to drag a *.wav file onto a button to change its file. If a non wav file is dragged, the event is consumed and no action occurs
	 *
	 * @param page the audio set
	 */
	private void setDragAndDrop(int page) {
		IntStream.range(0, ts.getNumberOfAudioButtons()).forEach(i -> {
			buttons[i].setOnDragOver(event -> {
				if (event.getGestureSource() != buttons[i] && event.getDragboard().hasFiles())
					event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
				event.consume();
			});

			buttons[i].setOnDragDropped(event -> {
				final Dragboard dragboard = event.getDragboard();

				final File file = dragboard.getFiles().get(0);
				if (!file.getPath().endsWith(".wav")) event.consume();

				ts.database[page][i] = new AudioPair(file, file.getName());

				buttons[i].setText(file.getName());
				setIsChanged(true);
			});
		});
	}

	/**
	 * Generates the context menu for each button. If button has no file, disable menu items.
	 *
	 * @param page the audio set
	 * @param j    the audio button
	 */
	private void makeContextMenu(int page, int j) {
		final ContextMenu contextMenu = new ContextMenu();

		final MenuItem rename = new MenuItem("Rename");
		final MenuItem remove = new MenuItem("Remove");
		final MenuItem change = new MenuItem("Change");
		contextMenu.getItems().addAll(rename, remove, change);

		change.setOnAction(event -> {
			setAudio(contextMenu, page, j);
			buttons[j].setText(ts.database[page][j].getValue());
		});

		rename.setOnAction(event -> changeName(page, j));
		remove.setOnAction(event -> remove(page, j));

		buttons[j].setContextMenu(contextMenu);

		// if button has no file, disable context menu items
		if (ts.database[page][j] == null) {
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
		final File f = ts.database[page][j].getKey();

		ts.database[page][j] = null;
		buttons[j].setText("Empty");
		setIsChanged(true);

		Try.newBuilder().setDefault(() -> {
			if (f.exists()) Files.delete(f.toPath());
		}).run();

		final ImageView blank = new ImageView();
		blank.setImage(null);
		buttons[j].setGraphic(blank);
	}

	/**
	 * Displays an input dialog box to change the name of the button text
	 *
	 * @param j the button whose text to change
	 */
	private void changeName(int page, int j) {
		final TextInputDialog dialog = new TextInputDialog(buttons[j].getText());
		dialog.setTitle("Change Button Name");
		dialog.setHeaderText("Change Button Name");
		dialog.setContentText("Please enter the new name:");

		final Optional<String> result = dialog.showAndWait();
		result.ifPresent(name -> {
			buttons[j].setText(name);

			ts.database[page][j].setValue(name);
			setIsChanged(true);
		});
	}

	/**
	 * Sets the audio file located at  [page, j] to the file the user chooses
	 *
	 * @param page the audio set
	 * @param j    the audio button
	 */
	private void setAudio(ContextMenu contextMenu, int page, int j) {
		final FileChooser audioFile = new FileChooser();
		final FileChooser.ExtensionFilter filter2 = new FileChooser.ExtensionFilter("Audio File", "*.mp3", "*.wav");
		audioFile.getExtensionFilters().add(filter2);

		audioFile.setTitle("Select Audio File");
		final File audio = audioFile.showOpenDialog(primaryStage);
		if (audio == null) return;

		Try.newBuilder().setDefault(() -> {
			FileOutputStream copied = new FileOutputStream(getFullPath(audio.getName()));
			Files.copy(audio.toPath(), copied);
		}).run();

		ts.database[page][j] = new AudioPair(audio, audio.getName());
		setIsChanged(true);

		if (contextMenu != null)
			contextMenu.getItems().forEach(menuItem -> menuItem.setDisable(false));

		setGraphic(j);
	}

	/**
	 * An intermediary method to change the state of <code>fileIsChanged</code>. If a file is saved, disable the Save menu item, and don't warn upon application exit. If a file is edited, add <code>(Edited)</code> to the title bar
	 *
	 * @param isChanged set to true if a property is modified; set to false upon file save
	 */
	static void setIsChanged(boolean isChanged) {
		if (fileIsChanged == isChanged) return;
		fileIsChanged = isChanged;

		save.setDisable(!isChanged);

		primaryStage.setTitle(MessageFormat.format("TalkBox Configurator \u2014 {0}{1}",
				file.getName(),
				(isChanged) ? " (Edited)" : ""));
	}

	/**
	 * Helper method to deserialize <code>file</code> into <code>ts</code>
	 *
	 * @throws Exception if an exception occurs
	 */
	private void readFile() throws Exception {
		FileInputStream fis;
		ObjectInputStream oin;

		fis = new FileInputStream(file);
		oin = new ObjectInputStream(fis);

		ts = (TalkBoxData) oin.readObject();
	}

	/**
	 * Saves and serializes <code>ts</code> to <code>file</code>
	 *
	 * @throws Exception exception
	 */
	private void save() throws Exception {
		final FileOutputStream fos = new FileOutputStream(file.toString());
		final ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(ts);
		oos.flush();
		oos.close();
		setIsChanged(false);
	}

	/**
	 * Creates a new ImageView and sets buttons[i] graphic as such
	 *
	 * @param i index of the button
	 */
	private void setGraphic(int i) {
		final ImageView graphic = new ImageView(GRAPHIC);

		graphic.setFitHeight(GRAPHIC_SIZE);
		graphic.setPreserveRatio(true);
		buttons[i].setGraphic(graphic);
	}

	private String getFullPath(String s) {
		return audioFolder.getPath().concat('/' + s);
	}
}
