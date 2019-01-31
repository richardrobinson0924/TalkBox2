package talkbox;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;

import java.util.Optional;

public class TTSWizard {

	public TTSWizard() {
		Dialog<Pair<String, Boolean>> dialog1 = new Dialog<>();
		dialog1.setTitle("Text to Speech Wizard");
		dialog1.setTitle("TTS Wizard");

		ButtonType create = new ButtonType("Test", ButtonBar.ButtonData.LEFT);
		dialog1.getDialogPane()
				.getButtonTypes()
				.addAll(create, ButtonType.OK, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField phrase = new TextField();
		phrase.setPromptText("Hello");

		ToggleGroup group = new ToggleGroup();
		RadioButton male = new RadioButton("Male");
		male.setToggleGroup(group);
		male.setSelected(true);
		RadioButton female = new RadioButton("Female");
		female.setToggleGroup(group);

		HBox box = new HBox(male, female);

		grid.add(new Label("Phrase:"), 0, 0);
		grid.add(phrase, 1, 0);
		grid.add(new Label("Gender:"), 0, 1);
		grid.add(box, 1, 1);

		Node createNode = dialog1.getDialogPane().lookupButton(create);
		createNode.setDisable(true);

		Node authorize = dialog1.getDialogPane().lookupButton(ButtonType.OK);
		authorize.setDisable(true);

		phrase.textProperty().addListener((observable, oldVal, newVal) -> {
			createNode.setDisable(newVal.isEmpty());
			authorize.setDisable(newVal.isEmpty());
		});

		Platform.runLater(phrase::requestFocus);

		dialog1.getDialogPane().setContent(grid);

		Optional<Pair<String, Boolean>> result = dialog1.showAndWait();
	}
}
