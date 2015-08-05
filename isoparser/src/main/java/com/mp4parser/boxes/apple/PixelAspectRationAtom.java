package com.mp4parser.boxes.apple;

import com.mp4parser.support.AbstractBox;

import java.nio.ByteBuffer;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * This extension specifies the height-to-width ratio of pixels found in
 * the video sample. This is a required extension for MPEG-4 and
 * uncompressed Y ́CbCr video formats when non-square pixels are used. It
 * is optional when square pixels are used.
 */
public class PixelAspectRationAtom extends AbstractBox {
    public static final String TYPE = "pasp";


    public PixelAspectRationAtom() {
        super(TYPE);
    }

    /**
     * An unsigned 32-bit integer specifying the horizontal spacing of pixels,
     * such as luma sampling instants for Y ́CbCr or YUV video.
     */
    private int hSpacing;
    /**
     * An unsigned 32-bit integer specifying the vertical spacing of pixels,
     * such as video picture lines.
     */
    private int vSpacing;


    public int gethSpacing() {
        return hSpacing;
    }

    public void sethSpacing(int hSpacing) {
        this.hSpacing = hSpacing;
    }

    public int getvSpacing() {
        return vSpacing;
    }

    public void setvSpacing(int vSpacing) {
        this.vSpacing = vSpacing;
    }

    @Override
    protected long getContentSize() {
        return 8;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        byteBuffer.putInt(hSpacing);
        byteBuffer.putInt(vSpacing);

    }

    @Override
    protected void _parseDetails(ByteBuffer content) {
        hSpacing = content.getInt();
        vSpacing = content.getInt();


    }
}
