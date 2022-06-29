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

public class MilterSessionFactoryBuilder<A extends SocketAddress> {

  public static <A extends SocketAddress> MilterSessionFactoryBuilder<A> builder() {
    return new MilterSessionFactoryBuilder<>();
  }

  ConnectionFactory<A> factory;

  public MilterSessionFactory create() {
    return new MilterSessionFactoryImpl<>(this);
  }

  MilterSessionFactoryBuilder<A> factory(ConnectionFactory<A> val) {
    factory = val;
    return this;
  }
}
