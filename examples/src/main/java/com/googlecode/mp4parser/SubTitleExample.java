package com.googlecode.mp4parser;


import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultFragmenterImpl;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.TextTrackImpl;

import java.io.*;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adds subtitles.
 */
public class SubTitleExample {
    public static void main(String[] args) throws IOException {
        Movie m1 = MovieCreator.build(("C:\\dev\\DRMTODAY-872\\Tears_Of_Steel_128000_eng.mp4"));
        Movie m2 = MovieCreator.build(("C:\\dev\\DRMTODAY-872\\Tears_Of_Steel_600000.mp4"));
        //WebVttTrack webVttTrack = new WebVttTrack(new , "subs", Locale.ENGLISH);
        TextTrackImpl textTrack = new TextTrackImpl();
        textTrack.getSubs().addAll(
                WebVttParser.parse(new FileInputStream("C:\\dev\\DRMTODAY-872\\Tears_Of_Steel_eng.vtt")));

        Movie m = new Movie();
        for (Track track : m2.getTracks()) {
            m.addTrack(track);
        }
        for (Track track : m1.getTracks()) {
            m.addTrack(track);
        }
        m.addTrack(textTrack);
        DefaultMp4Builder builder = new DefaultMp4Builder();
        builder.setFragmenter(new DefaultFragmenterImpl(2));
        Container c = builder.build(m);
        WritableByteChannel wbc = new FileOutputStream("output.mp4").getChannel();
        c.writeContainer(wbc);
    }

    public static class WebVttParser {
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

        static List<TextTrackImpl.Line> parse(InputStream in) throws IOException {
            BufferedReader webvttData = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            List<TextTrackImpl.Line> samples = new ArrayList<TextTrackImpl.Line>();

            // file should start with "WEBVTT"
            line = webvttData.readLine();
            if (line == null || !WEBVTT_FILE_HEADER.matcher(line).matches()) {
                throw new IOException("Expected WEBVTT. Got " + line);
            }
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


                samples.add(new TextTrackImpl.Line(startTime, endTime, payload.toString()));
            }
            return samples;
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
    }

}
