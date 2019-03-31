package talkboxnew;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.Serializable;

public class Entry implements Serializable {
	private File file;
	private File image;
	private String name;
	private int frequency;

	private transient static final Logger log = Logger.getLogger(Entry.class.getName());

	private Entry() {
	}

	public static class Builder implements Serializable {
		private final File file;
		private File image = null;
		private String name;
		private int frequency;

		private Builder(File f) {
			this.file = f;
			this.frequency = 0;
			this.name = f.getName();
			log.info("New Entry Created: " + toString());
		}

		public static Builder of(File f) {
			return new Builder(f);
		}

		public Builder withName(String name) {
			log.info("String '" + name + "' added to " + toString());
			this.name = name;
			return this;
		}

		public Builder withImage(File image) {
			log.info("Image '" + image.getName() + "' added to " + toString());
			this.image = image;
			return this;
		}

		public Entry build() {
			final Entry entry = new Entry();
			entry.file  = this.file;
			entry.image = this.image;
			entry.frequency = this.frequency;
			entry.name = this.name;

			return entry;
		}
	}

	public File getFile() {
		return this.file;
	}

	public String getName() {
		return this.name;
	}

	public File getImage() {
		return this.image;
	}

	public int getFrequency() {
		return this.frequency;
	}

	public void incrementFrequency() {
		this.frequency++;
	}

	@Override
	public String toString() {
		return String.format("Entry[%s, %s, %s, %d]",
				getFile().getName(),
				getName(),
				getImage().getName(),
				getFrequency()
		);
	}
}
