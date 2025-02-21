package parameters;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class UnitCellUpdate extends ListCell<HBox> {
    @Override
    protected void updateItem(HBox item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            // Remove default padding from the cell to avoid extra width.
            setPadding(Insets.EMPTY);
            
            // Bind the HBox's width to the cell's width.
            item.prefWidthProperty().bind(widthProperty());
            item.maxWidthProperty().bind(widthProperty());
            // Optionally set internal padding to add some spacing inside the cell.
            item.setPadding(new Insets(5));
            
            // Ensure the cell has exactly two children: left column and right column.
            if (item.getChildren().size() >= 2 &&
                item.getChildren().get(0) instanceof Region &&
                item.getChildren().get(1) instanceof VBox) {
                
                // Left Column: Contains the label.
                Region leftColumn = (Region) item.getChildren().get(0);
                // Bind left column to 70% of the cell's width.
                leftColumn.prefWidthProperty().bind(widthProperty().multiply(0.80));
                
                // Right Column: A VBox containing two buttons.
                VBox rightColumn = (VBox) item.getChildren().get(1);
                // Bind right column to 30% of the cell's width.
                rightColumn.prefWidthProperty().bind(widthProperty().multiply(0.20));
                rightColumn.setPadding(new Insets(5));
                rightColumn.setSpacing(5);
                rightColumn.setAlignment(Pos.CENTER);
                
                // If rightColumn contains two buttons, ensure they are the same size.
                if (rightColumn.getChildren().size() >= 2 &&
                    rightColumn.getChildren().get(0) instanceof Button &&
                    rightColumn.getChildren().get(1) instanceof Button) {
                    
                    Button editButton = (Button) rightColumn.getChildren().get(0);
                    Button removeButton = (Button) rightColumn.getChildren().get(1);
                    
                    // Bind the buttons' preferred widths to the right column's width.
                    editButton.prefWidthProperty().bind(rightColumn.widthProperty());
                    removeButton.prefWidthProperty().bind(rightColumn.widthProperty());
                    
                    // Make sure they can grow if needed.
                    editButton.setMaxWidth(Double.MAX_VALUE);
                    removeButton.setMaxWidth(Double.MAX_VALUE);
                    
                    // Optionally set minimum heights to ensure they're fully visible.
                    editButton.setMinHeight(25);   // adjust as necessary
                    removeButton.setMinHeight(25); // adjust as necessary
                }
            }
            setGraphic(item);
        }
    }
}

