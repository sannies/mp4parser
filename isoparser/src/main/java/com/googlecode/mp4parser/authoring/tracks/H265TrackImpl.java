package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.IsoTypeReader;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.SampleImpl;

import java.io.EOFException;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by sannies on 08.09.2014.
 */
public class H265TrackImpl {
    public static final int VPS_NUT = 32;
    public static final int SPS_NUT = 33;
    public static final int PPS_NUT = 34;
    public static final int AUD_NUT = 35;
    public static final int PREFIX_SEI_NUT = 39;
    public static final int RSV_NVCL41 = 41;
    public static final int RSV_NVCL42 = 42;
    public static final int RSV_NVCL43 = 43;
    public static final int RSV_NVCL44 = 44;
    public static final int UNSPEC48 = 48;
    public static final int UNSPEC49 = 49;
    public static final int UNSPEC50 = 50;
    public static final int UNSPEC51 = 51;
    public static final int UNSPEC52 = 52;
    public static final int UNSPEC53 = 53;
    public static final int UNSPEC54 = 54;
    public static final int UNSPEC55 = 55;
    private static final int TRAIL_N = 0;
    private static final int TRAIL_R = 1;
    private static final int TSA_N = 2;
    private static final int TSA_R = 3;
    private static final int STSA_N = 4;
    private static final int STSA_R = 5;
    private static final int RADL_N = 6;
    private static final int RADL_R = 7;
    private static final int RASL_N = 8;
    private static final int RASL_R = 9;
    private static final int BLA_W_LP = 16;
    private static final int BLA_W_RADL = 17;
    private static final int BLA_N_LP = 18;
    private static final int IDR_W_RADL = 19;
    private static final int IDR_N_LP = 20;
    private static final int CRA_NUT = 21;


    LinkedHashMap<Long, ByteBuffer> videoParamterSets = new LinkedHashMap<Long, ByteBuffer>();
    LinkedHashMap<Long, ByteBuffer> sequenceParamterSets = new LinkedHashMap<Long, ByteBuffer>();
    LinkedHashMap<Long, ByteBuffer> pictureParamterSets = new LinkedHashMap<Long, ByteBuffer>();
    List<Long> syncSamples = new ArrayList<Long>();
    List<Sample> samples = new ArrayList<Sample>();

    private static final long BUFFER = 1024 * 1024;

    class LookAhead {
        long bufferStartPos = 0;
        int inBufferPos = 0;
        DataSource dataSource;
        ByteBuffer buffer;

        long start;

        public void fillBuffer() throws IOException {
            buffer = dataSource.map(bufferStartPos, Math.min(dataSource.size() - bufferStartPos, BUFFER));
        }


        LookAhead(DataSource dataSource) throws IOException {
            this.dataSource = dataSource;
            fillBuffer();
        }

        boolean nextThreeEquals001() throws IOException {
            if (buffer.limit() - inBufferPos >= 3) {
                return (buffer.get(inBufferPos) == 0 &&
                        buffer.get(inBufferPos + 1) == 0 &&
                        buffer.get(inBufferPos + 2) == 1);
            } else {
                if (bufferStartPos + inBufferPos == dataSource.size()) {
                    throw new EOFException();
                }
                throw new RuntimeException("buffer repositioning require");
            }
        }

        boolean nextThreeEquals000or001orEof() throws IOException {
            if (buffer.limit() - inBufferPos >= 3) {
                return ((buffer.get(inBufferPos) == 0 &&
                        buffer.get(inBufferPos + 1) == 0 &&
                        (buffer.get(inBufferPos + 2) == 0 || buffer.get(inBufferPos + 2) == 1)));
            } else {
                if (bufferStartPos + inBufferPos + 3 > dataSource.size()) {
                    return bufferStartPos + inBufferPos == dataSource.size();
                } else {
                    bufferStartPos = start;
                    inBufferPos = 0;
                    fillBuffer();
                    return nextThreeEquals000or001orEof();
                }
            }
        }

        void discardByte() {
            inBufferPos++;
        }

        void discardNext3AndMarkStart() {
            inBufferPos += 3;
            start = bufferStartPos + inBufferPos;
        }

        public ByteBuffer getNal() {
            if (start >= bufferStartPos) {
                buffer.position((int) (start - bufferStartPos));
                Buffer sample = buffer.slice();
                sample.limit((int) (inBufferPos - (start - bufferStartPos)));
                return (ByteBuffer) sample;
            } else {
                throw new RuntimeException("damn! NAL exceeds buffer");
                // this can only happen if NAL is bigger than the buffer
            }

        }
    }


    private ByteBuffer findNextNal(LookAhead la) throws IOException {
        try {
            while (!la.nextThreeEquals001()) {
                la.discardByte();
            }
            la.discardNext3AndMarkStart();

            while (!la.nextThreeEquals000or001orEof()) {
                la.discardByte();
            }
            return la.getNal();
        } catch (EOFException e) {
            return null;
        }

    }

    public H265TrackImpl(DataSource ds) throws IOException {
        LookAhead la = new LookAhead(ds);

        long sampleNo = 1;
        List<ByteBuffer> accessUnit = new ArrayList<ByteBuffer>();
        int accessUnitNalType = 0;

        ByteBuffer nal;
        while ((nal = findNextNal(la)) != null) {

            NalUnitHeader nalUnitHeader = getNalUnitHeader(nal);

            //System.err.println(String.format("type: %3d - layer: %3d - tempId: %3d",
            //        nalUnitHeader.nalUnitType, nalUnitHeader.nuhLayerId, nalUnitHeader.nuhTemporalIdPlusOne));
            switch (nalUnitHeader.nalUnitType) {
                case VPS_NUT:
                    videoParamterSets.put(sampleNo, nal);
                    break;
                case SPS_NUT:
                    sequenceParamterSets.put(sampleNo, nal);
                    break;
                case PPS_NUT:
                    pictureParamterSets.put(sampleNo, nal);
                    break;
            }
            if (nalUnitHeader.nalUnitType < 32) {
                accessUnitNalType = nalUnitHeader.nalUnitType;
                // All NAL in one Access Unit Sample have same nal unit type
            }

            if (isFirstOfAU(nalUnitHeader.nalUnitType, nal, accessUnit) && !accessUnit.isEmpty()) {

                System.err.println("##########################");
                for (ByteBuffer byteBuffer : accessUnit) {
                    NalUnitHeader _nalUnitHeader = getNalUnitHeader(byteBuffer);
                    System.err.println(String.format("type: %3d - layer: %3d - tempId: %3d - size: %3d",
                            _nalUnitHeader.nalUnitType, _nalUnitHeader.nuhLayerId, _nalUnitHeader.nuhTemporalIdPlusOne, byteBuffer.limit()));
                }

                System.err.println("                          ##########################");
                samples.add(createSample(accessUnit));
                accessUnit.clear();
                sampleNo++;
            }
            accessUnit.add(nal);
            if (accessUnitNalType >= 16 && accessUnitNalType <= 21) {
                syncSamples.add(sampleNo);
            }


        }

        System.err.println("");
    }

    boolean isFirstOfAU(int nalUnitType, ByteBuffer nalUnit, List<ByteBuffer> accessUnit) {
        if (accessUnit.isEmpty()) {
            return true;
        }
        boolean vclPresentInCurrentAU = getNalUnitHeader(accessUnit.get(accessUnit.size() - 1)).nalUnitType <= 31;
        switch (nalUnitType) {
            case VPS_NUT:
            case SPS_NUT:
            case PPS_NUT:
            case AUD_NUT:
            case PREFIX_SEI_NUT:
            case RSV_NVCL41:
            case RSV_NVCL42:
            case RSV_NVCL43:
            case RSV_NVCL44:
            case UNSPEC48:
            case UNSPEC49:
            case UNSPEC50:
            case UNSPEC51:
            case UNSPEC52:
            case UNSPEC53:
            case UNSPEC54:
            case UNSPEC55:
                if (vclPresentInCurrentAU) {
                    return true;
                }
        }
        switch (nalUnitType) {
            case TRAIL_N:
            case TRAIL_R:
            case TSA_N:
            case TSA_R:
            case STSA_N:
            case STSA_R:
            case RADL_N:
            case RADL_R:
            case RASL_N:
            case RASL_R:
            case BLA_W_LP:
            case BLA_W_RADL:
            case BLA_N_LP:
            case IDR_W_RADL:
            case IDR_N_LP:
            case CRA_NUT:
                byte b[] = new byte[50];
                nalUnit.position(0);
                nalUnit.get(b);
                nalUnit.position(2);
                int firstRsbp8Bit = IsoTypeReader.readUInt8(nalUnit);

                return vclPresentInCurrentAU && (firstRsbp8Bit & 0x80) > 0;
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        new H265TrackImpl(new FileDataSourceImpl("c:\\dev\\hevc\\surfing.265"));
    }

    public enum PARSE_STATE {
        AUD_SEI_SLICE,
        SEI_SLICE,
        SLICE_OES_EOB,
    }

    public NalUnitHeader getNalUnitHeader(ByteBuffer nal) {
        nal.position(0);
        int nal_unit_header = IsoTypeReader.readUInt16(nal);


        NalUnitHeader nalUnitHeader = new NalUnitHeader();
        nalUnitHeader.forbiddenZeroFlag = (nal_unit_header & 0x8000) >> 15;
        nalUnitHeader.nalUnitType = (nal_unit_header & 0x7E00) >> 9;
        nalUnitHeader.nuhLayerId = (nal_unit_header & 0x1F8) >> 3;
        nalUnitHeader.nuhTemporalIdPlusOne = (nal_unit_header & 0x7);
        return nalUnitHeader;
    }

    public static class NalUnitHeader {
        int forbiddenZeroFlag;
        int nalUnitType;
        int nuhLayerId;
        int nuhTemporalIdPlusOne;
    }

    protected Sample createSample(List<ByteBuffer> nals) {
        byte[] sizeInfo = new byte[nals.size() * 4];
        ByteBuffer sizeBuf = ByteBuffer.wrap(sizeInfo);
        for (ByteBuffer b : nals) {
            sizeBuf.putInt(b.remaining());
        }

        ByteBuffer[] data = new ByteBuffer[nals.size() * 2];

        for (int i = 0; i < nals.size(); i++) {
            data[2 * i] = ByteBuffer.wrap(sizeInfo, i * 4, 4);
            data[2 * i + 1] = nals.get(i);
        }

        return new SampleImpl(data);
    }
}
