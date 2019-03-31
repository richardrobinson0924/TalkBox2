package talkboxnew.AddWizard;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextFlow;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import talkboxnew.Entry;

import static talkboxnew.AddWizard.AddWizardView.*;

public class NamePane extends WizardPane {
	private TextField textField;

	public static final String[] nodesId = {"AudioName"};

	NamePane(Entry oldEntry) {
		super();
		this.textField = new TextField(oldEntry == null ? "" : oldEntry.getName());

		this.setHeaderText("Enter Button Name");
		this.setContent(getView());
		this.setPrefSize(WIDTH, HEIGHT);
	}

	private GridPane getView() {
		final GridPane grid = new GridPane();

		textField.requestFocus();
		textField.setId(nodesId[0]);

		grid.setHgap(SPACING);
		grid.setVgap(SPACING);
		grid.setPadding(new Insets(10,120,20,10));

		final Label text = new Label("Please enter the name of the button you would like to display \nto appear on the button.");
		text.setWrapText(true);

		grid.add(new TextFlow(text), 0, 0, 2, 1);
		grid.addRow(1, new Label("Audio Name:"), textField);

		return grid;
	}

	@Override
	public void onEnteringPage(Wizard wizard) {
		wizard.invalidProperty().unbind();
		wizard.invalidProperty().bind(textField.textProperty().isEmpty());
		AddWizardView.next.setValue(5);
	}
}
