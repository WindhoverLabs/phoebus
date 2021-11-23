package com.windhoverlabs.commander.applications.connections;

import java.util.HashMap;

import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;

public class YamcsServerItemTreeCell extends TreeCell<YamcsModelItemCell> {
	String data; // Could be a node or the entire context.

	HashMap<TreeCell<YamcsModelItemCell>, TreeCell<YamcsModelItemCell>> model2ui = new HashMap<TreeCell<YamcsModelItemCell>, TreeCell<YamcsModelItemCell>>();

	public YamcsServerItemTreeCell() {
		
	}

	@Override
	protected void updateItem(final YamcsModelItemCell item, final boolean empty) {
		super.updateItem(item, empty);
		if (empty || item == null) {
			setText(null);
			setGraphic(null);
		} else {
//			setText(data);
			setGraphic(new Label(item.getData()));
		}
	}

}
