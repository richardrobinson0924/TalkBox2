package talkbox.Commands;

import javafx.scene.image.ImageView;
import talkbox.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static talkbox.Commands.History.appInstance;

public final class RemoveCommand implements History.Command {
	private final int i;
	private final int j;
	private AudioPair oldPair;
	private Path oldPath;
	private final String oldText;

	public RemoveCommand(int i, int j) {
		this.i = i;
		this.j = j;
		this.oldPair = appInstance.data.get(i).get(j);
		this.oldText = oldPair.getValue();
	}

	@Override
	public void execute() {
		final File f = oldPair.getKey();
		Try.newBuilder().setDefault(() -> {
			oldPath = Files.createTempFile(null, ".wav");
			Files.copy(f.toPath(), oldPath, StandardCopyOption.REPLACE_EXISTING);
		}).run();

		appInstance.data.get(i).get(j).set(null, "");

		Try.newBuilder().setDefault(() -> {
			if (f.exists()) Files.delete(f.toPath());
		}).run();
	}

	@Override
	public void undo() {
		new AddCommand(i, j, oldPath.toFile(), AddCommand.Type.FILE, oldText).execute();
	}
}
