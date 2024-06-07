package com.windhoverlabs.yamcs.applications.xtce;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.phoebus.framework.preferences.AnnotatedPreferences;
import org.phoebus.framework.preferences.Preference;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;

@SuppressWarnings("nls")
public class XTCEExport implements AppDescriptor {

  public static final String Name = "XTCEExport";

  public static final String DisplayName = Messages.DisplayName;

  public static final Logger log = Logger.getLogger(XTCEExport.class.getPackageName());

  @Preference public static String css_path;

  static {
    AnnotatedPreferences.initialize(XTCEExport.class, "/eventviewer_preferences.properties");
  }

  @Override
  public String getName() {
    return Name;
  }

  @Override
  public AppInstance create() {

    if (XTCEExportViewerInstance.INSTANCE == null) {
      try {
        XTCEExportViewerInstance.INSTANCE = new XTCEExportViewerInstance(this);
      } catch (Exception ex) {
        Logger.getLogger(XTCEExport.class.getPackageName())
            .log(Level.WARNING, "Cannot create Error Log", ex);
        return null;
      }
    } else {
      XTCEExportViewerInstance.INSTANCE.raise();
    }
    return XTCEExportViewerInstance.INSTANCE;
  }
}
