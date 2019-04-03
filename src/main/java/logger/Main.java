package logger;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import talkboxnew.Utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("TalkBox Log");
		primaryStage.setMinWidth(600);
		primaryStage.setMinHeight(100);

		Utils.tryFactory.attemptTo(() -> {
			primaryStage.setScene(new Scene(getTableView()));
			primaryStage.show();
		});
	}

	private TableView<String[]> getTableView() throws Exception {
		final TableView<String[]> tableView = new TableView<>();

		final List<TableColumn<String[], String>> colList = new ArrayList<>(Arrays.asList(
				new TableColumn<>("Time"),
				new TableColumn<>("Type"),
				new TableColumn<>("Line Number"),
				new TableColumn<>("Message")
		));

		colList.get(3).prefWidthProperty().bind(tableView
				.widthProperty()
				.subtract(colList.get(0).widthProperty())
				.subtract(colList.get(1).widthProperty())
				.subtract(colList.get(2).widthProperty())
				.subtract(5)
		);

		IntStream.range(0, colList.size()).forEach(i -> colList.get(i).setCellValueFactory(
				param -> new ReadOnlyStringWrapper(param.getValue()[i]))
		);

		tableView.getColumns().addAll(colList);
		tableView.setItems(parseFile());

		return tableView;
	}

	private ObservableList<String[]> parseFile() throws Exception {
		final ObservableList<String[]> list = FXCollections.observableArrayList();

		final File file = FileUtils
				.getUserDirectory()
				.toPath()
				.resolve("TalkBox")
				.resolve("logging.log")
				.toFile();

		final Scanner sc = new Scanner(file);
		while (sc.hasNextLine()) list.add(sc.nextLine().split(","));

		return list;
	}
}
