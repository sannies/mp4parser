package org.mp4parser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class SkipBox implements ParsableBox {

    private String type;
    private long size;
    private long sourcePosition = -1;
    
    public SkipBox(String type, byte[] usertype, String parentType) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public long getContentSize() {
        return size-8;
    }
    
    /**
     * Get the seekable position of the content for this box within the source data.
     * @return The data offset, or -1 if it is not known
     */
    public long getSourcePosition() {
        return sourcePosition;
    }
    
    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        throw new RuntimeException("Cannot retrieve a skipped box - type "+type);
    }

    public void parse(ReadableByteChannel dataSource, ByteBuffer header, long contentSize, BoxParser boxParser)
            throws IOException {
        this.size = contentSize+8;
        
        if( dataSource instanceof FileChannel ) {
            FileChannel seekable = (FileChannel) dataSource;
            sourcePosition = seekable.position();
            long newPosition = sourcePosition + contentSize;
            seekable.position(newPosition);
        }
        else {
            throw new RuntimeException("Cannot skip box "+type+" if data source is not seekable");
        }
    }

}
