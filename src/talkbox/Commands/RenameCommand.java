package talkbox.Commands;

import javafx.scene.control.TextInputDialog;
import talkbox.TalkBoxApp;

import java.util.Optional;

public final class RenameCommand implements History.Command {
	private final int i;
	private final int j;
	private String oldName;

	public RenameCommand(int i, int j) {
		this.i = i;
		this.j = j;
		this.oldName = TalkBoxApp.data.get(i).get(j).getValue();
	}

	@Override
	public void execute() {
		final TextInputDialog dialog = new TextInputDialog(TalkBoxApp.data.get(i).get(j).getValue());
		dialog.setTitle("Change Button Name");
		dialog.setHeaderText("Change Button Name");
		dialog.setContentText("Please enter the new name:");

		final Optional<String> result = dialog.showAndWait();
		result.ifPresent(name -> TalkBoxApp.data.get(i).get(j).setValue(name));
	}

	@Override
	public void undo() {
		TalkBoxApp.data.get(i).get(j).setValue(oldName);
	}
}
