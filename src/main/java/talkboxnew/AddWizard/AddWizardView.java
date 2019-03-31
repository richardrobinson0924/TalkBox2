package talkboxnew.AddWizard;

import com.sun.media.sound.WaveFileWriter;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.stage.Window;
import org.apache.log4j.Logger;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import talkboxnew.Commands.ChangeCommand;
import talkboxnew.Commands.History;
import talkboxnew.ConfigStage;
import talkboxnew.Entry;
import talkboxnew.Utils;

import javax.sound.sampled.AudioFileFormat;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.Optional;
import java.util.stream.IntStream;

import static talkboxnew.Utils.tryFactory;

public class AddWizardView extends Wizard {
	private final int index;
	public final static SimpleIntegerProperty next = new SimpleIntegerProperty(1);
	private static final Logger log = Logger.getLogger(AddWizardView.class.getName());

	static final double SPACING = 10, WIDTH = 400, HEIGHT = 210;
	static final Insets PADDING = new Insets(SPACING, 20, 20, SPACING);

	public AddWizardView(int index, Window owner) {
		super(owner);
		this.index = index;
		final Entry oldEntry = ConfigStage.data.get(index);

		final WizardPane[] panes = new WizardPane[]{
				new IntroPane(),
				new TTSPane(oldEntry),
				new FilePane(),
				new RecordingPane(),
				new NamePane(oldEntry),
				new ImagePane(oldEntry, this)
		};

		final Wizard.Flow branchFlow = new Wizard.Flow() {
			@Override
			public Optional<WizardPane> advance(WizardPane wizardPane) {
				log.info("Advancing to pane " + next.getValue());
				return Optional.of(getNext(wizardPane));
			}

			@Override
			public boolean canAdvance(WizardPane wizardPane) {
				return IntStream.range(0, 5).anyMatch(i1 -> wizardPane == panes[i1]);
			}

			private WizardPane getNext(WizardPane currentPage) {
				return currentPage == null ? panes[0] : panes[next.getValue()];
			}
		};

		this.setFlow(branchFlow);
	}

	public void doOnFinish() {
		final ObservableMap<String, Object> map = getSettings();
		Entry.Builder eb = null;

		if ((boolean) map.get("TTSButton")) try {
			final WaveFileWriter w = new WaveFileWriter();
			final File file = Files.createTempFile(null, null).toFile();

			w.write(TTSPane.getAudio(
					(String) map.get("TTSPhrase"),
					(TTSPane.Voice) map.get("TTSVoice")
			), AudioFileFormat.Type.WAVE, file);

			eb = Entry.Builder.of(file);
		} catch (Exception e) {
			Utils.release(e);
		}
		else if ((boolean) map.get("WAVButton")) {
			eb = Entry.Builder.of(new File((String) map.get("filename")));
		} else {
			eb = Entry.Builder.of((File) map.get("recordFile"));
		}

		if (eb == null) {
			Utils.release(new Exception("Could not add audio to button"));
			return;
		}

		eb.withName((String) map.get("AudioName"));
		eb.withImage((File) map.get("image"));

		final Entry finalEb = eb.build();
		tryFactory.attemptTo(() -> History.getInstance().execute(new ChangeCommand(index, finalEb)));
	}
}
