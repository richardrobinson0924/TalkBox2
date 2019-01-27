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
        oos.writeObject(ts);
        oos.flush();
        oos.close();
    }
}
