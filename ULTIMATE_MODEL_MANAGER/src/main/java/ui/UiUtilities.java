package ui;

import property.Property;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListView;

public class UiUtilities {

    public static <T> void makeListViewTextSelectable(ListView<T> listView) {
        listView.setCellFactory(lv -> new ListCell<T>() {
            private final TextArea textArea = new TextArea();
            {
                textArea.setWrapText(true);
                textArea.setMouseTransparent(false);
                textArea.setEditable(false); // selectable
                textArea.setPrefRowCount(1);
            }
            
            
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    textArea.setText(item.toString());
                    setGraphic(textArea);
                }
            }
        });
    }
}