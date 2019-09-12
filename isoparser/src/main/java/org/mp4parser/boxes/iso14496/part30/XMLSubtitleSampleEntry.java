package org.mp4parser.boxes.iso14496.part30;

import org.mp4parser.BoxParser;
import org.mp4parser.boxes.sampleentry.AbstractSampleEntry;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;
import org.mp4parser.tools.Mp4Arrays;
import org.mp4parser.tools.Utf8;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class XMLSubtitleSampleEntry extends AbstractSampleEntry {

    public static final String TYPE = "stpp";

    private String namespace = "";
    private String schemaLocation = "";
    private String auxiliaryMimeTypes = "";

    public XMLSubtitleSampleEntry() {
        super(TYPE);
    }

    @Override
    public long getSize() {
        long s = getContainerSize();
        long t = 8 + namespace.length() + schemaLocation.length() + auxiliaryMimeTypes.length() + 3;
        return s + t + ((largeBox || (s + t + 8) >= (1L << 32)) ? 16 : 8);
    }

    @Override
    public void parse(ReadableByteChannel dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        dataSource.read((ByteBuffer) ((Buffer)byteBuffer).rewind());
        ((Buffer)byteBuffer).position(6);
        dataReferenceIndex = IsoTypeReader.readUInt16(byteBuffer);


        byte[] namespaceBytes = new byte[0];
        int read;
        while ((read = Channels.newInputStream(dataSource).read()) != 0) {
            namespaceBytes = Mp4Arrays.copyOfAndAppend(namespaceBytes, (byte) read);
        }
        namespace = Utf8.convert(namespaceBytes);


        byte[] schemaLocationBytes = new byte[0];

        while ((read = Channels.newInputStream(dataSource).read()) != 0) {
            schemaLocationBytes = Mp4Arrays.copyOfAndAppend(schemaLocationBytes, (byte) read);
        }
        schemaLocation = Utf8.convert(schemaLocationBytes);


        byte[] auxiliaryMimeTypesBytes = new byte[0];

        while ((read = Channels.newInputStream(dataSource).read()) != 0) {
            auxiliaryMimeTypesBytes = Mp4Arrays.copyOfAndAppend(auxiliaryMimeTypesBytes, (byte) read);
        }
        auxiliaryMimeTypes = Utf8.convert(auxiliaryMimeTypesBytes);

        initContainer(dataSource, contentSize - (header.remaining() + namespace.length() + schemaLocation.length() + auxiliaryMimeTypes.length() + 3), boxParser);
    }

    @Override
    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        writableByteChannel.write(getHeader());
        ByteBuffer byteBuffer = ByteBuffer.allocate(8 + namespace.length() + schemaLocation.length() + auxiliaryMimeTypes.length() + 3);
        ((Buffer)byteBuffer).position(6);
        IsoTypeWriter.writeUInt16(byteBuffer, dataReferenceIndex);
        IsoTypeWriter.writeZeroTermUtf8String(byteBuffer, namespace);
        IsoTypeWriter.writeZeroTermUtf8String(byteBuffer, schemaLocation);
        IsoTypeWriter.writeZeroTermUtf8String(byteBuffer, auxiliaryMimeTypes);
        writableByteChannel.write((ByteBuffer) ((Buffer)byteBuffer).rewind());
        writeContainer(writableByteChannel);
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    public String getAuxiliaryMimeTypes() {
        return auxiliaryMimeTypes;
    }

    public void setAuxiliaryMimeTypes(String auxiliaryMimeTypes) {
        this.auxiliaryMimeTypes = auxiliaryMimeTypes;
    }
}

