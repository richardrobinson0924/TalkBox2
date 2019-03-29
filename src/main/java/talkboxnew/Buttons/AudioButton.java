package talkboxnew.Buttons;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.apache.log4j.Logger;
import talkboxnew.AddWizard.AddWizardView;
import talkboxnew.Commands.ChangeCommand;
import talkboxnew.Commands.History;
import talkboxnew.Commands.RemoveCommand;
import talkboxnew.Entry;
import talkboxnew.Utils;

import java.io.File;

import static talkboxnew.ConfigStage.WAV;
import static talkboxnew.ConfigStage.data;
import static talkboxnew.Entry.Builder;
import static talkboxnew.Utils.tryFactory;

/**
 * Pre-customized button to use for selecting / playing audio with provided data logic. All necessary methods
 * implemented appropriately and no further changes needed.
 * <p></p>
 * If {@code database[i][j]} in the configuration file is null, pressing the button adds audio via TTSWizard.
 * Else, the audio is played. An individual {@code *.wav} file may be associated with the button via right
 * clicking -> {@code Change} or by dragging and dropping.
 * <p></p>
 * Comes preconfigured with:
 * <ul>
 * <li>Automatically updating text and graphic to represent the button's content</li>
 * <li>Automatically updating tooltip</li>
 * <li>Accessibility options enabled</li>
 * <li>Can be renamed, removed, or changed / added</li>
 * </ul>
 */
public final class AudioButton extends Button {
	private final int i;

	private final static int GRAPHIC_SIZE = 60;
	private final static int BUTTON_SIZE = 100;

	private static final Logger log = Logger.getLogger(Utils.class.getName());

	public AudioButton(int i) {
		super();
		this.i = i;

		this.setUI();
		this.setAccessibility();
		this.setDragged();
		this.setContextMenu();

		data.addListener((ListChangeListener<Entry>) c -> {
			c.next();
			setProperties();
		});

		this.setOnAction(this::onAction);
		this.setProperties();
	}

	private void setProperties() {
		if (data.get(i) == null) {
			this.setText("Empty");
			this.setGraphic(null);

			this.setTooltip(new Tooltip("Click to add audio"));
		} else {
			final Label name = new Label(data.get(i).getName());
			final Label freq = new Label(String.format("Hits: %.0f%%", Utils.getFrequencyPercentage(data.get(i), data)).toUpperCase());

			freq.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.SEMI_BOLD, 11));
			freq.setTextFill(Color.GRAY);

			this.setTooltip(new Tooltip("Click to play audio"));

			final ImageView graphic = new ImageView(new Image(data
					.get(i)
					.getImage()
					.toURI()
					.toString()));

			graphic.setFitHeight(GRAPHIC_SIZE);
			graphic.setPreserveRatio(true);

			final Region r = new Region();
			r.setPrefHeight(7);

			final VBox vbox = new VBox(0, graphic, r, name, freq);
			vbox.setAlignment(Pos.CENTER);

			this.setGraphic(vbox);
			this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		}
	}

	private static MediaPlayer player = null;

	private synchronized void onAction(ActionEvent event) {
		if (data.get(i) == null) tryFactory.attemptTo(() -> {
			log.info("Entry empty. Invoking AddWizardView for index " + i);
			final AddWizardView aav = new AddWizardView(i, this.getScene().getWindow());

			aav.showAndWait().ifPresent(button -> {
				if (button == ButtonType.FINISH) aav.doOnFinish();
			});
		});
		else {
			log.info("Playing audio for Entry " + i);
			final File soundFile = data.get(i).getFile();
			final Media media = new Media(soundFile.toURI().toString());

			if (player != null && player.getStatus() == MediaPlayer.Status.PLAYING) player.stop();

			player = new MediaPlayer(media);
			player.play();
		}
	}

	private void setUI() {
		this.setContentDisplay(ContentDisplay.TOP);
		this.setPrefSize(BUTTON_SIZE, BUTTON_SIZE + 20);
		this.setCursor(Cursor.HAND);
	}

	private void setDragged() {
		this.setOnDragOver(event -> {
			if (event.getGestureSource() != this && event.getDragboard().hasFiles())
				event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			event.consume();
		});

		this.setOnDragDropped(event -> {
			final Dragboard dragboard = event.getDragboard();
			final File file = dragboard.getFiles().get(0);

			if (file.getName().matches(WAV))
				tryFactory.attemptTo(() -> History.getInstance().execute(new ChangeCommand(i, Builder.of(file).build())));
		});
	}

	private void setAccessibility() {
		this.setAccessibleRole(AccessibleRole.BUTTON);
		this.accessibleTextProperty().bind(this.textProperty());
	}

	private void setContextMenu() {
		final ContextMenu contextMenu = new ContextMenu();

		final MenuItem remove = new MenuItem("Remove");
		final MenuItem change = new MenuItem("Change");
		contextMenu.getItems().addAll(remove, change);

		change.setOnAction(event -> tryFactory.attemptTo(() -> {
			final AddWizardView awv = new AddWizardView(i, this.getScene().getWindow());
			awv.showAndWait().ifPresent(button -> {
				if (button == ButtonType.FINISH) awv.doOnFinish();
			});
		}));

		remove.setOnAction(event -> tryFactory.attemptTo(() -> History.getInstance().execute(new RemoveCommand(i))));

		this.setContextMenu(contextMenu);
	}
}

