package com.windhoverlabs.yamcs.applications.parameterviewer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.phoebus.framework.preferences.AnnotatedPreferences;
import org.phoebus.framework.preferences.Preference;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;

@SuppressWarnings("nls")
public class ParameterViewer implements AppDescriptor {

  public static final String Name = "ParameterViewer";

  public static final String DisplayName = Messages.DisplayName;

  public static final Logger log = Logger.getLogger(ParameterViewer.class.getPackageName());

  @Preference public static String css_path;

  static {
    AnnotatedPreferences.initialize(ParameterViewer.class, "/eventviewer_preferences.properties");
  }

  @Override
  public String getName() {
    return Name;
  }

  @Override
  public AppInstance create() {

    if (ParameterViewerInstance.INSTANCE == null) {
      try {
        ParameterViewerInstance.INSTANCE = new ParameterViewerInstance(this);
      } catch (Exception ex) {
        Logger.getLogger(ParameterViewer.class.getPackageName())
            .log(Level.WARNING, "Cannot create Error Log", ex);
        return null;
      }
    } else {
      ParameterViewerInstance.INSTANCE.raise();
    }
    return ParameterViewerInstance.INSTANCE;
  }
}
