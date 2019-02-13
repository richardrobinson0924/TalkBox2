package talkbox;

import java.io.File;
import java.nio.file.Path;

/**
 * The base data structure of the TalkBox data is a double array of <code>Mapping</code>s, in which each Mapping contains the audio filename (the key) and the audio file's alias (the value; by default, the file name). To retrieve the double array of only the filenames, use <code>getAudioFilenames</code> to produce a shallow copy.
 * <p>
 * The file system structure assumes there exists an /Audio folder in the same parent directory as the source .tbc file.
 *
 * Precondition for TalkBoxApp: There exists a directory in which there is a *.tbc file and another directory entitled "Audio"
 */
public class TalkBoxData implements TalkBoxConfiguration {
    public int numberOfAudioButtons = 0;
    public int numberOfAudioSets = 0;
    public int totalNumberOfButtons = 0;
    public Path relativePathToAudioFiles = null;
    public Mapping[][] audioList = new Mapping[numberOfAudioSets][numberOfAudioButtons];

	public File getFile(int i, int j) {
		return audioList[i][j].getKey();
	}

	public String getAlias(int i, int j) {
		return audioList[i][j].getValue();
	}

	public void setKey(int i, int j, File key) {
		audioList[i][j].setKey(key);
	}

	public void setAlias(int i, int j, String value) {
		audioList[i][j].setValue(value);
	}

	public boolean isNull(int i, int j) {
		return audioList[i][j] == null;
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
	            fileNames[i][j] = audioList[i][j].<File>getKey().getPath();
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
                '}';
    }
}
