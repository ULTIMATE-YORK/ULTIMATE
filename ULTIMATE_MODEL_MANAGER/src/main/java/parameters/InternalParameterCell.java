package parameters;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class InternalParameterCell extends ListCell<InternalParameter> {
	
	 // Listener to notify when buttons are pressed.
    private InternalUnitListener internalUnitListener;

    /**
     * Set the listener that will handle edit/remove events.
     */
    public void setInternalUnitListener(InternalUnitListener listener) {
        this.internalUnitListener = listener;
    }

    @Override
    protected void updateItem(InternalParameter ip, boolean empty) {
        super.updateItem(ip, empty);

        if (empty || ip == null) {
            setText(null);
            setGraphic(null);
        } else {
            setPadding(Insets.EMPTY);
            // Build the left column: A Label showing the dependency details.
            Label details = new Label(ip.toString());
            details.setWrapText(true);

            // Wrap the details in a VBox (so we can later bind its width)
            VBox leftColumn = new VBox(details);

            // Build the right column: A VBox with two buttons.
            Button editButton = new Button("Edit");
            Button removeButton = new Button("Remove");

            // Set button event handlers to call the listener, if set.
            editButton.setOnAction(e -> {
                if (internalUnitListener != null) {
                    internalUnitListener.onEdit(ip);
                }
            });
            removeButton.setOnAction(e -> {
                if (internalUnitListener != null) {
                    internalUnitListener.onRemove(ip);
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
     * A listener interface for internal parameter actions.
     * Implement this in your controller to receive button events.
     */
    public interface InternalUnitListener {
        void onEdit(InternalParameter ip);
        void onRemove(InternalParameter ip);
    }
}