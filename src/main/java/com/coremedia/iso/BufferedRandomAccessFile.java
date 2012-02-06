package com.coremedia.iso;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class BufferedRandomAccessFile {
    RandomAccessFile raf;

    static final int BUFFER_SIZE = 32768; // must be power of two!

    boolean bufferDirty = false; // buffer needs to be written back to disk? (If true, reads MUST use buffer.)

    byte[] buffer = new byte[BUFFER_SIZE];
    long bufferOffset = -1; // what file offset does this buffer start at?
    int bufferLength = -1; // how many bytes of the buffer are valid? ( < BUFFER_SIZE near end of file)

    int bufferPosition = -1; // current file position in the buffer [0, BUFFER_SIZE-1]

    long fileLength; // length of the file

    /**
     * Invariant: the current file position = bufferOffset +
     * bufferPosition.  This position is always stored inside the
     * buffer, or this position is the byte after the current buffer
     * (in which case the next read will re-fill the buffer. *
     */
    public BufferedRandomAccessFile(RandomAccessFile raf) throws IOException {
        this.raf = raf;
        fileLength = raf.length();
        bufferSeek(0);
    }

    public void close() throws IOException {
        flushBuffer();
        raf.close();
    }

    public long getFilePointer() {
        return bufferOffset + bufferPosition;
    }

    public long length() throws IOException {
        return fileLength;
    }

    int max(int a, int b) {
        return a > b ? a : b;
    }

    long max(long a, long b) {
        return a > b ? a : b;
    }

    long min(long a, long b) {
        return a < b ? a : b;
    }

    public void seek(long pos) throws IOException {
        bufferSeek(pos);
    }

    /**
     * Writes the buffer if it contains any dirty data *
     */
    void flushBuffer() throws IOException {
        if (!bufferDirty)
            return;

        raf.seek(bufferOffset);
        raf.write(buffer, 0, bufferLength);

        bufferDirty = false;
    }

    /**
     * Performs a seek and fills the buffer accordingly. *
     */
    void bufferSeek(long seekOffset) throws IOException {
        flushBuffer();

        long newOffset = seekOffset - (seekOffset & (BUFFER_SIZE - 1L));
        if (newOffset == bufferOffset) {
            bufferPosition = (int) (seekOffset - bufferOffset);
            return;
        }

        bufferOffset = newOffset;
        bufferLength = (int) min(BUFFER_SIZE, fileLength - bufferOffset);
        if (bufferLength < 0)
            bufferLength = 0;
        bufferPosition = (int) (seekOffset - bufferOffset);

        // we always ask for an amount that should be exactly available.
        raf.seek(bufferOffset);
        raf.readFully(buffer, 0, bufferLength);

        // System.out.printf("%08x %08x %08x %08x\n", seekOffset, bufferOffset, bufferPosition, bufferLength);
    }

    public final int read() throws IOException {
        if (bufferOffset + bufferPosition >= fileLength)
            throw new EOFException("EOF");

        if (bufferPosition >= bufferLength)
            bufferSeek(bufferOffset + bufferPosition);

        return buffer[bufferPosition++] & 0xff;
    }

    public boolean hasMore() throws IOException {
        return bufferPosition + bufferOffset < fileLength;
    }

    public byte peek() throws IOException {
        if (bufferPosition < bufferLength)
            return buffer[bufferPosition];

        raf.seek(bufferOffset + bufferPosition);
        return raf.readByte();
    }

    public void write(int v) throws IOException {
        write((byte) (v & 0xff));
    }

    public void writeBoolean(boolean b) throws IOException {
        write((byte) (b ? 1 : 0));
    }

    public boolean readBoolean() throws IOException {
        return read() != 0;
    }

    public void writeShort(short v) throws IOException {
        write((byte) (v >> 8));
        write((byte) (v & 0xff));
    }

    public byte readByte() throws IOException {
        int v = read();

        return (byte) (v & 0xff);
    }

    public short readShort() throws IOException {
        short v = 0;
        v |= (read() << 8);
        v |= (read());

        return v;
    }

    public void readFully(byte[] b, int offset, int length) throws IOException {
        while (length > 0) {
            int bufferAvailable = bufferLength - bufferPosition;
            int thiscopy = Math.min(bufferAvailable, length);
            if (thiscopy == 0) {
                flushBuffer();

                if (bufferOffset + bufferPosition >= fileLength)
                    throw new EOFException("EOF");

                bufferSeek(bufferOffset + bufferLength);
                continue;
            }

            System.arraycopy(buffer, bufferPosition, b, offset, thiscopy);
            bufferPosition += thiscopy;
            offset += thiscopy;
            length -= thiscopy;

        }
    }

    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    public void writeInt(long v) throws IOException {
        write((byte) (v >> 24));
        write((byte) (v >> 16));
        write((byte) (v >> 8));
        write((byte) (v & 0xff));
    }

    public int readInt() throws IOException {
        int v = 0;
        v |= (read() << 24);
        v |= (read() << 16);
        v |= (read() << 8);
        v |= (read());

        return v;
    }

    public void writeLong(long v) throws IOException {
        write((byte) (v >> 56));
        write((byte) (v >> 48));
        write((byte) (v >> 40));
        write((byte) (v >> 32));
        write((byte) (v >> 24));
        write((byte) (v >> 16));
        write((byte) (v >> 8));
        write((byte) (v & 0xff));
    }

    public long readLong() throws IOException {
        long v = 0;
        v |= (((long) read()) << 56);
        v |= (((long) read()) << 48);
        v |= (((long) read()) << 40);
        v |= (((long) read()) << 32);
        v |= (((long) read()) << 24);
        v |= (((long) read()) << 16);
        v |= (((long) read()) << 8);
        v |= (((long) read()));

        return v;
    }

    public void writeFloat(float f) throws IOException {
        writeInt(Float.floatToIntBits(f));
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public void writeDouble(double f) throws IOException {
        writeLong(Double.doubleToLongBits(f));
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public void writeUTF(String s) throws IOException {
        writeShort((short) s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // XXX BUG, not compliant with DataOutput
            write(s.charAt(i) & 0xff);
        }
    }

    public String readUTF() throws IOException {
        // XXX BUG, not compliant with DataInput
        int length = readShort();

        StringBuffer sb = new StringBuffer(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) read());
        }

        return sb.toString();
    }

    /*
      public void write(byte src[], int offset, int writelen) throws IOException
      {
      bufferDirty = true;

      while (writelen > 0)
      {
      if (bufferPosition == BUFFER_SIZE)
      flushBuffer();

      // how many bytes of this write will fit in the current buffer?
      long copylen = min(writelen, BUFFER_SIZE - bufferPosition);
      System.arraycopy(src, offset, buffer, bufferPosition, (int) copylen);
      bufferPosition += copylen;

      // have we made the file longer?
      if (bufferPosition > bufferLength)
      {
      length += bufferPosition - bufferLength;
      bufferLength = bufferPosition;
      }

      // get ready for the next copy
      writelen -= copylen;
      offset += copylen;
      }
      }
    */

    public void write(byte src[], int offset, int writelen) throws IOException {
        for (int i = offset; i < offset + writelen; i++)
            write(src[i]);
    }

    public void write(byte v) throws IOException {
        bufferDirty = true;

        // they're doing a write within our current buffer.
        if (bufferPosition < bufferLength) {
            buffer[bufferPosition++] = v;
            return;
        }

        // they're increasing the size of the file, but it still fits inside our buffer
        if (bufferLength < BUFFER_SIZE) {
            buffer[bufferPosition++] = v;
            bufferLength++;
            fileLength++;
            return;
        }

        // they're doing a write, but we're out of buffer.
        flushBuffer();
        bufferSeek(bufferOffset + bufferPosition);
        write(v);
    }

    public static boolean check;

    public String readLineCheck() throws IOException {
        if (!check)
            return readLine();

        raf.seek(bufferOffset + bufferPosition);
        String s2 = raf.readLine();

        String s1 = readLine();

        System.out.println("braf: " + s1);
        System.out.println(" raf: " + s2);

        return s1;
    }

    public String readLine() throws IOException {
        StringBuilder sb = null;

        while (true) {
            int buffstart = bufferPosition;

            String piece = null;

            // suck as much out of this buffer as we can
            while (bufferPosition < bufferLength) {
                char c = (char) (buffer[bufferPosition++] & 0xff);

                if (c == '\n') {
                    piece = new String(buffer, buffstart, bufferPosition - buffstart - 1);
                    break;
                }

                if (c == '\r') {
                    piece = new String(buffer, buffstart, bufferPosition - buffstart - 1);

                    // this logic is untested.
                    if (false && bufferPosition + bufferPosition < fileLength) {
                        // consume \r\n if it appears
                        if (peek() == '\n')
                            read();
                    }
                    break;
                }
            }

            // if a piece has been created, then we have found a newline
            if (piece != null) {
                if (sb == null)
                    return piece;

                sb.append(piece);
                return sb.toString();
            }

            piece = new String(buffer, buffstart, bufferPosition - buffstart);

            if (sb == null)
                sb = new StringBuilder();

            sb.append(piece);

            // EOF?
            if (bufferOffset + bufferPosition >= fileLength) {
                // return the string so far...
                if (sb.length() > 0) {
                    return sb.toString();
                } else
                    return null; // EOF!
            }

            bufferSeek(bufferOffset + bufferPosition);
        }
    }

    public FileChannel getChannel() {
        return raf.getChannel();
    }
}