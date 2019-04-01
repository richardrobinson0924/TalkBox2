package Tests;

import talkboxnew.AddWizard.IntroPane;
import talkboxnew.AddWizard.NamePane;
import talkboxnew.AddWizard.RecordingPane;
import talkboxnew.AddWizard.TTSPane;

// change them all to getMethods real soon
public class AddWizardTestingNodes {

    static class IntroPaneNodes {
        private static String[] introPaneNodes = getIntroPaneNodes();
        public static final String TTS_BUTTON = getTTsButton();
        public static final String WAV_BUTTON = getWavButton();
        public static final String RECORD_BUTTON = getRecordButton();

        static String[] getIntroPaneNodes(){
            String [] Id;
            Id=IntroPane.nodesId;
            return Id;
        }
        static String getTTsButton(){
            String []Id;
            Id=getIntroPaneNodes();
            String TTS;
            TTS=Id[0];
            return TTS;
        }
        static String getRecordButton(){
            String []Id;
            Id=getIntroPaneNodes();
            String WAV;
            WAV=Id[1];
            return WAV;
        }
        static String getWavButton(){
            String []Id;
            Id=getIntroPaneNodes();
            String RECORD;
            RECORD=Id[2];
            return RECORD;
        }
    }


    static class TTSPaneNodes {
        private static String[] TTSPaneNodes = getTTSNodes();
        public static final String TTSPHRASE_TEXTFIELD = getTTsText();
        public static final String TTSPLAY_BUTTON = getTTsPlay();
        static String[] getTTSNodes(){
            String [] Id;
            Id=TTSPane.nodesId;
            return Id;
        }
        static String getTTsText(){
            String []Id;
            Id=getTTSNodes();
            String TTS;
            TTS=Id[0];
            return TTS;
        }
        static String getTTsPlay(){
            String []Id;
            Id=getTTSNodes();
            String TTS;
            TTS=Id[1];
            return TTS;
        }

    }

    static class NamePaneNodes {
        private static String[] namePaneNodes = getNamePaneNodes();
        public static String AUDIONAME_TEXTFIELD = getNamePaneText();
        static String[] getNamePaneNodes(){
            String [] Id;
            Id=NamePane.nodesId;
            return Id;
        }
        static String getNamePaneText(){
            String []Id;
            Id=getNamePaneNodes();
            String TTS;
            TTS=Id[0];
            return TTS;
        }
    }

    static class RecordingPaneNodes {
        private static String[] recordingPaneNodes = getRecordingPaneNodes();
        public static String PLAY_BUTTON = getRecordingPanePlay();
        public static String RECORDING_TOGGLE = getRecordingTogglePlay();
        static String[] getRecordingPaneNodes(){
            String [] Id;
            Id=RecordingPane.nodesId;
            return Id;
        }
        static String getRecordingPanePlay(){
            String []Id;
            Id=getRecordingPaneNodes();
            String TTS;
            TTS=Id[1];
            return TTS;
        }
        static String getRecordingTogglePlay(){
            String []Id;
            Id=getRecordingPaneNodes();
            String TTS;
            TTS=Id[0];
            return TTS;
        }

    }
}
