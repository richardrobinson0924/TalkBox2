package talkboxnew.AddWizard;

import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextFlow;
import org.apache.log4j.Logger;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

import java.util.Arrays;
import java.util.List;

import static talkboxnew.AddWizard.AddWizardView.*;

public class IntroPane extends WizardPane {
	private ToggleGroup tg;
	private List<RadioButton> radioList;
	private RadioButton[] radioButtons;

	private static final Logger log = Logger.getLogger(IntroPane.class.getName());

	IntroPane() {
		super();

		this.tg = new ToggleGroup();
		this.radioList = getList();

		tg.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			log.info("Button " + newValue.toString() + " selected");
			next.setValue(1 + radioList.indexOf((RadioButton) newValue));
		});

		this.setHeaderText("Select Add Method");
		this.setContent(getView());
		this.setPrefSize(WIDTH, HEIGHT);
	}

	@Override
	public void onEnteringPage(Wizard wizard) {
		wizard.invalidProperty().unbind();
		wizard.setInvalid(false);
		next.setValue(1 + radioList.indexOf((RadioButton) tg.getSelectedToggle()));
	}

	private List<RadioButton> getList() {
		final RadioButton TTS = new RadioButton("TTS");
		TTS.setId("TTSButton");

		final RadioButton wavFile = new RadioButton("WAV File");
		wavFile.setId("WAVButton");

		final RadioButton recording = new RadioButton("Record Audio");
		recording.setId("RecordingButton");

		radioButtons = new RadioButton[]{TTS, wavFile, recording};
		final List<RadioButton> list = Arrays.asList(radioButtons);

		Arrays.stream(radioButtons).forEach(b -> b.setToggleGroup(tg));
		tg.selectToggle(radioButtons[0]);

		return list;
	}

	public GridPane getView() {
		final HBox hbox = new HBox(20, radioButtons);
		final GridPane gridPane = new GridPane();

		gridPane.setVgap(SPACING);
		gridPane.setHgap(SPACING);
		gridPane.setPadding(PADDING);

		final Label text = new Label("How would you like to add Audio?\nSelect TTS for automatic natural speech generation.");
		text.setWrapText(true);

		gridPane.add(new TextFlow(text), 0, 0, 2, 1);
		gridPane.addRow(1, hbox);

		return gridPane;
	}
}