package talkboxnew;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

import static talkboxnew.Utils.tryFactory;

public final class CreateConfigWizard {
	private final Wizard wiz = new Wizard();
	private final TalkBoxData tbd = new TalkBoxData();
	private final StringProperty filename = new SimpleStringProperty();
	private final StringProperty pathname = new SimpleStringProperty();

	private static final Logger log = Logger.getLogger(CreateConfigWizard.class.getName());

	private final static int PANE_HEIGHT = 210,
			PANE_WIDTH = 500,
			SPACING = 10,
			NUM_PANES = 4;

	private final static Insets PADDING = new Insets(2 * SPACING, 100, SPACING, SPACING);

	public static final String[] NAMEWIZARDPANE_NODES = {"NAME_TEXTFIELD"};
	public static final String[] NUMBERSWIZARDPANE_NODES = {"NUMBUTTONS_SPINNER","NUMSETS_SPINNER"};
	public static final String[] FILEWIZARDPANE_NODES = {"CHOOSE_BUTTON"};


	public CreateConfigWizard() {
		log.info("Config Wizard invoked...");
		tbd.database = new ArrayList<>();
		tbd.customWords = new ArrayList<>(NUM_PANES);

		IntStream.range(0, NUM_PANES)
				.forEach(i -> tbd.customWords.add(new ArrayList<>()));

		tbd.customWords.get(3).addAll(Arrays.asList(
				"Future",
				"Past",
				"Present",
				"Negate",
				"Question?"
		));

		final Wizard.LinearFlow wizFlow = new Wizard.LinearFlow(
				getName(),
				getNumbers(),
				getFile()
		);

		wiz.flowProperty().setValue(wizFlow);
	}

	public boolean getWizard() {
		final Optional<ButtonType> result = wiz.showAndWait();

		if (result.isPresent() && result.get() == ButtonType.FINISH) {
			tryFactory.attemptTo(() -> end(result.get()));
			return true;
		}

		return false;
	}

	private WizardPane getFile() {
		log.info("File pane invoked");
		final GridPane grid = new GridPane();
		grid.setHgap(SPACING);
		grid.setVgap(SPACING);
		grid.setPadding(PADDING);

		final TextField textField = new TextField(System.getProperty("user.home"));
		final Button choose = new Button("Choose");
		// setting id for choose button
		choose.setId(FILEWIZARDPANE_NODES[0]);

		choose.setOnAction(e -> {
			final DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());

			final File dir = directoryChooser.showDialog(null);
			textField.textProperty().setValue(dir.getAbsolutePath());
			log.info("Path to Directory: " + dir.toString());
		});

		grid.addRow(0, new Label("Select Directory:"), textField, choose);

		final ValidationSupport validation = new ValidationSupport();
		validation.registerValidator(textField, (Validator<String>) (control, s) -> new ValidationResult().addErrorIf(
				control,
				"Directory does not exist",
				!Files.exists(Paths.get(s)))
		);

		final ValidationSupport alreadyExistsValidation = new ValidationSupport();
		alreadyExistsValidation.registerValidator(textField, (Validator<String>) (control, s) -> new ValidationResult().addErrorIf(control,
						"File already exists",
						Files.exists(Paths.get(pathname.getValueSafe(), filename.getValueSafe())))
		);

		pathname.bind(textField.textProperty());

		final WizardPane pane = new WizardPane() {
			@Override
			public void onEnteringPage(Wizard wizard) {
				wiz.invalidProperty().unbind();
				wiz.invalidProperty().bind(validation.invalidProperty());

				this.lookupButton(ButtonType.FINISH).addEventFilter(ActionEvent.ACTION, e -> finishAttempt(alreadyExistsValidation, e));
			}
		};

		pane.setContent(grid);
		pane.setHeaderText("Select File Destination");
		return pane;
	}

	private void finishAttempt(ValidationSupport validation, ActionEvent event) {
		if (!validation.isInvalid()) return;

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Directory Exists");
		alert.setHeaderText("Directory Already Exists");

		alert.setContentText("The directory "
				+ Paths.get(pathname.getValueSafe(), filename.getValueSafe()).toString()
				+ " already exists. Would you like to overwrite the contents with the new TalkBox directory? This action cannot be undone."
		);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			log.error("Overwriting exists file");
			alert.close();
		} else {
			log.error("Event consumed. Returning to wizard.");
			event.consume();
		}
	}

	private WizardPane getNumbers() {
		log.info("Numbers pane invoked");
		final GridPane grid = new GridPane();
		grid.setHgap(SPACING);
		grid.setVgap(SPACING);
		grid.setPadding(PADDING);

		final Spinner<Integer> numButtons = new Spinner<>(1, 10, 1);
		final Spinner<Integer> numSets = new Spinner<>(1, 10, 1);

		grid.addRow(0, new Label("Audio Buttons:"), numButtons);
		grid.addRow(1, new Label("Audio Sets:"), numSets);

		final WizardPane wizardPane = new WizardPane() {
			@Override
			public void onExitingPage(Wizard wizard) {
				tbd.numberOfAudioSets = numSets.getValue();
				tbd.numberOfAudioButtons = numButtons.getValue();
			}
		};

		wizardPane.setHeaderText("Select Number of Audio Buttons");

		numButtons.setId(NUMBERSWIZARDPANE_NODES[0]);
		numSets.setId(NUMBERSWIZARDPANE_NODES[1]);

		wizardPane.setContent(grid);
		return wizardPane;
	}

	private WizardPane getName() {
		log.info("Name wizard pane invoked");
		final TextField textField = new TextField();

		textField.setPromptText("TalkBox Configuration");
		textField.requestFocus();

		filename.bind(textField.textProperty());
		wiz.invalidProperty().bind(textField.textProperty().isEmpty());

		final WizardPane namePane = new WizardPane();
		namePane.setHeaderText("Choose Name for File");
		namePane.setPrefHeight(210);

		final GridPane grid = new GridPane();
		grid.setHgap(SPACING);
		grid.setVgap(SPACING);
		grid.setPadding(PADDING);
		grid.addRow(0, new Label("File Name:"), textField);

		namePane.setContent(grid);

		// set Id for name wizard pane and textfield in wizard pane
		textField.setId(NAMEWIZARDPANE_NODES[0]);
		return namePane;
	}

	private File makeDirectory() throws IOException {
		final File file = Paths.get(pathname.getValueSafe(), filename.getValueSafe()).toFile();
		if (file.exists()) FileUtils.deleteDirectory(file);
		if (!file.mkdir()) Utils.release(new FileSystemException(file.getName()));

		final File audio = Paths.get(file.getPath(), "Audio").toFile();
		if (!audio.mkdir()) Utils.release(new FileSystemException(file.getName()));

		final File images = Paths.get(file.getPath(), "Images").toFile();
		if (!images.mkdir()) Utils.release(new FileSystemException(file.getName()));

		return file;
	}

	private void end(ButtonType buttonType1) throws IOException {
		final File file = makeDirectory();
		final File tbcFile = Paths
				.get(file.getPath(), filename.getValueSafe() + ".tbc")
				.toFile();

		IntStream.range(0, tbd.getTotalNumberOfButtons())
				.forEach(i -> tbd.database.add(null));

		IntStream.range(0, 3)
				.forEach(i -> tbd.customWords.add(new ArrayList<>()));

		tryFactory.attemptTo(() -> {
			final FileOutputStream fos = new FileOutputStream(tbcFile);
			final ObjectOutputStream oos = new ObjectOutputStream(fos);

			oos.writeObject(tbd);
			oos.flush();
			oos.close();
		});

		log.info("TBC Directory Created (" + file.toString() + ") with " + tbd.getTotalNumberOfButtons() + " buttons");

		tryFactory.attemptTo(() -> {
			ConfigStage configStage = new ConfigStage(file.toPath());
			configStage.show();
			log.info("Wizard complete. Opening Configurator stage with created file");
		});
	}
}
