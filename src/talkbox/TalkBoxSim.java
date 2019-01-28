package talkbox;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class TalkBoxSim {
    public static void main(String... args) throws IOException {
        FileOutputStream fos = new FileOutputStream("/Users/richardrobinson/Desktop/test.tbc");
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
