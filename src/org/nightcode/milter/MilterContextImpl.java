package org.nightcode.milter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.nightcode.milter.net.MilterPacket;
import org.nightcode.milter.net.MilterPacketSender;
import org.nightcode.milter.util.Actions;
import org.nightcode.milter.util.IntMap;
import org.nightcode.milter.util.Log;
import org.nightcode.milter.util.MilterPacketUtil;
import org.nightcode.milter.util.ProtocolSteps;

import static java.lang.String.format;

public class MilterContextImpl implements MilterContext {

  private static final int PROTOCOL_VERSION = 6;

  private final UUID id;

  private final Actions milterActions;
  private final ProtocolSteps milterProtocolSteps;
  private final MilterPacketSender milterPacketSender;

  private volatile int mtaProtocolVersion;
  private volatile Actions mtaActions;
  private volatile ProtocolSteps mtaProtocolSteps;

  private volatile int sessionProtocolVersion;
  private volatile ProtocolSteps sessionProtocolSteps;
  private volatile MilterState sessionState;

  private final IntMap<Map<String, String>> macros = new IntMap<>();

  public MilterContextImpl(Actions milterActions, ProtocolSteps milterProtocolSteps, MilterPacketSender milterPacketSender) {
    this.milterActions = milterActions;
    this.milterProtocolSteps = milterProtocolSteps;
    this.milterPacketSender = milterPacketSender;

    this.id = UUID.randomUUID();
  }

  @Override public void destroy() {
    milterPacketSender.close();
  }

  @Override public Map<String, String> getMacros(int type) {
    return macros.get(type);
  }

  @Override public Actions getMtaActions() {
    return mtaActions;
  }

  @Override public ProtocolSteps getMtaProtocolSteps() {
    return mtaProtocolSteps;
  }

  @Override public int getMtaProtocolVersion() {
    return mtaProtocolVersion;
  }

  @Override public ProtocolSteps getSessionProtocolSteps() {
    return sessionProtocolSteps;
  }

  @Override public int getSessionProtocolVersion() {
    return sessionProtocolVersion;
  }

  @Override public MilterState getSessionState() {
    return sessionState;
  }

  @Override public UUID id() {
    return id;
  }

  @Override public Actions milterActions() {
    return milterActions;
  }

  @Override public ProtocolSteps milterProtocolSteps() {
    return milterProtocolSteps;
  }

  @Override public int milterProtocolVersion() {
    return PROTOCOL_VERSION;
  }

  @Override public void sendContinue() throws MilterException {
    sendPacket(MilterPacketUtil.SMFIS_CONTINUE);
  }

  @Override public void sendPacket(MilterPacket packet) throws MilterException {
    int noReplyBit = getSessionState().noReplyBit();
    if (noReplyBit != 0 && ((getSessionProtocolSteps().bitmap() & noReplyBit) != 0)) {
      Log.debug().log(getClass()
          , () -> format("NR bit has non-zero value for state %s but attempt to send packet has been caught", sessionState));
      if ((milterProtocolSteps().bitmap() & noReplyBit) != 0
          && (getMtaProtocolSteps().bitmap() & noReplyBit) == 0) {
        Log.debug().log(getClass(), () -> format("MTA doesn't support NR for state %s, trying to send SMFIR_CONTINUE", sessionState));
        sendPacket0(MilterPacketUtil.SMFIS_CONTINUE);
      }
      return;
    }

    sendPacket0(packet);
  }

  @Override public void setMacros(int type, Map<String, String> macros) {
    synchronized (this.macros) {
      this.macros.put(type, macros);
    }
  }

  @Override public void setMtaActions(Actions mtaActions) {
    this.mtaActions = mtaActions;
  }

  @Override public void setMtaProtocolSteps(ProtocolSteps mtaProtocolSteps) {
    this.mtaProtocolSteps = mtaProtocolSteps;
  }

  @Override public void setMtaProtocolVersion(int mtaProtocolVersion) {
    this.mtaProtocolVersion = mtaProtocolVersion;
  }

  @Override public void setSessionProtocolSteps(ProtocolSteps sessionProtocolSteps) {
    this.sessionProtocolSteps = sessionProtocolSteps;
  }

  @Override public void setSessionProtocolVersion(int sessionProtocolVersion) {
    this.sessionProtocolVersion = sessionProtocolVersion;
  }

  @Override public void setSessionState(MilterState sessionState) {
    this.sessionState = sessionState;
  }

  private void sendPacket0(MilterPacket packet) throws MilterException {
    try {
      milterPacketSender.send(packet);
    } catch (IOException ex) {
      throw new MilterException("unable to send packet: " + packet, ex);
    }
  }
}
