package talkbox.Commands;

import javafx.scene.control.TextInputDialog;
import java.util.Optional;

import static talkbox.Commands.History.appInstance;

public final class RenameCommand implements History.Command {
	private final int i;
	private final int j;
	private String oldName;

	public RenameCommand(int i, int j) {
		this.i = i;
		this.j = j;
		this.oldName = appInstance.data.get(i).get(j).getValue();
	}

	@Override
	public void execute() {
		final TextInputDialog dialog = new TextInputDialog(appInstance.buttons[j].getText());
		dialog.setTitle("Change Button Name");
		dialog.setHeaderText("Change Button Name");
		dialog.setContentText("Please enter the new name:");

		final Optional<String> result = dialog.showAndWait();
		result.ifPresent(name -> appInstance.data.get(i).get(j).setValue(name));
	}

	@Override
	public void undo() {
		appInstance.buttons[j].setText(oldName);
		appInstance.data.get(i).get(j).setValue(oldName);
	}
}
