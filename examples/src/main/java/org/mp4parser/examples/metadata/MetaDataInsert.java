package org.mp4parser.examples.metadata;


import org.mp4parser.Box;
import org.mp4parser.Container;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.apple.AppleItemListBox;
import org.mp4parser.boxes.apple.AppleNameBox;
import org.mp4parser.boxes.iso14496.part12.*;
import org.mp4parser.tools.Path;

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
        cmd.writeRandomMetadata("c:\\content\\Mobile_H264 - Kopie.mp4",
                "lore ipsum tralalala");

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
        while ((transferred += tmpWrite.transferTo(0, tmpWrite.size() - transferred, write)) != tmpWrite.size()) {
            System.out.println(transferred);
        }
        System.out.println(transferred);
        tmpWrite.close();
        tmp.delete();
        return write;
    }


    private boolean needsOffsetCorrection(IsoFile isoFile) {
        if (Path.getPath(isoFile, "moov[0]/mvex[0]") != null) {
            // Fragmented files don't need a correction
            return false;
        } else {
            // no correction needed if mdat is before moov as insert into moov want change the offsets of mdat
            for (Box box : isoFile.getBoxes()) {
                if ("moov".equals(box.getType())) {
                    return true;
                }
                if ("mdat".equals(box.getType())) {
                    return false;
                }
            }
            throw new RuntimeException("I need moov or mdat. Otherwise all this doesn't make sense");
        }
    }

    public void writeRandomMetadata(String videoFilePath, String title) throws IOException {

        File videoFile = new File(videoFilePath);
        if (!videoFile.exists()) {
            throw new FileNotFoundException("File " + videoFilePath + " not exists");
        }

        if (!videoFile.canWrite()) {
            throw new IllegalStateException("No write permissions to file " + videoFilePath);
        }
        IsoFile isoFile = new IsoFile(videoFilePath);

        MovieBox moov = isoFile.getBoxes(MovieBox.class).get(0);
        FreeBox freeBox = findFreeBox(moov);

        boolean correctOffset = needsOffsetCorrection(isoFile);
        long sizeBefore = moov.getSize();
        long offset = 0;
        for (Box box : isoFile.getBoxes()) {
            if ("moov".equals(box.getType())) {
                break;
            }
            offset += box.getSize();
        }

        // Create structure or just navigate to Apple List Box.
        UserDataBox userDataBox;
        if ((userDataBox = Path.getPath(moov, "udta")) == null) {
            userDataBox = new UserDataBox();
            moov.addBox(userDataBox);
        }
        MetaBox metaBox;
        if ((metaBox = Path.getPath(userDataBox, "meta")) == null) {
            metaBox = new MetaBox();
            HandlerBox hdlr = new HandlerBox();
            hdlr.setHandlerType("mdir");
            metaBox.addBox(hdlr);
            userDataBox.addBox(metaBox);
        }
        AppleItemListBox ilst;
        if ((ilst = Path.getPath(metaBox, "ilst")) == null) {
            ilst = new AppleItemListBox();
            metaBox.addBox(ilst);

        }
        if (freeBox == null) {
            freeBox = new FreeBox(128 * 1024);
            metaBox.addBox(freeBox);
        }
        // Got Apple List Box

        AppleNameBox nam;
        if ((nam = Path.getPath(ilst, "©nam")) == null) {
            nam = new AppleNameBox();
        }
        nam.setDataCountry(0);
        nam.setDataLanguage(0);
        nam.setValue(title);
        ilst.addBox(nam);

        long sizeAfter = moov.getSize();
        long diff = sizeAfter - sizeBefore;
        // This is the difference of before/after

        // can we compensate by resizing a Free Box we have found?
        if (freeBox.getData().limit() > diff) {
            // either shrink or grow!
            freeBox.setData(ByteBuffer.allocate((int) (freeBox.getData().limit() - diff)));
            sizeAfter = moov.getSize();
            diff = sizeAfter - sizeBefore;
        }
        if (correctOffset && diff != 0) {
            correctChunkOffsets(moov, diff);
        }
        BetterByteArrayOutputStream baos = new BetterByteArrayOutputStream();
        moov.getBox(Channels.newChannel(baos));
        isoFile.close();
        FileChannel fc;
        if (diff != 0) {
            // this is not good: We have to insert bytes in the middle of the file
            // and this costs time as it requires re-writing most of the file's data
            fc = splitFileAndInsert(videoFile, offset, sizeAfter - sizeBefore);
        } else {
            // simple overwrite of something with the file
            fc = new RandomAccessFile(videoFile, "rw").getChannel();
        }
        fc.position(offset);
        fc.write(ByteBuffer.wrap(baos.getBuffer(), 0, baos.size()));
        fc.close();
    }

    FreeBox findFreeBox(Container c) {
        for (Box box : c.getBoxes()) {
            System.err.println(box.getType());
            if (box instanceof FreeBox) {
                return (FreeBox) box;
            }
            if (box instanceof Container) {
                FreeBox freeBox = findFreeBox((Container) box);
                if (freeBox != null) {
                    return freeBox;
                }
            }
        }
        return null;
    }

    private void correctChunkOffsets(MovieBox movieBox, long correction) {
        List<ChunkOffsetBox> chunkOffsetBoxes = Path.getPaths((Box) movieBox, "trak/mdia[0]/minf[0]/stbl[0]/stco[0]");
        if (chunkOffsetBoxes.isEmpty()) {
            chunkOffsetBoxes = Path.getPaths((Box) movieBox, "trak/mdia[0]/minf[0]/stbl[0]/st64[0]");
        }
        for (ChunkOffsetBox chunkOffsetBox : chunkOffsetBoxes) {
            long[] cOffsets = chunkOffsetBox.getChunkOffsets();
            for (int i = 0; i < cOffsets.length; i++) {
                cOffsets[i] += correction;
            }
        }
    }

    private static class BetterByteArrayOutputStream extends ByteArrayOutputStream {
        byte[] getBuffer() {
            return buf;
        }
    }


}
