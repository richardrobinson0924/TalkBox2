package talkbox;

import java.nio.file.Path;
import java.util.Arrays;

public class TalkBoxData implements TalkBoxConfiguration {
    public int numberOfAudioButtons = 0;
    public int numberOfAudioSets = 0;
    public int totalNumberOfButtons = 0;
    public Path relativePathToAudioFiles = null;
    public String[][] audioFilenames = new String[0][0];

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
        return audioFilenames;
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
