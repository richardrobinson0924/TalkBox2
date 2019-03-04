package talkbox.Commands;

import talkbox.AudioPair;
import talkbox.TTSWizard;
import talkbox.TalkBoxApp;
import talkbox.Try;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
		this.isNull = TalkBoxApp.data.get(i).get(j).isNull().get();
		this.r = new RemoveCommand(i, j);
		this.type = type;
		this.oldPair = TalkBoxApp.data.get(i).get(j);
		this.text = text;
	}

	public enum Type {
		FILE, TTS
	}

	@Override
	public void execute() {
		if (!oldPair.isNull().get()) r.execute();

		Try.newBuilder().setDefault(() -> {
			if (type.equals(Type.FILE)) {
				Path p = Files.copy(file.toPath(),
						Paths.get(TalkBoxApp.getFullPath(file.getName())),
						StandardCopyOption.REPLACE_EXISTING);

				TalkBoxApp.data.get(i).get(j).set(p.toFile(), text);
			} else {
				TalkBoxApp.data.get(i).get(j).set(file, TTSWizard.text);
			}
		}).run();
	}

	@Override
	public void undo() {
		if (isNull) TalkBoxApp.data.get(i).get(j).set(null, null);
		else r.undo();
	}
}
