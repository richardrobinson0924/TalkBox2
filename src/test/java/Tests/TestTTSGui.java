
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
    The first test class to test the functionality of the TTS class
    All the Tests here are ordered and performed in ascending order in lexicographic order
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestTTSGui extends ApplicationTest {

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
        writeText("foobarTTS");
        Assertions.assertThat(newTextField).hasText("foobarTTS");
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
        for (int i=0; i<7; i++) {
            clickOn();
        }
        assertThat(newButtonSpinner.getValue(), is(equalTo(8)));
    }

    @Test
    // decrement buttons (IDB = increment/decrement buttons test)
    public void testC_IDB2() {
        Spinner <Integer> newButtonSpinner = lookfor(CreateConfigWizardTestingNodes.NumbersPane.getIdNumButtonsSpinner());
        moveBy(0, 10);
        for (int i=0; i<2; i++) {
            clickOn();
        }
        assertThat(newButtonSpinner.getValue(), is(equalTo(6)));
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
    // Set Text-To-Speech test1
    public void testH_TTS0() {
        // check if the buttons exists
        FlowPane newFlowPane = lookfor(ConfigStageTestingNodes.BUTTONS_FLOWPANE);
        //goes to the first button and clicks it
        clickOn(LabeledMatchers.hasText("Empty"));
    }

    @Test//REPEAT

    public void testH_TTS1() {
        // click on the record radio button and then the tts radio button
        RadioButton newRadioButton = lookfor(AddWizardTestingNodes.IntroPaneNodes.RECORD_BUTTON);
        clickOn(newRadioButton);
        newRadioButton = lookfor(AddWizardTestingNodes.IntroPaneNodes.WAV_BUTTON);
        clickOn(newRadioButton);
        newRadioButton = lookfor(AddWizardTestingNodes.IntroPaneNodes.TTS_BUTTON);
        clickOn(newRadioButton);
    }

    @Test
    // Set Text-To-Speech test2
    public void testI_TTS2() {
        clickOn(LabeledMatchers.hasText("Next"));
        TextField newTextField = lookfor(AddWizardTestingNodes.TTSPaneNodes.TTSPHRASE_TEXTFIELD);
        Button newButton = lookfor(AddWizardTestingNodes.TTSPaneNodes.TTSPLAY_BUTTON);
        moveTo(newTextField);
        clickOn();
        writeText("Hello World");
        clickOn(newButton);
        // wait 5 seconds
        sleep(5,TimeUnit.SECONDS);
    }

    @Test
    // Set Text-To-Speech test3 and play it in the configurator
    public void testJ_TTS3() {
        clickOn(LabeledMatchers.hasText("Next"));
        TextField newTextField = lookfor(AddWizardTestingNodes.NamePaneNodes.AUDIONAME_TEXTFIELD);
        clickOn(newTextField);
        writeText("Hello World");
        clickOn(LabeledMatchers.hasText("Previous"));
        clickOn(LabeledMatchers.hasText("Next"));
        clickOn(LabeledMatchers.hasText("Next"));
        moveBy(0, 20);
        clickOn();
    }

    @Test
    // Play Audio Button 1
    public void testK_PAB1() {
        clickOn(LabeledMatchers.hasText("Hello World"));
        sleep(3, TimeUnit.SECONDS);
    }

    @Test
    // Save TTS
    public void testL_STTS() {
        clickOn(LabeledMatchers.hasText("File"));
        clickOn(LabeledMatchers.hasText("Save"));
    }

    //TEST V2 Start HOW TO MAKE THIS RUN AFTER L
    @Test//NEW
    public void testM_TTS0() {
        // check if the buttons exists
        FlowPane newFlowPane = lookfor(ConfigStageTestingNodes.BUTTONS_FLOWPANE);
        moveBy(150,40);
        clickOn();

        try {
            TimeUnit.SECONDS.sleep(1);
        }
        catch(Exception e){

        }
    }

    @Test//REPEAT
    public void testM_TTS1() {
        // click on the record radio button and then the tts radio button
        RadioButton newRadioButton = lookfor(AddWizardTestingNodes.IntroPaneNodes.TTS_BUTTON);
        clickOn(newRadioButton);
    }
    @Test
    // Set Text-To-Speech test2
    // test the male 1 voice
    public void testM_TTS2() {
        clickOn(LabeledMatchers.hasText("Next"));
        TextField newTextField = lookfor(AddWizardTestingNodes.TTSPaneNodes.TTSPHRASE_TEXTFIELD);
        Button newButton = lookfor(AddWizardTestingNodes.TTSPaneNodes.TTSPLAY_BUTTON);
        ComboBox newComboBox = lookfor(AddWizardTestingNodes.TTSPaneNodes.TTSVOICE_COMBOBOX);
        clickOn(newComboBox);
        clickOn(LabeledMatchers.hasText("Male 2"));
        moveTo(newTextField);
        clickOn();
        writeText("Hello World 2");
        clickOn(newButton);
        // wait 3 seconds
        sleep(3,TimeUnit.SECONDS);
    }

    @Test
    // test male voice 2
    public void testM_TTS3() {
        Button newButton = lookfor(AddWizardTestingNodes.TTSPaneNodes.TTSPLAY_BUTTON);
        ComboBox newComboBox = lookfor(AddWizardTestingNodes.TTSPaneNodes.TTSVOICE_COMBOBOX);
        clickOn(newComboBox);
        clickOn(LabeledMatchers.hasText("Male 3"));
        clickOn(newButton);
        sleep(3,TimeUnit.SECONDS);
    }

    @Test
    // test male voice 3
    public void testM_TTS4() {
        Button newButton = lookfor(AddWizardTestingNodes.TTSPaneNodes.TTSPLAY_BUTTON);
        ComboBox newComboBox = lookfor(AddWizardTestingNodes.TTSPaneNodes.TTSVOICE_COMBOBOX);
        clickOn(newComboBox);
        clickOn(LabeledMatchers.hasText("Female 1"));
        clickOn(newButton);
    }

    @Test
    public void testM_TTS5() {
        Button newButton = lookfor(AddWizardTestingNodes.TTSPaneNodes.TTSPLAY_BUTTON);
        ComboBox newComboBox = lookfor(AddWizardTestingNodes.TTSPaneNodes.TTSVOICE_COMBOBOX);
        clickOn(newComboBox);
        clickOn(LabeledMatchers.hasText("Female 2"));
        clickOn(newButton);
    }

    @Test
    public void testM_TTS6() {
        Button newButton = lookfor(AddWizardTestingNodes.TTSPaneNodes.TTSPLAY_BUTTON);
        ComboBox newComboBox = lookfor(AddWizardTestingNodes.TTSPaneNodes.TTSVOICE_COMBOBOX);
        clickOn(newComboBox);
        clickOn(LabeledMatchers.hasText("Female 3"));
        clickOn(newButton);
        sleep(3,TimeUnit.SECONDS);
    }

    @Test
    public void testM_TTS7() {
        clickOn(LabeledMatchers.hasText("Next"));
        TextField newTextField = lookfor(AddWizardTestingNodes.NamePaneNodes.AUDIONAME_TEXTFIELD);
        clickOn(newTextField);
        writeText("Hello World 2");
        clickOn(LabeledMatchers.hasText("Next"));
        moveBy(0, 20);
        clickOn();

        try {
            TimeUnit.SECONDS.sleep(2);
        }
        catch(Exception e){

        }
    }

    @Test
    // Play Audio Button 1
    public void testM_TTS8() {//CHANGE EVERY TEST
        clickOn(LabeledMatchers.hasText("Hello World 2"));
    }

    @Test
    // Save TTS
    public void testM_TTS9() {
        clickOn(LabeledMatchers.hasText("File"));
        clickOn(LabeledMatchers.hasText("Save"));
    }
    //TEST V2 END

//    @Test
//    // Perform Undo Function and save
//    public void testM_PUF1() {

//	    clickOn(LabeledMatchers.hasText("Edit"));
//        clickOn(LabeledMatchers.hasText("Undo"));
//
//        push(KeyCode.CONTROL, KeyCode.S);
//    }

    @Test
    // Set Custom Function1
    public void testN_SCF1() {
        clickOn(LabeledMatchers.hasText("Edit"));
        clickOn(LabeledMatchers.hasText("Custom Phrase List"));
        TextField newTextField = lookfor(CustomDataViewTestingNodes.ADD_TEXTFIELD);
        Assertions.assertThat(newTextField).hasText("");
    }

    @Test
    // Set Custom Function2
    public void testN_SCF2() {
        TextField newTextField = lookfor(CustomDataViewTestingNodes.ADD_TEXTFIELD);
        clickOn(newTextField);
        writeText("Jump");
        press(KeyCode.ENTER);
        release(new KeyCode[]{});
        writeText("Slip");
        press(KeyCode.ENTER);
        release(new KeyCode[]{});
        writeText("Fall");
        press(KeyCode.ENTER);
        Assertions.assertThat(newTextField).hasText("");
    }

    @Test
    //
    public void testN_SCF3() {
        clickOn(LabeledMatchers.hasText("Subjects"));
        TextField newTextField = lookfor(CustomDataViewTestingNodes.ADD_TEXTFIELD);
        clickOn(newTextField);
        writeText("I");
        press(KeyCode.ENTER);
        release(new KeyCode[]{});
        writeText("We");
        press(KeyCode.ENTER);
        release(new KeyCode[]{});
        writeText("You");
        press(KeyCode.ENTER);
        Assertions.assertThat(newTextField).hasText("");
    }

    @Test
    //
    public void testN_SCF4(){
        clickOn(LabeledMatchers.hasText("Objects"));
        TextField newTextField = lookfor(CustomDataViewTestingNodes.ADD_TEXTFIELD);
        clickOn(newTextField);
        writeText("Banana");
        press(KeyCode.ENTER);
        release(new KeyCode[]{});
        writeText("Cliff");
        press(KeyCode.ENTER);
        release(new KeyCode[]{});
        writeText("Stairs");
        press(KeyCode.ENTER);
        Assertions.assertThat(newTextField).hasText("");
    }

    @Test
    // open Simulator Test1
    public void testO_OST1() {
        push(KeyCode.ALT, KeyCode.F4);
        release(new KeyCode[]{});
        push(KeyCode.CONTROL, KeyCode.S);
        release(new KeyCode[]{});
        clickOn(LabeledMatchers.hasText("View"));
        clickOn(LabeledMatchers.hasText("Open in Simulator..."));
    }

    @Test
    // Create Sentence and play it
    public void testP_CSP1() {
        Label newLabel = lookfor(SimulatorStageTestingNodes.CUSTOM_LABEL);
        clickOn(LabeledMatchers.hasText("Custom Phrase"));
        clickOn(LabeledMatchers.hasText("Jump"));
        clickOn(LabeledMatchers.hasText("2"));
        sleep(1, TimeUnit.SECONDS);
        clickOn(LabeledMatchers.hasText("I"));
        clickOn(LabeledMatchers.hasText("3"));
        sleep(1, TimeUnit.SECONDS);
        clickOn(LabeledMatchers.hasText("Cliff"));
        clickOn(LabeledMatchers.hasText("4"));
        sleep(1, TimeUnit.SECONDS);
        clickOn(LabeledMatchers.hasText("Past"));
        clickOn(LabeledMatchers.hasText("Play"));
        sleep(3, TimeUnit.SECONDS);
        Assertions.assertThat(newLabel).hasText("I Jumped Cliff.");
    }

    @Test
    // Save TTS
    public void testQ_STTS() {
        push(KeyCode.ALT, KeyCode.F4);
        release(new KeyCode[]{});
        push(KeyCode.CONTROL, KeyCode.S);
    }

}
