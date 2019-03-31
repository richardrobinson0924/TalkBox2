package talkboxnew;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Main extends Application {
	private static final String FOLDER = "TalkBox";
	private static final String LOG = "logging.log";
	private static final String RECENT = "recents.bin";

	public static void main(String... args) {
		launch(args);
	}

	@Override
	public void init() throws Exception {
		final Path userDir = FileUtils.getUserDirectory().toPath();
		final Path talkboxDir = Paths.get(userDir.toString(), FOLDER);

		if (!talkboxDir.toFile().exists()) {
			final Path created = Files.createDirectory(talkboxDir);

			Files.createFile(Paths.get(created.toString(), LOG));
			Files.createFile(Paths.get(created.toString(), RECENT));
		}
	}

	@Override
	public void start(Stage primaryStage) {

		final Stage splash = new SplashStage();
		splash.show();
	}

}
