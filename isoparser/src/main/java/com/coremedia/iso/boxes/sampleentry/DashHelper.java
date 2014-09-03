package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.Hex;
import com.coremedia.iso.boxes.OriginalFormatBox;
import com.coremedia.iso.boxes.h264.AvcConfigurationBox;
import com.googlecode.mp4parser.boxes.AC3SpecificBox;
import com.googlecode.mp4parser.boxes.DTSSpecificBox;
import com.googlecode.mp4parser.boxes.EC3SpecificBox;
import com.googlecode.mp4parser.boxes.MLPSpecificBox;
import com.googlecode.mp4parser.boxes.mp4.ESDescriptorBox;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.AudioSpecificConfig;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.DecoderConfigDescriptor;
import com.googlecode.mp4parser.util.Path;

import java.util.List;

/**
 * Gets the precise MIME type according to RFC6381.
 * http://tools.ietf.org/html/rfc6381
 */
public final class DashHelper {

    public static class ChannelConfiguration {
        public String schemeIdUri = "";
        public String value = "";
    }

    public static ChannelConfiguration getChannelConfiguration(AudioSampleEntry e) {
        DTSSpecificBox ddts = Path.getPath(e, "ddts");
        if (ddts != null) {
            return getDTSChannelConfig(e, ddts);
        }
        MLPSpecificBox dmlp = Path.getPath(e, "dmlp");
        if (dmlp != null) {
            return null; // getMLPChannelConfig(e, dmlp);
        }
        ESDescriptorBox esds = Path.getPath(e, "esds");
        if (esds != null) {
            return getAACChannelConfig(e, esds);
        }
        esds = Path.getPath(e, "..../esds"); // Apple does weird things
        if (esds != null) {
            return getAACChannelConfig(e, esds);
        }
        AC3SpecificBox dac3 = Path.getPath(e, "dac3");
        if (dac3 != null) {
            return getAC3ChannelConfig(e, dac3);
        }
        EC3SpecificBox dec3 = Path.getPath(e, "dec3");
        if (dec3 != null) {
            return getEC3ChannelConfig(e, dec3);
        }

        return null;
    }

    private static ChannelConfiguration getEC3ChannelConfig(AudioSampleEntry e, EC3SpecificBox dec3) {
        final List<EC3SpecificBox.Entry> ec3SpecificBoxEntries = dec3.getEntries();
        int audioChannelValue = 0;
        for (EC3SpecificBox.Entry ec3SpecificBoxEntry : ec3SpecificBoxEntries) {
            audioChannelValue |= getDolbyAudioChannelValue(ec3SpecificBoxEntry.acmod, ec3SpecificBoxEntry.lfeon);
        }
        ChannelConfiguration cc = new ChannelConfiguration();
        cc.value = Hex.encodeHex(new byte[]{(byte) ((audioChannelValue >> 8) & 0xFF), (byte) (audioChannelValue & 0xFF)});
        cc.schemeIdUri = "urn:dolby:dash:audio_channel_configuration:2011";
        return cc;
    }

    private static ChannelConfiguration getAC3ChannelConfig(AudioSampleEntry e, AC3SpecificBox dac3) {
        ChannelConfiguration cc = new ChannelConfiguration();
        int audioChannelValue = getDolbyAudioChannelValue(dac3.getAcmod(), dac3.getLfeon());
        cc.value = Hex.encodeHex(new byte[]{(byte) ((audioChannelValue >> 8) & 0xFF), (byte) (audioChannelValue & 0xFF)});
        cc.schemeIdUri = "urn:dolby:dash:audio_channel_configuration:2011";
        return cc;
    }

    private static int getDolbyAudioChannelValue(int acmod, int lfeon) {
        int audioChannelValue;
        switch (acmod) {
            case 0:
                audioChannelValue = 0xA000;
                break;
            case 1:
                audioChannelValue = 0x4000;
                break;
            case 2:
                audioChannelValue = 0xA000;
                break;
            case 3:
                audioChannelValue = 0xE000;
                break;
            case 4:
                audioChannelValue = 0xA100;
                break;
            case 5:
                audioChannelValue = 0xE100;
                break;
            case 6:
                audioChannelValue = 0xB800;
                break;
            case 7:
                audioChannelValue = 0xF800;
                break;
            default:
                throw new RuntimeException("Unexpected acmod " + acmod);
        }
        if (lfeon == 1) {
            audioChannelValue += 1;
        }
        return audioChannelValue;
    }

    private static ChannelConfiguration getAACChannelConfig(AudioSampleEntry e, ESDescriptorBox esds) {

        final DecoderConfigDescriptor decoderConfigDescriptor = esds.getEsDescriptor().getDecoderConfigDescriptor();
        final AudioSpecificConfig audioSpecificConfig = decoderConfigDescriptor.getAudioSpecificInfo();
        ChannelConfiguration cc = new ChannelConfiguration();
        cc.schemeIdUri = "urn:mpeg:dash:23003:3:audio_channel_configuration:2011";
        cc.value = String.valueOf(audioSpecificConfig.getChannelConfiguration());
        return cc;
    }

    private static int getNumChannels(DTSSpecificBox dtsSpecificBox) {
        final int channelLayout = dtsSpecificBox.getChannelLayout();
        int numChannels = 0;
        int dwChannelMask = 0;
        if ((channelLayout & 0x0001) == 0x0001) {
            //0001h Center in front of listener 1
            numChannels += 1;
            dwChannelMask |= 0x00000004; //SPEAKER_FRONT_CENTER
        }
        if ((channelLayout & 0x0002) == 0x0002) {
            //0002h Left/Right in front 2
            numChannels += 2;
            dwChannelMask |= 0x00000001; //SPEAKER_FRONT_LEFT
            dwChannelMask |= 0x00000002; //SPEAKER_FRONT_RIGHT
        }
        if ((channelLayout & 0x0004) == 0x0004) {
            //0004h Left/Right surround on side in rear 2
            numChannels += 2;
            //* if Lss, Rss exist, then this position is equivalent to Lsr, Rsr respectively
            dwChannelMask |= 0x00000010; //SPEAKER_BACK_LEFT
            dwChannelMask |= 0x00000020; //SPEAKER_BACK_RIGHT
        }
        if ((channelLayout & 0x0008) == 0x0008) {
            //0008h Low frequency effects subwoofer 1
            numChannels += 1;
            dwChannelMask |= 0x00000008; //SPEAKER_LOW_FREQUENCY
        }
        if ((channelLayout & 0x0010) == 0x0010) {
            //0010h Center surround in rear 1
            numChannels += 1;
            dwChannelMask |= 0x00000100; //SPEAKER_BACK_CENTER
        }
        if ((channelLayout & 0x0020) == 0x0020) {
            //0020h Left/Right height in front 2
            numChannels += 2;
            dwChannelMask |= 0x00001000; //SPEAKER_TOP_FRONT_LEFT
            dwChannelMask |= 0x00004000; //SPEAKER_TOP_FRONT_RIGHT
        }
        if ((channelLayout & 0x0040) == 0x0040) {
            //0040h Left/Right surround in rear 2
            numChannels += 2;
            dwChannelMask |= 0x00000010; //SPEAKER_BACK_LEFT
            dwChannelMask |= 0x00000020; //SPEAKER_BACK_RIGHT
        }
        if ((channelLayout & 0x0080) == 0x0080) {
            //0080h Center Height in front 1
            numChannels += 1;
            dwChannelMask |= 0x00002000; //SPEAKER_TOP_FRONT_CENTER
        }
        if ((channelLayout & 0x0100) == 0x0100) {
            //0100h Over the listener’s head 1
            numChannels += 1;
            dwChannelMask |= 0x00000800; //SPEAKER_TOP_CENTER
        }
        if ((channelLayout & 0x0200) == 0x0200) {
            //0200h Between left/right and center in front 2
            numChannels += 2;
            dwChannelMask |= 0x00000040; //SPEAKER_FRONT_LEFT_OF_CENTER
            dwChannelMask |= 0x00000080; //SPEAKER_FRONT_RIGHT_OF_CENTER
        }
        if ((channelLayout & 0x0400) == 0x0400) {
            //0400h Left/Right on side in front 2
            numChannels += 2;
            dwChannelMask |= 0x00000200; //SPEAKER_SIDE_LEFT
            dwChannelMask |= 0x00000400; //SPEAKER_SIDE_RIGHT
        }
        if ((channelLayout & 0x0800) == 0x0800) {
            //0800h Left/Right surround on side 2
            numChannels += 2;
            //* if Lss, Rss exist, then this position is equivalent to Lsr, Rsr respectively
            dwChannelMask |= 0x00000010; //SPEAKER_BACK_LEFT
            dwChannelMask |= 0x00000020; //SPEAKER_BACK_RIGHT
        }
        if ((channelLayout & 0x1000) == 0x1000) {
            //1000h Second low frequency effects subwoofer 1
            numChannels += 1;
            dwChannelMask |= 0x00000008; //SPEAKER_LOW_FREQUENCY
        }
        if ((channelLayout & 0x2000) == 0x2000) {
            //2000h Left/Right height on side 2
            numChannels += 2;
            dwChannelMask |= 0x00000010; //SPEAKER_BACK_LEFT
            dwChannelMask |= 0x00000020; //SPEAKER_BACK_RIGHT
        }
        if ((channelLayout & 0x4000) == 0x4000) {
            //4000h Center height in rear 1
            numChannels += 1;
            dwChannelMask |= 0x00010000; //SPEAKER_TOP_BACK_CENTER
        }
        if ((channelLayout & 0x8000) == 0x8000) {
            //8000h Left/Right height in rear 2
            numChannels += 2;
            dwChannelMask |= 0x00008000; //SPEAKER_TOP_BACK_LEFT
            dwChannelMask |= 0x00020000; //SPEAKER_TOP_BACK_RIGHT
        }
        if ((channelLayout & 0x10000) == 0x10000) {
            //10000h Center below in front
            numChannels += 1;
        }
        if ((channelLayout & 0x20000) == 0x20000) {
            //20000h Left/Right below in front
            numChannels += 2;
        }
        return numChannels;
    }

    private static ChannelConfiguration getDTSChannelConfig(AudioSampleEntry e, DTSSpecificBox ddts) {
        ChannelConfiguration cc = new ChannelConfiguration();
        cc.value = Integer.toString(getNumChannels(ddts));
        cc.schemeIdUri = "urn:dts:dash:audio_channel_configuration:2012";
        return cc;
    }


    /**
     * Gets the codec according to RFC 6381 from a <code>SampleEntry</code>.
     *
     * @param se <code>SampleEntry</code> to id the codec.
     * @return codec according to RFC
     */
    public static String getRfc6381Codec(SampleEntry se) {

        String type = se.getType();
        if (type.equals("encv")) {
            OriginalFormatBox frma = ((VisualSampleEntry) se).getBoxes(OriginalFormatBox.class, true).get(0);
            type = frma.getDataFormat();
        } else if (type.equals("enca")) {
            OriginalFormatBox frma = ((AudioSampleEntry) se).getBoxes(OriginalFormatBox.class, true).get(0);
            type = frma.getDataFormat();
        }

        if ("avc1".equals(type)) {
            AvcConfigurationBox avcConfigurationBox = Path.getPath(se, "avcC");
            List<byte[]> spsbytes = avcConfigurationBox.getSequenceParameterSets();
            byte[] CodecInit = new byte[3];
            CodecInit[0] = spsbytes.get(0)[1];
            CodecInit[1] = spsbytes.get(0)[2];
            CodecInit[2] = spsbytes.get(0)[3];
            return (type + "." + Hex.encodeHex(CodecInit)).toLowerCase();
        } else if (type.equals("mp4a")) {
            ESDescriptorBox esDescriptorBox = Path.getPath(se, "esds");
            if (esDescriptorBox == null) {
                esDescriptorBox = Path.getPath(se, "..../esds"); // Apple does weird things
            }
            final DecoderConfigDescriptor decoderConfigDescriptor = esDescriptorBox.getEsDescriptor().getDecoderConfigDescriptor();
            final AudioSpecificConfig audioSpecificConfig = decoderConfigDescriptor.getAudioSpecificInfo();
            if (audioSpecificConfig.getSbrPresentFlag() == 1) {
                return "mp4a.40.5";
            } else if (audioSpecificConfig.getPsPresentFlag() == 1) {
                return "mp4a.40.29";
            } else {
                return "mp4a.40.2";
            }
        } else if (type.equals("dtsl") || type.equals("dtsl") || type.equals("dtse")) {
            return type;
        } else if (type.equals("ec-3") || type.equals("ac-3") || type.equals("mlpa")) {
            return type;
        } else if (type.equals("stpp")) {
            XMLSubtitleSampleEntry stpp = (XMLSubtitleSampleEntry) se;
            if (stpp.getSchemaLocation().contains("cff-tt-text-ttaf1-dfxp")) {
                return "cfft";
            } else if (stpp.getSchemaLocation().contains("cff-tt-image-ttaf1-dfxp")) {
                return "cffi";
            } else {
                return "stpp";
            }

        } else {
            throw new RuntimeException("I don't know how to get codec of type " + se.getType());
        }

    }
}
