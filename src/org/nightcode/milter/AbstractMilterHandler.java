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

import java.net.InetAddress;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.net.MilterPacketSender;
import org.nightcode.milter.util.Actions;
import org.nightcode.milter.util.ByteArrays;
import org.nightcode.milter.util.Log;
import org.nightcode.milter.util.MilterPacketUtil;
import org.nightcode.milter.util.ProtocolSteps;

import static java.lang.String.format;
import static org.nightcode.milter.CommandCode.SMFIC_OPTNEG;

public abstract class AbstractMilterHandler implements MilterHandler {

  private final Actions milterActions;
  private final ProtocolSteps milterProtocolSteps;

  protected final MessageModificationService messageModificationService;

  protected AbstractMilterHandler(Actions milterActions, ProtocolSteps milterProtocolSteps) {
    this(milterActions, milterProtocolSteps, new MessageModificationServiceImpl());
  }

  protected AbstractMilterHandler(Actions milterActions, ProtocolSteps milterProtocolSteps,
      MessageModificationService messageModificationService) {
    this.milterActions = milterActions;
    this.milterProtocolSteps = milterProtocolSteps;
    this.messageModificationService = messageModificationService;
  }

  @Override public void connect(MilterContext context, String hostname, @Nullable InetAddress address)
      throws MilterException {
    context.sendContinue();
  }

  @Override public void helo(MilterContext context, String helohost) throws MilterException {
    context.sendContinue();
  }

  @Override public void envfrom(MilterContext context, List<String> from) throws MilterException {
    context.sendContinue();
  }

  @Override public void envrcpt(MilterContext context, List<String> recipients) throws MilterException {
    context.sendContinue();
  }

  @Override public void header(MilterContext context, String headerName, String headerValue) throws MilterException {
    context.sendContinue();
  }

  @Override public void eoh(MilterContext context) throws MilterException {
    context.sendContinue();
  }

  @Override public void body(MilterContext context, String bodyChunk) throws MilterException {
    context.sendContinue();
  }

  @Override public void eom(MilterContext context, @Nullable String bodyChunk) throws MilterException {
    context.sendContinue();
  }

  @Override public void abort(MilterContext context, @Nullable MilterPacket packet) throws MilterException {
    // do nothing
  }

  @Override public void data(MilterContext context, byte[] payload) throws MilterException {
    context.sendContinue();
  }

  @Override public void negotiate(MilterContext context, int mtaProtocolVersion, Actions mtaActions, ProtocolSteps mtaProtocolSteps)
      throws MilterException {
    Log.debug().log(getClass(), format("protocol negotiation\n"
            + "\tMTA    { Version: %s %s %s}\n"
            + "\tMilter { Version: %s %s %s}"
            , mtaProtocolVersion
            , mtaActions
            , mtaProtocolSteps
            , context.milterProtocolVersion()
            , context.milterActions()
            , context.milterProtocolSteps()
    ));

    context.setMtaProtocolVersion(mtaProtocolVersion);

    int sessionProtocolVersion = Math.min(context.milterProtocolVersion(), mtaProtocolVersion);
    context.setSessionProtocolVersion(sessionProtocolVersion);

    if (mtaActions.bitmap() == 0) {
      mtaActions = Actions.DEF_ACTIONS;
    }
    context.setMtaActions(mtaActions);

    if ((context.getMtaActions().bitmap() & context.milterActions().bitmap()) != context.milterActions().bitmap()) {
      Log.warn().log(getClass(), format("MTA %s doesn't fulfill Milter requirements %s", context.getMtaActions(), context.milterActions()));
      abortSession(context, null);
      return;
    }

    if (mtaProtocolSteps.bitmap() == 0) {
      mtaProtocolSteps = ProtocolSteps.DEF_PROTOCOL_STEPS;
    }
    context.setMtaProtocolSteps(mtaProtocolSteps);

    byte[] buffer = new byte[4];
    for (int i = 0; i < buffer.length; i++) {
      buffer[i] = (byte) (mtaProtocolSteps.array()[i] & context.milterProtocolSteps().array()[i]);
    }
    context.setSessionProtocolSteps(new ProtocolSteps(buffer, 0));

    byte[] version = ByteArrays.intToByteArray(context.getSessionProtocolVersion());
    byte[] actions = context.milterActions().array();
    byte[] protocolSteps = context.getSessionProtocolSteps().array();

    byte[] payload = new byte[version.length + actions.length + protocolSteps.length];

    System.arraycopy(version, 0, payload, 0, version.length);
    System.arraycopy(actions, 0, payload, version.length, actions.length);
    System.arraycopy(protocolSteps, 0, payload, version.length + actions.length, protocolSteps.length);

    MilterPacket response = MilterPacket.builder()
        .command(SMFIC_OPTNEG)
        .payload(payload)
        .build();
    context.sendPacket(response);
  }

  @Override public void unknown(MilterContext context, byte[] payload) throws MilterException {
    context.sendContinue();
  }

  @Override public void abortSession(MilterContext context, MilterPacket packet) {
    if (MilterPacketUtil.isMessageState(context.getSessionState())) {
      try {
        abort(context, packet);
      } catch (MilterException ex) {
        Log.info().log(getClass(), () -> format("[%s] can't execute abort command", context.id()), ex);
      }
    }
    closeSession(context);
  }

  @Override public void closeSession(MilterContext context) {
    try {
      close(context);
    } finally {
      context.destroy();
    }
  }

  @Override public MilterContext createSession(MilterPacketSender sender) {
    return new MilterContextImpl(milterActions, milterProtocolSteps, sender);
  }
}
