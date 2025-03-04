package parameters;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * A custom ListCell that takes a DependencyParameter as its item and
 * creates a formatted HBox that displays the dependency details on the left
 * and two buttons ("Edit" and "Remove") on the right.
 *
 * The cell fires callbacks via the DependencyUnitListener interface.
 */
public class DependencyParameterCell extends ListCell<DependencyParameter> {

    // Listener to notify when buttons are pressed.
    private DependencyUnitListener dependencyUnitListener;

    /**
     * Set the listener that will handle edit/remove events.
     */
    public void setDependencyUnitListener(DependencyUnitListener listener) {
        this.dependencyUnitListener = listener;
    }

    @Override
    protected void updateItem(DependencyParameter dp, boolean empty) {
        super.updateItem(dp, empty);

        if (empty || dp == null) {
            setText(null);
            setGraphic(null);
        } else {
            setPadding(Insets.EMPTY);
            // Build the left column: A Label showing the dependency details.
            Label details = new Label("DependencyParameter Name: " + dp.getName() + 
                                        "\nModel ID: " + dp.getModel().getModelId() +
                                        "\nProperty Definition: " + dp.getDefinition().replace("\\", ""));
            details.setWrapText(true);

            // Wrap the details in a VBox (so we can later bind its width)
            VBox leftColumn = new VBox(details);

            // Build the right column: A VBox with two buttons.
            Button editButton = new Button("Edit");
            Button removeButton = new Button("Remove");

            // Set button event handlers to call the listener, if set.
            editButton.setOnAction(e -> {
                if (dependencyUnitListener != null) {
                    dependencyUnitListener.onEdit(dp);
                }
            });
            removeButton.setOnAction(e -> {
                if (dependencyUnitListener != null) {
                    dependencyUnitListener.onRemove(dp);
                }
            });

            // Create the right column VBox and configure it.
            VBox rightColumn = new VBox(editButton, removeButton);
            rightColumn.setSpacing(5);
            rightColumn.setAlignment(Pos.CENTER);
            rightColumn.setPadding(new Insets(5));

            // Create the overall HBox to contain both columns.
            HBox cellBox = new HBox(leftColumn, rightColumn);
            cellBox.setPadding(new Insets(5));

            // Bind the HBox width to the cell's width.
            cellBox.prefWidthProperty().bind(widthProperty());
            cellBox.maxWidthProperty().bind(widthProperty());

            // Bind left and right columns to proportions of the cell's width.
            leftColumn.prefWidthProperty().bind(widthProperty().multiply(0.80));
            rightColumn.prefWidthProperty().bind(widthProperty().multiply(0.20));

            // Ensure the buttons expand to fill the right column.
            editButton.prefWidthProperty().bind(rightColumn.widthProperty());
            removeButton.prefWidthProperty().bind(rightColumn.widthProperty());
            editButton.setMaxWidth(Double.MAX_VALUE);
            removeButton.setMaxWidth(Double.MAX_VALUE);
            editButton.setMinHeight(25);    // adjust as needed
            removeButton.setMinHeight(25);  // adjust as needed

            setGraphic(cellBox);
        }
    }

    /**
     * A listener interface for dependency parameter actions.
     * Implement this in your controller to receive button events.
     */
    public interface DependencyUnitListener {
        void onEdit(DependencyParameter dp);
        void onRemove(DependencyParameter dp);
    }
}
