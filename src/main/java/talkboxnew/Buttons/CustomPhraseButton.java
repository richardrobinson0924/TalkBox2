package talkboxnew.Buttons;

import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import simplenlg.features.Feature;
import simplenlg.features.InterrogativeType;
import simplenlg.features.Tense;
import talkboxnew.Utils;

import static talkboxnew.SimulatorStage.phraseSpec;
import static talkboxnew.SimulatorStage.realiser;

public class CustomPhraseButton extends Button {
	private final String str;
	private boolean shouldNegate = true, shouldAsk = true;

	private final static int BUTTON_SIZE = 100;

	public CustomPhraseButton(int page, String str, StringProperty phrase) {
		super(str);
		this.str = str;

		this.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
		this.setOnAction(event -> onAction(page, phrase));
	}

	private void onAction(int page, StringProperty phrase) {
		switch (page) {
			case 0:
				phraseSpec.setVerb(str);
				break;

			case 1:
				phraseSpec.setSubject(str);
				break;

			case 2:
				phraseSpec.setObject(str);
				break;

			case 3:
				setPhraseProperties(str);
				break;

			default:
				Utils.release(new Exception("Could not generate custom phrase."));
		}

		phrase.setValue(realiser.realiseSentence(phraseSpec));
	}

	private void setPhraseProperties(String str) {
		if (str.equals("Negate")) {
			phraseSpec.setFeature(Feature.NEGATED, shouldNegate);
			shouldNegate = !shouldNegate;

		} else if (str.equals("Question?")) {
			if (shouldAsk) phraseSpec.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.YES_NO);
			else phraseSpec.removeFeature(Feature.INTERROGATIVE_TYPE);
			shouldAsk = !shouldAsk;

		} else {
			phraseSpec.setFeature(Feature.TENSE, Tense.valueOf(str.toUpperCase()));

		}
	}
}
