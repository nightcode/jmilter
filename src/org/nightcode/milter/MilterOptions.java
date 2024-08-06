/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nightcode.milter;

import org.nightcode.milter.util.ConfigOption;

public enum MilterOptions implements ConfigOption {

  NETTY_LOGGING_ENABLED     ("jmilter.netty.loggingEnabled"),
  NETTY_LOG_LEVEL           ("jmilter.netty.logLevel"),
  NETTY_NUMBER_OF_THREADS   ("jmilter.netty.nThreads"),
  NETTY_FAIL_STOP_MODE      ("jmilter.netty.failStopMode"),
  NETTY_CONNECT_TIMEOUT_MS  ("jmilter.netty.connectTimeoutMs"),
  NETTY_RECONNECT_TIMEOUT_MS("jmilter.netty.reconnectTimeoutMs"),
  NETTY_AUTO_READ           ("jmilter.netty.autoRead"),
  NETTY_KEEP_ALIVE          ("jmilter.netty.keepAlive"),
  NETTY_TCP_NO_DELAY        ("jmilter.netty.tcpNoDelay"),
  NETTY_REUSE_ADDRESS       ("jmilter.netty.reuseAddress"),
  NETTY_SO_BACKLOG          ("jmilter.netty.soBacklog"),
  ;

  private final String key;

  MilterOptions(String key) {
    this.key = key;
  }

  @Override public String key() {
    return key;
  }
}
