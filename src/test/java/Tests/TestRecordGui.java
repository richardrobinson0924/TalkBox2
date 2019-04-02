package Tests;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.ListViewMatchers;
import talkboxnew.Main;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/*
    The first test class to test the functionality of the Recording function and the save button
    All the Tests here are ordered and performed in ascending order in lexicographic order
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestRecordGui extends ApplicationTest {
    // maybe change the structuring of the nodes to be a new class?
    // CreateConfigWizardTestingNodes.NamePane().Node; This should return a string of the node
    // CreateConfigWizardTestingNodes.FilePane().Node;
    // SplashStageTestingNodes.Node

    private <T extends Node> T lookfor(final String node) {
        return (T) lookup("#" + node).queryAll().iterator().next();
    }

    private void writeText(String text){
        new FxRobot().write(text);
    }

    @BeforeAll
    public static void launch() throws Exception {
        ApplicationTest.launch(Main.class);
    }

    @After
    public void afterEachTest() throws TimeoutException {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    @Test
    // testAinitialAppearance test1
    public void testA_IAT1() {
        Button openButton = lookfor(SplashStageTestingNodes.OPEN_BUTTON);
        Assertions.assertThat(openButton).hasText("Open Existing Directory...");
    }

    @Test
    // initialAppearance2 test2
    public void testA_IAT2() {
        Button newButton = lookfor(SplashStageTestingNodes.NEW_BUTTON);
        Assertions.assertThat(newButton).hasText(" New TalkBox Directory...");
    }

    @Test
    // initialAppearance3 test3
    public void testA_IAT3() {
        ListView newView = lookfor(SplashStageTestingNodes.LIST);
        if (newView.getItems().isEmpty()){
            assertThat(newView, ListViewMatchers.isEmpty());
        }
    }

    @Test
    // setName test 1
    public void testB_SNT1() {
        Button newButton = lookfor(SplashStageTestingNodes.NEW_BUTTON);
        clickOn(newButton);
        TextField newTextField = lookfor(CreateConfigWizardTestingNodes.NamePane.getIdTextfield());
        clickOn(newTextField);
        writeText("foobarRecording");
        Assertions.assertThat(newTextField).hasText("foobarRecording");
    }

    // successfully moved onto the next page
    // move im on the numbers pane
    // click("") brings it back to the middle of the window to be clicked
    // clickOn() if I just wanna click where the cursor is currently on
    // moveBy(x,y) to move x pixels right(+) or left(-) and y pixels up(-) or down(+)

    @Test
    // increment buttons
    public void testC_IDB1() {
        clickOn(LabeledMatchers.hasText("Next"));
        Spinner <Integer> newButtonSpinner = lookfor(CreateConfigWizardTestingNodes.NumbersPane.getIdNumButtonsSpinner());
        moveTo(newButtonSpinner);
        moveBy(70,-5);
        for (int i=0; i<5; i++) {
            clickOn();
        }
        assertThat(newButtonSpinner.getValue(), is(equalTo(6)));
    }

    @Test
    // decrement buttons (IDB = increment/decrement buttons test)
    public void testC_IDB2() {
        Spinner <Integer> newButtonSpinner = lookfor(CreateConfigWizardTestingNodes.NumbersPane.getIdNumButtonsSpinner());
        moveBy(0, 10);
        for (int i=0; i<2; i++) {
            clickOn();
        }
        assertThat(newButtonSpinner.getValue(), is(equalTo(4)));
    }

    // The next tests aren't working...  ---> not in lexicographic order

    @Test
    // increment number of sets (increment/decrement sets)
    public void testD_IDS1() {
        Spinner <Integer> newSetSpinner = lookfor(CreateConfigWizardTestingNodes.NumbersPane.getIdNumSetsSpinner());
        moveBy(0,23);
        for (int i=0; i<6; i++) {
            clickOn();
        }
        assertThat(newSetSpinner.getValue(), is(equalTo(7)));
    }
    //
    @Test
    // decrement number of sets
    public void testD_IDS2() {
        Spinner <Integer> newSetSpinner = lookfor(CreateConfigWizardTestingNodes.NumbersPane.getIdNumSetsSpinner());
        moveBy(0,10);
        for (int i=0; i<3; i++) {
            clickOn();
        }
        assertThat(newSetSpinner.getValue(), is(equalTo(4)));
    }

    @Test
    // Go Back Forward test1
    public void testE_GBF1() {
        clickOn(LabeledMatchers.hasText("Previous"));
        clickOn(LabeledMatchers.hasText("Next"));
        clickOn(LabeledMatchers.hasText("Next"));
        Button newButton = lookfor(CreateConfigWizardTestingNodes.FilePane.getIdFileTextfield());
        Assertions.assertThat(newButton).hasText("Choose");
    }

    @Test
    // Checking Storing Directory
    public void testF_CSD1() {
        Button newButton = lookfor(CreateConfigWizardTestingNodes.FilePane.getIdFileTextfield());
        clickOn(newButton);
        push(KeyCode.ENTER);
        Assertions.assertThat(newButton).hasText("Choose");
    }

    @Test
    // Check File Exists (and confirms the overwriting of a file)
    public void testG_CFE1() {
        moveBy(-30,45);
        clickOn();
        // no overwriting file here...
        try {
            FlowPane newFlowPane = lookfor(ConfigStageTestingNodes.BUTTONS_FLOWPANE);
        }
        // confirms the overwriting of file
        catch (Exception e){
            clickOn();
        }
    }

    @Test
    // Set Recording Audio Button test0
    public void testH_RAB0() {
        // check if the buttons exists
        FlowPane newFlowPane = lookfor(ConfigStageTestingNodes.BUTTONS_FLOWPANE);
        //goes to the first button and clicks it
        clickOn(LabeledMatchers.hasText("Empty"));
    }

    @Test
    // set recording audio button test1
    public void testH_RAB1() {
        // click on the record radio button and then the tts radio button
        RadioButton newRadioButton = lookfor(AddWizardTestingNodes.IntroPaneNodes.WAV_BUTTON);
        clickOn(newRadioButton);
        newRadioButton = lookfor(AddWizardTestingNodes.IntroPaneNodes.TTS_BUTTON);
        clickOn(newRadioButton);
        newRadioButton = lookfor(AddWizardTestingNodes.IntroPaneNodes.RECORD_BUTTON);
        clickOn(newRadioButton);
        Assertions.assertThat(newRadioButton).hasText("Record Audio");
    }

    @Test
    //set recording audio button test2 and play it
    public void testH_RAB2() {
        clickOn(LabeledMatchers.hasText("Next"));
        ToggleButton newToggleButton = lookfor(AddWizardTestingNodes.RecordingPaneNodes.RECORDING_TOGGLE);
        Button newButton = lookfor(AddWizardTestingNodes.RecordingPaneNodes.PLAY_BUTTON);
        clickOn(newToggleButton);
        // records for 5 seconds
        sleep(5, TimeUnit.SECONDS);
        clickOn(newToggleButton);
        clickOn(newButton);
        sleep(5, TimeUnit.SECONDS);
        Assertions.assertThat(newButton).hasText("Play");
    }

    @Test
    // Recording Audio Button -- naming
    public void testH_RAB3() {
        clickOn(LabeledMatchers.hasText("Next"));
        TextField newTextField = lookfor(AddWizardTestingNodes.NamePaneNodes.AUDIONAME_TEXTFIELD);
        clickOn(newTextField);
        writeText("Hello World");
        Assertions.assertThat(newTextField).hasText("Hello World");
    }

    @Test
    // Recording Audio Button -- going to end
    public void testH_RAB4(){
        clickOn("Next");
        moveBy(0,30);
        clickOn();
    }

    @Test
    // click recording button
    public void testI_CRB1() {
        clickOn(LabeledMatchers.hasText("Hello World"));
        sleep(6, TimeUnit.SECONDS);
    }

    @Test
    // Save Audio Recording
    public void testJ_SAR1() {
        clickOn(LabeledMatchers.hasText("File"));
        clickOn(LabeledMatchers.hasText("Save"));

    }
}
