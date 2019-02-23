package talkbox;

import com.google.api.client.util.Beta;
import com.sun.media.sound.WaveFileWriter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import talkbox.Commands.*;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.swing.*;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static talkbox.Commands.History.*;

/**
 * NOT SUITABLE YET FOR PRODUCTION USE
 * <p></p>
 * The TalkBox Configuration App. Launched via the TalkBox Simulator, with runtime argument supplied byy the latter
 * for the path to the *.tbc serialized object data file.
 * <p></p>
 * Upon clicking any one of the buttons, the button plays the audio if it has any; otherwise, the user is prompted to
 * select an audio file to use, or create their own via a TTS Wizard. The configuration may then be saved via
 * <code>File > Save</code>.
 * <p></p>
 * The backend of the app uses a FlowPane of buttons in addition to a Pagination control, wrapped together in a VBox.
 * Upon exit, a dialog is presented to ask the user to save the file before exit, or discard its state.
 * <p></p>
 * Furthermore, the TalkBoxApp communicates with a TalkBoxSimulator or TalkBoxDevice via the use of TalkBoxInfo
 * serialized objects described via *.tbc files. As of 02/22/19, TalkBoxApp is fully functional itself.
 *
 * @author EECS 2311 W2019 Z, Group 2 (TalkBoxApp: Richard Robinson)
 * @version vMidterm
 */
@Beta
public class TalkBoxApp extends Application {
	private TalkBoxData ts;
	public Button[] buttons;
	private static File audioFolder;
	private MenuItem save;
	private Path path;
	private Stage primaryStage;

	public List<ObservableList<AudioPair>> data = new ArrayList<>();

	private final static int GRAPHIC_SIZE = 55;
	private final static int BUTTON_SIZE = 100;
	private final static Image GRAPHIC = new Image(TalkBoxApp.class.getResource("/Resources/button_graphic.png").toString());
	private final static String AUDIO_PATH = "/Audio";

	/**
	 * Initializes the app.
	 *
	 * @param primaryStage cuz Java needs this
	 * @see #configButtons(int) the main process of the app which configures and sets the buttons and repeats for each
     * data set in the pagination. In general, *everything* aside from global aspects of the app should be in here
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		Try.setFailSafe(TalkBoxApp::setFailSafe);
		path = Paths.get(this.getParameters().getRaw().get(0));

		/* Sets the UI */
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		/* Sets window size and title */
		primaryStage.setTitle("TalkBox Config");
		primaryStage.setWidth(500);
		primaryStage.setHeight(400);
		primaryStage.getIcons().add(new Image(TalkBoxApp.class.getResourceAsStream("/Resources/icon2.png")));

		/* Creates the outermost container, composing of a `MenuBar` and `FlowPane` */
		final VBox box = new VBox();
		final Scene scene = new Scene(box);

		/* Creates the menu bar */
		final MenuBar menuBar = makeMenuBar(box, scene);
		box.getChildren().addAll(menuBar);

		// show window
		primaryStage.setScene(scene);
		primaryStage.show();

		/* start app by opening a file with `open()` */
		open(box, scene);

		/* Upon exit, call method to prompt user to save */
		primaryStage.setOnCloseRequest(this::warnBeforeExit);
	}

	/**
	 * The method to execute upon user close. If the file is saved, the app closes immediately. Otherwise the user is
	 * prompted to save the file, and then the app closes.
	 *
	 * @param event the event triggering the method
	 */
	private void warnBeforeExit(WindowEvent event) {
		if (save.isDisable()) return;

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
					.setDefault(TalkBoxApp.this::save)
					.run();
			Platform.exit();
		} else if (result.isPresent() && result.get() == noButton) {
			Platform.exit();
		} else if (result.isPresent() && result.get() == cancelButton) {
			event.consume();
		}
	}

	/**
	 * Creates the menu bar for the app with the following Menus:
	 * <ul>
	 *     <li>File: Save, Import Audio Files</li>
	 *     <li>Edit: Undo, Custom Phrase List</li>
	 * </ul>
	 * Sets all menu item actions and accelerators as needed. The {@code save} item's disabled property is
	 * initially bound previously so as to be disabled if no new changes have occured.
	 *
	 * @param box the box enclosing this menu bar
	 * @param scene the scene enclosing this menu bar
	 * @return the generated menu bar
	 */
	private MenuBar makeMenuBar(VBox box, Scene scene) {
		MenuBar menuBar = new MenuBar();

		menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

		final Menu menuFile = new Menu("File");
		final Menu menuEdit = new Menu("Edit");

		save = new MenuItem("Save");

		final MenuItem custom = new MenuItem("Custom Phrase List");
		final MenuItem undo = new MenuItem("Undo");
		final MenuItem importM = new MenuItem("Import Audio Files");

		undo.setOnAction(event -> History.getInstance().undo());
		undo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN));

		save.setDisable(true);
		save.setOnAction(e -> Try.newBuilder()
				.setDefault(this::save)
				.run());
		save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));

		custom.setOnAction(event -> {
			final CustomDataView c = new CustomDataView(ts, primaryStage);
			Try.newBuilder()
					.setDefault(() -> c.start(new Stage()))
					.setOtherwise(event::consume)
					.run();
		});
		custom.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN));

		importM.setOnAction(this::importFiles);
		importM.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN));

		menuFile.getItems().addAll(save, importM);
		menuEdit.getItems().addAll(undo, custom);

		menuBar.getMenus().addAll(menuFile, menuEdit);

		return menuBar;
	}

	/**
	 * The action for the {@code Import} menu item. Allows the user to mass import all compatable files from a
	 * directory to the buttons, filling all empty buttons.
	 *
	 * @param actionEvent the event triggering this method
	 */
	private void importFiles(ActionEvent actionEvent) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));
		chooser.setTitle("Select a Directory");

		File dir = chooser.showDialog(primaryStage);

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir.toPath(),
				entry -> entry.toString().matches(".*\\.wav\\s*$"))) {
			History.getInstance().execute(new ImportCommand(stream));
		} catch (IOException ignored) {
		}
	}

	/**
	 * The method that is called whenever an exception is thrown. When an error occurs, an error dialog appears
	 * presenting the error and the exception's stacktrace, and consumes the excepted action.
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

	/**
	 * Method reference to open a file and sets the page factory method of the Pagination to the
	 * <code>configButtons</code> method.
	 *
	 * @see #configButtons(int)
	 */
	private void open(VBox box, Scene scene) {
		audioFolder = new File(path.getParent().toString().concat(AUDIO_PATH));

		primaryStage.setTitle("TalkBox Configurator — " + path.getFileName().toString());

		Try.newBuilder()
				.setDefault(this::readFile)
				.setOtherwise(() -> open(box, scene))
				.run();

		buttons = new Button[ts.numberOfAudioButtons];
		setTalkBoxData(this);

		scene.setOnKeyTyped(e -> {
			if (!e.getCode().isDigitKey()) return;

			final int index = Integer.parseInt(e.getCharacter()) - 1;
			if (index < ts.getNumberOfAudioButtons())
				buttons[index].fire();
		});

		final Pagination pagination = new Pagination(ts.numberOfAudioSets);
		box.getChildren().add(pagination);

		pagination.setPageFactory(this::configButtons);
	}

	/**
	 * The main process of the app. Asynchronously continuously repeats for each page in the pagination of audio sets
	 * . Creates a FlowPane for each audio set, to use as a method reference for <code>setPageFactory()</code> method
	 * of a pagination. Each button has an optional image, a caption, preferred size, cursor property, a bound
	 * tooltip, and a bound text property.
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
			final String caption = (!data.get(page).get(i).isNull())
					? data.get(page).get(i).getValue()
					: "Empty";

			buttons[i] = new Button(caption);

			buttons[i].setContentDisplay(ContentDisplay.TOP);
			buttons[i].setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
			buttons[i].setCursor(Cursor.HAND);

			buttons[i].textProperty().bind(
					Bindings.when(data.get(page).get(i).str.isEmpty().not())
							.then(data.get(page).get(i).str)
							.otherwise("Empty")
			);

			int finalI = i;
			data.get(page).get(i).file.addListener((observable, oldValue, newValue) -> buttons[finalI].setTooltip(new Tooltip(newValue == null ? "Click to Add Audio" : "Click to Play Audio")));

			flowPane.getChildren().add(buttons[i]);

			buttons[i].setOnAction(event -> setAction(page, finalI));
			makeContextMenu(page, i);
		}

		setDragAndDrop(page);

		IntStream.range(0, ts.getNumberOfAudioButtons())
				.filter(i -> !data.get(page).get(i).isNull())
				.forEach(this::setGraphic);

		return flowPane;
	}

	/**
	 * Sets the action for each button. If the button does not contain a file, the TTSWizaed is launched and the
	 * button is set accordingly. Otherwise, the media loaded in the button's file is played.
	 *
	 * @param page the audio set
	 * @param i the audio button
	 */
	private void setAction(int page, int i) {
		if (data.get(page).get(i).isNull()) {
			final AudioInputStream audio = TTSWizard.launch(primaryStage);
			if (audio == null) return;

			final WaveFileWriter w = new WaveFileWriter();
			final File f = new File(getFullPath("Audio_" + page + i + ".wav"));

			Try.newBuilder()
					.setDefault(() -> w.write(audio, AudioFileFormat.Type.WAVE, f))
					.run();

			History.getInstance().execute(new AddCommand(page, i, f, AddCommand.Type.TTS));

		} else if (!data.get(page).get(i).isNull()) {
			final File soundFile = data.get(page).get(i).getKey();

			Try.newBuilder().setDefault(() -> {
				final Media media = new Media(soundFile.toURI().toString());
				final MediaPlayer player = new MediaPlayer(media);
				player.play();
			}).setOtherwise(() -> History.getInstance().execute(new RemoveCommand(page, i))).run();
		}
	}

	/**
	 * Configures the drag and drop operation to allow a user to drag a *.wav file onto a button to change its file,
     * If a non wav file is dragged, the event is consumed and no action occurs
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

				History.getInstance().execute(new AddCommand(page, i, file, AddCommand.Type.FILE));
			});
		});
	}

	/**
	 * Generates the context menu for each button. If button has no file, disable menu items.
	 *
	 * @param page the audio set
	 * @param j    the audio button
	 */
	public void makeContextMenu(int page, int j) {
		final ContextMenu contextMenu = new ContextMenu();

		final MenuItem rename = new MenuItem("Rename");
		final MenuItem remove = new MenuItem("Remove");
		final MenuItem change = new MenuItem("Change");
		contextMenu.getItems().addAll(rename, remove, change);

		change.setOnAction(event -> setAudio(page, j));
		rename.setOnAction(event -> History.getInstance().execute(new RenameCommand(page, j)));
		remove.setOnAction(event -> History.getInstance().execute(new RemoveCommand(page, j)));

		buttons[j].setContextMenu(contextMenu);
	}

	/**
	 * Sets the audio file located at  [page, j] to the file the user chooses
	 *
	 * @param page the audio set
	 * @param j    the audio button
	 */
	private void setAudio(int page, int j) {
		final FileChooser audioFile = new FileChooser();
		final FileChooser.ExtensionFilter filter2 = new FileChooser.ExtensionFilter("Audio File", "*.wav");
		audioFile.getExtensionFilters().add(filter2);

		audioFile.setTitle("Select Audio File");
		final File audio = audioFile.showOpenDialog(primaryStage);
		if (audio == null) return;

		Try.newBuilder().setDefault(() -> {
			FileOutputStream copied = new FileOutputStream(getFullPath(audio.getName()));
			Files.copy(audio.toPath(), copied);
		}).run();

		History.getInstance().execute(new AddCommand(page, j, audio, AddCommand.Type.FILE));
	}

	/**
	 * Helper method to deserialize <code>file</code> into <code>ts</code>
	 *
	 * @throws Exception if an exception occurs
	 */
	private void readFile() throws Exception {
		FileInputStream fis;
		ObjectInputStream oin;

		fis = new FileInputStream(path.toFile());
		oin = new ObjectInputStream(fis);

		ts = (TalkBoxData) oin.readObject();

		for (List<AudioPair> list : ts.database) {
			final ObservableList<AudioPair> inner = FXCollections.observableList(list, (AudioPair p) -> new Observable[]{p.file, p.str});

			inner.addListener((ListChangeListener<AudioPair>) c -> save.setDisable(false));
			data.add(inner);
		}

		fis.close();
		oin.close();
	}

	/**
	 * Saves and serializes <code>ts</code> to <code>file</code>
	 *
	 * @throws Exception exception
	 */
	private void save() throws Exception {
		final FileOutputStream fos = new FileOutputStream(path.toFile());
		final ObjectOutputStream oos = new ObjectOutputStream(fos);

		ts.database = data.stream().map(ArrayList::new).collect(Collectors.toList());

		oos.writeObject(ts);
		oos.flush();
		oos.close();
		fos.flush();
		fos.close();

		save.setDisable(true);
	}

	/**
	 * Creates a new ImageView and sets buttons[i] graphic as such
	 *
	 * @param i index of the button
	 */
	public void setGraphic(int i) {
		final ImageView graphic = new ImageView(GRAPHIC);

		graphic.setFitHeight(GRAPHIC_SIZE);
		graphic.setPreserveRatio(true);
		buttons[i].setGraphic(graphic);
	}

	/**
	 * Tiny helper methiod to construct a path to a filename {@code s} in the audio directory
	 *
	 * @param s the filename
	 * @return the abstract path of the filename as a string
	 */
	public static String getFullPath(String s) {
		return audioFolder.getPath().concat('/' + s);
	}
}
