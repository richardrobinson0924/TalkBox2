package talkbox;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @apiNote DO NOT use <code>audioFilenames[i][j]</code> field to retrieve filenames. Instead, use <code>getPath(i, j)</code>. This is because each filename in the matrix is a pseudomap, of form <code>"actualFilename|alias"</code>.
 */
public class TalkBoxSim {
    // hello John doe fix me
    public static void main(String... args) throws IOException {
        FileOutputStream fos = new FileOutputStream("test.tbc");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        TalkBoxData ts = new TalkBoxData();
        ts.numberOfAudioButtons = 5;
        ts.numberOfAudioSets = 8;

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
