package talkbox;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CustomDataView extends Application {
	private TalkBoxData data;

	CustomDataView(TalkBoxData tbd) {
		this.data = tbd;
	}

	@Override
	public void start(Stage stage) {
		stage.setTitle("Custom Button Lists");
		final TabPane tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		final Tab verbs = makeTab("Verbs", 0);
		tabPane.getTabs().add(verbs);

		final Scene scene = new Scene(tabPane, 300, 500);
		stage.setScene(scene);
		stage.initModality(Modality.NONE);
		stage.show();
	}

	private Tab makeTab(String name, int index) {
		Tab tab = new Tab(name);

		Scene scene = new Scene(new Group());

		final TableView<String> table = new TableView<>();
		table.setEditable(true);

		final TableColumn<String, String> col = new TableColumn<>();

		table.getColumns().add(col);

		final VBox vbox = new VBox();
		vbox.setAlignment(Pos.CENTER);

		vbox.setSpacing(5);
		vbox.setPadding(new Insets(10, 10, 10, 10));
		vbox.getChildren().add(table);

		tab.setContent(vbox);

		return tab;
	}

}
