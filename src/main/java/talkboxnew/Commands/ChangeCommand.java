package talkboxnew.Commands;

import talkboxnew.Entry;
import talkboxnew.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static talkboxnew.ConfigStage.data;
import static talkboxnew.Entry.Builder;

public class ChangeCommand implements History.Command {
	private final int index;
	private final Entry tuple;
	private boolean wasEmpty = false;
	private RemoveCommand rc;

	/**
	 * Precondition: the file in {@code tuple} exists in the Audio directory
	 * @param i     the index of the tuple
	 * @param tuple the tuple to change to
	 */
	public ChangeCommand(int i, Entry tuple) {
		this.index = i;
		this.tuple = tuple;
		if (data.get(index) != null) rc = new RemoveCommand(i);
	}

	@Override
	public void execute() throws IOException {
		if (data.get(index) == null) wasEmpty = true;
		else rc.execute();

		final Path to = Utils.getAudio(tuple.getName() + ".wav");
		Files.copy(tuple.getFile().toPath(), to, StandardCopyOption.REPLACE_EXISTING);

		final Path imgTo = Utils.getImage(tuple.getName() + ".png");
		Files.copy(tuple.getImage().toPath(), imgTo, StandardCopyOption.REPLACE_EXISTING);

		data.set(index, Builder.of(to.toFile())
				.withName(tuple.getName())
				.withImage(imgTo.toFile())
				.build()
		);
	}

	@Override
	public void undo() throws IOException {
		if (wasEmpty) data.set(index, null);
		else rc.undo();
	}
}
