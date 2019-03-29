package talkboxnew;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class Utils {
	private static final Logger log = Logger.getLogger(Utils.class.getName());

	public static final TryFactory tryFactory = new TryFactory(Utils::release);

	private Utils() {
	}

	public static void release(Throwable ex) {
		final Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("An Error has Occurred");
		alert.setHeaderText(alert.getTitle());
		alert.setContentText(ex.getMessage());

		alert.initModality(Modality.APPLICATION_MODAL);

		final StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		final String exceptionText = sw.toString();

		final Label label = new Label("Full error message:");

		final TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);

		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		final GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		alert.getDialogPane().setExpandableContent(expContent);
		alert.showAndWait();
	}

	public static URL getResource(String path) {
		return Objects.requireNonNull(Utils.class.getClassLoader().getResource(path));
	}

	/**
	 * Returns the file containing the audio with filename {@code audioName}. Such file is located in the {@code /Audio} subdirectory alongside {@code /Images}
	 *
	 * @param audioName the filename of the audio (with extension)
	 * @return the File containing such image
	 */
	public static Path getAudio(String audioName) {
		return Paths.get(ConfigStage.masterPath.toString(), "Audio", audioName);
	}

	/**
	 * Returns the file containing the image with filename {@code imgName}. Such file is located in the {@code /Images} subdirectory
	 *
	 * @param imgName the filename of the image (with extension)
	 * @return the File containing such image
	 */
	public static Path getImage(String imgName) {
		return Paths.get(ConfigStage.masterPath.toString(), "Images", imgName);
	}

	/**
	 * Returns the {@code *.tbc} configuration file. Such file has a filename equal to that of the parent folder.
	 *
	 * @return the configuration file
	 */
	static File getTBC() {
		return Paths.get(ConfigStage.masterPath.toString(), ConfigStage.masterPath.getFileName().toString() + ".tbc").toFile();
	}

	public static InputStream getStream(String path) {
		return Objects.requireNonNull(Utils.class.getClassLoader().getResourceAsStream(path));
	}

	public static Path getLogPath() {
		return Paths.get(FileUtils.getUserDirectory().toPath().toString(), "TalkBox", "logging.log");
	}

	public static Path getRecentsPath() {
		return Paths.get(FileUtils.getUserDirectory().toPath().toString(), "TalkBox", "recents.bin");
	}

	public static double getFrequencyPercentage(Entry e, List<Entry> list) {
		if (list.size() <= 1) return 100;

		double total = list.stream()
				.filter(Objects::nonNull)
				.mapToDouble(Entry::getFrequency)
				.sum();

		return (total == 0) ? 0 : (e.getFrequency() / total) * 100;
	}

	static boolean isValidFile(File f) {
		return Files.exists(f.toPath())
				&& Files.exists(Paths.get(f.getPath(), f.getName() + ".tbc"))
				&& Files.isDirectory(f.toPath())
				&& Files.isDirectory(Paths.get(f.getPath(), "Audio"))
				&& Files.isDirectory(Paths.get(f.getPath(), "Images"));
	}

	private static Clip clip;
	public static synchronized void play(final AudioInputStream stream) {
		tryFactory.attemptTo(() -> {
			if (clip == null) clip = AudioSystem.getClip();
			else clip.close();

			clip.open(stream);
			clip.start();
		});
	}

	public static final class TryFactory {
		public interface TrySupplier {
			void run() throws Throwable;
		}

		private final Consumer<Throwable> orElse;

		public TryFactory(Consumer<Throwable> orElse) {
			this.orElse = orElse;
		}

		public final void attemptTo(TrySupplier supplier) {
			attemptTo(supplier, null);
		}

		final void attemptTo(TrySupplier supplier, Runnable otherwise) {
			try {
				supplier.run();
			} catch (Throwable throwable) {
				orElse.accept(throwable);
				throwable.printStackTrace(System.err);
				otherwise.run();
			}
		}
	}
}
