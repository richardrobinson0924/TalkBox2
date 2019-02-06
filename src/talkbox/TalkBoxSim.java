package talkbox;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @apiNote DO NOT use <code>audioFilenames[i][j]</code> field to retrieve filenames. Instead, use <code>getPath(i, j)</code>. This is because each filename in the matrix is a pseudomap, of form <code>"actualFilename|alias"</code>.
 */
public class TalkBoxSim extends Application {
    // instructions:
    // you wanna ask the user if they wanna make a new tbc file or open an existing one. (2 buttons)
    // when the .tbc file is created, it opens (or when the existing file is opened) and a simple interface of the buttons
    public static void main(String... args) {

    }

    public void start(Stage primaryStage) {

    }

    public void createNewTBC() throws IOException {
        FileOutputStream fos = new FileOutputStream("test.tbc");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        TalkBoxData ts = new TalkBoxData();
        ts.numberOfAudioButtons = 5;
        ts.numberOfAudioSets = 8;
        // testing to see the branch

        ts.audioFilenames = new String[8][5];

        for (int i = 0; i < ts.numberOfAudioSets; i++) {
            for (int j = 0; j < ts.getNumberOfAudioButtons(); j++) {
                ts.audioFilenames[i][j] = null;
            }
        }
        oos.writeObject(ts);
        oos.flush();
        oos.close();
    }
}
