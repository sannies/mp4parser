package com.googlecode.mp4parser.stuff;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ChunkOffsetBox;
import com.coremedia.iso.boxes.MetaBox;
import com.coremedia.iso.boxes.StaticChunkOffsetBox;
import com.coremedia.iso.boxes.UserDataBox;
import com.coremedia.iso.boxes.XmlBox;
import com.googlecode.mp4parser.util.Path;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Change metadata and make sure chunkoffsets are corrected.
 */
public class ChangeMetaData {


    public static void main(String[] args) throws IOException {
        ChangeMetaData cmd = new ChangeMetaData();
        cmd.writeRandomMetadata("D:\\downloads\\Rectify.S01E01.HDTV.x264-2HD.mp4", "lore ipsum tralalala");
    }


    public boolean needsOffsetCorrection(IsoFile isoFile) {

        if (Path.getPaths(isoFile, "mdat").size() > 1) {
            throw new RuntimeException("There might be the weird case that a file has two mdats. One before" +
                    " moov and one after moov. That would need special handling therefore I just throw an " +
                    "exception here. ");
        }

        if (Path.getPaths(isoFile, "moof").size() > 0) {
            throw new RuntimeException("Fragmented MP4 files need correction, too. (But I would need to look where)");
        }

        for (Box box : isoFile.getBoxes()) {
            if ("mdat".equals(box.getType())) {
                return false;
            }
            if ("moov".equals(box.getType())) {
                return true;
            }
        }
        throw new RuntimeException("Hmmm - shouldn't happen");
    }

    private void writeRandomMetadata(String videoFilePath, String text) throws IOException {
        File tempFile = null;
        FileOutputStream videoFileOutputStream = null;
        IsoFile tempIsoFile = null;

        try {
            File videoFile = new File(videoFilePath);
            if (!videoFile.exists())
                throw new FileNotFoundException("File " + videoFilePath + " not exists");

            if (!videoFile.canWrite())
                throw new IllegalStateException("No write permissions to file " + videoFilePath);

            tempFile = File.createTempFile("ChangeMetaData", "");
            FileUtils.copyFile(videoFile, tempFile);

            tempIsoFile = new IsoFile(tempFile.getAbsolutePath());


            UserDataBox userDataBox;
            long sizeBefore;
            if ((userDataBox = (UserDataBox) Path.getPath(tempIsoFile, "/moov/udta")) == null) {
                sizeBefore = 0;
                userDataBox = new UserDataBox();
            } else {
                sizeBefore = userDataBox.getSize();
            }
            MetaBox metaBox;
            if ((metaBox = (MetaBox) Path.getPath(userDataBox, "meta")) == null) {
                metaBox = new MetaBox();
                userDataBox.addBox(metaBox);
            }


            XmlBox xmlBox = new XmlBox();
            xmlBox.setXml(text);
            metaBox.addBox(xmlBox);

            long sizeAfter = userDataBox.getSize();
            if (needsOffsetCorrection(tempIsoFile)) {
                correctChunkOffsets(tempIsoFile, sizeAfter - sizeBefore);
            }
            tempIsoFile.getMovieBox().addBox(userDataBox);
            videoFileOutputStream = new FileOutputStream(videoFilePath + "_mod.mp4");
            tempIsoFile.getBox(videoFileOutputStream.getChannel());
        } finally {
            IOUtils.closeQuietly(tempIsoFile);
            IOUtils.closeQuietly(videoFileOutputStream);
            FileUtils.deleteQuietly(tempFile);
        }
    }

    private void correctChunkOffsets(IsoFile tempIsoFile, long correction) {
        List<Box> chunkOffsetBoxes = Path.getPaths(tempIsoFile, "/moov[0]/trak/mdia[0]/minf[0]/stbl[0]/stco[0]");
        for (Box chunkOffsetBox : chunkOffsetBoxes) {

            LinkedList<Box> stblChildren = new LinkedList<Box>(chunkOffsetBox.getParent().getBoxes());
            stblChildren.remove(chunkOffsetBox);

            long[] cOffsets = ((ChunkOffsetBox) chunkOffsetBox).getChunkOffsets();
            for (int i = 0; i < cOffsets.length; i++) {
                cOffsets[i] += correction;
            }

            StaticChunkOffsetBox cob = new StaticChunkOffsetBox();
            cob.setChunkOffsets(cOffsets);
            stblChildren.add(cob);
            chunkOffsetBox.getParent().setBoxes(stblChildren);
        }
    }


}
