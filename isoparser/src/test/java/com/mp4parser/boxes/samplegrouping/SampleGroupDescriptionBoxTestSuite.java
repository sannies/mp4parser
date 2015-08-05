package com.mp4parser.boxes.samplegrouping;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;


@RunWith(Suite.class)
@Suite.SuiteClasses(value = {
        SampleGroupDescriptionBoxTestSuite.TestRateShareEntry.class,
        SampleGroupDescriptionBoxTestSuite.TestUnkownEntry.class,
        SampleGroupDescriptionBoxTestSuite.TestRollRecoveryEntry.class
})
public class SampleGroupDescriptionBoxTestSuite {

    public static class TestRateShareEntry extends BoxWriteReadBase<SampleGroupDescriptionBox> {
        @Override
        public Class<SampleGroupDescriptionBox> getBoxUnderTest() {
            return SampleGroupDescriptionBox.class;
        }

        @Override
        public void setupProperties(Map<String, Object> addPropsHere, SampleGroupDescriptionBox box) {
            RateShareEntry rateShareEntry = new RateShareEntry();
            rateShareEntry.setDiscardPriority((short) 56);
            rateShareEntry.setMaximumBitrate(1000);
            rateShareEntry.setMinimumBitrate(100);
            rateShareEntry.setOperationPointCut((short) 2);
            rateShareEntry.setEntries(Arrays.asList(
                    new RateShareEntry.Entry(100, (short) 50),
                    new RateShareEntry.Entry(1000, (short) 90)
            ));


            addPropsHere.put("defaultLength", 5);
            addPropsHere.put("version", 1);
            addPropsHere.put("groupEntries", Arrays.asList(
                    rateShareEntry
            ));
            addPropsHere.put("groupingType", RateShareEntry.TYPE);
        }
    }

    public static class TestUnkownEntry extends BoxWriteReadBase<SampleGroupDescriptionBox> {
        @Override
        public Class<SampleGroupDescriptionBox> getBoxUnderTest() {
            return SampleGroupDescriptionBox.class;
        }

        @Override
        public void setupProperties(Map<String, Object> addPropsHere, SampleGroupDescriptionBox box) {
            UnknownEntry unknownEntry = new UnknownEntry("abcd");
            unknownEntry.setContent(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6}));

            addPropsHere.put("defaultLength", 5);
            addPropsHere.put("version", 1);
            addPropsHere.put("groupEntries", Arrays.asList(
                    unknownEntry
            ));
            addPropsHere.put("groupingType", "unkn");
        }
    }

    public static class TestRollRecoveryEntry extends BoxWriteReadBase<SampleGroupDescriptionBox> {
        @Override
        public Class<SampleGroupDescriptionBox> getBoxUnderTest() {
            return SampleGroupDescriptionBox.class;
        }

        @Override
        public void setupProperties(Map<String, Object> addPropsHere, SampleGroupDescriptionBox box) {
            RollRecoveryEntry entry = new RollRecoveryEntry();
            entry.setRollDistance((short) 6);

            addPropsHere.put("defaultLength", 5);
            addPropsHere.put("version", 1);
            addPropsHere.put("groupEntries", Arrays.asList(
                    entry
            ));
            addPropsHere.put("groupingType", "roll");
        }
    }


}

