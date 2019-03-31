package talkboxnew.AddWizard;

import com.darkprograms.speech.microphone.Microphone;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import talkboxnew.Utils;

import javax.sound.sampled.*;
import java.io.File;
import java.nio.file.Files;

import static talkboxnew.AddWizard.AddWizardView.*;
import static talkboxnew.Utils.tryFactory;

public class RecordingPane extends WizardPane {
	private final SimpleBooleanProperty isInvalid;
	private File file;
	private ComboBox<Pair<String, Line.Info>> mixerBox;

	public static final String[] nodesId = {"RECORD_TOGGLE","PLAY_BUTTON"};

	public RecordingPane() {
		super();

		this.mixerBox = getLineComboBox();
		this.isInvalid = new SimpleBooleanProperty(true);
		Utils.tryFactory.attemptTo(() -> this.file = Files.createTempFile(null, null).toFile());

		this.setHeaderText("Select a File");
		this.setContent(makeView());
		this.setPrefSize(WIDTH, HEIGHT);
	}

	private VBox makeView() {
		final Label label = new Label("Click Record to record a custom phrase. Click again to stop. To preview the audio, click the Play button.");
		label.setWrapText(true);

		final Button play = new Button("Play", new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT));
		play.setId(nodesId[1]);

		final ToggleButton tb = new ToggleButton("Record", new Glyph("FontAwesome", FontAwesome.Glyph.CIRCLE));
		tb.setId(nodesId[0]);

		final HBox hbox = new HBox(SPACING, tb, play);
		hbox.setAlignment(Pos.CENTER);
		hbox.setPadding(PADDING);

		final Label l = new Label("Microphone:");

		mixerBox.disableProperty().bind(tb.selectedProperty());

		final Microphone mic = new Microphone(AudioFileFormat.Type.WAVE);
		play.setDisable(false);

		play.setOnAction(e -> {
			if (file == null) return;

			final Media media = new Media(file.toURI().toString());
			final MediaPlayer player = new MediaPlayer(media);
			player.play();
		});

		tb.setOnAction(e -> onAction(tb, mic, play));

		final HBox hbox2 = new HBox(SPACING, l, mixerBox);
		hbox2.setAlignment(Pos.CENTER_LEFT);

		final VBox box = new VBox(SPACING, label, hbox, hbox2);
		box.setPadding(PADDING);

		return box;
	}

	private ComboBox<Pair<String, Line.Info>> getLineComboBox() {
		final ObservableList<Pair<String, Line.Info>> list = FXCollections.observableArrayList();

		for (final Mixer.Info info : AudioSystem.getMixerInfo()) {
			final Mixer m = AudioSystem.getMixer(info);
			final Line.Info[] lineInfos = m.getTargetLineInfo();

			if (lineInfos.length > 0 && lineInfos[0].getLineClass().equals(TargetDataLine.class))
				list.add(new Pair<>(info.getName(), lineInfos[0]));
		}

		final ComboBox<Pair<String, Line.Info>> cb = new ComboBox<>(list);
		cb.setCellFactory(param -> new ListCell<Pair<String, Line.Info>>() {
			@Override
			protected void updateItem(Pair<String, Line.Info> item, boolean empty) {
				super.updateItem(item, empty);
				setText(item != null ? item.getKey().split(",")[0] : null);
			}
		});

		cb.setConverter(new StringConverter<Pair<String, Line.Info>>() {
			@Override
			public String toString(Pair<String, Line.Info> item) {
				return item != null ? item.getKey().split(",")[0] : null;
			}

			@Override
			public Pair<String, Line.Info> fromString(String string) {
				return cb.getItems()
						.stream()
						.filter(i -> i.getKey().equals(string))
						.findFirst()
						.orElse(null);
			}
		});

		cb.setValue(list.get(0));

		return cb;
	}

	private void onAction(ToggleButton tb, Microphone mic, Button play) {
		if (tb.isSelected()) tryFactory.attemptTo(() -> {
			tb.setText("Stop");
			tb.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.STOP));
			play.setDisable(true);
			final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(mixerBox.getSelectionModel().getSelectedItem().getValue());

			mic.setTargetDataLine(line);
			mic.open();
			mic.captureAudioToFile(file);
		});

		else {
			isInvalid.setValue(false);
			tb.setText("Record");
			tb.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.CIRCLE));
			play.setDisable(false);
			mic.close();
		}
	}

	@Override
	public void onEnteringPage(Wizard wizard) {
		AddWizardView.next.setValue(4);
		wizard.invalidProperty().unbind();
		wizard.invalidProperty().bind(isInvalid);
	}

	@Override
	public void onExitingPage(Wizard wizard) {
		wizard.invalidProperty().unbind();
		wizard.invalidProperty().setValue(false);
		wizard.getSettings().put("recordFile", file);
	}
}
