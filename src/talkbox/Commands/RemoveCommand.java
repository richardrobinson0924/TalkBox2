package talkbox.Commands;

import javafx.scene.image.ImageView;
import talkbox.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;;

import static talkbox.Commands.History.appInstance;

public final class RemoveCommand implements History.Command {
	private final int i;
	private final int j;
	private TalkBoxData.AudioPair oldPair;
	private Path oldPath;

	public RemoveCommand(int i, int j) {
		this.i = i;
		this.j = j;
		this.oldPair = appInstance.ts.database[i][j];
	}

	@Override
	public void execute() {
		final File f = oldPair.getKey();
		Try.newBuilder().setDefault(() -> {
			oldPath = Files.createTempFile(null, ".wav");
			Files.copy(f.toPath(), oldPath, StandardCopyOption.REPLACE_EXISTING);
		}).run();

		appInstance.ts.database[i][j] = null;
		appInstance.buttons[j].setText("Empty");
		TalkBoxApp.setIsChanged(true);

		Try.newBuilder().setDefault(() -> {
			if (f.exists()) Files.delete(f.toPath());
		}).run();

		final ImageView blank = new ImageView();
		blank.setImage(null);
		appInstance.buttons[j].setGraphic(blank);
	}

	@Override
	public void undo() {
		System.out.println("h");
		appInstance.ts.database[i][j] = oldPair;
		appInstance.buttons[j].setText(oldPair.getValue());

		Try.newBuilder()
				.setDefault(() -> Files.copy(oldPath, new FileOutputStream(oldPair.getKey().getPath())))
				.run();

		TalkBoxApp.setIsChanged(true);

		appInstance.setGraphic(j);
	}
}
