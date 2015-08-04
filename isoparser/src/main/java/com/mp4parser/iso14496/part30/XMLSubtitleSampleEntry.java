package com.mp4parser.iso14496.part30;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.coremedia.iso.Utf8;
import com.coremedia.iso.boxes.sampleentry.AbstractSampleEntry;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.util.Mp4Arrays;

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
        dataSource.read((ByteBuffer) byteBuffer.rewind());
        byteBuffer.position(6);
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
        byteBuffer.position(6);
        IsoTypeWriter.writeUInt16(byteBuffer, dataReferenceIndex);
        IsoTypeWriter.writeZeroTermUtf8String(byteBuffer, namespace);
        IsoTypeWriter.writeZeroTermUtf8String(byteBuffer, schemaLocation);
        IsoTypeWriter.writeZeroTermUtf8String(byteBuffer, auxiliaryMimeTypes);
        writableByteChannel.write((ByteBuffer) byteBuffer.rewind());
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

