package com.googlecode.mp4parser.stuff;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.*;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.util.Path;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Change metadata and make sure chunkoffsets are corrected.
 */
public class MetaDataInsert {


    public static void main(String[] args) throws IOException {
        MetaDataInsert cmd = new MetaDataInsert();
        cmd.writeRandomMetadata("c:\\content\\pbs_sba101d_browser_multi.wvm_0.mp4", "lore ipsum tralalala");

    }

    public FileChannel splitFileAndInsert(File f, long pos, long length) throws IOException {
        FileChannel read = new RandomAccessFile(f, "r").getChannel();
        File tmp = File.createTempFile("ChangeMetaData", "splitFileAndInsert");
        FileChannel tmpWrite = new RandomAccessFile(tmp, "rw").getChannel();
        read.position(pos);
        tmpWrite.transferFrom(read, 0, read.size() - pos);
        read.close();
        FileChannel write = new RandomAccessFile(f, "rw").getChannel();
        write.position(pos + length);
        tmpWrite.position(0);
        long transferred = 0;
        while ((transferred += tmpWrite.transferTo(0, tmpWrite.size()-transferred, write))!=tmpWrite.size()) {
            System.out.println(transferred);
        }
        System.out.println(transferred);
        tmpWrite.close();
        tmp.delete();
        return write;
    }


    private boolean needsOffsetCorrection(IsoFile isoFile) {
        if (Path.getPath(isoFile, "/moov[0]/mvex[0]") != null) {
            // Fragmented files don't need a correction
            return false;
        } else {
            // no correction needed if mdat is before moov as insert into moov want change the offsets of mdat
            return Path.getPath(isoFile, "/moov[0]").getOffset() <Path.getPath(isoFile, "/mdat[0]").getOffset();
        }
    }

    public void writeRandomMetadata(String videoFilePath, String text) throws IOException {

        File videoFile = new File(videoFilePath);
        if (!videoFile.exists()) {
            throw new FileNotFoundException("File " + videoFilePath + " not exists");
        }

        if (!videoFile.canWrite()) {
            throw new IllegalStateException("No write permissions to file " + videoFilePath);
        }
        IsoFile isoFile = new IsoFile(new FileDataSourceImpl(videoFilePath));

        MovieBox moov = isoFile.getBoxes(MovieBox.class).get(0);
        boolean correctOffset = needsOffsetCorrection(isoFile);
        long sizeBefore = moov.getSize();
        long offset = moov.getOffset();

        UserDataBox userDataBox;

        if ((userDataBox = Path.getPath(moov, "udta")) == null) {
            userDataBox = new UserDataBox();
            moov.addBox(userDataBox);
        }
        MetaBox metaBox;
        if ((metaBox = Path.getPath(userDataBox, "meta")) == null) {
            metaBox = new MetaBox();
            userDataBox.addBox(metaBox);
        }
        XmlBox xmlBox = new XmlBox();
        xmlBox.setXml(text);
        metaBox.addBox(xmlBox);
        long sizeAfter = moov.getSize();
        if (correctOffset) {
            correctChunkOffsets(moov, sizeAfter - sizeBefore);
        }
        BetterByteArrayOutputStream baos = new BetterByteArrayOutputStream();
        moov.getBox(Channels.newChannel(baos));
        isoFile.close();
        FileChannel fc = splitFileAndInsert(videoFile, offset, sizeAfter - sizeBefore);
        fc.position(offset);
        fc.write(ByteBuffer.wrap(baos.getBuffer(), 0, baos.size()));
        fc.close();
    }

    private static class BetterByteArrayOutputStream extends ByteArrayOutputStream {
        byte[] getBuffer() {
            return buf;
        }
    }

    private void correctChunkOffsets(MovieBox movieBox, long correction) {
        List<ChunkOffsetBox> chunkOffsetBoxes = Path.getPaths((Box) movieBox, "trak/mdia[0]/minf[0]/stbl[0]/stco[0]");
        if (chunkOffsetBoxes.size() == 0) {
            chunkOffsetBoxes = Path.getPaths((Box) movieBox, "trak/mdia[0]/minf[0]/stbl[0]/st64[0]");
        }
        for (ChunkOffsetBox chunkOffsetBox : chunkOffsetBoxes) {
            long[] cOffsets = chunkOffsetBox.getChunkOffsets();
            for (int i = 0; i < cOffsets.length; i++) {
                cOffsets[i] += correction;
            }
        }
    }


}
