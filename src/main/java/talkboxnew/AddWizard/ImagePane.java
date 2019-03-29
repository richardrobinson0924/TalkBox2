package talkboxnew.AddWizard;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import talkboxnew.Entry;
import talkboxnew.Utils;

import java.io.File;
import java.net.URI;

import static talkboxnew.AddWizard.AddWizardView.*;
import static talkboxnew.Utils.tryFactory;

class ImagePane extends WizardPane {
	ImagePane(Entry oldEntry, Wizard wiz) {
		super();

		this.setHeaderText("Select Button Image");
		this.setContent(getView(oldEntry, wiz));
		this.setPrefSize(WIDTH, HEIGHT);
	}

	private VBox getView(Entry oldEntry, Wizard wiz) {
		final Label text = new Label("Select an image to use with your audio button. If no image \nis selected, the default one will be used.");
		text.setWrapText(true);
		final TextFlow textFlow = new TextFlow(text);
		textFlow.setTextAlignment(TextAlignment.LEFT);

		final VBox box = new VBox(SPACING);
		box.setAlignment(Pos.CENTER);

		box.getChildren().addAll(textFlow, getButton(oldEntry, wiz));
		return box;
	}

	private Button getButton(Entry oldEntry, Wizard wiz) {
		final Button img = new Button();

		final SimpleObjectProperty<URI> imgName = new SimpleObjectProperty<>();
		tryFactory.attemptTo(() -> imgName.set((oldEntry == null)
				? Utils.getResource("button_graphic.png").toURI()
				: oldEntry.getImage().toURI())
		);

		final Image image = new Image(imgName.get().toString());
		final ImageView imageView = new ImageView();

		imageView.setImage(image);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(80);

		img.setGraphic(imageView);

		img.setOnAction(e -> {
			final FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files (.png, .jpg, .jpeg)", "*.png", "*.jpg", "*.jpeg"));
			final File received = fileChooser.showOpenDialog(null);

			if (received != null) {
				imgName.set(received.toURI());
				tryFactory.attemptTo(() -> imageView.setImage(new Image(received.toURI().toURL().toString()))
				);
			}
		});

		wiz.getSettings().put("image", imgName);
		return img;
	}
}
