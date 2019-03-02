package talkbox;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import javax.swing.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * The TalkBox Simulator simulates the physical device. When the app is launched, the user is prompted to either (a)
 * create a new .tbc file or (b) open an existing file. At any point in the application, the user can launch the TalkBox
 * configurator with the current .tbc file pre-loaded in the Configurator.
 *
 * <p>
 *     <b>Creating a new file:</b>
 * <p>
 *
 * This opens a wizard-like dialog, with the following steps:
 * <ul>
 *     <li> Asks where to save file on the disk
 *     <li> Asks how many audio buttons and swap buttons it should have
 *     <li> Once done, on backend creates a TalkBox directory in the location specified. Within the directory, there
 *     will be the .tbc file, as well as another directory entitled "Audio" to contain the audio files
 * </ul>
 *
 * Afterwards, the user will have the option of opening the Configurator with this newly created .tbc file
 *
 * <p>
 *     <b>Opening an Existing File</b>
 * <p>
 *`
 * This presents a FileChooser allowing a user to select a .tbc TalkBox Configuration file. Then, an  interface will
 * appear with the following presentation:
 * <ul>
 *     <li> All the required specifications as described on the project outline (buttons with swap buttons acting accordingly
 *     <li> There shall also exist a <code>Custom</code> button with a <code>Play</code> button beside it.
 * </ul>
 *
 * <p>
 *     <b>Using the Custom button</b>
 * <p>
 *
 * The custom button is an on-board sentence TTS generator. Upon pressing the button, each audio button transforms
 * into a sentence Subject word (for example, "Richard", "Myself", "You"). Once the user selects the Subject,
 * the audio buttons again transform into Verbs. This process continues for the following sentence structures:
 * subjects, verbs, objects, tenses, and propositional meanings. After the final selection, the
 * <code>simplenlg</code> API creates a new sentence out of the different words.
 *
 * <p>
 * The list of different options for each sentence structure will be provided in a list of lists within the directory
 * (first column is Subject, next is Verbs, etc...), which must first be parsed by the Simulator. The user can then
 * press <code>Play</code> to play the newly generated sentence using the Google Cloud TTS service.
 *
 * <p>
 *     <b>Operation</b>
 * </p>
 *
 * The actual device will only have single LED text panel. Hence, the device and simulator should operate precisely as follows:
 * <ul>
 *     <li>On the simulator, the buttons will not have labels so as to mimic the device</li>
 *     <li>The simulator will have a single (decorated) text label centered above the buttons</li>
 *     <li>When clicking a button, the text label will change to that button's label / caption</li>
 *     <li>Only after clicking the button again will the audio play</li>
 *     <li>If the user clicks the 'Custom' button, the text label ill temporarily (~2 seconds) show the text 'Custom'</li>
 *     <li>Furthermore within custom, clicking a button once will again make the label that of the button's temporarily</li>
 *     <li>Clicking twice will add the button's label / text to the text label in addition to any other labels that may be there</li>
 * </ul>
 *
 * <p>
 * The following example highlights how the text label works with the Custom button:
 * <ul>
 *     <li>User clicks Custom. (Text Label: "Custom" for ~2 seconds, then blank)</li>
 *     <li>User selects a subject (Bob). (Text Label: "Bob" for ~1 seconds, then blank)</li>
 *     <li>User selects the button again. (Text Label: "Bob" [persistant])</li>
 *
 *     <li>User selects a verb (run) (Text Label: "Run" for ~2 seconds, then previous state ("Bob")</li>
 *     <li>User selects button again. (Text Label: "Bob, Run" [persistent])</li>
 *     <li>...</li>
 *     <li>Upon selecting last option (tense), the sentence is created and generated onto the text label.</li>
 *     <li>Clicking the "Play" button plays the sentence and ends the 'custom' creation</li>
 * </ul>
 *
 * <b>Note:</b> If the device is not connected to internet, the device / Simulator should not use the Google TTS service; instead, it should switch to an offline service.
 */

public class TalkBoxSim extends Application {
	// instructions:
	// you wanna ask the user if they wanna make a new tbc file or open an existing one. (2 buttons)
	// when the .tbc file is created, it opens (or when the existing file is opened) and a simple interface of the buttons

	private File file;
	private TalkBoxData ts;
	private Button[] buttons;
	private Stage simStage;
	private VBox box;
	private Scene scene;
    private MenuBar menuBar;

	private static final String AUDIO_PATH = "/Audio";
	private File audioFolder;

    private static int SIMSTAGE_HEIGHT = 400;
    private static int SIMSTAGE_WIDTH = 500;
	private static final int MINOR_BUTTON_HEIGHT = 20;
	private static final int MINOR_BUTTON_WIDTH = 85;
	private boolean solveCanMkAudioDirMethod = false;
	private boolean solveCanMakeFileMethod = false;
	private boolean canContinue = false;
	private String savedDir;
	private String savedName;
	private int numAudioBtns;
	private int numAudioSets;

	public static void main(String... args) {
		launch(args);
	}

	public void start(Stage simStage) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
		FlowPane onStartPane = new FlowPane();
		onStartPane.setPadding(new Insets(30, 20, 30, 20));
		onStartPane.setVgap(10);
		onStartPane.setHgap(10);
		onStartPane.setAlignment(Pos.CENTER);

		/* Sets the UI */
		this.simStage = simStage;
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		/* Creates the outermost container, composing of a `FlowPane` */
		box = new VBox();

		/* Sets window size and title */
		simStage.setTitle("TalkBox Simulator");
		simStage.setResizable(false);
		simStage.getIcons().add(new Image(TalkBoxApp.class.getResourceAsStream("/Resources/icon2.png")));

		/* Added the Creating a new file button */
		Button newFileBtn = new Button("Create a New File");

		// FYI: when code appears grey (like in the next line) press Alt-Enter and intelliJ will let you convert
        // to lambda expression :)
		newFileBtn.setOnAction(event -> {
			try {
				createNewTBC();
				simStage.setTitle("TalkBox Configurator — " + file.getName());

				buttons = new Button[ts.numberOfAudioButtons];

				// ------------------------------ MENUBAR TEST ----------------------------
                MenuBar mb = makeMenuBar();

				Pagination pagination = new Pagination(ts.numberOfAudioSets);
				box.getChildren().addAll(mb, pagination);

				pagination.setPageFactory(TalkBoxSim.this::configButtons);
				//if the customWord is not empty then implement the custom and play button
				if(!(ts.customWords.get(0).isEmpty()==true)) {
					//create new flow plane for the buttons
					FlowPane flowPane = new FlowPane();
					//to prevent "hard coding"
					int flowpane_top;
					flowpane_top = 30;
					int flowpane_right;
					flowpane_right = 20;
					int flowpane_bottom;
					flowpane_bottom = 30;
					int flowpane_left;
					flowpane_left = 20;
					flowPane.setPadding(new Insets(flowpane_top, flowpane_right, flowpane_bottom, flowpane_left));
					int flowpane_width;
					flowpane_width = 100;
					int flowpane_height;
					flowpane_height = 25;
					flowPane.setPrefSize(flowpane_width, flowpane_height);
					int flowpane_vGap;
					flowpane_vGap = 10;
					int flowpane_hGap;
					flowpane_hGap = 10;
					flowPane.setVgap(flowpane_vGap);
					flowPane.setHgap(flowpane_hGap);
					flowPane.setAlignment(Pos.BOTTOM_RIGHT);

					//custom Button
					Button custom;
					String titleCustom;
					titleCustom = "Custom";
					//create new instance of button called custom
					custom = new Button(titleCustom);
					//set width of custom button
					int customWidth;
					customWidth = 80;
					//set height of custom button
					int customHeight;
					customHeight = 50;
					//set size of custom button
					custom.setPrefSize(customWidth, customHeight);
					//add custom button to flowpane
					flowPane.getChildren().add(custom);
					custom.setAlignment(Pos.CENTER);
					//translate custom button to bottom left
					int customX_Translate;
					customX_Translate = 90;
					int customY_Translate;
					customY_Translate = -40;
					custom.setTranslateX(customX_Translate);
					custom.setTranslateY(customY_Translate);
					//string of subject,verb and object
					//this string will be what is displayed.
					ArrayList<String> sentence = new ArrayList<>();
					custom.setOnAction(e -> {//event E should do..
				//for every button
				// input action after custom button is clicked here
						custom.setText("Choosing Subject");
						/*for(int i=0;i<ts.getNumberOfAudioButtons();i++){
							Button b;
							b = new Button();
							int finalI=i;
							b.setOnAction(ea->{
								sentence.add(pagination.getCurrentPageIndex(),ts.customWords.get(pagination.getCurrentPageIndex())
                                        .get(finalI));
							});
						}*/

					/*	for (int i = 0; i < ts.getNumberOfAudioSets(); i++) {
							for(int j =0; j<ts.getNumberOfAudioButtons();i++) {
								// change the all the buttons to the subject array
							}
						}*/


					// if there are verbs in the list then
				if(!(ts.customWords.get(1).isEmpty()==true)) {
					custom.setOnAction(ev -> {
						custom.setText("Choosing verb");
						/*for (int i = 0; i < ts.numberOfAudioButtons; i++) {
							buttons[i].setText(ts.customWords.get(1).get(i).toString());
						}
						if (!(ts.customWords.get(3).isEmpty() == true)) {
							custom.setOnAction(eve -> {
										custom.setText("Choosing subject");
										for (int i = 0; i < ts.numberOfAudioButtons; i++) {
											buttons[i].setText(ts.customWords.get(2).get(i).toString());
										}


									}
							);
						}*/
					});
				}
					});

					//PLAY BUTTON-------------------------------------------------------
					Button play;
					String titlePlay;
					titlePlay = "Play";
					//create new instance of button called play
					play = new Button(titlePlay);
					//set width of play button
					int playWidth;
					playWidth = 80;
					//set height of play button
					int playHeight;
					playHeight = 50;
					//set size of play button
					play.setPrefSize(playWidth, playHeight);
					//add play button to flow pane
					flowPane.getChildren().add(play);
					play.setAlignment(Pos.CENTER);
					//translate play button to bottom left
					int playX_Translate;
					playX_Translate = 0;
					int playY_Translate;
					playY_Translate = 20;
					play.setTranslateX(playX_Translate);
					play.setTranslateY(playY_Translate);
					box.getChildren().add(flowPane);
					//set action of play for open TBC
					play.setOnAction(e -> {//event E should do..

						// input action after browse button is clicked here

					});
				}
				adjustedSimStageWidthHeight();
				simStage.setHeight(SIMSTAGE_HEIGHT);
				simStage.setWidth(SIMSTAGE_WIDTH);
				Scene newTBCscene = new Scene(box);
				simStage.setScene(newTBCscene);

			} catch (IOException e) {
				e.printStackTrace();
			}

		});
		newFileBtn.setPrefSize(100, 100);
		newFileBtn.setWrapText(true);
		newFileBtn.setAlignment(Pos.CENTER);

		/* Added the Open an Existing File */
		Button openExistFileBtn = new Button("Open an Existing File");
		openExistFileBtn.setOnAction(event -> {
			open();

			adjustedSimStageWidthHeight();
            simStage.setHeight(SIMSTAGE_HEIGHT);
            simStage.setWidth(SIMSTAGE_WIDTH);
			Scene newTBCscene = new Scene(box);
			simStage.setScene(newTBCscene);
		});
		openExistFileBtn.setPrefSize(100, 100);
		openExistFileBtn.setWrapText(true);
		openExistFileBtn.setAlignment(Pos.CENTER);

		/* add the two buttons on the pane */
		onStartPane.getChildren().add(newFileBtn);
		onStartPane.getChildren().add(openExistFileBtn);

		/* Creates main scene */
		scene = new Scene(onStartPane);

		// show window
		simStage.setScene(scene);
		simStage.show();

		//open(null);
	}

	private void adjustedSimStageWidthHeight() {
	    if (buttons.length > 8) {
            int multiplier = (buttons.length/4) - 1;
            SIMSTAGE_HEIGHT += 120 * multiplier;
        }
    }

    private MenuBar makeMenuBar() {
	    MenuBar menuBar = new MenuBar();
        menuBar.prefWidthProperty().bind(simStage.widthProperty());

        final Menu menuFile = new Menu("File");

        final MenuItem config = new MenuItem("Switch to Config...");
        // input action for the switch to config here

        config.setOnAction(event -> Try.newBuilder()
		        .setDefault(() -> TalkBoxApp.init(file.toPath()).showAndWait())
		        .run());

        menuFile.getItems().add(config);

        menuBar.getMenus().add(menuFile);

        return menuBar;
    }

	private void open() {
		FileChooser fileChooser = new FileChooser();
		File workingDirectory = new File(System.getProperty("user.dir"));
		fileChooser.setInitialDirectory(workingDirectory);
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("TalkBox Config File (.tbc)",
                "*.tbc"); // specifies file type
		fileChooser.getExtensionFilters().add(filter); // specifies file type

		fileChooser.setTitle("Open TalkBox File"); // specifies file prompt
		file = fileChooser.showOpenDialog(simStage); // displays file chooser window

		// adds file name to Window title
		simStage.setTitle("TalkBox Configurator — " + file.getName());

		Try.newBuilder()
				.setDefault(this::readFile)
				.setOtherwise(this::open)
				.run();

		audioFolder = new File(file.getParent().concat(AUDIO_PATH));

		buttons = new Button[ts.numberOfAudioButtons];

		Pagination pagination = new Pagination(ts.numberOfAudioSets);

		// ------------------------------- MENUBAR TEST----------------------------------------
		MenuBar mb = makeMenuBar();

		box.getChildren().addAll(mb, pagination);
		pagination.setPageFactory(this::configButtons);
		//if the custom word SUBJECT array is not empty then implement the custom and play button
		if(!(ts.customWords.get(0).isEmpty())) {
			//create new flow plane for the buttons
			FlowPane flowPane = new FlowPane();
			//to prevent "hard coding"
			int flowpane_top;
			flowpane_top = 30;
			int flowpane_right;
			flowpane_right = 20;
			int flowpane_bottom;
			flowpane_bottom = 30;
			int flowpane_left;
			flowpane_left = 20;
			flowPane.setPadding(new Insets(flowpane_top, flowpane_right, flowpane_bottom, flowpane_left));
			int flowpane_width;
			flowpane_width = 100;
			int flowpane_height;
			flowpane_height = 25;
			flowPane.setPrefSize(flowpane_width, flowpane_height);
			int flowpane_vGap;
			flowpane_vGap = 10;
			int flowpane_hGap;
			flowpane_hGap = 10;
			flowPane.setVgap(flowpane_vGap);
			flowPane.setHgap(flowpane_hGap);
			flowPane.setAlignment(Pos.BOTTOM_RIGHT);

			//custom Button
			Button custom;
			String titleCustom;
			titleCustom = "Custom";
			//create new instance of button called custom
			custom = new Button(titleCustom);
			//set width of custom button
			int customWidth;
			customWidth = 80;
			//set height of custom button
			int customHeight;
			customHeight = 50;
			//set size of custom button
			custom.setPrefSize(customWidth, customHeight);
			//add custom button to flowpane
			flowPane.getChildren().add(custom);
			custom.setAlignment(Pos.CENTER);
			//translate custom button to bottom left
			int customX_Translate;
			customX_Translate = 90;
			int customY_Translate;
			customY_Translate = -40;
			custom.setTranslateX(customX_Translate);
			custom.setTranslateY(customY_Translate);
			//string of subject,verb and object
			//this string will be what is displayed.
			String text_f;
			custom.setOnAction(e -> {//event E should do..
			/*	//Functionality of custom button
				// input action after custom button is clicked here
						custom.setText("Choosing Subject");
						ts.getNumberOfAudioSets();

						for (int i = 0; i < ts.getNumberOfAudioSets(); i++) {
							for(int j =0; j<ts.getNumberOfAudioButtons();i++) {
								// change the all the buttons to the subject array
							}
						}


					// if there are verbs in the list then
				if(!(ts.customWords.get(1).isEmpty()==true)) {
					custom.setOnAction(ev -> {
						custom.setText("Choosing verb");
						for (int i = 0; i < ts.numberOfAudioButtons; i++) {
							buttons[i].setText(ts.customWords.get(1).get(i).toString());
						}
						if (!(ts.customWords.get(3).isEmpty() == true)) {
							custom.setOnAction(eve -> {
										custom.setText("Choosing subject");
										for (int i = 0; i < ts.numberOfAudioButtons; i++) {
											buttons[i].setText(ts.customWords.get(2).get(i).toString());
										}


									}
							);
						}
					});
				}*/
			});

			//PLAY BUTTON-------------------------------------------------------
			Button play;
			String titlePlay;
			titlePlay = "Play";
			//create new instance of button called play
			play = new Button(titlePlay);
			//set width of play button
			int playWidth;
			playWidth = 80;
			//set height of play button
			int playHeight;
			playHeight = 50;
			//set size of play button
			play.setPrefSize(playWidth, playHeight);
			//add play button to flow pane
			flowPane.getChildren().add(play);
			play.setAlignment(Pos.CENTER);
			//translate play button to bottom left
			int playX_Translate;
			playX_Translate = 0;
			int playY_Translate;
			playY_Translate = 20;
			play.setTranslateX(playX_Translate);
			play.setTranslateY(playY_Translate);
			box.getChildren().add(flowPane);
			//set action of play for open TBC
			play.setOnAction(e -> {//event E should do..

				// input action after browse button is clicked here

			});
		}

	}

	private FlowPane configButtons(int page) {
		/*FlowPane flowPane = new FlowPane();
		flowPane.setPadding(new Insets(30, 20, 30, 20));
		flowPane.setPrefSize(100,25);
		flowPane.setVgap(10);
		flowPane.setHgap(10);
		flowPane.setAlignment(Pos.CENTER);*/
		//create new flow plane for the buttons
		FlowPane flowPane = new FlowPane();
		//to prevent "hard coding"
		int flowpane_top;
		flowpane_top=30;
		int flowpane_right;
		flowpane_right=20;
		int flowpane_bottom;
		flowpane_bottom=30;
		int flowpane_left;
		flowpane_left=20;
		flowPane.setPadding(new Insets(flowpane_top, flowpane_right, flowpane_bottom, flowpane_left));
		int flowpane_width;
		flowpane_width=100;
		int flowpane_height;
		flowpane_height=25;
		flowPane.setPrefSize(flowpane_width,flowpane_height);
		int flowpane_vGap;
		flowpane_vGap=10;
		int flowpane_hGap;
		flowpane_hGap=10;
		flowPane.setVgap(flowpane_vGap);
		flowPane.setHgap(flowpane_hGap);
		flowPane.setAlignment(Pos.CENTER);

		// make the buttons
		for (int i = 0; i < ts.numberOfAudioButtons; i++) {
			String caption = (!ts.database.get(page).get(i).isNull().get())
					? ts.database.get(page).get(i).getValue()
					: "Empty";

			buttons[i] = new Button(caption);
			buttons[i].setPrefSize(100, 100);
			flowPane.getChildren().add(buttons[i]);
		}
		// on button press
		IntStream.range(0, ts.getNumberOfAudioButtons()).forEach(i -> buttons[i].setOnAction(event2 -> {

			//File soundFile = new File(getFullPath(ts.audioList[page][i].getKey()));
//			Try.newBuilder().setDefault(() -> {
//				Media media = new Media(soundFile.toURI().toString());
//				MediaPlayer player = new MediaPlayer(media);
//				player.play();
//			}).run();
		}));

		/*Testing custom and play buttons: TO BE REMOVED
				//custom Button
		Button custom;
		String titleCustom;
		titleCustom="Custom";
		//create new instance of button called custom
		custom = new Button (titleCustom);
		//set width of custom button
		int customWidth;
		customWidth=80;
		//set height of custom button
		int customHeight;
		customHeight=50;
		//set size of custom button
		custom.setPrefSize(customWidth,customHeight);
		flowPane.getChildren().add(custom);
		custom.setAlignment(Pos.CENTER);
		//translate custom button to bottom left
		int customX_Translate;
		customX_Translate=-160;
		int customY_Translate;
		customY_Translate=180;
		custom.setTranslateX(customX_Translate);
		custom.setTranslateY(customY_Translate);
		//PLAY BUTTON-------------------------------------------------------
		Button play;
		String titlePlay;
		titlePlay="Play";
		//create new instance of button called play
		play = new Button (titlePlay);
		//set width of play button
		int playWidth;
		playWidth=80;
		//set height of play button
		int playHeight;
		playHeight=50;
		//set size of play button
		play.setPrefSize(playWidth,playHeight);
		flowPane.getChildren().add(play);
		play.setAlignment(Pos.CENTER);
		//translate play button to bottom left
		int playX_Translate;
		playX_Translate=50;
		int playY_Translate;
		playY_Translate=180;
		play.setTranslateX(playX_Translate);
		play.setTranslateY(playY_Translate);
		 */


		return flowPane;
	}

	private void createNewTBC() throws IOException {
        openWizardDialog();

        if (!canContinue) {
        	return;
		}

		File testTBC = Paths.get(savedDir, savedName + ".tbc").toFile();
		FileOutputStream fos = new FileOutputStream(testTBC);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		TalkBoxData ts = new TalkBoxData();
		ts.numberOfAudioButtons = numAudioBtns;
		ts.numberOfAudioSets = numAudioSets;


		ts.database = new ArrayList<>();

		for (int i = 0; i < ts.numberOfAudioSets; i++) {
			List<AudioPair> list = new ArrayList<>();
			for (int j = 0; j < ts.getNumberOfAudioButtons(); j++) {
				list.add(new AudioPair());
			}
			ts.database.add(list);
		}

		List<String> list = new ArrayList<>();

		List<String> list1 = new ArrayList<>();

		List<String> list2 = new ArrayList<>();

		List<List<String>> master = new ArrayList<>();
		master.add(list);
		master.add(list1);
		master.add(list2);

		ts.customWords = master;

		oos.writeObject(ts);
		oos.flush();
		oos.close();

		file = testTBC;
		this.ts = ts;
	}

	private void openWizardDialog() {
	    // opens up a pop-up dialog with a wizard-like interface using a stage. Uses a Vbox (children added vertically),
        // multiple flow panes are added to it and then the Vbox is added to the scene
	    // creates a dialog after the create new talkbox is pressed
		// uses a canContinue boolean variable to decide whether the program is ready to move on to the next stage
        // uses the errorLbl to display an error message for missing info
        // add more instructions before the specific pane

        Region newLine = addNewLinePane();

        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(simStage);
        dialog.setTitle("New TalkBox");
        dialog.setResizable(false);
        dialog.setHeight(SIMSTAGE_HEIGHT);
        dialog.getIcons().add(new Image(TalkBoxApp.class.getResourceAsStream("/Resources/icon2.png")));

        // 0th pane
		FlowPane dialogPane0 = new FlowPane();
		dialogPane0.setPrefSize(500,25);
		dialogPane0.setPadding(new Insets(30, 20, 0, 35));
		dialogPane0.setAlignment(Pos.BOTTOM_LEFT);

        // first pane
        FlowPane dialogPane1 = new FlowPane();
        dialogPane1.setPrefSize(500,40);
        dialogPane1.setPadding(new Insets(0, 20, 0, 35));
		dialogPane1.setHgap(10);
        dialogPane1.setAlignment(Pos.CENTER_LEFT);

        // second pane
        FlowPane dialogPane2 = new FlowPane();
        dialogPane2.setPrefSize(500,50);
        dialogPane2.setPadding(new Insets(0, 20, 0, 35));
		dialogPane2.setHgap(10);
		dialogPane2.setAlignment(Pos.CENTER_LEFT);

		// Fourth pane
		FlowPane dialogPane3 = new FlowPane();
		dialogPane3.setPrefSize(500,50);
		dialogPane3.setPadding(new Insets(0, 35, 0, 35));
		dialogPane3.setHgap(20);
		dialogPane3.setAlignment(Pos.CENTER_LEFT);

		// Fifth pane
		FlowPane dialogPane4 = new FlowPane();
		dialogPane4.setPrefSize(500,50);
		dialogPane4.setPadding(new Insets(25, 20, 20, 35));
		dialogPane4.setHgap(10);
		dialogPane4.setAlignment(Pos.BOTTOM_RIGHT);


		// The following "nodes" are to be added to the 0th pane
		Label enterNameLbl = new Label();
		enterNameLbl.setText("Creating a New TalkBox\nEnter a TalkBox name.");
		enterNameLbl.setStyle("-fx-font-weight: bold;");

        Label errorLbl = new Label();
        errorLbl.setTextFill(Color.RED);


        // The following "nodes" are to be added to the first pane
        Label nameLbl = new Label();
        nameLbl.setText("TextBox file name (file type: .tbc): ");

		TextField nameTxtField = new TextField();
		nameTxtField.setPrefWidth(265);

		ValidationSupport nameValidation = new ValidationSupport();
		nameValidation.registerValidator(nameTxtField, Validator.createEmptyValidator("Name is required"));
		nameValidation.setErrorDecorationEnabled(true);


        // The following "nodes are to be added to the second pane"
		Label locationLbl = new Label();
		locationLbl.setText("Location:");

		TextField locationTxtField = new TextField();
		locationTxtField.setEditable(false);
		locationTxtField.setPrefWidth(300);
		locationTxtField.setMouseTransparent(true);
		locationTxtField.setText(System.getProperty("user.dir"));

		Button browseBtn = new Button();
		browseBtn.setPrefSize(MINOR_BUTTON_WIDTH, MINOR_BUTTON_HEIGHT);
		browseBtn.setText("Browse...");
		browseBtn.setWrapText(true);
		browseBtn.setAlignment(Pos.CENTER);

		browseBtn.setOnAction(event -> {
			// input action after browse button is clicked here
			DirectoryChooser dir = new DirectoryChooser();
			dir.setTitle("Browse for a folder");
			File defaultDirectory = new File(System.getProperty("user.dir"));
			dir.setInitialDirectory(defaultDirectory);
			file = dir.showDialog(dialog);

			locationTxtField.setText(file.getPath());
		});


		// The following "nodes are to be added to the third pane"
		Label numBtnsLbl = new Label();
		numBtnsLbl.setText("Number of Buttons: ");

		Label numSetsLbl = new Label();
		numSetsLbl.setText("Number of Swap buttons: ");

		TextField numBtnsTxtField = new TextField();
		TextField numSetsTxtField = new TextField();
		numBtnsTxtField.setPrefSize(60, MINOR_BUTTON_HEIGHT);
		numSetsTxtField.setPrefSize(60, MINOR_BUTTON_HEIGHT);

		ValidationSupport numBtnsValidation = new ValidationSupport();
		numBtnsValidation.registerValidator(numBtnsTxtField, Validator.createEmptyValidator("Enter a number"));
		numBtnsValidation.setErrorDecorationEnabled(true);

		ValidationSupport numSetsValidation = new ValidationSupport();
		numSetsValidation.registerValidator(numSetsTxtField, Validator.createEmptyValidator("Enter a number"));
		numSetsValidation.setErrorDecorationEnabled(true);


		// The following "nodes are to be added to the fourth pane"
        Button finishBtn = new Button();
        finishBtn.setPrefSize(MINOR_BUTTON_WIDTH, MINOR_BUTTON_HEIGHT);
        finishBtn.setText("Finish");
        finishBtn.setWrapText(true);
        finishBtn.setAlignment(Pos.CENTER);

        finishBtn.setOnAction(event -> {
        	// input action after finish button is clicked here
			try {
				int numBtns = Integer.parseInt(numBtnsTxtField.getText().trim());
				int numSets = Integer.parseInt(numSetsTxtField.getText().trim());
				String selectedDir = locationTxtField.getText().trim();
				String talkBoxName = nameTxtField.getText().trim();
				if (!isValidFilePath(selectedDir)) {
					throw new Exception();
				}
                if (nameTxtField.getText().trim().isEmpty()) {
					nameValidation.initInitialDecoration();
                    throw new Exception();
                }

                numAudioBtns = numBtns;
                numAudioSets = numSets;
                savedDir = selectedDir;
                savedName = talkBoxName;
				if (canMkFileAndCanMkAudioDir()) {
					canContinue = true;
					dialog.close();
				}
			}
			catch (Exception e) {
                e.printStackTrace();
			}
		});

        Button cancelBtn = new Button();
        cancelBtn.setPrefSize(MINOR_BUTTON_WIDTH, MINOR_BUTTON_HEIGHT);
        cancelBtn.setText("Cancel");
        cancelBtn.setWrapText(true);
        cancelBtn.setAlignment(Pos.CENTER);
        cancelBtn.setOnAction(event -> {
        	// input action after cancel button is clicked here
        	dialog.close();
		});


		// add all the panes to a Vbox
        VBox dialogVbox = new VBox(20);
        dialogPane0.getChildren().add(enterNameLbl);
        dialogPane1.getChildren().addAll(nameLbl, nameTxtField);
        dialogPane2.getChildren().addAll(locationLbl, locationTxtField, browseBtn);
        dialogPane3.getChildren().addAll(numBtnsLbl,numBtnsTxtField, newLine, numSetsLbl,numSetsTxtField);
        dialogPane4.getChildren().addAll(finishBtn,cancelBtn);

		// add the Vbox to the dialog scene and show it
        dialogVbox.getChildren().addAll(dialogPane0,dialogPane1,dialogPane2,dialogPane3,dialogPane4);
        Scene dialogScene = new Scene(dialogVbox);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private Region addNewLinePane() {
        Region r = new Region();
        r.setPrefSize(SIMSTAGE_WIDTH, 10);
        return r;
    }

	private void readFile() throws Exception {
		FileInputStream fis;
		ObjectInputStream oin;

		fis = new FileInputStream(file);
		oin = new ObjectInputStream(fis);

		ts = (TalkBoxData) oin.readObject();
	}

	private String getFullPath(String s) {
		return audioFolder.getPath().concat('/' + s);
	}

	private boolean isValidFilePath(String path) {
		File currentPath = new File(path);
		return currentPath.isDirectory();
	}

	// make many of the Nodes to be field (global) variables to be accessed here in this method for example
	private boolean isValidFileName (String name){
		if (name == null) {
			return false;
		}
		return true;
	}

	// checks if it could make the file (make sure there isn't another copy in the directory) and then
	// checks if it could make the audio directory
	private boolean canMkFileAndCanMkAudioDir() {
		if (canMkFile()) {
			return canMkAudioDirectory();
		}
		return false;
	}

	// checks if we could make the .tbc file
	private boolean canMkFile() {
		File tbcFile = new File(savedDir, savedName + ".tbc");
		ButtonType yesBtn = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
		ButtonType noBtn = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

		// only goes through the if statement if the file exists in the directory
		if (tbcFile.exists()) {
			Alert overwriteAlert = new Alert(Alert.AlertType.WARNING,
					savedName + ".tbc already exists in this folder.\nDo you want to replace it?",
					yesBtn,
					noBtn);
			overwriteAlert.setTitle("Confirm Overwrite " + savedName + ".tbc");
			Optional<ButtonType> result = overwriteAlert.showAndWait();

			if (result.get() == noBtn) {
				return false;
			}
		}
		return true;
	}

	// checks if we could make the audio folder
	private boolean canMkAudioDirectory() {
		File mkAudioDir = new File(savedDir + "/Audio");
		ButtonType yesBtn = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
		ButtonType noBtn = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

		// only goes through the if statement if the audio directory exists in savedDir
		if (!mkAudioDir.mkdir()) {
			Alert overwriteAlert = new Alert(Alert.AlertType.WARNING,
					"Audio file already exists in this folder.\nDo you want to replace it?",
					yesBtn,
					noBtn);
			overwriteAlert.setTitle("Confirm Overwrite Audio File");
			Optional<ButtonType> result = overwriteAlert.showAndWait();

			if (result.get() == noBtn) {
				return false;
			}
		}
		return true;
	}
}
