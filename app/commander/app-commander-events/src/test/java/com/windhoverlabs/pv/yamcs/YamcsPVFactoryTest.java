package com.windhoverlabs.pv.yamcs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import org.junit.Test;

public class YamcsPVFactoryTest {
  @Test
  public void testExtractServerName() {
    String testPV = "Server_A:yamcs-cfs://cfs/CPD/amc/AMC_HkTlm_t.usCmdCnt";
    assertThat(YamcsPVFactory.extractServerName(testPV), equalTo("Server_A"));
  }
}
