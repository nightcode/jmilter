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

package org.nightcode.milter.client;

import java.net.SocketAddress;
import java.util.Objects;

import org.nightcode.milter.Actions;
import org.nightcode.milter.ProtocolSteps;

public class MilterSessionFactoryBuilder<A extends SocketAddress> {

  public static <A extends SocketAddress> MilterSessionFactoryBuilder<A> builder() {
    return new MilterSessionFactoryBuilder<>();
  }

  ConnectionFactory<A> factory;
  int                  protocolVersion;
  Actions              actions;
  ProtocolSteps        protocolSteps;

  private int bitMask = 0x00;

  public MilterSessionFactory create() {
    requireValue(0x01, "ConnectionFactory");
    requireValue(0x01 << 1, "ProtocolVersion");
    requireValue(0x01 << 2, "Actions");
    requireValue(0x01 << 3, "ProtocolSteps");
    return new MilterSessionFactoryImpl<>(this);
  }

  public MilterSessionFactoryBuilder<A> actions(Actions val) {
    Objects.requireNonNull(val, "actions");
    bitMask |= (0x01 << 2);
    this.actions = val;
    return this;
  }

  public MilterSessionFactoryBuilder<A> factory(ConnectionFactory<A> val) {
    Objects.requireNonNull(val, "connection factory");
    bitMask |= 0x01;
    factory = val;
    return this;
  }

  public MilterSessionFactoryBuilder<A> protocolSteps(ProtocolSteps val) {
    Objects.requireNonNull(val, "protocol steps");
    bitMask |= (0x01 << 3);
    this.protocolSteps = val;
    return this;
  }

  public MilterSessionFactoryBuilder<A> protocolVersion(int val) {
    bitMask |= (0x01 << 1);
    this.protocolVersion = val;
    return this;
  }

  private void requireValue(int bitField, String object) {
    if ((bitMask & bitField) != bitField) {
      throw new IllegalStateException(object + " should be initialized");
    }
  }
}
