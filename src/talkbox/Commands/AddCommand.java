package talkbox.Commands;

import javafx.scene.image.ImageView;
import talkbox.AudioPair;
import talkbox.TTSWizard;
import talkbox.TalkBoxApp;
import talkbox.Try;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static talkbox.Commands.History.appInstance;

/**
 * A command to associate an audio file and alias with a button and with the TBC {@code audioList}.
 * <p></p>
 * If an external file is added, the button is associated to a copy of the file in the default audio folder
 * directory, and the alias is associated with the file's name.
 * <p></p>
 * If a TTSWizard file is added, the button is associated with its file and the phrase the audio was generated from.
 */
public final class AddCommand implements History.Command {
	private final int i;
	private final int j;
	private final File file;
	private final boolean isNull;
	private final RemoveCommand r;
	private final Type type;
	private final AudioPair oldPair;
	private final String text;

	public AddCommand(int i, int j, File f, Type type) {
		this(i, j, f, type, f.getName().replace(".wav", ""));
	}

	public AddCommand(int i, int j, File f, Type type, String text) {
		this.i = i;
		this.j = j;
		this.file = f;
		this.isNull = appInstance.data.get(i).get(j).isNull();
		this.r = new RemoveCommand(i, j);
		this.type = type;
		this.oldPair = appInstance.data.get(i).get(j);
		this.text = text;
	}

	public enum Type {
		FILE, TTS
	}

	@Override
	public void execute() {
		if (!oldPair.isNull()) r.execute();

		Try.newBuilder().setDefault(() -> {
			if (type.equals(Type.FILE)) {
				Path p = Files.copy(file.toPath(),
						Paths.get(TalkBoxApp.getFullPath(file.getName())),
						StandardCopyOption.REPLACE_EXISTING);

				appInstance.data.get(i).get(j).set(p.toFile(), text);
			} else {
				appInstance.data.get(i).get(j).set(file, TTSWizard.text);
			}
		}).run();

		appInstance.makeContextMenu(i, j);
		appInstance.setGraphic(j);
	}

	@Override
	public void undo() {
		if (isNull) {
			appInstance.data.get(i).get(j).set(null, null);

			final ImageView blank = new ImageView();
			blank.setImage(null);
			appInstance.buttons[j].setGraphic(blank);
		}
		else r.undo();
	}
}
