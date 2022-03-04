package com.windhoverlabs.yamcs.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.windhoverlabs.yamcs.core.AbstractIntegrationTest.MyConnectionListener;
import com.windhoverlabs.yamcs.core.AbstractIntegrationTest.PacketProvider;
import com.windhoverlabs.yamcs.core.AbstractIntegrationTest.ParameterProvider;
import org.yamcs.client.ClientException;
import org.yamcs.client.YamcsClient;
import org.yamcs.utils.TimeEncoding;

/**
 * Simulated YAMCS with no users.
 *
 * @author lgomez
 */
public class IntegrationTestNoUsers extends AbstractIntegrationTest {

  protected final String yamcsHost = "localhost";
  //  protected static int yamcsPort = 9191;
  //
  //  protected static String yamcsInstance = "IntegrationTest";

  ParameterProvider parameterProvider;
  MyConnectionListener connectionListener;
  protected YamcsClient yamcsClient;

  RefMdbPacketGenerator packetGenerator; // sends data to tm_realtime
  RefMdbPacketGenerator packetGenerator2; // sends data to tm2_realtime
  //  static org.yamcs.YamcsServer yamcs;

  static {
    // LoggingUtils.enableLogging();
  }

  /**
   * Add @BeforeAll to subclasses
   *
   * @throws Exception
   */
  //    public void beforeClass() throws Exception {
  //      yamcsInstance = "IntegrationTestNoUsers";
  //      setupYamcs();
  //    }

  //  public static void setupYamcs() throws Exception {
  //    Path dataDir = Paths.get("/tmp/yamcs-IntegrationTest-data");
  //    FileUtils.deleteRecursivelyIfExists(dataDir);
  //
  //    YConfiguration.setupTest("IntegrationTestNoUsers");
  //
  //    yamcs = org.yamcs.YamcsServer.getServer();
  //    yamcs.prepareStart();
  //    yamcs.start();
  //  }

  /**
   * Add @BeforeAll to subclasses
   *
   * @throws Exception
   */
  //  public static void configure() throws Exception {
  //    yamcsInstance = "IntegrationTestNoUsers";
  //    yamcsPort = 9191;
  //    setupYamcs();
  //  }

  public static void setupYamcs() throws Exception {
    yamcsInstance = "IntegrationTestNoUsers";
    //    yamcsPort = 9191;
    //    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    //        Path dataDir = Paths.get("/tmp/yamcs-IntegrationTest-data");
    //        FileUtils.deleteRecursivelyIfExists(dataDir);
    //
    //        YConfiguration.setupTest("IntegrationTestNoUsers");
    //
    //        yamcs = org.yamcs.YamcsServer.getServer();
    //        yamcs.prepareStart();
    //        yamcs.start();

    AbstractIntegrationTest.setupYamcs();
  }

  /**
   * Add @BeforeEach to subclasses
   *
   * @override
   * @throws ClientException
   */
  public void before() throws ClientException {
    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$7");
    parameterProvider = ParameterProvider.instance[0];
    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$8");
    assertNotNull(parameterProvider);

    connectionListener = new MyConnectionListener();
    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$9-->host:" + yamcsHost);

    yamcsClient = YamcsClient.newBuilder(yamcsHost, yamcsPort).withUserAgent("it-junit").build();
    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$10");

    yamcsClient.addConnectionListener(connectionListener);
    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$11");

    //    if (!yamcs.getSecurityStore().getGuestUser().isActive()) {
    //      yamcsClient.login(adminUsername, adminPassword);
    //    }

    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$12");

    yamcsClient.connectWebSocket();

    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$13");

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
