package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

/**
 * The optional composition shift least greatest atom summarizes the calculated
 * minimum and maximum offsets between decode and composition time, as well as
 * the start and end times, for all samples. This allows a reader to determine
 * the minimum required time for decode to obtain proper presentation order without
 * needing to scan the sample table for the range of offsets. The type of the
 * composition shift least greatest atom is ‘cslg’.
 */
public class CompositionShiftLeastGreatestAtom extends AbstractFullBox {
    public CompositionShiftLeastGreatestAtom() {
        super(IsoFile.fourCCtoBytes("cslg"));
    }

    // A 32-bit unsigned integer that specifies the calculated value.
    int compositionOffsetToDisplayOffsetShift;

    // A 32-bit signed integer that specifies the calculated value.
    int leastDisplayOffset;

    // A 32-bit signed integer that specifies the calculated value.
    int greatestDisplayOffset;

    //A 32-bit signed integer that specifies the calculated value.
    int displayStartTime;

    //A 32-bit signed integer that specifies the calculated value.
    int displayEndTime;


    @Override
    protected long getContentSize() {
        return 20;
    }

    @Override
    public String getDisplayName() {
        return "Composition Shift Least Greatest Atom";
    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeInt32(compositionOffsetToDisplayOffsetShift);
        os.writeInt32(leastDisplayOffset);
        os.writeInt32(greatestDisplayOffset);
        os.writeInt32(displayStartTime);
        os.writeInt32(displayEndTime);
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        compositionOffsetToDisplayOffsetShift = in.readInt32();
        leastDisplayOffset = in.readInt32();
        greatestDisplayOffset = in.readInt32();
        displayStartTime = in.readInt32();
        displayEndTime = in.readInt32();
    }

    public int getCompositionOffsetToDisplayOffsetShift() {
        return compositionOffsetToDisplayOffsetShift;
    }

    public void setCompositionOffsetToDisplayOffsetShift(int compositionOffsetToDisplayOffsetShift) {
        this.compositionOffsetToDisplayOffsetShift = compositionOffsetToDisplayOffsetShift;
    }

    public int getLeastDisplayOffset() {
        return leastDisplayOffset;
    }

    public void setLeastDisplayOffset(int leastDisplayOffset) {
        this.leastDisplayOffset = leastDisplayOffset;
    }

    public int getGreatestDisplayOffset() {
        return greatestDisplayOffset;
    }

    public void setGreatestDisplayOffset(int greatestDisplayOffset) {
        this.greatestDisplayOffset = greatestDisplayOffset;
    }

    public int getDisplayStartTime() {
        return displayStartTime;
    }

    public void setDisplayStartTime(int displayStartTime) {
        this.displayStartTime = displayStartTime;
    }

    public int getDisplayEndTime() {
        return displayEndTime;
    }

    public void setDisplayEndTime(int displayEndTime) {
        this.displayEndTime = displayEndTime;
    }
}
