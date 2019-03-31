package Tests;

import talkboxnew.AddWizard.IntroPane;
import talkboxnew.AddWizard.NamePane;
import talkboxnew.AddWizard.RecordingPane;
import talkboxnew.AddWizard.TTSPane;

// change them all to getMethods real soon
public class AddWizardTestingNodes {

    static class IntroPaneNodes {
        private static String[] introPaneNodes = IntroPane.nodesId;
        public static final String TTS_BUTTON = introPaneNodes[0];
        public static final String WAV_BUTTON = introPaneNodes[1];
        public static final String RECORD_BUTTON = introPaneNodes[2];
    }

    static class TTSPaneNodes {
        private static String[] TTSPaneNodes = TTSPane.nodesId;
        public static final String TTSPHRASE_TEXTFIELD = TTSPaneNodes[0];
        public static final String TTSPLAY_BUTTON = TTSPaneNodes[1];
    }

    static class NamePaneNodes {
        private static String[] namePaneNodes = NamePane.nodesId;
        public static String AUDIONAME_TEXTFIELD = namePaneNodes[0];
    }

    static class RecordingPaneNodes {
        private static String[] recordingPaneNodes = RecordingPane.nodesId;
        public static String PLAY_BUTTON = recordingPaneNodes[1];
        public static String RECORDING_TOGGLE = recordingPaneNodes[0];
    }
}
