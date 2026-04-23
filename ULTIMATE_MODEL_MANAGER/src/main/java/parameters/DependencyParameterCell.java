package parameters;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DependencyParameterCell extends ListCell<DependencyParameter> {

    private DependencyUnitListener dependencyUnitListener;

    public DependencyParameterCell() {
        // Re-evaluate cell height whenever the cell's actual width is known/changes.
        // On Linux/GTK, computePrefHeight is first called with width=-1 before the
        // cell has been sized, so we request a layout pass once the real width arrives.
        widthProperty().addListener((obs, oldW, newW) -> {
            if (newW.doubleValue() > 0 && getGraphic() != null) {
                requestLayout();
            }
        });
    }

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

            Label details = new Label(dp.toString());
            details.setWrapText(true);
            details.setMaxWidth(Double.MAX_VALUE);

            VBox leftColumn = new VBox(details);
            leftColumn.setMaxWidth(Double.MAX_VALUE);

            Button editButton = new Button("Edit");
            Button removeButton = new Button("Remove");

            editButton.setOnAction(e -> {
                if (dependencyUnitListener != null) dependencyUnitListener.onEdit(dp);
            });
            removeButton.setOnAction(e -> {
                if (dependencyUnitListener != null) dependencyUnitListener.onRemove(dp);
            });

            VBox rightColumn = new VBox(editButton, removeButton);
            rightColumn.setSpacing(5);
            rightColumn.setAlignment(Pos.CENTER);
            rightColumn.setPadding(new Insets(5));
            rightColumn.setMinWidth(Region.USE_PREF_SIZE);
            rightColumn.setMaxWidth(Region.USE_PREF_SIZE);

            editButton.setMaxWidth(Double.MAX_VALUE);
            removeButton.setMaxWidth(Double.MAX_VALUE);
            editButton.setMinHeight(25);
            removeButton.setMinHeight(25);

            HBox cellBox = new HBox(leftColumn, rightColumn);
            cellBox.setPadding(new Insets(5));

            // Let the layout engine size columns rather than binding to widthProperty(),
            // which may be 0 on first render on Linux and causes text truncation instead
            // of wrapping.
            HBox.setHgrow(leftColumn, Priority.ALWAYS);

            setGraphic(cellBox);
        }
    }

    @Override
    protected double computePrefHeight(double width) {
        if (getGraphic() != null) {
            Insets ins = getPadding();
            // width is -1 when the ListView hasn't sized us yet; fall back to actual width
            double w = width > 0 ? width : getWidth();
            if (w > 0) {
                return getGraphic().prefHeight(w - ins.getLeft() - ins.getRight()) + ins.getTop() + ins.getBottom();
            }
        }
        return super.computePrefHeight(width);
    }

    public interface DependencyUnitListener {
        void onEdit(DependencyParameter dp);
        void onRemove(DependencyParameter dp);
    }
}
