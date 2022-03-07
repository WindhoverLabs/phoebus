package com.windhoverlabs.yamcs.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.windhoverlabs.yamcs.core.AbstractIntegrationTest.MyConnectionListener;
import com.windhoverlabs.yamcs.core.AbstractIntegrationTest.PacketProvider;
import com.windhoverlabs.yamcs.core.AbstractIntegrationTest.ParameterProvider;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.yamcs.YConfiguration;
import org.yamcs.client.ClientException;
import org.yamcs.client.YamcsClient;
import org.yamcs.utils.FileUtils;
import org.yamcs.utils.TimeEncoding;

/**
 * Simulated YAMCS with no users.
 *
 * @author lgomez
 */
public class IntegrationTestNoUsers extends AbstractIntegrationTest {

  static {
    // LoggingUtils.enableLogging();
  }

  /**
   * Add @BeforeAll to subclasses
   *
   * @throws Exception
   */
  public static void setupYamcs() throws Exception {
    yamcsInstance = "IntegrationTestNoUsers";
    yamcsPort = 9191;

    Path dataDir = Paths.get("/tmp/yamcs-IntegrationTestNoUsers-data");
    FileUtils.deleteRecursivelyIfExists(dataDir);
    YConfiguration.setupTest("IntegrationTestNoUsers");

    yamcs = org.yamcs.YamcsServer.getServer();
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

    yamcsClient = YamcsClient.newBuilder(yamcsHost, yamcsPort).withUserAgent("it-junit").build();
    yamcsClient.addConnectionListener(connectionListener);

    yamcsClient.connectWebSocket();

    packetGenerator =
        com.windhoverlabs.yamcs.core.AbstractIntegrationTest.PacketProvider.instance[0]
            .mdbPacketGenerator;
    packetGenerator.setGenerationTime(TimeEncoding.INVALID_INSTANT);
    packetGenerator2 = PacketProvider.instance[1].mdbPacketGenerator;
    packetGenerator2.setGenerationTime(TimeEncoding.INVALID_INSTANT);

    yamcs
        .getInstance(yamcsInstance)
        .getProcessor("realtime")
        .getParameterProcessorManager()
        .getAlarmServer()
        .clearAll();
  }
}
