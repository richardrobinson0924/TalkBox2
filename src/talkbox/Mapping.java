package talkbox;

import java.io.Serializable;

/**
 * A bare-bones pair class. Maps a key with a value using method type inferences (allows for arrays)
 *
 * @author Richard Robinson
 */
public class Mapping implements Serializable {
	private static final long serialVersionUID = 6582190898809790391L;

	private Object key;
	private Object value;

	public <T, V> Mapping(T key, V value) {
		this.setKey(key);
		this.setValue(value);
	}

	public <T> T getKey() {
		return (T) key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public <T> T getValue() {
		return (T) value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
