package talkboxnew;

import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class MenuItemBuilder {
	private String name;
	private ObservableBooleanValue bool;
	private EventHandler<ActionEvent> action;
	private KeyCode key;

	public MenuItemBuilder(String name) {
		this.name = name;
	}

	public MenuItemBuilder disableWhen(ObservableBooleanValue bool) {
		this.bool = bool;
		return this;
	}

	public MenuItemBuilder withAction(EventHandler<ActionEvent> action) {
		this.action = action;
		return this;
	}

	public MenuItemBuilder withShortcutKey(KeyCode key) {
		this.key = key;
		return this;
	}

	public MenuItem build() {
		final MenuItem menuItem = new MenuItem(name);
		menuItem.setId(name);

		if (bool != null) menuItem.disableProperty().bind(bool);

		menuItem.setOnAction(e -> {
			ConfigStage.log.info("MenuItem '" + name + "' selected");
			action.handle(e);
		});

		menuItem.setAccelerator(new KeyCodeCombination(key, KeyCombination.SHORTCUT_DOWN));
		return menuItem;
	}
}
