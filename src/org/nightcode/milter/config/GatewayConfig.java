package org.nightcode.milter.config;

public class GatewayConfig {

  private String address = "127.0.0.1";
  private int port;

  private String logLevel = "INFO";
  private boolean loggingEnabled = false;

  public String getAddress() {
    return address;
  }

  public int getPort() {
    return port;
  }

  public String getLogLevel() {
    return logLevel;
  }

  public boolean isLoggingEnabled() {
    return loggingEnabled;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setLogLevel(String logLevel) {
    this.logLevel = logLevel;
  }

  public void setLoggingEnabled(boolean loggingEnabled) {
    this.loggingEnabled = loggingEnabled;
  }

  @Override public String toString() {
    return "GatewayConfig{"
        + "address='" + address + '\''
        + ", port=" + port
        + ", logLevel='" + logLevel + '\''
        + ", loggingEnabled=" + loggingEnabled
        + '}';
  }
}
