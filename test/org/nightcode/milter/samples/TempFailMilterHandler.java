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

package org.nightcode.milter.samples;

import org.nightcode.milter.AbstractMilterHandler;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.Actions;
import org.nightcode.milter.util.Log;
import org.nightcode.milter.ProtocolSteps;

import static org.nightcode.milter.util.MilterPackets.SMFIS_TEMPFAIL;

public class TempFailMilterHandler extends AbstractMilterHandler {

  protected TempFailMilterHandler(Actions milterActions, ProtocolSteps milterProtocolSteps) {
    super(milterActions, milterProtocolSteps);
  }

  @Override public void eoh(MilterContext context) throws MilterException {
    Log.info().log(getClass(), "sending TEMPFAIL..");
    context.sendPacket(SMFIS_TEMPFAIL);
  }

  @Override public void quit(MilterContext context) {
    Log.debug().log(getClass(), "<CLOSE>");
  }
}
