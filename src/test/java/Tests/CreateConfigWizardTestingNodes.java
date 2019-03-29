package Tests;

import talkboxnew.CreateConfigWizard;

public class CreateConfigWizardTestingNodes {

    static class NamePane {
        private static String[] namePaneNodes = CreateConfigWizard.NAMEWIZARDPANE_NODES;
        static String getIdTextfield () {return namePaneNodes[0];}
    }

    static class NumbersPane {
        private static String[] numbersPaneNodes = CreateConfigWizard.NUMBERSWIZARDPANE_NODES;
        static String getIdNumButtonsSpinner () {return numbersPaneNodes[0];}
        static String getIdNumSetsSpinner() {return numbersPaneNodes[1];}
    }

    static class FilePane {
        private static String[] filePaneNodes = CreateConfigWizard.FILEWIZARDPANE_NODES;
        static String getIdFileTextfield() {return filePaneNodes[0];}
    }

    private CreateConfigWizardTestingNodes() {

    }
}
