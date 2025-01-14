package controllers;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import persistent_objects.Property;

public class Properties {
	
	private String font = "-fx-font-size: 30px;";

	@FXML private ListView<Property> propertyListView;
	@FXML private VBox propertyVBox;
	
	private ObservableList<Property> properties = FXCollections.observableArrayList();
	
	@FXML
	private void initialize() {
		setUpPropertyListView();
	}
	
	private void setUpPropertyListView() {
	    propertyListView.setItems(properties);

	    // Bind visibility and manage properties to the emptiness of the list
	    propertyVBox.visibleProperty().bind(Bindings.isNotEmpty(properties));
	    propertyVBox.managedProperty().bind(propertyVBox.visibleProperty());
	}
}
