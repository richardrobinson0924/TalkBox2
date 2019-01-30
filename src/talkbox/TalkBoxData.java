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

    /**
     * Identical to <code>TalkBoxData.getPath(getAudioFileNames()[i, j])</code>
     *
     * @param i the audio set
     * @param j the audio button
     * @return the true path
     * @see #getPath(String)
     */
    public String getPath(int i, int j) {
        return TalkBoxData.getPath(getAudioFileNames()[i][j]);
    }

    /**
     * Identical to <code>TalkBoxData.getAlias(getAudioFileNames()[i, j])</code>
     *
     * @param i the audio set
     * @param j the audio button
     * @return the alias
     * @see #getAlias(String)
     */
    public String getAlias(int i, int j) {
        return TalkBoxData.getAlias(getAudioFileNames()[i][j]);
    }

    /**
     * Extracts the true file path string from the default concatenation of the pathname with the button label text alias
     *
     * @param path the concatenated string of the true path with its alias
     * @return the true path
     */
    private static String getPath(String path) {
        String truePath = path.split("\\|")[0];
        return truePath.substring(0, truePath.length() - 1);
    }

    /**
     * Extracts the button label text alias string from the default concatenation of such with the true file path
     *
     * @param path the concatenated string of the true path with its alias
     * @return the alias
     */
    private static String getAlias(String path) {
        return path.split("\\|")[1];
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
