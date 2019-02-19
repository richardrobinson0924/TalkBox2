package talkbox.Commands;

import talkbox.TalkBoxApp;
import talkbox.Try;

import java.util.Stack;

public final class History {
	public static TalkBoxApp appInstance;

	private static History instance = null;
	private final Stack<Command> stack = new Stack<>();

	public interface Command {
		void execute();
		void undo();
	}

	private History() {}

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
		TalkBoxApp.setIsChanged(true);
	}

	public void undo() {
		Try.newBuilder()
				.setDefault(stack.pop()::undo)
				.run();
		TalkBoxApp.setIsChanged(true);
	}

	public static void setTalkBoxData(TalkBoxApp tba) {
		appInstance = tba;
	}
}
