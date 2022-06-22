/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.nightcode.milter;

import java.util.Map;
import java.util.UUID;

import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.Actions;
import org.nightcode.milter.util.ProtocolSteps;

public interface MilterContext {

  int PROTOCOL_VERSION = 6;

  void destroy();

  Map<String, String> getMacros(int type);

  Actions getMtaActions();

  ProtocolSteps getMtaProtocolSteps();

  int getMtaProtocolVersion();

  ProtocolSteps getSessionProtocolSteps();

  int getSessionProtocolVersion();

  MilterState getSessionState();

  UUID id();

  Actions milterActions();

  ProtocolSteps milterProtocolSteps();

  int milterProtocolVersion();

  void sendContinue() throws MilterException;

  void sendPacket(MilterPacket packet) throws MilterException;

  void setMacros(int type, Map<String, String> macros);

  void setMtaActions(Actions mtaActions);

  void setMtaProtocolSteps(ProtocolSteps mtaProtocolSteps);

  void setMtaProtocolVersion(int mtaProtocolVersion);

  void setSessionProtocolSteps(ProtocolSteps sessionProtocolSteps);

  void setSessionProtocolVersion(int sessionProtocolVersion);

  void setSessionState(MilterState sessionState);
}
