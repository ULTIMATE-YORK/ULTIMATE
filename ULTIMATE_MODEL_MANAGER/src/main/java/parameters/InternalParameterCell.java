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

public class InternalParameterCell extends ListCell<InternalParameter> {

    private InternalUnitListener internalUnitListener;

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

            VBox leftColumn = new VBox(2);
            leftColumn.setPadding(new Insets(3, 0, 3, 3));
            for (String line : ip.toString().split("\n")) {
                if (!line.trim().isEmpty()) {
                    Label lbl = new Label(line);
                    lbl.setWrapText(true);
                    lbl.setMaxWidth(Double.MAX_VALUE);
                    if (getListView() != null) {
                        lbl.prefWidthProperty().bind(getListView().widthProperty().subtract(120));
                    }
                    leftColumn.getChildren().add(lbl);
                }
            }
            leftColumn.setMaxWidth(Double.MAX_VALUE);

            Button editButton = new Button("Edit");
            Button removeButton = new Button("Remove");
            editButton.setOnAction(e -> {
                if (internalUnitListener != null) internalUnitListener.onEdit(ip);
            });
            removeButton.setOnAction(e -> {
                if (internalUnitListener != null) internalUnitListener.onRemove(ip);
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

    public interface InternalUnitListener {
        void onEdit(InternalParameter ip);
        void onRemove(InternalParameter ip);
    }
}
