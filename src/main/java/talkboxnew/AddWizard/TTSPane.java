package talkboxnew.AddWizard;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.common.collect.Lists;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextFlow;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import org.apache.log4j.Logger;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import talkboxnew.Entry;
import talkboxnew.Utils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.EnumSet;
import java.util.Objects;

import static talkboxnew.AddWizard.AddWizardView.*;
import static talkboxnew.Utils.tryFactory;

public class TTSPane extends WizardPane {
	private final TextField phrase;
	private ComboBox<Voice> comboBox;
	private static SimpleBooleanProperty isReady = new SimpleBooleanProperty();

	private static final Logger log = Logger.getLogger(TTSPane.class.getName());
	private static final String LANG = "en-US";

	public TTSPane(Entry oldEntry) {
		super();

		this.phrase = new TextField();
		phrase.setId("TTSPhrase");

		phrase.setPromptText(oldEntry == null
				? "Enter a phrase"
				: oldEntry.getName()
		);

		this.setHeaderText("Text to Speech");
		this.setContent(getView());
		this.setPrefSize(WIDTH, HEIGHT);
	}

	@Override
	public void onEnteringPage(Wizard wizard) {
		AddWizardView.next.setValue(4);
		wizard.invalidProperty().unbind();
		wizard.invalidProperty().bind(phrase.textProperty().isEmpty());
	}

	@Override
	public void onExitingPage(Wizard wizard) {
		wizard.invalidProperty().unbind();
		wizard.invalidProperty().setValue(false);
	}

	private GridPane getView() {
		final GridPane grid = new GridPane();

		grid.setHgap(SPACING);
		grid.setVgap(SPACING);
		grid.setPadding(PADDING);

		final Label text = new Label("Enter the text you would like the button to speak. Click the \nPlay button to preview what it sounds like.");
		text.setWrapText(true);

		grid.add(new TextFlow(text), 0, 0, 2, 1);
		grid.addRow(1, new Label("Phrase:"), getHBox());
		grid.addRow(2, new Label("Voice:"), comboBox);

		return grid;
	}

	private HBox getHBox() {
		final ObservableList<Voice> options = FXCollections.observableArrayList(EnumSet.allOf(Voice.class));

		comboBox = new ComboBox<>(options);

		comboBox.setId("TTSVoice");
		comboBox.setValue(comboBox.getItems().get(0));
		comboBox.setDisable(!isConnectedToInternet());

		final Button b = new Button("Play");
		b.disableProperty().bind(phrase.textProperty().isEmpty());

		b.setCursor(Cursor.HAND);

		b.setOnAction(e -> {
//			this.getScene().setCursor(Cursor.WAIT);
//
//			final Task<Void> task = new Task<Void>() {
//				@Override
//				protected Void call() {
//
//
//					return null;
//				}
//			};
//
//			task.setOnSucceeded(e1 -> this.getScene().setCursor(Cursor.DEFAULT));
//
//			final Thread thread = new Thread(task);
//			thread.start();

			tryFactory.attemptTo(() -> {
				final Clip clip = AudioSystem.getClip();
				clip.open(getAudio(phrase.getText(), comboBox.valueProperty().get()));
				clip.start();
			});
		});

		return new HBox(SPACING, phrase, b);
	}

	public static AudioInputStream getAudio(String phrase, Voice voice) throws Exception {
		return isConnectedToInternet()
				? getHiFi(phrase, voice)
				: getLoFi(phrase);
	}

	private static AudioInputStream getLoFi(String phrase) throws Exception {
		isReady.setValue(false);
		final MaryInterface marytts = new LocalMaryInterface();

		final AudioInputStream stream =  marytts.generateAudio(phrase);
		isReady.setValue(true);
		return stream;
	}

	private static AudioInputStream getHiFi(String phrase, Voice voice) throws Exception {
		isReady.setValue(false);

		final GoogleCredentials credentials = GoogleCredentials
			.fromStream(Objects.requireNonNull(TTSPane
					.class
					.getClassLoader()
					.getResourceAsStream("spatial-iris-230217-b424f67b42f9.json")))
			.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		final TextToSpeechSettings settings = TextToSpeechSettings
			.newBuilder()
			.setCredentialsProvider(() -> credentials)
			.build();

		try (final TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings)) {
			final SynthesisInput input = SynthesisInput.newBuilder()
					.setText(phrase)
					.build();

			final VoiceSelectionParams vsp = VoiceSelectionParams.newBuilder()
					.setLanguageCode(LANG)
					.setName(LANG + "-Wavenet-" + voice.variant)
					.build();

			final AudioConfig audioConfig = AudioConfig.newBuilder()
					.setAudioEncoding(AudioEncoding.LINEAR16)
					.build();

			final SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, vsp, audioConfig);

			final ByteArrayInputStream bin = new ByteArrayInputStream(response.getAudioContent().toByteArray());

			final AudioInputStream stream = AudioSystem.getAudioInputStream(bin);
			textToSpeechClient.shutdown();

			isReady.setValue(true);
			return stream;
		} catch (Exception e) {
			Utils.release(e);
			return null;
		}
	}

	private static boolean isConnectedToInternet() {
		boolean b = true;
		try {
			final URL url = new URL("https://cloud.google.com/");
			final URLConnection connection = url.openConnection();
			connection.connect();
		} catch (Exception e) {
			log.info("Not connected to internet. Using default TTS...");
			b = false;
		}

		return b;
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
