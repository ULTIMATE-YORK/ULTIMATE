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

public class ExternalParameterCell extends ListCell<ExternalParameter> {

    private ExternalUnitListener externalUnitListener;

    public ExternalParameterCell() {
        widthProperty().addListener((obs, oldW, newW) -> {
            if (newW.doubleValue() > 0 && getGraphic() != null) {
                requestLayout();
            }
        });
    }

    public void setExternalUnitListener(ExternalUnitListener listener) {
        this.externalUnitListener = listener;
    }

    @Override
    protected void updateItem(ExternalParameter ep, boolean empty) {
        super.updateItem(ep, empty);

        if (empty || ep == null) {
            setText(null);
            setGraphic(null);
        } else {
            setPadding(Insets.EMPTY);

            Label details = new Label(ep.toString());
            details.setWrapText(true);
            details.setMaxWidth(Double.MAX_VALUE);

            VBox leftColumn = new VBox(details);
            leftColumn.setMaxWidth(Double.MAX_VALUE);

            Button editButton = new Button("Edit");
            Button removeButton = new Button("Remove");

            editButton.setOnAction(e -> {
                if (externalUnitListener != null) externalUnitListener.onEdit(ep);
            });
            removeButton.setOnAction(e -> {
                if (externalUnitListener != null) externalUnitListener.onRemove(ep);
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

            HBox.setHgrow(leftColumn, Priority.ALWAYS);

            setGraphic(cellBox);
        }
    }

    @Override
    protected double computePrefHeight(double width) {
        if (getGraphic() != null) {
            Insets ins = getPadding();
            double w = width > 0 ? width : getWidth();
            if (w > 0) {
                return getGraphic().prefHeight(w - ins.getLeft() - ins.getRight()) + ins.getTop() + ins.getBottom();
            }
        }
        return super.computePrefHeight(width);
    }

    public interface ExternalUnitListener {
        void onEdit(ExternalParameter ep);
        void onRemove(ExternalParameter ep);
    }
}
