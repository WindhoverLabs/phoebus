package com.windhoverlabs.commander.core;

import org.yamcs.client.YamcsClient;
import org.yamcs.client.processor.ProcessorClient;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class YamcsObject<T extends YamcsObject<?>> {

	public YamcsObject(String name) {
		setName(name);
	}

	private final StringProperty name = new SimpleStringProperty();

	public final StringProperty nameProperty() {
		return this.name;
	}

	public final String getName() {
		return this.nameProperty().get();
	}

	public final void setName(final String name) {
		this.nameProperty().set(name);
	}

	private final ObservableList<T> items = FXCollections.observableArrayList();

	public ObservableList<T> getItems() {
		return items;
	}

	public abstract void createAndAddChild(String name);

	public abstract String getObjectType();
}
