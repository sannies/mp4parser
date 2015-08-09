package com.mp4parser.muxer.tracks.h265;

import com.mp4parser.Container;
import com.mp4parser.muxer.*;
import com.mp4parser.muxer.builder.DefaultMp4Builder;
import com.mp4parser.muxer.tracks.AbstractH26XTrack;
import com.mp4parser.boxes.iso14496.part1.objectdescriptors.BitReaderBuffer;
import com.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import com.mp4parser.boxes.iso14496.part15.HevcConfigurationBox;
import com.mp4parser.boxes.iso14496.part15.HevcDecoderConfigurationRecord;
import com.mp4parser.boxes.sampleentry.VisualSampleEntry;
import com.mp4parser.tools.ByteBufferByteChannel;
import com.mp4parser.tools.IsoTypeReader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Takes a raw H265 stream and muxes into an MP4.
 */
public class H265TrackImpl extends AbstractH26XTrack implements H265NalUnitTypes {

    ArrayList<ByteBuffer> sps = new ArrayList<ByteBuffer>();
    ArrayList<ByteBuffer> pps = new ArrayList<ByteBuffer>();
    ArrayList<ByteBuffer> vps = new ArrayList<ByteBuffer>();
    ArrayList<Sample> samples = new ArrayList<Sample>();

    SampleDescriptionBox stsd;

    public H265TrackImpl(DataSource dataSource) throws IOException {
        super(dataSource);
        ArrayList<ByteBuffer> nals = new ArrayList<ByteBuffer>();
        LookAhead la = new LookAhead(dataSource);
        ByteBuffer nal;
        boolean[] vclNalUnitSeenInAU = new boolean[]{false};
        boolean[] isIdr = new boolean[]{true};


        while ((nal = findNextNal(la)) != null) {

            H265NalUnitHeader unitHeader = getNalUnitHeader(nal);
            //
            if (vclNalUnitSeenInAU[0]) { // we need at least 1 VCL per AU
                // This branch checks if we encountered the start of a samples/AU
                if (isVcl(unitHeader)) {
                    if ((nal.get(2) & -128) != 0) { // this is: first_slice_segment_in_pic_flag  u(1)
                        wrapUp(nals, vclNalUnitSeenInAU, isIdr);
                    }
                } else {
                    switch (unitHeader.nalUnitType) {
                        case NAL_TYPE_PREFIX_SEI_NUT:
                        case NAL_TYPE_AUD_NUT:
                        case NAL_TYPE_PPS_NUT:
                        case NAL_TYPE_VPS_NUT:
                        case NAL_TYPE_SPS_NUT:
                        case NAL_TYPE_RSV_NVCL41:
                        case NAL_TYPE_RSV_NVCL42:
                        case NAL_TYPE_RSV_NVCL43:
                        case NAL_TYPE_RSV_NVCL44:
                        case NAL_TYPE_UNSPEC48:
                        case NAL_TYPE_UNSPEC49:
                        case NAL_TYPE_UNSPEC50:
                        case NAL_TYPE_UNSPEC51:
                        case NAL_TYPE_UNSPEC52:
                        case NAL_TYPE_UNSPEC53:
                        case NAL_TYPE_UNSPEC54:
                        case NAL_TYPE_UNSPEC55:

                        case NAL_TYPE_EOB_NUT: // a bit special but also causes a sample to be formed
                        case NAL_TYPE_EOS_NUT:
                            wrapUp(nals, vclNalUnitSeenInAU, isIdr);
                            break;
                    }
                }
            }
            // collect sps/vps/pps
            switch (unitHeader.nalUnitType) {
                case NAL_TYPE_PPS_NUT:
                    nal.position(2);
                    pps.add(nal.slice());
                    System.err.println("Stored PPS");
                    break;
                case NAL_TYPE_VPS_NUT:
                    nal.position(2);
                    vps.add(nal.slice());
                    System.err.println("Stored VPS");
                    break;
                case NAL_TYPE_SPS_NUT:
                    nal.position(2);
                    sps.add(nal.slice());
                    nal.position(1);
                    new SequenceParameterSetRbsp(Channels.newInputStream(new ByteBufferByteChannel(nal.slice())));
                    System.err.println("Stored SPS");
                    break;
                case NAL_TYPE_PREFIX_SEI_NUT:
                    new SEIMessage(new BitReaderBuffer(nal.slice()));
                    break;
            }


            switch (unitHeader.nalUnitType) {
                case NAL_TYPE_SPS_NUT:
                case NAL_TYPE_VPS_NUT:
                case NAL_TYPE_PPS_NUT:
                case NAL_TYPE_EOB_NUT:
                case NAL_TYPE_EOS_NUT:
                case NAL_TYPE_AUD_NUT:
                case NAL_TYPE_FD_NUT:
                    // ignore these
                    break;
                default:
                    System.err.println("Adding " + unitHeader.nalUnitType);
                    nals.add(nal);
            }
            if (isVcl(unitHeader)) {
                switch (unitHeader.nalUnitType) {
                    case NAL_TYPE_IDR_W_RADL:
                    case NAL_TYPE_IDR_N_LP:
                        isIdr[0] &= true;
                        break;
                    default:
                        isIdr[0] = false;
                }
            }

            vclNalUnitSeenInAU[0] |= isVcl(unitHeader);

        }
        stsd = createSampleDescriptionBox();
        decodingTimes = new long[samples.size()];
        getTrackMetaData().setTimescale(25);
        Arrays.fill(decodingTimes, 1);
    }

    public static H265NalUnitHeader getNalUnitHeader(ByteBuffer nal) {
        nal.position(0);
        int nal_unit_header = IsoTypeReader.readUInt16(nal);


        H265NalUnitHeader nalUnitHeader = new H265NalUnitHeader();
        nalUnitHeader.forbiddenZeroFlag = (nal_unit_header & 0x8000) >> 15;
        nalUnitHeader.nalUnitType = (nal_unit_header & 0x7E00) >> 9;
        nalUnitHeader.nuhLayerId = (nal_unit_header & 0x1F8) >> 3;
        nalUnitHeader.nuhTemporalIdPlusOne = (nal_unit_header & 0x7);
        return nalUnitHeader;
    }

    public static void main(String[] args) throws IOException {
        Track track = new H265TrackImpl(new FileDataSourceImpl("c:\\content\\test-UHD-HEVC_01_FMV_Med_track1.hvc"));
        Movie movie = new Movie();
        movie.addTrack(track);
        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
        Container c = mp4Builder.build(movie);
        c.writeContainer(new FileOutputStream("output.mp4").getChannel());

    }

    private SampleDescriptionBox createSampleDescriptionBox() {

        stsd = new SampleDescriptionBox();
        VisualSampleEntry visualSampleEntry = new VisualSampleEntry("hvc1");
        visualSampleEntry.setDataReferenceIndex(1);
        visualSampleEntry.setDepth(24);
        visualSampleEntry.setFrameCount(1);
        visualSampleEntry.setHorizresolution(72);
        visualSampleEntry.setVertresolution(72);
        visualSampleEntry.setWidth(640);
        visualSampleEntry.setHeight(480);
        visualSampleEntry.setCompressorname("HEVC Coding");

        HevcConfigurationBox hevcConfigurationBox = new HevcConfigurationBox();

        HevcDecoderConfigurationRecord.Array spsArray = new HevcDecoderConfigurationRecord.Array();
        spsArray.array_completeness = true;
        spsArray.nal_unit_type = NAL_TYPE_SPS_NUT;
        spsArray.nalUnits = new ArrayList<byte[]>();
        for (ByteBuffer sp : sps) {
            spsArray.nalUnits.add(toArray(sp));
        }

        HevcDecoderConfigurationRecord.Array ppsArray = new HevcDecoderConfigurationRecord.Array();
        ppsArray.array_completeness = true;
        ppsArray.nal_unit_type = NAL_TYPE_PPS_NUT;
        ppsArray.nalUnits = new ArrayList<byte[]>();
        for (ByteBuffer pp : pps) {
            ppsArray.nalUnits.add(toArray(pp));
        }

        HevcDecoderConfigurationRecord.Array vpsArray = new HevcDecoderConfigurationRecord.Array();
        vpsArray.array_completeness = true;
        vpsArray.nal_unit_type = NAL_TYPE_PPS_NUT;
        vpsArray.nalUnits = new ArrayList<byte[]>();
        for (ByteBuffer vp : vps) {
            vpsArray.nalUnits.add(toArray(vp));
        }

        hevcConfigurationBox.getArrays().addAll(Arrays.asList(spsArray, vpsArray, ppsArray));

        visualSampleEntry.addBox(hevcConfigurationBox);
        stsd.addBox(visualSampleEntry);

        return stsd;
    }

    public void wrapUp(List<ByteBuffer> nals, boolean[] vclNalUnitSeenInAU, boolean[] isIdr) {

        samples.add(createSampleObject(nals));
        System.err.print("Create AU from " + nals.size() + " NALs");
        if (isIdr[0]) {
            System.err.println("  IDR");
        } else {
            System.err.println();
        }
        vclNalUnitSeenInAU[0] = false;
        isIdr[0] = true;
        nals.clear();
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return null;
    }

    public String getHandler() {
        return "vide";
    }

    public List<Sample> getSamples() {
        return samples;
    }

    boolean isVcl(H265NalUnitHeader nalUnitHeader) {
        return nalUnitHeader.nalUnitType >= 0 && nalUnitHeader.nalUnitType <= 31;
    }

}
