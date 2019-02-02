package talkbox;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.common.collect.Lists;
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
import java.util.EnumSet;
import java.util.Optional;


/**
 * A singleton class for a custom dialog wizard window to create an audio file of user-inputted text. A user types their desired text into the text field, and the gender of the voice they wish to use. The user can see what the audio file will sound like by clicking the "Play" button. If they are satisfied, the "OK" button saves the <code>*.wav</code> to a specified destination.
 *
 * @author Richard Robinson
 * @apiNote This class is fully independent, and can be launched from any JavaFX stage
 */
class TTSWizard {

	private TTSWizard() {
	}

	/**
	 * The only public TTS Wizard Dialog launcher and configurator method. If any exceptions occur, the Dialog window is closed and view returns to <code>primaryStage</code>
	 *
	 * @param primaryStage the stage from which the TTSWizard instance is launched from
	 */
	static void launch(Stage primaryStage) {
		Clip[] clip = new Clip[]{null};

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

		ObservableList<Voice> options = FXCollections.observableArrayList(EnumSet.allOf(Voice.class));

		final ComboBox<Voice> comboBox = new ComboBox<>(options);
		comboBox.setValue(EnumSet.allOf(Voice.class).iterator().next());

		Button b = new Button("Play");
		b.setDisable(true);
		b.setOnAction(event1 -> {
			if (clip[0] != null && clip[0].isRunning()) {
				System.out.println("hi");
				clip[0].stop();
			} else try {
				AudioInputStream audio = generateAudio(phrase.getText(), comboBox.getValue());
				clip[0] = AudioSystem.getClip();

				clip[0].open(audio);
				clip[0].start();
			} catch (Exception e) {
				System.out.println(e.getMessage());
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
				AudioInputStream audio = generateAudio(phrase.getText(), comboBox.getValue());

				WaveFileWriter writer = new WaveFileWriter();
				FileChooser fileChooser = new FileChooser();
				fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("WAV file (*.wav)", "*.wav"));

				fileChooser.setTitle("Save Audio File"); // specifies file prompt
				File audioFile = fileChooser.showSaveDialog(primaryStage); // displays file chooser window
				audioFile = new File(audioFile.getAbsolutePath() + ".wav");

				writer.write(audio, AudioFileFormat.Type.WAVE, audioFile);
			} catch (Exception e) {
				dialog1.close();
			}
		}
	}

	/**
	 * DO NOT MODIFY. Independent method to generate an AudioInputStream of <code>text</code>, with the speech variant indicated by <code>Voices</code>, with acceptable values of 'A' through 'F', inclusive.
	 *
	 * @param text    the text to be converted to audio
	 * @param v       the voice to be used
	 * @return an AudioInputStream of the TTS translation of <code>text</code>. Intended to be used to output to a file or use with <code>Clip</code> class to play directly.
	 * @throws Exception if any exception occurs
	 */
	private static AudioInputStream generateAudio(String text, Voice v) throws Exception {
		GoogleCredentials credentials = GoogleCredentials
				.fromStream(TTSWizard.class.getResourceAsStream("/TalkBox-0d25e5d8c6d7.json"))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		TextToSpeechSettings auth = TextToSpeechSettings.newBuilder()
				.setCredentialsProvider(() -> credentials)
				.build();

		TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(auth);

		// Set the text input to be synthesized
		SynthesisInput input = SynthesisInput.newBuilder()
				.setText(text)
				.build();

		// Build the voice request, select the language code ("en-US") and the ssml voice gender
		// ("neutral")
		VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
				.setLanguageCode("en-US")
				.setName("en-US-Wavenet-" + v.variant)
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

	private enum Voice {
		MALE1("Male 1", 'A'),
		MALE2("Male 2", 'B'),
		MALE3("Male 3", 'D'),
		FEMALE1("Female 1", 'C'),
		FEMALE2("Female 2", 'E'),
		FEMALE3("Female 3", 'F');

		private final char variant;
		private final String name;

		Voice(String name, char variant) {
			this.variant = variant;
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
