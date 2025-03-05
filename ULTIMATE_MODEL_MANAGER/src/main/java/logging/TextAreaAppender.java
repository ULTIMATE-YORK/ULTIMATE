package logging;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class TextAreaAppender extends AppenderBase<ILoggingEvent> {
    // A static reference to the TextArea where log messages will be displayed.
    private static TextArea textArea;

    // Setter to inject the TextArea from your JavaFX controller
    public static void setTextArea(TextArea ta) {
        textArea = ta;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (textArea == null) {
            return; // No TextArea set, skip logging to UI.
        }
        // Format the log message. You could enhance this by adding timestamps or log levels.
        final String logMessage = eventObject.getFormattedMessage() + "\n";
        // Ensure the update happens on the JavaFX Application Thread.
        Platform.runLater(() -> textArea.appendText(logMessage));
    }
}
