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

package org.nightcode.milter.samples;

import org.nightcode.common.base.Hexs;
import org.nightcode.milter.AbstractMilterHandler;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.net.MilterPacket;
import org.nightcode.milter.util.Actions;
import org.nightcode.milter.util.ProtocolSteps;

import java.net.InetAddress;
import java.util.List;

import javax.annotation.Nullable;

public class AddHeaderMilterHandler extends AbstractMilterHandler {

  private static final Hexs HEX = Hexs.hex();

  AddHeaderMilterHandler(Actions milterActions, ProtocolSteps milterProtocolSteps) {
    super(milterActions, milterProtocolSteps);
  }

  @Override public void connect(MilterContext context, String hostname, @Nullable InetAddress address)
      throws MilterException {
    logger.debug("<CONNECT> hostname: %s, address: %s", hostname, address);
    super.connect(context, hostname, address);
  }

  @Override public void helo(MilterContext context, String helohost) throws MilterException {
    logger.debug("<HELO> helohost: %s", helohost);
    super.helo(context, helohost);
  }

  @Override public void envfrom(MilterContext context, List<String> from) throws MilterException {
    logger.debug("<ENVFROM> from: %s", from);
    super.envfrom(context, from);
  }

  @Override public void envrcpt(MilterContext context, List<String> recipients) throws MilterException {
    logger.debug("<ENVRCPT> recipients: %s", recipients);
    super.envrcpt(context, recipients);
  }

  @Override public void header(MilterContext context, String headerName, String headerValue) throws MilterException {
    logger.debug("<HEADER> headerName: %s, headerValue: %s", headerName, headerValue);
    super.header(context, headerName, headerValue);
  }

  @Override public void eoh(MilterContext context) throws MilterException {
    logger.debug("<EOH>");
    super.eoh(context);
  }

  @Override public void body(MilterContext context, String bodyChunk) throws MilterException {
    logger.debug("<BODY> bodyChunk: %s", bodyChunk);
    super.body(context, bodyChunk);
  }

  @Override public void eom(MilterContext context, @Nullable String bodyChunk) throws MilterException {
    logger.debug("<EOM> bodyChunk: %s", bodyChunk);

    messageModificationService.addHeader(context, "X-Received", "Tue, 31 Oct 2018 17:56:00 -0700 (PDT)");

    super.eom(context, bodyChunk);
  }

  @Override public void abort(MilterContext context, MilterPacket packet) throws MilterException {
    logger.debug("<ABORT> abort: %s", packet);
    super.abort(context, packet);
  }

  @Override public void close(MilterContext context) {
    logger.debug("<CLOSE>");
  }

  @Override public void data(MilterContext context, byte[] payload) throws MilterException {
    logger.debug("<DATA>");
    super.data(context, payload);
  }

  @Override public void negotiate(MilterContext context, int mtaProtocolVersion, Actions mtaActions,
      ProtocolSteps mtaProtocolSteps) throws MilterException {
    logger.debug("<NEGOTIATE> %s, %s, %s", mtaProtocolVersion, mtaActions, mtaProtocolSteps);
    super.negotiate(context, mtaProtocolVersion, mtaActions, mtaProtocolSteps);
  }

  @Override public void unknown(MilterContext context, byte[] payload) throws MilterException {
    logger.debug("<UNKNOWN> unknown: %s", payload.length > 0 ? HEX.fromByteArray(payload) : "NULL");
    super.unknown(context, payload);
  }
}
