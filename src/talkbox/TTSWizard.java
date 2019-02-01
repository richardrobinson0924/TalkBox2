package talkbox;

import com.google.cloud.texttospeech.v1.*;
import com.sun.media.sound.WaveFileWriter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Optional;


/**
 * A singleton class for a custom dialog wizard window to create an audio file of user-inputted text. A user types their desired text into the text field, and the gender of the voice they wish to use. The user can see what the audio file will sound like by clicking the "Play" button. If they are satisfied, the "OK" button saves the <code>*.wav</code> to a specified destination.
 * <p>
 * The dialog uses a custom GridPane with the following hierarchy:
 * <code>
 * |- Label (Phrase)
 * |- HBox
 * |- TextField
 * |- Button (Play)
 * |- Label (Gender)
 * |- HBox
 * |- Radio Button (Male)
 * |- Radio Button (Female)
 * </code>
 * <p>
 * The phrase a user enters must only consist of alphanumeric characters, and without leading whitespaces.
 *
 * @author Richard Robinson
 * @apiNote This class is fully independent, and can be launched from any JavaFX stage
 */
final class TTSWizard {
	private static final char[] VOICES = {'A', 'B', 'C', 'D', 'E', 'F'};

	private TTSWizard() {
	}

	static synchronized void launch(Stage primaryStage) throws Exception {
		Dialog<ButtonType> dialog1 = new Dialog<>();
		dialog1.setTitle("Text to Speech Wizard");
		dialog1.setHeaderText("TTS Wizard");

		/* Use custom dialog graphic */
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

		ObservableList<String> options = FXCollections.observableArrayList("Male 1", "Male 2", "Female 1", "Male 3", "Female 2", "Female 3");

		final ComboBox<String> comboBox = new ComboBox<>(options);
		comboBox.setValue(options.get(0));

		Button b = new Button("Play");
		b.setDisable(true);
		b.setOnAction(event1 -> {
			try {
				char variant = VOICES[options.indexOf(comboBox.getValue())];
				AudioInputStream audio = generateAudio(phrase.getText(), variant);

				Clip clip = AudioSystem.getClip();
				clip.open(audio);
				clip.start();
			} catch (Exception e) {
				dialog1.close();
			}
		});

		HBox box2 = new HBox(phrase, b);
		box2.setSpacing(10);

		grid.add(new Label("Phrase:"), 0, 0);
		grid.add(box2, 1, 0);
		grid.add(new Label("Voice:"), 0, 1);
		grid.add(comboBox, 1, 1);

		Node authorize = dialog1.getDialogPane().lookupButton(ButtonType.OK);
		authorize.setDisable(true);

		phrase.textProperty().addListener((observable, oldVal, newVal) -> {
			boolean condition = newVal.isEmpty();
			authorize.setDisable(condition);
			b.setDisable(condition);
		});

		Platform.runLater(phrase::requestFocus);

		dialog1.getDialogPane().setContent(grid);

		Optional<ButtonType> result = dialog1.showAndWait();

		if (!result.isPresent() || result.get() == ButtonType.CANCEL) {
			dialog1.close();
		} else if (result.get() == ButtonType.OK) {
			try {
				char variant = VOICES[options.indexOf(comboBox.getValue())];
				AudioInputStream audio = generateAudio(phrase.getText(), variant);

				WaveFileWriter writer = new WaveFileWriter();
				FileChooser fileChooser = new FileChooser();

				fileChooser.setTitle("Save Audio File"); // specifies file prompt
				File audioFile = fileChooser.showSaveDialog(primaryStage); // displays file chooser window

				writer.write(audio, AudioFileFormat.Type.WAVE, audioFile);
			} catch (Exception e) {
				dialog1.close();
			}
		}
	}


	public static AudioInputStream generateAudio(String text, char variant) throws Exception {
		TextToSpeechClient textToSpeechClient = TextToSpeechClient.create();

		System.out.println(textToSpeechClient.listVoices("en-*"));
		// Set the text input to be synthesized
		SynthesisInput input = SynthesisInput.newBuilder()
				.setText(text)
				.build();

		// Build the voice request, select the language code ("en-US") and the ssml voice gender
		// ("neutral")
		VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
				.setLanguageCode("en-US")
				.setName("en-US-Wavenet-" + variant)
				.build();

		// Select the type of audio file you want returned
		AudioConfig audioConfig = AudioConfig.newBuilder()
				.setAudioEncoding(AudioEncoding.LINEAR16)
				.build();

		// Perform the text-to-speech request on the text input with the selected voice parameters and
		// audio file type
		SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice,
				audioConfig);

		ByteArrayInputStream bin = new ByteArrayInputStream(response.getAudioContent().toByteArray());
		return AudioSystem.getAudioInputStream(bin);
	}
}
