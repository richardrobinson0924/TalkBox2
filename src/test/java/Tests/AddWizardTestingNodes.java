package Tests;

import talkboxnew.AddWizard.IntroPane;
import talkboxnew.AddWizard.NamePane;
import talkboxnew.AddWizard.RecordingPane;
import talkboxnew.AddWizard.TTSPane;

// change them all to getMethods real soon
public class AddWizardTestingNodes {

    static class IntroPaneNodes {
        public static final String TTS_BUTTON = getTTsButton();
        public static final String WAV_BUTTON = getWavButton();
        public static final String RECORD_BUTTON = getRecordButton();

        private static String[] getIntroPaneNodes(){
            String [] Id = IntroPane.nodesId;
            return Id;
        }
        private static String getTTsButton(){
            String []Id = getIntroPaneNodes();
            String TTS = Id[0];
            return TTS;
        }
        private static String getRecordButton(){
            String []Id = getIntroPaneNodes();
            String WAV = Id[2];
            return WAV;
        }
        private static String getWavButton(){
            String []Id = getIntroPaneNodes();
            String RECORD = Id[1];
            return RECORD;
        }
    }


    static class TTSPaneNodes {
        public static final String TTSPHRASE_TEXTFIELD = getTTsText();
        public static final String TTSPLAY_BUTTON = getTTsPlay();
        public static final String TTSVOICE_COMBOBOX = getTTsComboBox();

        private static String[] getTTSNodes(){
            String [] Id = TTSPane.nodesId;
            return Id;
        }
        private static String getTTsText(){
            String []Id = getTTSNodes();
            String TTS = Id[0];
            return TTS;
        }
        private static String getTTsPlay(){
            String []Id = getTTSNodes();
            String TTS = Id[1];
            return TTS;
        }
        private static String getTTsComboBox(){
            String []Id = getTTSNodes();
            String TTS = Id[2];
            return TTS;
        }

    }

    static class NamePaneNodes {
        public static String AUDIONAME_TEXTFIELD = getNamePaneText();

        private static String[] getNamePaneNodes(){
            String [] Id = NamePane.nodesId;
            return Id;
        }
        private static String getNamePaneText(){
            String []Id = getNamePaneNodes();
            String TTS = Id[0];
            return TTS;
        }
    }

    static class RecordingPaneNodes {
        public static String PLAY_BUTTON = getRecordingPanePlay();
        public static String RECORDING_TOGGLE = getRecordingTogglePlay();

        private static String[] getRecordingPaneNodes(){
            String [] Id = RecordingPane.nodesId;
            return Id;
        }
        private static String getRecordingPanePlay(){
            String []Id = getRecordingPaneNodes();
            String TTS = Id[1];
            return TTS;
        }
        private static String getRecordingTogglePlay(){
            String []Id = getRecordingPaneNodes();
            String TTS = Id[0];
            return TTS;
        }

    }
}
