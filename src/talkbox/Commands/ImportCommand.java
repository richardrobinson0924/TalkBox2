package talkbox.Commands;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;

import static talkbox.Commands.History.appInstance;

public final class ImportCommand implements History.Command {
	private final DirectoryStream<Path> files;
	private int index;
	private AddCommand[] adds;

	public ImportCommand(DirectoryStream<Path> files) {
		this.files = files;
		this.index = 0;
		this.adds = new AddCommand[appInstance.data.size() * appInstance.data.get(0).size()];
	}

	@Override
	public void execute() {
		Iterator<Path> it = files.iterator();
		System.out.println("as");

		for (int i = 0; i < appInstance.data.size(); i++) {
			for (int j = 0; j < appInstance.data.get(0).size(); j++) {
				if (!it.hasNext()) return;
				if (!appInstance.data.get(i).get(j).isNull()) continue;

				adds[index] = new AddCommand(i, j, it.next().toFile(), AddCommand.Type.FILE);
				adds[index++].execute();
			}
		}
	}

	@Override
	public void undo() {
		while (index-- > 0) adds[index].undo();
	}
}

