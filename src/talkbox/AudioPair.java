package talkbox;

import javafx.beans.property.*;
import java.io.*;

/**
 * A beans-based Pair class which implements serializable and allows Listeners to track changes to each field, as they
 * are encapsulated in transient Property wrappers. Never modify these fields directly; instead, use the getters
 * and setter methods. The transient properties are manually serialized via {@code readObject(), writeObject()}
 * <p></p>
 * When used in an {@code ObservableList<AudioPair>}, the list initially be filled with AudioPairs created via {@code
 * AudioPair} to set all fields to null. The list should contain no null elements. For single use cases, the
 * overloaded constructor may be used.
 * <p></p>
 * <b>Note:</b> Never test against null directly; instead use {@code isNull()}
 *
 * @see javafx.beans.value.ChangeListener
 */
public class AudioPair implements Serializable {
    private static final long serialVersionUID = -6122359436305270578L;

    public transient ObjectProperty<File> file;
    public transient SimpleStringProperty str;

    public AudioPair(File key, String value) {
        this.file = new SimpleObjectProperty<>(key);
        this.str = new SimpleStringProperty(value);
    }

    public AudioPair() {
        this(null, null);
    }

    public File getKey() {
        return this.file.get();
    }

    public File setKey(File file) {
        this.file.setValue(file);
        return file;
    }

    public String getValue() {
        return this.str.getValue();
    }

    public String setValue(String str) {
        this.str.setValue(str);
        return str;
    }

    public void set(File key, String value) {
        this.file.setValue(key);
        this.str.setValue(value);
    }

    public boolean isNull() {
        return file.isNull().and(str.isEmpty()).get();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.file = new SimpleObjectProperty<>((File) in.readObject());
        this.str = new SimpleStringProperty(in.readUTF());
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        out.writeObject(file.getValue());
        out.writeUTF(str.getValueSafe());
    }

    private void readObjectNoData() throws ObjectStreamException {

    }
}
