package talkbox;

import java.util.function.Consumer;

/**
 * Try is a Runnable-like static builder class to assure <code>failSafe.accept()</code> is executed upon failure with a possible additional(s) failure statement executions, such that
 * <pre>
 * Try.to(() -> {
 *  // statements if possible
 * }).otherwise(() -> {
 *  // otherwise if failed
 * }.run();
 * </pre>
 * is equivalent to
 * <pre>
 * try {
 *  // statements if possible
 * } catch (Exception e) {
 *  failSafe(e)
 *  // otherwise if failed
 * }
 * </pre>
 * <p>
 * Note the <code>otherwise</code> actions are optional. The <code>to()</code> method must be included and <code>run()</code> always last.
 *
 * @author Richard Robinson
 * @apiNote The <code>failSafe</code> method should be implemented in the calling class via
 * <pre>
 * Try.setFailSafe(this::failSafe)
 * </pre>
 * Otherwise, by default, the exception stack trace is printed
 */
public final class Try {
	private Try() {
	}

	private static Try TryMember = new Try();

	private RunnableEx statements = () -> {
	};

	private RunnableEx otherwise = () -> {
	};

	private static Consumer<Exception> failSafe = Exception::printStackTrace;

	/**
	 * The starter builder method. Attempts to execute the statements in <code>r</code>. If unable to do so, <code>failSafe.accept()</code> is executed
	 *
	 * @param r the statement(s) to execute.
	 * @return the static Try instance
	 * @apiNote should be used in the fashion
	 * <pre>
	 * Try.to(() -> statements).run();
	 * </pre>
	 */
	public static Try attemptTo(RunnableEx r) {
		Try.TryMember = new Try();
		Try.TryMember.statements = r;
		return Try.TryMember;
	}

	public Try otherwise(RunnableEx otherwise) {
		this.otherwise = otherwise;
		return Try.TryMember;
	}

	public static void setFailSafe(Consumer<Exception> ex) {
		Try.failSafe = ex;
	}

	/**
	 * The final builder method. Executes the code. Should <b>always</b> be included as the last call in the builder chain.
	 */
	public void run() {
		try {
			this.statements.run();
		} catch (Exception ex) {
			Try.failSafe.accept(ex);
			try {
				this.otherwise.run();
			} catch (Exception ignored) {
			}
		}
	}

	@FunctionalInterface
	public interface RunnableEx {
		void run() throws Exception;
	}
}
