package talkbox.Commands;

import talkbox.TalkBoxApp;

import java.util.Stack;

/**
 * The parent singleton class for all Command pattern class implementations, so as to enable undo functionality. The
 * {@code setTalkBoxData(TalkBoxApp tbc)} method should only ever be called once, at the beginning of the {@code
 * TalkBoxApp} class to associate it.
 * <p></p>
 * All modifications to the configuration file should be done via implementations of this class. The {@code execute(), undo()} methods should only be used via this class and no implementing classes, via
 * <p><code>
 *     History.getInstance().execute(new CustomCommand());
 * </code></p>
 */
public final class History {
	static TalkBoxApp appInstance;

	private static History instance = null;
	private final Stack<Command> stack = new Stack<>();

	public interface Command {
		void execute();

		void undo();
	}

	private History() {
	}

	public boolean isEmpty() {
		return stack.empty();
	}

	public static History getInstance() {
		if (History.instance == null) synchronized (History.class) {
			if (History.instance == null) History.instance = new History();
		}

		return History.instance;
	}

	public void execute(final Command command) {
		stack.push(command);
		command.execute();
	}

	public void undo() {
		if (!isEmpty()) stack.pop().undo();
	}

	public static void setTalkBoxData(TalkBoxApp tba) {
		appInstance = tba;
	}
}
