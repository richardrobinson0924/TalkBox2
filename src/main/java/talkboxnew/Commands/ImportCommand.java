package talkboxnew.Commands;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;

import static talkboxnew.ConfigStage.data;
import static talkboxnew.Entry.Builder;

public final class ImportCommand implements History.Command {
	private final DirectoryStream<Path> files;
	private int index = 0;
	private ChangeCommand[] adds;

	public ImportCommand(DirectoryStream<Path> files) {
		this.files = files;
		this.adds = new ChangeCommand[data.size()];
	}

	@Override
	public void execute() throws IOException {
		Iterator<Path> it = files.iterator();

		for (int j = 0; j < data.size(); j++) {
			if (!it.hasNext()) return;
			if (data.get(j) != null) continue;

			Path p = it.next();
			adds[index] = new ChangeCommand(j,
					Builder.of(p.toFile())
							.withName(p.getFileName().toString())
							.build()
			);

			adds[index++].execute();
		}
	}

	@Override
	public void undo() throws IOException {
		while (index-- > 0) adds[index].undo();
	}
}
