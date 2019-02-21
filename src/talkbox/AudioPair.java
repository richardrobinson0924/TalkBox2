package talkbox;

import javafx.beans.property.*;
import java.io.*;

/**
 * DO NOT MODIFY. Only use {@code getKey(), getValue(), isNull()}, and {@code AudioPair()}
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

    /**
     * Reads and re-wraps the transient fields from the serialization
     *
     * @param in the serialization voodoo
     * @throws IOException if it fails
     * @throws ClassNotFoundException if the types mismatch
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.file = new SimpleObjectProperty<>((File) in.readObject());
        this.str = new SimpleStringProperty(in.readUTF());
    }

    /**
     * When serializing, only serialize the wrapped value of the transient fields, and if there's nothing to serialize, serialize null values.
     *
     * @param out the serialization voodoo
     * @throws IOException if it fails
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        if (isNull()) {
            out.writeObject(null);
            out.writeUTF("");
        } else {
            out.writeObject(file.get());
            out.writeUTF(str.get());
        }
    }

    /**
     * This method does literally nothing but it needs to be here
     *
     * @throws ObjectStreamException for absolutely no reason
     */
    private void readObjectNoData() throws ObjectStreamException {

    }
}
