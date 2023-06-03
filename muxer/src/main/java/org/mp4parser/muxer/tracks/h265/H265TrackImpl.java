package org.mp4parser.muxer.tracks.h265;

import org.mp4parser.Container;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.BitReaderBuffer;
import org.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import org.mp4parser.boxes.iso14496.part15.HevcConfigurationBox;
import org.mp4parser.boxes.iso14496.part15.HevcDecoderConfigurationRecord;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.boxes.sampleentry.VisualSampleEntry;
import org.mp4parser.muxer.*;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.tracks.AbstractH26XTrack;
import org.mp4parser.tools.ByteBufferByteChannel;
import org.mp4parser.tools.IsoTypeReader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.*;

/**
 * Takes a raw H265 stream and muxes into an MP4.
 */
public class H265TrackImpl extends AbstractH26XTrack implements H265NalUnitTypes {

    ArrayList<ByteBuffer> sps = new ArrayList<ByteBuffer>();
    ArrayList<ByteBuffer> pps = new ArrayList<ByteBuffer>();
    ArrayList<ByteBuffer> vps = new ArrayList<ByteBuffer>();
    ArrayList<Sample> samples = new ArrayList<Sample>();

    VisualSampleEntry visualSampleEntry;

    public H265TrackImpl(DataSource dataSource) throws IOException {
        super(dataSource);
        ArrayList<ByteBuffer> nals = new ArrayList<ByteBuffer>();
        LookAhead la = new LookAhead(dataSource);
        ByteBuffer nal;
        boolean[] vclNalUnitSeenInAU = new boolean[]{false};
        boolean[] isIdr = new boolean[]{true};

        visualSampleEntry = new VisualSampleEntry("hvc1");
        visualSampleEntry.setDataReferenceIndex(1);
        visualSampleEntry.setDepth(24);
        visualSampleEntry.setFrameCount(1);
        visualSampleEntry.setHorizresolution(72);
        visualSampleEntry.setVertresolution(72);
        visualSampleEntry.setWidth(640);
        visualSampleEntry.setHeight(480);
        visualSampleEntry.setCompressorname("HEVC Coding");

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
                    ((Buffer)nal).position(0);
                    pps.add(nal.slice());
                    // TODO: skip 2 bytes, remove emulation prevention bytes, add PPS parser, parse PPS
                    System.err.println("Stored PPS");
                    break;
                case NAL_TYPE_VPS_NUT:
                    ((Buffer)nal).position(0);
                    vps.add(nal.slice());
                    // TODO: skip 2 bytes, remove emulation prevention bytes, parse VPS
                    // parsedVPS = new VideoParemeterSet(Channels.newInputStream(new ByteBufferByteChannel(nal.slice())));
                    System.err.println("Stored VPS");
                    break;
                case NAL_TYPE_SPS_NUT:
                    ((Buffer)nal).position(0);
                    sps.add(nal.slice());
                    // TODO: skip 2 bytes, remove emulation prevention bytes, parse SPS
                    // parsedSPS = new SequenceParameterSetRbsp(Channels.newInputStream(new ByteBufferByteChannel(nal.slice())));
                    System.err.println("Stored SPS");
                    break;
                case NAL_TYPE_PREFIX_SEI_NUT:
                    ((Buffer)nal).position(2);
                    new SEIMessage(new BitReaderBuffer(nal.slice()));
                    break;
            }


            switch (unitHeader.nalUnitType) {
                // for hvc1 these must be in mdat!!! Otherwise the video is not playable.
                // case NAL_TYPE_SPS_NUT:
                // case NAL_TYPE_VPS_NUT:
                // case NAL_TYPE_PPS_NUT:
                case NAL_TYPE_EOB_NUT:
                case NAL_TYPE_EOS_NUT:
                case NAL_TYPE_AUD_NUT:
                case NAL_TYPE_FD_NUT:
                    // ignore these
                    break;
                default:
                    System.err.println("Adding " + unitHeader.nalUnitType);
                    nal.position(0);
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
        visualSampleEntry = fillSampleEntry();
        decodingTimes = new long[samples.size()];
        getTrackMetaData().setTimescale(25);
        Arrays.fill(decodingTimes, 1);
    }


    @Override
    protected SampleEntry getCurrentSampleEntry() {
        return visualSampleEntry;
    }

    public static H265NalUnitHeader getNalUnitHeader(ByteBuffer nal) {
        ((Buffer)nal).position(0);
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

    private VisualSampleEntry fillSampleEntry() {

        HevcConfigurationBox hevcConfigurationBox = new HevcConfigurationBox();
        hevcConfigurationBox.getHevcDecoderConfigurationRecord().setConfigurationVersion(1);
        hevcConfigurationBox.getHevcDecoderConfigurationRecord().setLengthSizeMinusOne(3); // 4 bytes size block inserted in between NAL units
        // TODO: fill in other metadata from parsed VPS/SPS
        
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
        vpsArray.nal_unit_type = NAL_TYPE_VPS_NUT;
        vpsArray.nalUnits = new ArrayList<byte[]>();
        for (ByteBuffer vp : vps) {
            vpsArray.nalUnits.add(toArray(vp));
        }

        // correct order is VPS, SPS, PPS. Other order produced ffmpeg errors such as "VPS 0 does not exist" and "SPS 0 does not exist."
        hevcConfigurationBox.getArrays().addAll(Arrays.asList(vpsArray, spsArray, ppsArray));

        visualSampleEntry.addBox(hevcConfigurationBox);

        trackMetaData.setCreationTime(new Date());
        trackMetaData.setModificationTime(new Date());
        trackMetaData.setLanguage("enu");
        trackMetaData.setTimescale(25);
        // TODO: fill in other metadata from parsed VPS/SPS

        return visualSampleEntry;
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

    public List<SampleEntry> getSampleEntries() {
        return Collections.<SampleEntry>singletonList(visualSampleEntry);
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
