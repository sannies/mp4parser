package org.mp4parser.muxer.tracks.webvtt;

import org.mp4parser.Box;
import org.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import org.mp4parser.boxes.iso14496.part30.WebVTTConfigurationBox;
import org.mp4parser.boxes.iso14496.part30.WebVTTSampleEntry;
import org.mp4parser.boxes.iso14496.part30.WebVTTSourceLabelBox;
import org.mp4parser.muxer.AbstractTrack;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.TrackMetaData;
import org.mp4parser.muxer.tracks.webvtt.sampleboxes.CuePayloadBox;
import org.mp4parser.muxer.tracks.webvtt.sampleboxes.CueSettingsBox;
import org.mp4parser.muxer.tracks.webvtt.sampleboxes.VTTCueBox;
import org.mp4parser.muxer.tracks.webvtt.sampleboxes.VTTEmptyCueBox;
import org.mp4parser.tools.ByteBufferByteChannel;
import org.mp4parser.tools.Mp4Arrays;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mp4parser.tools.CastUtils.l2i;


public class WebVttTrack extends AbstractTrack {
    private static final String WEBVTT_FILE_HEADER_STRING = "^\uFEFF?WEBVTT((\\u0020|\u0009).*)?$";
    private static final Pattern WEBVTT_FILE_HEADER =
            Pattern.compile(WEBVTT_FILE_HEADER_STRING);
    private static final String WEBVTT_METADATA_HEADER_STRING = "\\S*[:=]\\S*";
    private static final Pattern WEBVTT_METADATA_HEADER =
            Pattern.compile(WEBVTT_METADATA_HEADER_STRING);
    private static final String WEBVTT_CUE_IDENTIFIER_STRING = "^(?!.*(-->)).*$";
    private static final Pattern WEBVTT_CUE_IDENTIFIER =
            Pattern.compile(WEBVTT_CUE_IDENTIFIER_STRING);
    private static final String WEBVTT_TIMESTAMP_STRING = "(\\d+:)?[0-5]\\d:[0-5]\\d\\.\\d{3}";
    private static final Pattern WEBVTT_TIMESTAMP = Pattern.compile(WEBVTT_TIMESTAMP_STRING);
    private static final String WEBVTT_CUE_SETTING_STRING = "\\S*:\\S*";
    private static final Pattern WEBVTT_CUE_SETTING = Pattern.compile(WEBVTT_CUE_SETTING_STRING);
    private static final Sample EMPTY_SAMPLE = new Sample() {
        ByteBuffer vtte;

        {
            VTTEmptyCueBox vttEmptyCueBox = new VTTEmptyCueBox();
            vtte = ByteBuffer.allocate(l2i(vttEmptyCueBox.getSize()));
            try {
                vttEmptyCueBox.getBox(new ByteBufferByteChannel(vtte));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            vtte.rewind();
        }


        public void writeTo(WritableByteChannel channel) throws java.io.IOException {
            channel.write(vtte.duplicate());
        }

        public long getSize() {
            return vtte.remaining();
        }

        public ByteBuffer asByteBuffer() {
            return vtte.duplicate();
        }
    };
    TrackMetaData trackMetaData = new TrackMetaData();
    SampleDescriptionBox stsd;
    List<Sample> samples = new ArrayList<Sample>();
    long[] sampleDurations = new long[0];
    WebVTTSampleEntry sampleEntry;

    public WebVttTrack(InputStream is, String trackName, Locale locale) throws IOException {
        super(trackName);
        trackMetaData.setTimescale(1000);
        trackMetaData.setLanguage(locale.getISO3Language());
        long mediaTimestampUs = 0;

        stsd = new SampleDescriptionBox();

        sampleEntry = new WebVTTSampleEntry();
        stsd.addBox(sampleEntry);
        WebVTTConfigurationBox webVttConf = new WebVTTConfigurationBox();
        sampleEntry.addBox(webVttConf);
        sampleEntry.addBox(new WebVTTSourceLabelBox());

        BufferedReader webvttData = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String line;

        // file should start with "WEBVTT"
        line = webvttData.readLine();
        if (line == null || !WEBVTT_FILE_HEADER.matcher(line).matches()) {
            throw new IOException("Expected WEBVTT. Got " + line);
        }
        webVttConf.setConfig(webVttConf.getConfig() + "\n" + line);
        while (true) {
            line = webvttData.readLine();
            if (line == null) {
                // we reached EOF before finishing the header
                throw new IOException("Expected an empty line after webvtt header");
            } else if (line.isEmpty()) {
                // we've read the newline that separates the header from the body
                break;
            }

            Matcher matcher = WEBVTT_METADATA_HEADER.matcher(line);
            if (!matcher.find()) {
                throw new IOException("Expected WebVTT metadata header. Got " + line);
            }
            webVttConf.setConfig(webVttConf.getConfig() + "\n" + line);
        }


        // process the cues and text
        while ((line = webvttData.readLine()) != null) {
            if ("".equals(line.trim())) {
                continue;
            }
            // parse the cue identifier (if present) {
            Matcher matcher = WEBVTT_CUE_IDENTIFIER.matcher(line);
            if (matcher.find()) {
                // ignore the identifier (we currently don't use it) and read the next line
                line = webvttData.readLine();
            }

            long startTime;
            long endTime;

            // parse the cue timestamps
            matcher = WEBVTT_TIMESTAMP.matcher(line);

            // parse start timestamp
            if (!matcher.find()) {
                throw new IOException("Expected cue start time: " + line);
            } else {
                startTime = parseTimestampUs(matcher.group());
            }

            // parse end timestamp
            String endTimeString;
            if (!matcher.find()) {
                throw new IOException("Expected cue end time: " + line);
            } else {
                endTimeString = matcher.group();
                endTime = parseTimestampUs(endTimeString);
            }

            // parse the (optional) cue setting list
            line = line.substring(line.indexOf(endTimeString) + endTimeString.length());
            matcher = WEBVTT_CUE_SETTING.matcher(line);
            String settings = null;
            while (matcher.find()) {
                settings = matcher.group();
            }
            StringBuilder payload = new StringBuilder();
            while (((line = webvttData.readLine()) != null) && (!line.isEmpty())) {
                if (payload.length() > 0) {
                    payload.append("\n");
                }
                payload.append(line.trim());
            }

            if (startTime != mediaTimestampUs) {
                //System.err.println("" + mediaTimestampUs + " - " + startTime + " Add empty sample");
                sampleDurations = Mp4Arrays.copyOfAndAppend(sampleDurations, startTime - mediaTimestampUs);
                samples.add(EMPTY_SAMPLE);
            }
            sampleDurations = Mp4Arrays.copyOfAndAppend(sampleDurations, endTime - startTime);
            VTTCueBox vttCueBox = new VTTCueBox();
            if (settings != null) {
                CueSettingsBox csb = new CueSettingsBox();
                csb.setContent(settings);
                vttCueBox.setCueSettingsBox(csb);
            }
            CuePayloadBox cuePayloadBox = new CuePayloadBox();
            cuePayloadBox.setContent(payload.toString());
            vttCueBox.setCuePayloadBox(cuePayloadBox);

            samples.add(new BoxBearingSample(Collections.<Box>singletonList(vttCueBox)));


            mediaTimestampUs = endTime;
            // samples.add();
        }


    }

    private static long parseTimestampUs(String s) throws NumberFormatException {
        if (!s.matches(WEBVTT_TIMESTAMP_STRING)) {
            throw new NumberFormatException("has invalid format");
        }

        String[] parts = s.split("\\.", 2);
        long value = 0;
        for (String group : parts[0].split(":")) {
            value = value * 60 + Long.parseLong(group);
        }
        return (value * 1000 + Long.parseLong(parts[1]));
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return stsd;
    }

    public long[] getSampleDurations() {
        long[] adoptedSampleDuration = new long[sampleDurations.length];
        for (int i = 0; i < adoptedSampleDuration.length; i++) {
            adoptedSampleDuration[i] = sampleDurations[i] * trackMetaData.getTimescale() / 1000;
        }
        return adoptedSampleDuration;

    }

    public TrackMetaData getTrackMetaData() {
        return trackMetaData;
    }

    public String getHandler() {
        return "text";
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public void close() throws java.io.IOException {

    }

    private static class BoxBearingSample implements Sample {
        List<Box> boxes;

        public BoxBearingSample(List<Box> boxes) {
            this.boxes = boxes;
        }

        public void writeTo(WritableByteChannel channel) throws java.io.IOException {
            for (Box box : boxes) {
                box.getBox(channel);
            }
        }

        public long getSize() {
            long l = 0;
            for (Box box : boxes) {
                l += box.getSize();
            }
            return l;
        }

        public ByteBuffer asByteBuffer() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                writeTo(Channels.newChannel(baos));
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }

            return ByteBuffer.wrap(baos.toByteArray());
        }
    }
}
