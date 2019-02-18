package talkbox;

import javafx.util.Pair;

import java.io.Serializable;
import java.util.Vector;

public class VectorMap<K, V> extends Vector<Pair<K, V>> implements Serializable {
	private Vector<Pair<K, V>> map;
	private static final long serialVersionUID = 3425883796208836563L;

	public VectorMap(int size) {
		this.map = new Vector<>(size);
	}

	public VectorMap() {
		this.map = new Vector<>();
	}

	public K getKey(int i) {
		return super.get(i).getKey();
	}

	public V getValue(int i) {
		return super.get(i).getValue();
	}

	public void set(int i, K key, V value) {
		super.set(i, new Pair<>(key, value));
	}

	public void setKey(int i, K key) {
		final V oldVal = getValue(i);
		this.set(i, key, oldVal);
	}

	public void setValue(int i, V value) {
		final K oldKey = getKey(i);
		this.set(i, oldKey, value);
	}
}
