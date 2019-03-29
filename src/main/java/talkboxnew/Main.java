package talkboxnew;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.FilePermission;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Main extends Application {

	public static void main(String... args) {
		launch(args);
	}

	@Override
	public void init() throws Exception {
		final Path userDir = FileUtils.getUserDirectory().toPath();
		final Path talkboxDir = Paths.get(userDir.toString(), "TalkBox");

		if (!talkboxDir.toFile().exists()) {
			Path created = Files.createDirectory(talkboxDir);
			Files.createFile(Paths.get(created.toString(), "logging.log"));
			Files.createFile(Paths.get(created.toString(), "recents.bin"));
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		final Stage splash = new SplashStage();
		splash.show();
	}

}
