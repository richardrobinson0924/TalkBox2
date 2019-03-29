package talkboxnew;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
	private static final long serialVersionUID = -512851111601552178L;

	int numberOfAudioButtons = 0;
	int numberOfAudioSets = 0;

	List<Entry> database = new ArrayList<>();

	List<List<String>> customWords = new ArrayList<>();

	public int getNumberOfAudioButtons() {
		return this.numberOfAudioButtons;
	}

	public int getNumberOfAudioSets() {
		return numberOfAudioSets;
	}

	public int getTotalNumberOfButtons() {
		return getNumberOfAudioButtons() * getNumberOfAudioSets();
	}

	@Override
	public String toString() {
		return database.stream()
				.filter(Objects::nonNull)
				.map(Entry::toString)
				.collect(Collectors.joining(", ", "[", "]"));
	}

	@Override
	public Path getRelativePathToAudioFiles() {
		return Utils.getAudio("");
	}

	@Override
	public String[][] getAudioFileNames() {
		String[][] filenames = new String[getNumberOfAudioSets()][getNumberOfAudioButtons()];
		int k = 0;

		for (int i = 0; i < getNumberOfAudioSets(); i++) {
			for (int j = 0; j < getNumberOfAudioButtons(); j++) {
				filenames[i][j] = database.get(k++).getFile().toString();
			}
		}

		return filenames;
	}
}
