package talkboxnew.AddWizard;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.controlsfx.validation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static talkboxnew.AddWizard.AddWizardView.*;

public class FilePane extends WizardPane {
	private TextField textField;

	FilePane() {
		super();
		textField = new TextField();

		this.setHeaderText("Select a File");
		this.setContent(getView());
		this.setPrefSize(WIDTH, HEIGHT);
	}

	private GridPane getView() {
		final GridPane grid = new GridPane();

		final Label label = new Label("Audio File");
		final Button file = new Button("Choose");

		final TextField textField = new TextField();
		textField.setId("filename");

		final Label text = new Label("Choose an existing audio file to use with this button.\nOnly *.wav files are supported.");
		text.setWrapText(true);

		grid.add(new TextFlow(text), 0, 0, 3, 1);
		grid.addRow(1, label, textField, file);

		file.setOnAction(this::onAction);

		grid.setHgap(SPACING);
		grid.setVgap(SPACING);
		grid.setPadding(PADDING);

		return grid;
	}

	@Override
	public void onEnteringPage(Wizard wizard) {
		AddWizardView.next.setValue(4);
		wizard.invalidProperty().unbind();
		wizard.invalidProperty().bind(getValidation().invalidProperty());
	}

	private void onAction(ActionEvent ae) {
		final FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("(.wav)", "*.wav"));

		final File f = fileChooser.showOpenDialog(null);
		if (f.exists()) textField.setText(f.getPath());
	}

	private ValidationSupport getValidation() {
		final ValidationSupport validation = new ValidationSupport();
		validation.registerValidator(textField, (Validator<String>) (control, s) -> new ValidationResult().addErrorIf(
				control,
				"File not valid",
				s.isEmpty() || !Files.exists(Paths.get(s)) || s.matches("^.wav$"))
		);

		return validation;
	}
}
