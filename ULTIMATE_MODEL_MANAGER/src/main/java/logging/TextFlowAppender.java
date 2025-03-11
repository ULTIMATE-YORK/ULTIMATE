
package logging;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // Create the log message text nodes.
        String message = eventObject.getFormattedMessage();
        Pattern pattern = Pattern.compile("ERROR");
        Matcher matcher = pattern.matcher(message);
        final int[] lastEnd = {0};
        Platform.runLater(() -> {
            textFlow.getChildren().addAll(levelText, loggerText);
            while (matcher.find()) {
                if (matcher.start() > lastEnd[0]) {
                    textFlow.getChildren().add(new Text(message.substring(lastEnd[0], matcher.start())));
                }
                Text errorText = new Text("[ERROR]");
                errorText.setStyle("-fx-fill: red;");
                textFlow.getChildren().add(errorText);
                lastEnd[0] = matcher.end();
            }
            if (lastEnd[0] < message.length()) {
                textFlow.getChildren().add(new Text(message.substring(lastEnd[0]) + "\n"));
            } else {
                textFlow.getChildren().add(new Text("\n"));
            }
        });
    }
}
