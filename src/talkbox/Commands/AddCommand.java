package talkbox.Commands;

import javafx.scene.image.ImageView;
import talkbox.*;

import java.io.File;
import java.nio.file.Files;

import static talkbox.Commands.History.appInstance;

public final class AddCommand implements History.Command {
	private final int i;
	private final int j;
	private final File file;
	private final boolean isNull;
	private final RemoveCommand r;

	public AddCommand(int i, int j, File f) {
		this.i = i;
		this.j = j;
		this.file = f;
		this.isNull = appInstance.data.get(i).get(j).isNull();
		this.r = new RemoveCommand(i, j);
	}

	@Override
	public void execute() {
		if (!isNull) r.execute();

		appInstance.data.get(i).get(j).set(file, file.getName());
		appInstance.buttons[j].setText(file.getName());

		appInstance.makeContextMenu(i, j);
		appInstance.setGraphic(j);
	}

	@Override
	public void undo() {
		if (!isNull) r.undo();
		else {
			appInstance.data.get(i).get(j).set(null, "");
			appInstance.buttons[j].setText("Empty");

			final ImageView blank = new ImageView();
			blank.setImage(null);
			appInstance.buttons[j].setGraphic(blank);

			Try.newBuilder().setDefault(() -> {
				if (file.exists()) Files.delete(file.toPath());
			}).run();
		}
	}
}
