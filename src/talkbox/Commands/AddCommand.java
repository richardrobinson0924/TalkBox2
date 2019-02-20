package talkbox.Commands;

import javafx.scene.image.ImageView;
import talkbox.*;

import java.io.File;

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
		this.isNull = appInstance.ts.database[i][j] == null;
		this.r = new RemoveCommand(i, j);
	}

	@Override
	public void execute() {
		if (!isNull) r.execute();

		appInstance.ts.database[i][j] = new TalkBoxData.AudioPair(file, file.getName());
		appInstance.buttons[j].setText(file.getName());

		appInstance.makeContextMenu(i, j);
		appInstance.setGraphic(j);
	}

	@Override
	public void undo() {
		if (!isNull) r.undo();
		else {
			appInstance.ts.database[i][j] = null;
			appInstance.buttons[j].setText("Empty");

			final ImageView blank = new ImageView();
			blank.setImage(null);
			appInstance.buttons[j].setGraphic(blank);
		}
	}
}
