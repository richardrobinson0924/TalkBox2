package talkbox;

import javafx.util.Pair;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * The base data structure of the TalkBox data is a double array of <code>Mapping</code>s, in which each Mapping contains the audio filename (the key) and the audio file's alias (the value; by default, the file name). To retrieve the double array of only the filenames, use <code>getAudioFilenames</code> to produce a shallow copy.
 * <p>
 * The file system structure assumes there exists an /Audio folder in the same parent directory as the source .tbc file.
 *
 * Precondition for TalkBoxApp: There exists a directory in which there is a *.tbc file and another directory entitled "Audio"
 */
public class TalkBoxData implements TalkBoxConfiguration {
	private static final long serialVersionUID = 5729041712760286285L;

    int numberOfAudioButtons = 0;
    int numberOfAudioSets = 0;
    int totalNumberOfButtons = 0;
    Path relativePathToAudioFiles = null;

    Vector<VectorMap<File, String>> audioList = new Vector<>(totalNumberOfButtons);
    List<List<String>> customWords = new ArrayList<>();

    public void set(int i, int j, Pair<File, String> pair) {
        VectorMap<File, String> temp = this.audioList.get(i);
        temp.set(j, pair);
        this.audioList.set(i, temp);
    }

    public void setFile(int i, int j, File file) {
        VectorMap<File, String> temp = this.audioList.get(i);
        temp.setKey(j, file);
        this.audioList.set(i, temp);
    }

    public void setValue(int i, int j, String str) {
        VectorMap<File, String> temp = this.audioList.get(i);
        temp.setValue(j, str);
        this.audioList.set(i, temp);
    }

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
	            fileNames[i][j] = audioList.get(i).getKey(j).getName();
            }
        }

        return fileNames;
    }
}
