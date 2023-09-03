package com.windhoverlabs.yamcs.applications.parameter;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.phoebus.framework.preferences.AnnotatedPreferences;
import org.phoebus.framework.preferences.Preference;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;

@SuppressWarnings("nls")
public class ParameterExport implements AppDescriptor {

  public static final String Name = "ParameterExport";

  public static final String DisplayName = Messages.DisplayName;

  public static final Logger log = Logger.getLogger(ParameterExport.class.getPackageName());

  @Preference public static String css_path;

  static {
    AnnotatedPreferences.initialize(ParameterExport.class, "/eventviewer_preferences.properties");
  }

  @Override
  public String getName() {
    return Name;
  }

  @Override
  public AppInstance create() {

    if (ParameterExportViewerInstance.INSTANCE == null) {
      try {
        ParameterExportViewerInstance.INSTANCE = new ParameterExportViewerInstance(this);
      } catch (Exception ex) {
        Logger.getLogger(ParameterExport.class.getPackageName())
            .log(Level.WARNING, "Cannot create Error Log", ex);
        return null;
      }
    } else {
      ParameterExportViewerInstance.INSTANCE.raise();
    }
    return ParameterExportViewerInstance.INSTANCE;
  }
}
