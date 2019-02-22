package talkbox;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.common.collect.Lists;
import com.sun.istack.internal.NotNull;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.EnumSet;
import java.util.Optional;


/**
 * A singleton class for a custom dialog wizard window to create an audio file of user-inputted text. A user types their desired text into the text field, and the gender of the voice they wish to use. The user can see what the audio file will sound like by clicking the "Play" button. If they are satisfied, the "OK" button saves the <code>*.wav</code> to a specified destination.
 *
 * @author Richard Robinson
 * @apiNote This class is fully independent, and can be launched from any JavaFX stage
 */
public class TTSWizard {
	public static String text;

	private TTSWizard() {
	}

	/**
	 * The only public TTS Wizard Dialog launcher and configurator method. If any exceptions occur, the Dialog window is
	 * closed and view returns to <code>primaryStage</code>
	 *
	 * @param primaryStage the stage from which the TTSWizard instance is launched from
	 */
	public static AudioInputStream launch(Stage primaryStage) {
		Try.setFailSafe(TalkBoxApp::setFailSafe);

		try {
			final URL url = new URL("https://cloud.google.com/");
			final URLConnection connection = url.openConnection();
			connection.connect();
		} catch (Exception e) {
			TalkBoxApp.setFailSafe(e);
			return null;
		}

		Clip[] clip = new Clip[]{null};

		final Dialog<ButtonType> dialog1 = new Dialog<>();
		dialog1.setTitle("Text to Speech");
		dialog1.setHeaderText("Set Button Audio");

		/* Use custom dialog graphic */
		final ImageView imageView = new ImageView(TTSWizard.class.getResource("/Resources/magic-wand-2.png").toString());
		imageView.setFitHeight(40);
		imageView.setPreserveRatio(true);
		dialog1.setGraphic(imageView);

		dialog1.getDialogPane()
				.getButtonTypes()
				.addAll(ButtonType.OK, ButtonType.CANCEL);

		final GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		final TextField phrase = new TextField();
		phrase.setPromptText("Hello");

		final ObservableList<Voice> options = FXCollections.observableArrayList(EnumSet.allOf(Voice.class));

		final ComboBox<Voice> comboBox = new ComboBox<>(options);
		comboBox.setValue(EnumSet.allOf(Voice.class).iterator().next());

		final Button b = new Button("Play");
		b.setDisable(true);
		b.setOnAction(event1 -> {
			if (clip[0] != null && clip[0].isRunning()) {
				clip[0].stop();
			} else Try.newBuilder().setDefault(() -> {
				final AudioInputStream audio = generateAudio(phrase.getText(), comboBox.getValue());
				clip[0] = AudioSystem.getClip();

				clip[0].open(audio);
				clip[0].start();
			}).setOtherwise(dialog1::close).run();
		});

		final HBox box2 = new HBox(phrase, b);
		box2.setSpacing(10);

		grid.add(new Label("Phrase:"), 0, 0);
		grid.add(box2, 1, 0);
		grid.add(new Label("Voice:"), 0, 1);
		grid.add(comboBox, 1, 1);

		final Node authorize = dialog1.getDialogPane().lookupButton(ButtonType.OK);
		authorize.setDisable(true);

		phrase.textProperty().addListener((observable, oldVal, newVal) -> {
			boolean condition = newVal.isEmpty();
			authorize.setDisable(condition);
			b.setDisable(condition);
		});

		Platform.runLater(phrase::requestFocus);

		dialog1.getDialogPane().setContent(grid);

		final Optional<ButtonType> result = dialog1.showAndWait();
		AudioInputStream stream;

		if (!result.isPresent() || result.get() == ButtonType.CANCEL) {
			dialog1.close();
		} else if (result.get() == ButtonType.OK) {
			try {
				text = phrase.getText();
				return generateAudio(phrase.getText(), comboBox.getValue());
			} catch (Exception e) {
				TalkBoxApp.setFailSafe(e);
			}
		}

		return null;
	}

	/**
	 * DO NOT MODIFY. Independent method to generate an AudioInputStream of <code>text</code>, with the speech variant
	 * indicated by <code>Voices</code>, with acceptable values of 'A' through 'F', inclusive.
	 *
	 * @param text the text to be converted to audio
	 * @param v    the voice to be used
	 * @return an AudioInputStream of the TTS translation of <code>text</code>. Intended to be used to output to a file
	 * or use with <code>Clip</code> class to play directly.
	 * @throws Exception if any exception occurs
	 */
	static AudioInputStream generateAudio(@NotNull String text, Voice v) throws Exception {
		final GoogleCredentials credentials = GoogleCredentials
				.fromStream(TTSWizard.class.getResourceAsStream("/Resources/TalkBox-0d25e5d8c6d7.json"))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		final TextToSpeechSettings auth = TextToSpeechSettings.newBuilder()
				.setCredentialsProvider(() -> credentials)
				.build();

		AudioInputStream stream;

		try (final TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(auth)) {
			final SynthesisInput input = SynthesisInput.newBuilder()
					.setText(text)
					.build();

			final VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
					.setLanguageCode("en-US")
					.setName("en-US-Wavenet-" + v.variant)
					.build();

			final AudioConfig audioConfig = AudioConfig.newBuilder()
					.setAudioEncoding(AudioEncoding.LINEAR16)
					.build();

			final SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice,
					audioConfig);

			final ByteArrayInputStream bin = new ByteArrayInputStream(response.getAudioContent().toByteArray());

			stream = AudioSystem.getAudioInputStream(bin);
			textToSpeechClient.shutdown();
		}

		return stream;
	}

	public enum Voice {
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
