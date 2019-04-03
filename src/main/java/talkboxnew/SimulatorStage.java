package talkboxnew;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
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
	private SimpleIntegerProperty page = new SimpleIntegerProperty(0);
	private int size;

	public static SPhraseSpec phraseSpec;
	public static Realiser realiser;

	public static final String[] nodesId = {"CUSTOM_LABEL", "PLAY_BUTTON"};

	public SimulatorStage(Path masterPath, Window owner) {
		this.initOwner(owner);
		this.initModality(Modality.APPLICATION_MODAL);
		this.setTitle("TalkBox Simulator – " + masterPath.getFileName().toString());

		this.setHeight(owner.getHeight() - 20);

		final Lexicon lexicon = Lexicon.getDefaultLexicon();
		final NLGFactory factory = new NLGFactory(lexicon);

		phraseSpec = new SPhraseSpec(factory);
		realiser = new Realiser(lexicon);

		tryFactory.attemptTo(() -> ts = ConfigStage.readConfig());
		this.setWidth(ts.numberOfAudioButtons * 120 + 200);

		final HBox box = new HBox();
		box.setPadding(new Insets(0,10,0,10));
		box.setAlignment(Pos.CENTER);

		this.customToggle = new ToggleButton("Custom");

		box.getChildren().addAll(getFlow(), getSwap());
		if (ts.hasCustomButton) box.getChildren().add(0, customBar());

		this.setScene(new Scene(box));
		this.setOnCloseRequest(e -> {
			log.info("Closing simulator...");
			saveConfig();

			ConfigStage.data.setAll(ts.database);
			ConfigStage.save.fire();
			this.close();
		});
	}

	private VBox getSwap() {
		final VBox swapBox = new VBox(SPACING * 2);
		swapBox.setAlignment(Pos.CENTER);

		final Button next = new Button("Next");
		next.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
		next.setOnAction(e -> page.set((page.getValue() + 1) % size));

		if (ts.hasBackButton) {
			final Button back = new Button("Previous");
			next.setPrefSize(BUTTON_SIZE, BUTTON_SIZE / 2 - 10);
			back.setPrefSize(BUTTON_SIZE, BUTTON_SIZE / 2 - 10);

			back.setOnAction(e -> page.set((page.getValue() + size - 1) % size));
			swapBox.getChildren().addAll(back);
		}

		swapBox.getChildren().addAll(next);
		swapBox.setMinWidth(BUTTON_SIZE);

		return swapBox;
	}

	private VBox customBar() {
		final VBox custom = new VBox(20);
		custom.setAlignment(Pos.CENTER);
		this.customToggle.setPrefSize(BUTTON_SIZE, 40);

		final Button customPlay = new Button("Play");
		customPlay.setId(nodesId[1]);
		customPlay.disableProperty()
				.bind(customText
				.isEmpty()
				.or(customToggle.selectedProperty().not())
		);

		customPlay.setPrefSize(BUTTON_SIZE, 40);

		customPlay.setOnAction(event -> tryFactory.attemptTo(() -> {
			log.info("Playing custom audio...");
			final AudioInputStream phrase = TTSPane.getAudio(
					customText.getValueSafe(),
					TTSPane.Voice.FEMALE1
			);

			Utils.play(phrase);
		}));

		custom.getChildren().addAll(customToggle, customPlay);
		custom.setMinWidth(BUTTON_SIZE);

		return custom;
	}

	private void doIfCustom(int page, FlowPane flowPane) {
		this.size = 4;
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
		this.size = ts.numberOfAudioSets;
		flowPane.getChildren().clear();

		for (int i = 0; i < ts.numberOfAudioButtons; i++)
			flowPane.getChildren().add(new SimButton(ts.database.get(page * ts.numberOfAudioButtons + i)));
	}

	private FlowPane getFlow() {
		final FlowPane flowPane = new FlowPane();
		this.size = ts.numberOfAudioSets;

		flowPane.setVgap(SPACING);
		flowPane.setHgap(SPACING);
		flowPane.setAlignment(Pos.CENTER);

		changed(page.getValue(), flowPane);
		page.addListener((o, old, newValue) -> changed((Integer) newValue, flowPane));

		flowPane.setPrefWidth(ts.numberOfAudioButtons * 120);
		return flowPane;
	}

	private void changed(int newValue, FlowPane flowPane) {
		flowPane.getChildren().clear();

		customToggle.setOnAction(event -> {
			log.info("Custom toggle toggled");
			page.setValue(0);

			if (customToggle.isSelected()) {
				flowPane.getChildren().clear();
				doIfCustom(0, flowPane);
			} else {
				doIfNormal(0, flowPane);
				customText.set("");
			}
		});

		if (customToggle.isSelected()) {
			doIfCustom(newValue, flowPane);
		} else doIfNormal(newValue, flowPane);
	}

	private void saveConfig() {
		try (final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getTBC()))) {
			oos.writeObject(ts);
		} catch (IOException e) {
			release(e);
		}
	}

}
