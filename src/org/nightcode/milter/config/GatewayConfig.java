package org.nightcode.milter.config;

public class GatewayConfig {

  private String address = "127.0.0.1";
  private int port;

  private String tcpLogLevel = "INFO";
  private boolean tcpLoggingEnabled = false;

  public String getAddress() {
    return address;
  }

  public int getPort() {
    return port;
  }

  public String getTcpLogLevel() {
    return tcpLogLevel;
  }

  public boolean isTcpLoggingEnabled() {
    return tcpLoggingEnabled;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setTcpLogLevel(String tcpLogLevel) {
    this.tcpLogLevel = tcpLogLevel;
  }

  public void setTcpLoggingEnabled(boolean tcpLoggingEnabled) {
    this.tcpLoggingEnabled = tcpLoggingEnabled;
  }

  @Override public String toString() {
    return "GatewayConfig{"
        + "address='" + address + '\''
        + ", port=" + port
        + ", tcpLogLevel='" + tcpLogLevel + '\''
        + ", tcpLoggingEnabled=" + tcpLoggingEnabled
        + '}';
  }
}
