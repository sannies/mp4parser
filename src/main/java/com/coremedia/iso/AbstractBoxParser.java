package com.coremedia.iso;

import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * This BoxParser handles the basic stuff like reading size and extracting box type.
 */
public abstract class AbstractBoxParser implements BoxParser {

    private static Logger LOG = Logger.getLogger(AbstractBoxParser.class.getName());

    public abstract AbstractBox createBox(byte[] type, byte[] userType, byte[] parent, Box lastMovieFragmentBox);

    /**
     * Parses the next size and type, creates a box instance and parses the box's content.
     *
     * @param in                   the IsoBufferWrapper pointing to the ISO file
     * @param parent               the current box's parent (null if no parent)
     * @param lastMovieFragmentBox
     * @return the box just parsed
     * @throws java.io.IOException if reading from <code>in</code> fails
     */
    public AbstractBox parseBox(IsoBufferWrapper in, ContainerBox parent, Box lastMovieFragmentBox) throws IOException {
        long offset = in.position();

        long size = in.readUInt32();
        // do plausibility check
        if (size < 8 && size > 1) {
            LOG.severe("Plausibility check failed: size < 8 (size = " + size + "). Stop parsing!");
            return null;
        } else if ((offset + size) > in.size()) {
            LOG.severe("Plausibility check failed: offset + size > file size (size = " + size + "). Stop parsing!");
            return null;
        }


        byte[] type = in.read(4);

        byte[] usertype = null;
        long contentSize;

        if (size == 1) {
            size = in.readUInt64();
            contentSize = size - 16;
        } else if (size == 0) {
            //throw new RuntimeException("Not supported!");
            contentSize = -1;
            size = 1;
        } else {
            contentSize = size - 8;
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes("uuid"))) {
            usertype = in.read(16);
            contentSize -= 16;
        }
        AbstractBox box = createBox(type, usertype,
                parent.getType(), lastMovieFragmentBox);
        box.offset = offset;
        box.setParent((ContainerBox) parent);
        LOG.finest("Creating " + IsoFile.bytesToFourCC(box.getType()) + " box: (" + box.getDisplayName() + ")");
        // System.out.println("parsing " + Arrays.toString(box.getType()) + " " + box.getClass().getName() + " size=" + size);
        box.parse(in, contentSize, this, lastMovieFragmentBox);
        // System.out.println("box = " + box);
        if (in.position() - offset < size && contentSize != -1) {
            // System.out.println("dead bytes found in " + box);
            LOG.info(IsoFile.bytesToFourCC(type) + " has dead bytes");
            long length = (size - (in.position() - offset));
            box.setDeadBytes(in.getSegment(in.position(), length));
            in.skip(length);
        }


        assert size == box.getSize() : "Reconstructed Size is not equal to the number of parsed bytes! (" + box.getDisplayName() + ")"
                + " Actual Box size: " + size + " Calculated size: " + box.getSize();
        return box;
    }


}
