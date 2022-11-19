package com.windhoverlabs.yamcs.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.yamcs.utils.FileUtils;
import org.yamcs.utils.TimeEncoding;
import org.yamcs.YConfiguration;
import org.yamcs.client.YamcsClient;
// import com.windhoverlabs.yamcs.core.AbstractIntegrationTest.MyConnectionListener;
// import com.windhoverlabs.yamcs.core.AbstractIntegrationTest.PacketProvider;
// import com.windhoverlabs.yamcs.core.AbstractIntegrationTest.ParameterProvider;
import org.yamcs.tests.AbstractIntegrationTest;
import org.yamcs.tests.AbstractIntegrationTest.MyConnectionListener;
import org.yamcs.tests.AbstractIntegrationTest.PacketProvider;
import org.yamcs.tests.AbstractIntegrationTest.ParameterProvider;

/**
 * Simulated YAMCS with no users.
 *
 * @author lgomez
 */
public class IntegrationTestNoUsers extends AbstractIntegrationTest {

  static {
    // LoggingUtils.enableLogging();
  }
  
  static String  yamcsInstance = "IntegrationTestNoUsers";
  static int yamcsPort = 9191;

  static Path dataDir = Paths.get("/tmp/yamcs-IntegrationTestNoUsers-data");
  
  static org.yamcs.YamcsServer yamcs = org.yamcs.YamcsServer.getServer();

  /**
   * Add @BeforeAll to subclasses
   *
   * @throws Exception
   */
    public static void setupYamcs() throws Exception {
      try {
        FileUtils.deleteRecursivelyIfExists(dataDir);
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
      YConfiguration.setupTest("IntegrationTestNoUsers");
    
      yamcs.prepareStart();
      yamcs.start();

    }
  
    /**
     * Add @BeforeEach to subclasses
     *
     * @override
     * @throws ClientException
     */
    public void before() throws ClientException {
        parameterProvider = ParameterProvider.instance[0];
        assertNotNull(parameterProvider);

        connectionListener = new MyConnectionListener();
        yamcsClient = YamcsClient.newBuilder(yamcsHost, yamcsPort)
                .withUserAgent("it-junit")
                .build();
        yamcsClient.addConnectionListener(connectionListener);
        if (yamcs.getSecurityStore().isEnabled()) {
            yamcsClient.login(adminUsername, adminPassword);
        }
        yamcsClient.connectWebSocket();

        packetGenerator = PacketProvider.instance[0].mdbPacketGenerator;
        packetGenerator.setGenerationTime(TimeEncoding.INVALID_INSTANT);
        packetGenerator2 = PacketProvider.instance[1].mdbPacketGenerator;
        packetGenerator2.setGenerationTime(TimeEncoding.INVALID_INSTANT);

        yamcs.getInstance(yamcsInstance).getProcessor("realtime").getParameterProcessorManager().getAlarmServer()
                .clearAll();
    }
}
