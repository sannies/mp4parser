package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.BoxFactory;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.FullBox;

import java.io.IOException;

/**
 *
 */
public final class AppleLosslessSpecificBox extends FullBox {

  public static final String TYPE = "alac";
  /*
  Extradata: 32bit size 32bit tag (=alac) 32bit zero?
  32bit max sample per frame 8bit ?? (zero?) 8bit sample
  size 8bit history mult 8bit initial history 8bit kmodifier
  8bit channels? 16bit ?? 32bit max coded frame size 32bit
  bitrate? 32bit samplerate
   */
  private long maxSamplePerFrame; // 32bi
  private int unknown1; // 8bit
  private int sampleSize; // 8bit
  private int historyMult; // 8bit
  private int initialHistory; // 8bit
  private int kModifier; // 8bit
  private int channels; // 8bit
  private int unknown2; // 16bit
  private long maxCodedFrameSize; // 32bit
  private long bitRate; // 32bit
  private long sampleRate; // 32bit

  public long getMaxSamplePerFrame() {
    return maxSamplePerFrame;
  }

  public void setMaxSamplePerFrame(int maxSamplePerFrame) {
    this.maxSamplePerFrame = maxSamplePerFrame;
  }

  public int getUnknown1() {
    return unknown1;
  }

  public void setUnknown1(int unknown1) {
    this.unknown1 = unknown1;
  }

  public int getSampleSize() {
    return sampleSize;
  }

  public void setSampleSize(int sampleSize) {
    this.sampleSize = sampleSize;
  }

  public int getHistoryMult() {
    return historyMult;
  }

  public void setHistoryMult(int historyMult) {
    this.historyMult = historyMult;
  }

  public int getInitialHistory() {
    return initialHistory;
  }

  public void setInitialHistory(int initialHistory) {
    this.initialHistory = initialHistory;
  }

  public int getKModifier() {
    return kModifier;
  }

  public void setKModifier(int kModifier) {
    this.kModifier = kModifier;
  }

  public int getChannels() {
    return channels;
  }

  public void setChannels(int channels) {
    this.channels = channels;
  }

  public int getUnknown2() {
    return unknown2;
  }

  public void setUnknown2(int unknown2) {
    this.unknown2 = unknown2;
  }

  public long getMaxCodedFrameSize() {
    return maxCodedFrameSize;
  }

  public void setMaxCodedFrameSize(int maxCodedFrameSize) {
    this.maxCodedFrameSize = maxCodedFrameSize;
  }

  public long getBitRate() {
    return bitRate;
  }

  public void setBitRate(int bitRate) {
    this.bitRate = bitRate;
  }

  public long getSampleRate() {
    return sampleRate;
  }

  public void setSampleRate(int sampleRate) {
    this.sampleRate = sampleRate;
  }

  @Override
  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxFactory, lastMovieFragmentBox);
    maxSamplePerFrame = in.readUInt32();
    unknown1 = in.readUInt8();
    sampleSize = in.readUInt8();
    historyMult = in.readUInt8();
    initialHistory = in.readUInt8();
    kModifier = in.readUInt8();
    channels = in.readUInt8();
    unknown2 = in.readUInt16();
    maxCodedFrameSize = in.readUInt32();
    bitRate = in.readUInt32();
    sampleRate = in.readUInt32();
  }

  protected void getContent(IsoOutputStream os) throws IOException {
    os.writeUInt32(maxSamplePerFrame);
    os.writeUInt8(unknown1);
    os.writeUInt8(sampleSize);
    os.writeUInt8(historyMult);
    os.writeUInt8(initialHistory);
    os.writeUInt8(kModifier);
    os.writeUInt8(channels);
    os.writeUInt16(unknown2);
    os.writeUInt32(maxCodedFrameSize);
    os.writeUInt32(bitRate);
    os.writeUInt32(sampleRate);
  }


  public AppleLosslessSpecificBox() {
    super(IsoFile.fourCCtoBytes("alac"));
  }

  protected long getContentSize() {
    return 24;
  }

  public String getDisplayName() {
    return "Apple Lossless Codec Params";
  }


}
