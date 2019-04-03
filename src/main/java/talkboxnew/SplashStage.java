package talkboxnew;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static talkboxnew.Utils.isValidFile;
import static talkboxnew.Utils.tryFactory;

public final class SplashStage extends Stage {
	private static final Logger log = Logger.getLogger(SplashStage.class.getName());

	private final static int
			SPACING = 10,
			LIST_WIDTH = 250,
			BOX_WIDTH = (int) (LIST_WIDTH * 1.4),
			WIDTH = (LIST_WIDTH + BOX_WIDTH),
			HEIGHT = (int) (LIST_WIDTH * 1.6),
			LOGO_SIZE = (int) (LIST_WIDTH / 2.3),
			ICON_SIZE = (int) (LIST_WIDTH / 1.25);

	private final static String LOGO = "512-2.png";

	// each node in this string is used for testing... Id must be set for the testing to work
	// KEEP THIS RESPECTIVE ORDER.
	public static final String[] nodesId = {"OPEN_BUTTON", "NEW_BUTTON", "LIST"};

	public SplashStage() {
		log.info("Starting TalkBox App...");

		this.setWidth(WIDTH);
		this.setTitle("TalkBox");
		this.setHeight(HEIGHT);
		this.setResizable(false);

		HBox hbox = new HBox();
		hbox.getChildren().addAll(getRecents(), getSplash());

		final Scene scene = new Scene(hbox);

		scene.getStylesheets().addAll(Utils
				.getResource("global_style.css")
				.toExternalForm()
		);

		this.setScene(scene);
	}

	private ListView getRecents() {
		final ObservableList<File> data = FXCollections.observableArrayList();

		try {
			final ObjectInputStream oin = new ObjectInputStream(new FileInputStream(Utils.getRecentsPath().toString()));
			final ArrayList<File> tmp = (ArrayList<File>) oin.readObject();

			if (!tmp.isEmpty()) {
				Collections.reverse(tmp);
				data.addAll(tmp);
			}
		} catch (Exception ignored) {
		}

		System.out.println(data);
		data.removeIf(file -> !isValidFile(file));

		final ListView<File> list = new ListView<>();
		// set name for the listview
		list.setId(nodesId[2]);
		list.setPrefWidth(LIST_WIDTH);

		list.prefHeightProperty().bind(this.heightProperty());
		list.setItems(data);

		list.setPlaceholder(new Label("No Recent Files"));

		final SplashStage thisStage = this;
		list.setOnMouseClicked(event -> {
			list.getFocusModel().focus(list.getSelectionModel().getSelectedIndex());

			if (event.getClickCount() == 2) try {
				log.info("Attempting to open file from recents list...");
				if (!isValidFile(list.getSelectionModel().getSelectedItem())) {

					data.remove(list.getSelectionModel().getSelectedIndex());
					list.refresh();
				} else {
					new ConfigStage(list.getSelectionModel().
							getSelectedItem().
							toPath()
					).show();
					thisStage.close();
				}
			} catch (Exception ignored) {
				log.warn("File invalid. Removing file from list");
				data.remove(list.getSelectionModel().getSelectedIndex());
				list.refresh();
			}
		});

		list.setCellFactory(param -> new ListCell<File>() {
			@Override
			public void updateItem(File file, boolean empty) {
				super.updateItem(file, empty);

				if (file == null) {
					setGraphic(null);
					return;
				}

				final VBox vbox = new VBox();
				vbox.setAlignment(Pos.CENTER_LEFT);
				vbox.getChildren().clear();

				final Label firstLine = new Label("~/" + file
						.toPath().subpath(2, file.toPath().getNameCount())
						.toString()
				);

				final Label secondLine = new Label("Last Modified: " + new Date(file.lastModified())
						.toString()
						.substring(4)
						.replaceAll(" \\w{3} \\d{4}$", "")
				);

				final String styl = secondLine.getStyle();
				secondLine.styleProperty().bind(Bindings
						.when(selectedProperty().not())
						.then("-fx-text-fill: #A0A0A0")
						.otherwise(styl)
				);

				vbox.getChildren().addAll(firstLine, secondLine);
				this.setGraphic(vbox);
			}
		});

		return list;
	}

	private VBox getSplash() {
		final VBox box = new VBox();
		box.setPrefWidth(BOX_WIDTH);
		box.prefHeightProperty().bind(this.heightProperty());

		box.setAlignment(Pos.CENTER);
		box.setSpacing(SPACING);

		final Button openButton = new CustomButton(
				"Open Configuration...",
				FontAwesome.Glyph.FOLDER_OPEN,
				this::openAction
		);

		final Button newButton = new CustomButton(
				" New Configuration...",
				FontAwesome.Glyph.PLUS,
				this::newAction
		);

        openButton.setId(nodesId[0]);
        newButton.setId(nodesId[1]);

		final VBox vbox = new VBox(newButton, openButton);
		vbox.alignmentProperty().setValue(Pos.CENTER);

		final Label l = new Label("TalkBox (v2)");
		l.setFont(Font.font("Segoe UI", FontWeight.THIN, 24));

		final Label l2 = new Label("Developed by EECS Group 2, Inc.");

		final Region space = new Region();
		space.setPrefSize(0, SPACING);
		final Region space2 = new Region();
		space2.setPrefSize(0, SPACING);

		box.getChildren().addAll(l, l2, space, generateImage(), space2, vbox);

		return box;
	}

	private void newAction(ActionEvent actionEvent) {
		log.info("'New Configuration' button selected");
		final CreateConfigWizard wiz = new CreateConfigWizard();

		if (wiz.getWizard()) this.close();
	}

	private void openAction(ActionEvent actionEvent) {
		log.info("'Open Existing' button selected");
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		final File f = directoryChooser.showDialog(this);

		if (f == null) return;
		log.info("Opening directory " + f.toString());

		if (isValidFile(f)) {
			final Alert alert = new Alert(Alert.AlertType.ERROR);

			alert.setHeaderText("Directory Not Valid");
			alert.setTitle("Directory Not Valid");

			alert.setContentText("This directory is either not a TalkBox directory or is corrupted. Please select another option.");
			alert.showAndWait().ifPresent(a -> alert.close());
		} else tryFactory.attemptTo(() -> {
			new ConfigStage(f.toPath()).show();
			this.close();
		});
	}

	private ImageView generateImage() {
		final Image img = new Image(Utils.getResource(LOGO).toString());
		final ImageView imageView = new ImageView(img);

		imageView.setPreserveRatio(true);
		imageView.setFitHeight(LOGO_SIZE);

		return imageView;
	}

	public static final class CustomButton extends Button {
		CustomButton(String str, FontAwesome.Glyph icon, EventHandler<ActionEvent> ae) {
			super(str, new Glyph("FontAwesome", icon));

			this.setOnAction(ae);

			this.setAlignment(Pos.CENTER_LEFT);
			this.setPrefWidth(ICON_SIZE);

			this.getStylesheets().add(Utils
					.getResource("style.css")
					.toExternalForm()
			);

			this.setOnMouseEntered(e -> getScene().setCursor(Cursor.HAND));
			this.setOnMouseExited(e -> getScene().setCursor(Cursor.DEFAULT));
		}
	}

}
