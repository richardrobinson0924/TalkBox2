package Tests;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import org.junit.After;
import org.junit.Assert;
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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCustomGui extends ApplicationTest {

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
    // Set Name Foobar 1
    public void testB_SNF1() {
        Button newButton = lookfor(SplashStageTestingNodes.NEW_BUTTON);
        clickOn(newButton);
        TextField newTextField = lookfor(CreateConfigWizardTestingNodes.NamePane.getIdTextfield());
        clickOn(newTextField);
        writeText("foobarCustom");
        Assertions.assertThat(newTextField).hasText("foobarCustom");
    }

    @Test
    // set "Enablers" On (previous button)
    public void testB_SON1() {
        try {
            clickOn(LabeledMatchers.hasText("Next"));
            CheckBox newCB1 = lookfor(CreateConfigWizardTestingNodes.FeaturesPane.getIdBackCheckBox());
            clickOn(newCB1);
            clickOn(newCB1);
            boolean result = newCB1.isSelected();
            Assert.assertTrue("Expected True", result);
        }
        catch(Exception e) {
            System.exit(1);
        }
    }

    @Test
    // set "Enablers" On (custom button)
    public void testB_SON2() {
        CheckBox newCB2 = lookfor(CreateConfigWizardTestingNodes.FeaturesPane.getIdCustomCheckBox());
        clickOn(newCB2);
        clickOn(newCB2);
        boolean result = newCB2.isSelected();
        Assert.assertTrue("Expected True",result);
    }

    @Test
    // increment buttons
    public void testC_IDB1() {
        clickOn(LabeledMatchers.hasText("Next"));
        Spinner<Integer> newButtonSpinner = lookfor(CreateConfigWizardTestingNodes.NumbersPane.getIdNumButtonsSpinner());
        moveTo(newButtonSpinner);
        moveBy(70,-5);
        for (int i=0; i<6; i++) {
            clickOn();
        }
        assertThat(newButtonSpinner.getValue(), is(equalTo(7)));
    }

    @Test
    // decrement buttons (IDB = increment/decrement buttons test)
    public void testC_IDB2() {
        Spinner <Integer> newButtonSpinner = lookfor(CreateConfigWizardTestingNodes.NumbersPane.getIdNumButtonsSpinner());
        moveBy(0, 10);
        for (int i=0; i<3; i++) {
            clickOn();
        }
        assertThat(newButtonSpinner.getValue(), is(equalTo(4)));
    }

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
    // Set Custom Function1
    public void testH_SCF1() {
        clickOn(LabeledMatchers.hasText("Edit"));
        clickOn(LabeledMatchers.hasText("Custom Phrase List"));
        TextField newTextField = lookfor(CustomDataViewTestingNodes.ADD_TEXTFIELD);
//        Assertions.assertThat(newTextField).hasText("");
    }

    @Test
    // Set Custom Function2
    public void testH_SCF2() {
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
    public void testH_SCF3() {
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
    public void testH_SCF4(){
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
    public void testI_OST1() {
        push(KeyCode.ALT, KeyCode.F4);
        release(new KeyCode[]{});
        push(KeyCode.CONTROL, KeyCode.S);
        release(new KeyCode[]{});
        clickOn(LabeledMatchers.hasText("View"));
        clickOn(LabeledMatchers.hasText("Open in Simulator..."));
        Button newButton = lookfor(SimulatorStageTestingNodes.PLAY_BUTTON);
        boolean disabledButton = newButton.isDisabled();
        Assert.assertTrue("Expected Disabled Play Button", disabledButton);
    }

    @Test
    // Create Sentence and play it
    public void testJ_CSP1() {
        clickOn(LabeledMatchers.hasText("Custom"));
        clickOn(LabeledMatchers.hasText("Jump"));
        clickOn(LabeledMatchers.hasText("Next"));
        sleep(1, TimeUnit.SECONDS);
        clickOn(LabeledMatchers.hasText("I"));
        clickOn(LabeledMatchers.hasText("Next"));
        sleep(1, TimeUnit.SECONDS);
        clickOn(LabeledMatchers.hasText("Cliff"));
        clickOn(LabeledMatchers.hasText("Next"));
        sleep(1, TimeUnit.SECONDS);
        clickOn(LabeledMatchers.hasText("Past"));
        clickOn(LabeledMatchers.hasText("Play"));
        sleep(3, TimeUnit.SECONDS);
        Button newButton = lookfor(SimulatorStageTestingNodes.PLAY_BUTTON);
        boolean disabledButton = newButton.isDisabled();
        Assert.assertFalse("Expected Disabled Play Button", disabledButton);
    }

    @Test
    // Save TTS
    public void testK_STTS() {
        push(KeyCode.ALT, KeyCode.F4);
        release(new KeyCode[]{});
        push(KeyCode.CONTROL, KeyCode.S);

    }
}
