package org.mp4parser.boxes.iso23009.part1;

import org.mp4parser.support.AbstractFullBox;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;
import org.mp4parser.tools.Utf8;

import java.nio.ByteBuffer;

/**
 * The Event Message box ('emsg') provides signalling for generic events related to the media
 * presentation time.
 */
public class EventMessageBox extends AbstractFullBox {
    public static final String TYPE = "emsg";

    String schemeIdUri;
    String value;
    long timescale;
    long presentationTimeDelta;
    long eventDuration;
    long id;
    byte[] messageData;

    public EventMessageBox() {
        super(TYPE);
    }

    @Override
    protected void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        schemeIdUri = IsoTypeReader.readString(content);
        value = IsoTypeReader.readString(content);
        timescale = IsoTypeReader.readUInt32(content);
        presentationTimeDelta = IsoTypeReader.readUInt32(content);
        eventDuration = IsoTypeReader.readUInt32(content);
        id = IsoTypeReader.readUInt32(content);
        messageData = new byte[content.remaining()];
        content.get(messageData);
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUtf8String(byteBuffer, schemeIdUri);
        IsoTypeWriter.writeUtf8String(byteBuffer, value);
        IsoTypeWriter.writeUInt32(byteBuffer, timescale);
        IsoTypeWriter.writeUInt32(byteBuffer, presentationTimeDelta);
        IsoTypeWriter.writeUInt32(byteBuffer, eventDuration);
        IsoTypeWriter.writeUInt32(byteBuffer, id);
        byteBuffer.put(messageData);
    }

    @Override
    protected long getContentSize() {
        return 22 + Utf8.utf8StringLengthInBytes(schemeIdUri) + Utf8.utf8StringLengthInBytes(value) + messageData.length;
    }

    public String getSchemeIdUri() {
        return schemeIdUri;
    }

    public void setSchemeIdUri(String schemeIdUri) {
        this.schemeIdUri = schemeIdUri;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getTimescale() {
        return timescale;
    }

    public void setTimescale(long timescale) {
        this.timescale = timescale;
    }

    public long getPresentationTimeDelta() {
        return presentationTimeDelta;
    }

    public void setPresentationTimeDelta(long presentationTimeDelta) {
        this.presentationTimeDelta = presentationTimeDelta;
    }

    public long getEventDuration() {
        return eventDuration;
    }

    public void setEventDuration(long eventDuration) {
        this.eventDuration = eventDuration;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte[] getMessageData() {
        return messageData;
    }

    public void setMessageData(byte[] messageData) {
        this.messageData = messageData;
    }
}
