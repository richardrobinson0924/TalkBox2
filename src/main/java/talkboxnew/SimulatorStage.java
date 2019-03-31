package talkboxnew;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.log4j.Logger;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.realiser.english.Realiser;
import talkboxnew.AddWizard.TTSPane;
import talkboxnew.Buttons.CustomPhraseButton;
import talkboxnew.Buttons.SimButton;

import javax.sound.sampled.AudioInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;

import static talkboxnew.Utils.*;

public final class SimulatorStage extends Stage {
	private static final int SPACING = 10;
	private static final int BUTTON_SIZE = 100;

	private static final Logger log = Logger.getLogger(SimulatorStage.class.getName());

	private TalkBoxData ts;
	private SimpleStringProperty customText = new SimpleStringProperty();
	private ToggleButton customToggle;
	private Pagination pagination;

	public static SPhraseSpec phraseSpec;
	public static Realiser realiser;

	public SimulatorStage(Path masterPath, Window owner) {
		this.initOwner(owner);
		this.initModality(Modality.APPLICATION_MODAL);
		this.setTitle("TalkBox Simulator – " + masterPath.getFileName().toString());

		this.setHeight(owner.getHeight() - 20);
		this.setWidth(owner.getWidth());

		final Lexicon lexicon = Lexicon.getDefaultLexicon();
		final NLGFactory factory = new NLGFactory(lexicon);

		phraseSpec = new SPhraseSpec(factory);
		realiser = new Realiser(lexicon);

		tryFactory.attemptTo(() -> ts = ConfigStage.readConfig());

		pagination = new Pagination(ts.getNumberOfAudioSets());
		pagination.setPageFactory(this::pageFactory);

		final VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		box.setSpacing(SPACING * 2);
		box.getChildren().addAll(customBar(), pagination);

		this.setScene(new Scene(box));
		this.setOnCloseRequest(e -> {
			log.info("Closing simulator...");
			saveConfig();
			ConfigStage.data.setAll(ts.database);
			ConfigStage.save.fire();
			this.close();
		});
	}

	private HBox customBar() {
		final HBox custom = new HBox(20);
		custom.setAlignment(Pos.CENTER);
		customToggle = new ToggleButton("Custom Phrase");

		final Label customLabel = new Label();
		customLabel.textProperty().bind(customText);

		final Button customPlay = new Button("Play");
		customPlay.disableProperty().bind(customText
				.isEmpty()
				.or(customToggle.selectedProperty().not())
		);

		customPlay.setOnAction(event -> tryFactory.attemptTo(() -> {
			log.info("Playing custom audio...");
			final AudioInputStream phrase = TTSPane.getAudio(
					customText.getValueSafe(),
					TTSPane.Voice.FEMALE1
			);

			Utils.play(phrase);
		}));

		custom.getChildren().addAll(customToggle, customLabel, customPlay);
		return custom;
	}

	private void doIfCustom(int page, FlowPane flowPane) {
		pagination.setPageCount(4);

		for (int i = 0; i < ts.numberOfAudioButtons; i++)
			if (i >= ts.customWords.get(page).size()) {
				final Button b = new Button("Empty");
				b.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
				flowPane.getChildren().add(b);
			} else {
				final String word = ts.customWords.get(page).get(i);
				flowPane.getChildren().add(new CustomPhraseButton(page, word, customText));
			}
	}

	private void doIfNormal(int page, FlowPane flowPane) {
		pagination.setPageCount(ts.getNumberOfAudioSets());
		flowPane.getChildren().clear();

		for (int i = 0; i < ts.numberOfAudioButtons; i++)
			flowPane.getChildren().add(new SimButton(ts.database.get(page * ts.numberOfAudioButtons + i)));
	}

	private FlowPane pageFactory(int page) {
		final FlowPane flowPane = new FlowPane();

		flowPane.setVgap(SPACING);
		flowPane.setHgap(SPACING);
		flowPane.setAlignment(Pos.CENTER);

		if (customToggle.isSelected()) doIfCustom(page, flowPane);
		else doIfNormal(page, flowPane);

		customToggle.setOnAction(event -> {
			log.info("Custom toggle toggled");
			if (customToggle.isSelected()) {
				flowPane.getChildren().clear();
				doIfCustom(page, flowPane);
			} else {
				doIfNormal(page, flowPane);
				customText.set("");
			}
		});

		flowPane.minWidthProperty().bindBidirectional(this.minWidthProperty());
		return flowPane;
	}

	private void saveConfig() {
		try (final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getTBC()))) {
			oos.writeObject(ts);
		} catch (IOException e) {
			release(e);
		}
	}

}
