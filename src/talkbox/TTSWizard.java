package talkbox;

import com.sun.media.sound.WaveFileWriter;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import marytts.LocalMaryInterface;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.util.Optional;

class TTSWizard {

	private TTSWizard() {
	}

	static void launch(Stage primaryStage) {
		Dialog<ButtonType> dialog1 = new Dialog<>();
		dialog1.setTitle("Text to Speech Wizard");
		dialog1.setHeaderText("TTS Wizard");

		ImageView imageView = new ImageView(TTSWizard.class.getResource("magic-wand-2.png").toString());
		imageView.setFitHeight(50);
		imageView.setPreserveRatio(true);

		dialog1.setGraphic(imageView);

		dialog1.getDialogPane()
				.getButtonTypes()
				.addAll(ButtonType.OK, ButtonType.CANCEL);

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

		Button b = new Button("Play");
		b.setOnAction(event1 -> {
			AudioInputStream sound = null;
			try {
				LocalMaryInterface tts = new LocalMaryInterface();
				if (female.isSelected()) tts.setVoice("dfki-poppy-hsmm");
				sound = tts.generateAudio(phrase.getText());

				Clip clip = AudioSystem.getClip();
				clip.open(sound);
				clip.start();
			} catch (Exception e) {
				Platform.exit();
			}
		});

		HBox box = new HBox(male, female);
		box.setSpacing(20);
		HBox box2 = new HBox(phrase, b);
		box2.setSpacing(10);

		grid.add(new Label("Phrase:"), 0, 0);
		grid.add(box2, 1, 0);
		grid.add(new Label("Gender:"), 0, 1);
		grid.add(box, 1, 1);

		Node authorize = dialog1.getDialogPane().lookupButton(ButtonType.OK);
		authorize.setDisable(true);

		phrase.textProperty().addListener((observable, oldVal, newVal) -> authorize.setDisable(newVal.isEmpty()));

		Platform.runLater(phrase::requestFocus);

		dialog1.getDialogPane().setContent(grid);

		Optional<ButtonType> result = dialog1.showAndWait();

		if (!result.isPresent() || result.get() == ButtonType.CANCEL) {
			dialog1.close();
		} else if (result.get() == ButtonType.OK) {
			AudioInputStream sound;
			try {
				LocalMaryInterface tts = new LocalMaryInterface();
				if (female.isSelected()) tts.setVoice("dfki-poppy-hsmm");
				sound = tts.generateAudio(phrase.getText());

				WaveFileWriter writer = new WaveFileWriter();
				FileChooser fileChooser = new FileChooser();

				fileChooser.setTitle("Save Audio File"); // specifies file prompt
				File audioFile = fileChooser.showSaveDialog(primaryStage); // displays file chooser window

				try {
					writer.write(sound, AudioFileFormat.Type.WAVE, audioFile);
				} catch (Exception e) {
					Platform.exit();
				}
			} catch (Exception e) {
				Platform.exit();
			}
		}
	}
}
