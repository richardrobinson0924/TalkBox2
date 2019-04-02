package talkboxnew.Commands;

import talkboxnew.Entry;
import talkboxnew.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static talkboxnew.ConfigStage.data;
import static talkboxnew.Entry.Builder;

public class RemoveCommand implements History.Command {
	private final int i;
	private Entry oldPair;
	private String oldName;
	private Path oldPath;
	private Path oldImgPath;

	public RemoveCommand(int i) {
		this.i = i;
		this.oldPair = data.get(i);
		this.oldName = oldPair.getName();
	}

	@Override
	public void execute() throws IOException {
		final File f = oldPair.getFile();
		final File img = oldPair.getImage();

		oldPath = Files.createTempFile(null, ".wav");
		oldImgPath = Files.createTempFile(null, ".png");

		Files.copy(f.toPath(), oldPath, StandardCopyOption.REPLACE_EXISTING);
		Files.copy(img.toPath(), oldImgPath, StandardCopyOption.REPLACE_EXISTING);

		data.set(i, null);
		if (f.exists()) Files.delete(f.toPath());
	}

	@Override
	public void undo() throws IOException {
		final Path p = Files.copy(oldPath,
				Utils.getAudio(oldName),
				StandardCopyOption.REPLACE_EXISTING
		);

		final Path p2 = Files.copy(oldImgPath,
				Utils.getImage(oldName),
				StandardCopyOption.REPLACE_EXISTING
		);

		data.set(i, Builder.of(p.toFile())
				.withName(oldName)
				.withImage(p2.toFile())
				.build());
	}
}
