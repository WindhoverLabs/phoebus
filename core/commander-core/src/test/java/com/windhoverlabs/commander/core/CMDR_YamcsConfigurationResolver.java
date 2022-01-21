package com.windhoverlabs.commander.core;

import java.io.File;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yamcs.ConfigurationException;
import org.yamcs.YConfiguration;
import org.yamcs.YConfigurationResolver;

public class CMDR_YamcsConfigurationResolver implements YConfigurationResolver {
  private Object prefix = "IntegrationTest";
  private static Logger log = LoggerFactory.getLogger(YConfiguration.class.getName());

  @Override
  public InputStream getConfigurationStream(String name) throws ConfigurationException {
    InputStream is;
    if (prefix != null) {
      if ((is = CMDR_YamcsConfigurationResolver.class.getResourceAsStream("/" + prefix + name))
          != null) {
        log.debug(
            "Reading {}",
            new File(
                    CMDR_YamcsConfigurationResolver.class
                        .getResource("/" + prefix + name)
                        .getFile())
                .getAbsolutePath());
        return is;
      }
    }

    //      File configDirectory = null;
    //      // see if the users has an own version of the file
    //      if (configDirectory != null) {
    //          File f = new File(configDirectory, name);
    //          if (f.exists()) {
    //              try {
    //                  is = new FileInputStream(f);
    //                  log.debug("Reading {}", f.getAbsolutePath());
    //                  return is;
    //              } catch (FileNotFoundException e) {
    //                  throw new ConfigurationException("Cannot read file " + f, e);
    //              }
    //          }
    //      }
    //
    is = CMDR_YamcsConfigurationResolver.class.getResourceAsStream(name);
    //      if (is == null) {
    //          throw new ConfigurationNotFoundException("Cannot find resource " + name);
    //      }
    //      log.debug("Reading {}", new
    // File(YConfiguration.class.getResource(name).getFile()).getAbsolutePath());
    return is;
  }
}
