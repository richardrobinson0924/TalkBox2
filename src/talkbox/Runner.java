package talkbox;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Always use <code>RunnerClass.tryTo()</code> instead of <code>try-catch</code> blocks. RunnerClass is a Runnable-like custom class to assure <code>displayErrorMessage()</code> occurs, designed to always execute the <code>failSafe()</code> method upon failure such that
 * <code>
 * RunnerClass.tryTo(() -> {
 * // body;
 * }, () -> {
 * // otherwise
 * });
 * </code>
 * <p>
 * is equivalent to
 * <p>
 * <code>
 * try { // body } catch (Exception ex) {
 * failSafe()
 * // otherwise
 * }
 * </code>
 * <p>
 * Note the <code>otherwise</code> actions are optional
 */
public class Runner {
	public static Runnable failSafe;

	public static void tryTo(RunnerInterface r) {
		tryTo(r, null);
	}

	/**
	 * Attempts to run the statements in the caller's lambda. If unsuccessful, the exception is caught, displayErrorMessage(ex) is called, and otherwise is performed
	 *
	 * @param r         The main statement(s) to attempt
	 * @param otherwise The statement(s) to execute upon failure
	 */
	public static void tryTo(RunnerInterface r, RunnerInterface otherwise) {
		try {
			r.run();
		} catch (Exception ex) {
			failSafe(ex);
			if (otherwise != null) {
				try {
					otherwise.run();
				} catch (Exception ignored) {
				}
			}
		}
	}

	public static void setFailSafe(Runnable r) {
		Runner.failSafe = r;
	}

	private static void failSafe(Exception ex) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("An Error has Occurred");
		alert.setHeaderText(alert.getTitle());
		alert.setContentText(ex.getMessage());

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("Full error message:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		alert.getDialogPane().setExpandableContent(expContent);
		alert.showAndWait();
	}

	@FunctionalInterface
	public interface RunnerInterface {
		void run() throws Exception;
	}
}