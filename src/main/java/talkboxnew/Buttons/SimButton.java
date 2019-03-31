package talkboxnew.Buttons;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import talkboxnew.Entry;

public final class SimButton extends Button {
	private static final int IMAGE_HEIGHT = 60;
	private static final int BUTTON_SIZE = 100;

	private static MediaPlayer player = null;

	public SimButton(final Entry e) {
		super();

		this.setContentDisplay(ContentDisplay.TOP);
		this.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
		this.setCursor(Cursor.HAND);

		if (e == null) setText("Empty");
		else {
			setText(e.getName());
			setGraphic(getImage(e));
			setOnAction(event -> onAction(e));
		}
	}

	private synchronized void onAction(final Entry entry) {
		entry.incrementFrequency();
		final Media media = new Media(entry.getFile().toURI().toString());

		if (player != null && player.getStatus() == MediaPlayer.Status.PLAYING)
			player.stop();

		player = new MediaPlayer(media);
		player.play();
	}

	private ImageView getImage(final Entry e) {
		final Image image = new Image(e.getImage().toPath().toString());
		final ImageView imageView = new ImageView(image);

		imageView.setPreserveRatio(true);
		imageView.setFitHeight(IMAGE_HEIGHT);

		return imageView;
	}
}
