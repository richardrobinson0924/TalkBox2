package talkbox;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * The TalkBox Simulator simulates the physical device. When the app is launched, the user is prompted to either (a) create a new .tbc file or (b) open an existing file. At any point in the application, the user can launch the TalkBox configurator with the current .tbc file pre-loaded in the Configurator.
 *
 * <p>
 *     <b>Creating a new file:</b>
 * <p>
 *
 * This opens a wizard-like dialog, with the following steps:
 * <ul>
 *     <li> Asks where to save file on the disk
 *     <li> Asks how many audio buttons and swap buttons it should have
 *     <li> Once done, on backend creates a TalkBox directory in the location specified. Within the directory, there will be the .tbc file, as well as another directory entitled "Audio" to contain the audio files
 * </ul>
 *
 * Afterwards, the user will have the option of opening the Configurator with this newly created .tbc file
 *
 * <p>
 *     <b>Opening an Existing File</b>
 * <p>
 *
 * This presents a FileChooser allowing a user to select a .tbc TalkBox Configuration file. Then, an  interface will appear with the following presentation:
 * <ul>
 *     <li> All the required specifications as described on the project outline (buttons with swap buttons acting accordingly
 *     <li> There shall also exist a <code>Custom</code> button with a <code>Play</code> button beside it.
 * </ul>
 *
 * <p>
 *     <b>Using the Custom button</b>
 * <p>
 *
 * The custom button is an on-board sentence TTS generator. Upon pressing the button, each audio button transforms into a sentence Subject word (for example, "Richard", "Myself", "You"). Once the user selects the Subject, the audio buttons again transform into Verbs. This process continues for the following sentence structures: subjects, verbs, objects, tenses, and propositional meanings. After the final selection, the <code>simplenlg</code> API creates a new sentence out of the different words.
 *
 * <p>
 * The list of different options for each sentence structure will be provided in a CSV file within the directory (first column is Subject, next is Verbs, etc...), which must first be parsed by the Simulator. The user can then press <code>Play</code> to play the newly generated sentence using the Google Cloud TTS service.
 */
public class TalkBoxSim {
    // hello John doe fix me
    public static void main(String... args) throws IOException {
        FileOutputStream fos = new FileOutputStream("/Users/richardrobinson/Desktop/MyTalkBox/config.tbc");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        TalkBoxData ts = new TalkBoxData();
        ts.numberOfAudioButtons = 5;
        ts.numberOfAudioSets = 8;

        ts.audioList = new Mapping[ts.numberOfAudioSets][ts.numberOfAudioButtons];

        for (int i = 0; i < ts.numberOfAudioSets; i++) {
            for (int j = 0; j < ts.getNumberOfAudioButtons(); j++) {
                ts.audioList[i][j] = null;
            }
        }

        oos.writeObject(ts);
        oos.flush();
        oos.close();
    }
}
