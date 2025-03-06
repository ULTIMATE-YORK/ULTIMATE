package logging;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TextFlowAppender extends AppenderBase<ILoggingEvent> {
    // A static reference to the TextFlow where log messages will be displayed.
    private static TextFlow textFlow;

    // Setter to inject the TextFlow from your JavaFX controller.
    public static void setTextFlow(TextFlow tf) {
        textFlow = tf;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (textFlow == null) {
            return; // No TextFlow set, so skip logging to UI.
        }

        // Create the timestamp text node.
       // String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                //.format(new Date(eventObject.getTimeStamp()));
        //Text timestampText = new Text(timestamp + " ");

        // Determine the log level and apply the desired color and format.
        String level = eventObject.getLevel().toString();
        Text levelText = new Text();
        switch (level) {
            case "INFO":
                levelText.setText("[INFO]");
                levelText.setStyle("-fx-fill: green;");
                break;
            case "WARN":
                levelText.setText("[WARN]");
                levelText.setStyle("-fx-fill: orange;"); // Orange or yellow can be used.
                break;
            case "ERROR":
                levelText.setText("[ERROR]");
                levelText.setStyle("-fx-fill: red;");
                break;
            default:
                levelText.setText("[" + level + "]");
                levelText.setStyle("-fx-fill: black;");
                break;
        }

        // Create the logger name text node.
        Text loggerText = new Text(" " + eventObject.getLoggerName() + " - ");

        // Create the log message text node.
        Text messageText = new Text(eventObject.getFormattedMessage() + "\n");

        // Update the TextFlow on the JavaFX Application Thread.
        Platform.runLater(() -> {
            textFlow.getChildren().addAll(levelText, loggerText, messageText);
        });
    }
}
