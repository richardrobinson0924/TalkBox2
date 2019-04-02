package talkboxnew;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

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

		final Properties props = new Properties();
		try (final InputStream is = Utils.getStream("log4j.properties")) {
			props.load(is);
		}

		props.setProperty("log4j.appender.FILE.file", talkboxDir.resolve(LOG).toString());
		PropertyConfigurator.configure(props);
	}

	@Override
	public void start(Stage primaryStage) {

		final Stage splash = new SplashStage();
		splash.show();
	}

}
