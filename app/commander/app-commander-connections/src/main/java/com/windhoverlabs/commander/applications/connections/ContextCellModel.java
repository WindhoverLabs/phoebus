package com.windhoverlabs.commander.applications.connections;

import org.epics.vtype.AlarmSeverity;
import org.phoebus.ui.pv.SeverityColors;

import com.windhoverlabs.commander.core.NodeContext;

import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class ContextCellModel<T extends NodeContext<?>> extends TreeCell<NodeContext>  {
	T context;
	String  data; //Could be a node or the entire context.
	
	public ContextCellModel(String newContextData, boolean isRoot) 
	{
		data = newContextData;
	}
	
	
    @Override
    protected void updateItem(final NodeContext item, final boolean empty)
    {
        super.updateItem(item, empty);
        if (empty  ||  item == null)
        {
            setText(null);
            setGraphic(null);
        }
        else
        {
            setText(item.toString());
//            final AlarmSeverity severity = item.getSeverity();
//            if (severity == null)
//            {
////                setGraphic(new ImageView(NO_ICON));
//                setTextFill(Color.BLACK);
//            }
//            else
            {
//                final int ordinal = severity.ordinal();
//                setGraphic(new ImageView(ALARM_ICONS[ordinal]));
//                setTextFill(SeverityColors.getTextColor(severity));
                setGraphic(new Label(data));
//                setTextFill(SeverityColors.getTextColor(severity));
            }
        }
    }
	
}
