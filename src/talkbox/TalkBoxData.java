package talkbox;

import java.nio.file.Path;
import java.util.Arrays;

/**
 * The base data structure of the TalkBox data is a double array of <code>Mapping</code>s, in which each Mapping contains the audio filename and the audio file's alias (by default, the file name). To retrieve the double array of only the filenames, use <code>getAudioFilenames</code> to produce a shallow copy.
 * <p>
 * The file system structure assumes there exists an /Audio folder in the same parent directory as the source .tbc file.
 */
public class TalkBoxData implements TalkBoxConfiguration {
    public int numberOfAudioButtons = 0;
    public int numberOfAudioSets = 0;
    public int totalNumberOfButtons = 0;
    public Path relativePathToAudioFiles = null;
    public String[][] audioFilenames = new String[0][0];

    public Mapping[][] audioList = new Mapping[numberOfAudioSets][numberOfAudioButtons];

    @Override
    public int getNumberOfAudioButtons() {
        return this.numberOfAudioButtons;
    }

    @Override
    public int getNumberOfAudioSets() {
        return numberOfAudioSets;
    }

    @Override
    public int getTotalNumberOfButtons() {
        return totalNumberOfButtons;
    }

    @Override
    public Path getRelativePathToAudioFiles() {
        return relativePathToAudioFiles;
    }

    @Override
    public String[][] getAudioFileNames() {
        String[][] fileNames = new String[getNumberOfAudioSets()][getTotalNumberOfButtons()];

        for (int i = 0; i < getNumberOfAudioSets(); i++) {
            for (int j = 0; j < getTotalNumberOfButtons(); j++) {
                fileNames[i][j] = audioList[i][j].getKey();
            }
        }

        return fileNames;
    }


    @Override
    public String toString() {
        return "TalkBoxData{" +
                "numberOfAudioButtons=" + numberOfAudioButtons +
                ", numberOfAudioSets=" + numberOfAudioSets +
                ", totalNumberOfButtons=" + totalNumberOfButtons +
                ", relativePathToAudioFiles=" + relativePathToAudioFiles +
                ", audioFilenames=" + Arrays.toString(audioFilenames) +
                '}';
    }
}
