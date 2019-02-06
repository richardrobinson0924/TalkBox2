package talkbox;

import java.nio.file.Path;
import java.util.Arrays;

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
