package talkbox;

import com.sun.istack.internal.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Try is a Runnable-like static builder class to assure <code>failSafe.accept()</code> is executed upon failure with a possible
 * additional(s) failure statement executions, such that
 *
 * <pre>{@code
 * Try.newBuilder().setDefault(() -> {
 *     // commands
 * }).setOtherwise(() -> {
 *     // actions in addition to faiLSafe
 * }).run();
 * }</pre>
 *
 * is equivalent to
 *
 * <pre>{@code
 * try { actions } catch (Exception e) {
 *      failSafe(e)
 *      // otherwise if failed
 * }
 * }</pre>
 *
 * Note the <code>otherwise</code> actions are optional. The <code>to()</code> method must be included and <code>run()</code>always last.
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
	 * The builder static start method. Must be called first.
	 * @return the static Try instance
	 */
	public static Try newBuilder() {
		Try.TryMember = new Try();
		return Try.TryMember;
	}

	/**
	 * Attempts to execute the statements in <code>rex</code>. If unable to do so, <code>failSafe.accept()</code> is executed
	 *
	 * @param rex the statement(s) to execute.
	 * @return the static Try instance
	 * @apiNote should be used in the fashion
	 * <pre>
	 *  Try.attemptTo(() -> statements).run();
	 * </pre>
	 */
	public Try setDefault(RunnableEx rex) {
		Try.TryMember.statements = rex;
		return Try.TryMember;
	}

	/**
	 * Optional. The statements to execute in addition to <code>failSafe.accept()</code> upon exception being caught
	 *
	 * @param otherwise the statements execute upon failure
	 * @return the static Try instance
	 */
	public Try setOtherwise(RunnableEx otherwise) {
		this.otherwise = otherwise;
		return Try.TryMember;
	}

	/**
	 * Statically sets the failSafe statement(s) to always execute upon an exception being caught in the form of a
	 * <code>Consumer</code>. By default, the stack trace is printed. The fail safe should be set before any other
	 * methods are called, via
	 *
	 * <pre> Try.setFailSafe(ClassName::methodName); </pre>
	 *
	 * in which <code>methodName</code> is the method with signature <code> void methodName(Exception e)</code> which
	 * includes the statements to be executed
	 *
	 * @param ex The exception parameter
	 */
	public static void setFailSafe(Consumer<Exception> ex) {
		Try.failSafe = ex;
	}

	/**
	 * The final builder method to executes the instance. Must <b>always</b> be included as the last call in the builder chain.
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

	/**
	 * A <code>Runner</code> interface which throws an exception
	 * @see java.lang.Runnable
	 */
	@FunctionalInterface
	public interface RunnableEx {
		void run() throws Exception;
	}
}
