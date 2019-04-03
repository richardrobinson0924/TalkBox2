package talkboxnew.AddWizard;

import com.google.api.client.util.IOUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ImageInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import talkboxnew.Entry;
import talkboxnew.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.log4j.Logger.*;
import static talkboxnew.AddWizard.AddWizardView.*;
import static talkboxnew.Utils.tryFactory;

public class ImagePane extends WizardPane {
	Logger log = getLogger(this.getClass());
	public ImagePane(Entry oldEntry, Wizard wiz) {
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

		try {
			box.getChildren().addAll(textFlow, getButton(oldEntry, wiz));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return box;
	}

	private Button getButton(Entry oldEntry, Wizard wiz) throws Exception {
		final Button img = new Button();
		final File[] stream = new File[1];

		if (oldEntry == null) {
			final Path tmp = Files.createTempFile(null, null);
			Files.copy(Utils.getStream("button_graphic.png"), tmp, StandardCopyOption.REPLACE_EXISTING);
			stream[0] = tmp.toFile();
		} else {
			stream[0] = oldEntry.getImage();
		}

		final ImageView imageView = new ImageView();

		imageView.setImage(new Image(stream[0].toURI().toURL().toString()));
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(80);

		img.setGraphic(imageView);

		img.setOnAction(e -> {
			final FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files (.png, .jpg, .jpeg)", "*.png", "*.jpg", "*.jpeg"));
			final File received = fileChooser.showOpenDialog(null);

			if (received != null) {
				stream[0] = received;
				tryFactory.attemptTo(() -> {
					imageView.setImage(new Image(stream[0].toURI().toURL().toString()));
					wiz.getSettings().put("image", stream[0]);
				});
			}
		});

		wiz.getSettings().put("image", stream[0]);
		return img;
	}
}
