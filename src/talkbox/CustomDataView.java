package talkbox;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public final class CustomDataView extends Application {
	private TalkBoxData ts;
	private Stage owner;

	public CustomDataView(TalkBoxData tbd, Stage s) {
		this.ts = tbd;
		this.owner = s;
	}

	@Override
	public synchronized void start(Stage stage) throws Exception {
		stage.initStyle(StageStyle.UTILITY);
		stage.initOwner(owner);
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		stage.setTitle("Custom Button Lists");
		final TabPane tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		final Tab verbs = makeTab("Verbs", 0);
		final Tab subjects = makeTab("Subjects", 1);
		final Tab objects = makeTab("Objects", 2);

		tabPane.getTabs().addAll(verbs, subjects, objects);

		final Scene scene = new Scene(tabPane, 300, 500);
		stage.setScene(scene);
		stage.initModality(Modality.NONE);
		stage.show();
	}

	private Tab makeTab(String name, int index) {
		final Tab tab = new Tab(name);

		final TableView<String> table = new TableView<>();
		table.setEditable(true);

		final TableColumn<String, String> col = new TableColumn<>(name);

		final VBox vbox = new VBox();
		vbox.setAlignment(Pos.CENTER_RIGHT);

		vbox.setSpacing(5);
		vbox.setPadding(new Insets(10, 10, 10, 10));
		vbox.getChildren().add(table);

		tab.setContent(vbox);

		final List<String> rawData = ts.customWords.get(index);
		final ObservableList<String> data = FXCollections.observableArrayList(rawData);

		col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		table.setItems(data);
		table.getColumns().add(col);


		final HBox hbox = makeAddField(name.substring(0, name.length() - 1), table, index, data);
		vbox.getChildren().add(hbox);

		return tab;
	}

	private HBox makeAddField(String name, TableView<String> table, int index, ObservableList<String> data) {
		final HBox hBox = new HBox();
		hBox.setAlignment(Pos.CENTER);

		final TextField addString = new TextField();
		addString.setPromptText(name);
		addString.setMaxWidth(table.getPrefWidth());

		addString.setOnAction(event -> {
			data.add(addString.getText());
			ts.customWords.set(index, new ArrayList<>(data));

			addString.clear();
			TalkBoxApp.setIsChanged(true);
		});

		final Label label = new Label("Add " + name + ": ");

		hBox.getChildren().addAll(label, addString);
		return hBox;
	}

}
