package talkbox;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The base data structure of the TalkBox data is a double array of <code>Mapping</code>s, in which each Mapping contains the audio
 * filename (the key) and the audio file's alias (the value; by default, the file name). To retrieve the double array of only the
 * filenames, use <code>getAudioFilenames</code> to produce a shallow copy.
 * <p>
 * The file system structure assumes there exists an /Audio folder in the same parent directory as the source .tbc file.
 *
 * Precondition for TalkBoxApp: There exists a directory in which there is a *.tbc file and another directory entitled "Audio"
 */
public class TalkBoxData implements TalkBoxConfiguration {
	private static final long serialVersionUID = 3102272785185579993L;

    int numberOfAudioButtons = 0;
    int numberOfAudioSets = 0;
    private int totalNumberOfButtons = 0;
    private Path relativePathToAudioFiles = null;

    public List<List<AudioPair>> database = new ArrayList<>();

    public List<List<String>> customWords = new ArrayList<>();

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
	            fileNames[i][j] = database.get(i).get(j).getKey().getName();
            }
        }

        return fileNames;
    }
}