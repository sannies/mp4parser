package org.mp4parser.boxes.samplegrouping;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;


@RunWith(Suite.class)
@Suite.SuiteClasses(value = {
        SampleGroupDescriptionBoxTestSuite.TestRateShareEntryV1.class,
        SampleGroupDescriptionBoxTestSuite.TestRateShareEntryV0.class,
        SampleGroupDescriptionBoxTestSuite.TestUnkownEntryV1.class,
        SampleGroupDescriptionBoxTestSuite.TestRollRecoveryEntryV1.class,
        SampleGroupDescriptionBoxTestSuite.TestRollRecoveryEntryV0.class,
        SampleGroupDescriptionBoxTestSuite.TestVariableLengthV1.class,
        SampleGroupDescriptionBoxTestSuite.TestDeadBytesV1.class
})
public class SampleGroupDescriptionBoxTestSuite {

    public static class TestRateShareEntryV1 extends BoxWriteReadBase<SampleGroupDescriptionBox> {
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


            addPropsHere.put("defaultLength", rateShareEntry.size());
            addPropsHere.put("version", 1);
            addPropsHere.put("groupEntries", Arrays.asList(rateShareEntry, rateShareEntry));
            addPropsHere.put("groupingType", RateShareEntry.TYPE);
        }
    }

    public static class TestRateShareEntryV0 extends BoxWriteReadBase<SampleGroupDescriptionBox> {
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


            addPropsHere.put("defaultLength", 0);
            addPropsHere.put("version", 0);
            addPropsHere.put("groupEntries", Arrays.asList(rateShareEntry, rateShareEntry));
            addPropsHere.put("groupingType", RateShareEntry.TYPE);
        }
    }
    
    public static class TestUnkownEntryV1 extends BoxWriteReadBase<SampleGroupDescriptionBox> {
        @Override
        public Class<SampleGroupDescriptionBox> getBoxUnderTest() {
            return SampleGroupDescriptionBox.class;
        }

        @Override
        public void setupProperties(Map<String, Object> addPropsHere, SampleGroupDescriptionBox box) {
            UnknownEntry unknownEntry = new UnknownEntry("abcd");
            unknownEntry.setContent(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6}));

            addPropsHere.put("defaultLength", unknownEntry.size());
            addPropsHere.put("version", 1);
            addPropsHere.put("groupEntries", Arrays.asList(unknownEntry, unknownEntry));
            addPropsHere.put("groupingType", "unkn");
        }
    }

    public static class TestRollRecoveryEntryV1 extends BoxWriteReadBase<SampleGroupDescriptionBox> {
        @Override
        public Class<SampleGroupDescriptionBox> getBoxUnderTest() {
            return SampleGroupDescriptionBox.class;
        }

        @Override
        public void setupProperties(Map<String, Object> addPropsHere, SampleGroupDescriptionBox box) {
            RollRecoveryEntry entry = new RollRecoveryEntry();
            entry.setRollDistance((short) 6);

            addPropsHere.put("defaultLength", entry.size());
            addPropsHere.put("version", 1);
            addPropsHere.put("groupEntries", Arrays.asList(entry, entry));
            addPropsHere.put("groupingType", "roll");
        }
    }

    public static class TestRollRecoveryEntryV0 extends BoxWriteReadBase<SampleGroupDescriptionBox> {
        @Override
        public Class<SampleGroupDescriptionBox> getBoxUnderTest() {
            return SampleGroupDescriptionBox.class;
        }

        @Override
        public void setupProperties(Map<String, Object> addPropsHere, SampleGroupDescriptionBox box) {
            RollRecoveryEntry entry = new RollRecoveryEntry();
            entry.setRollDistance((short) 6);

            addPropsHere.put("defaultLength", 0);
            addPropsHere.put("version", 0);
            addPropsHere.put("groupEntries", Arrays.asList(entry, entry));
            addPropsHere.put("groupingType", "roll");
        }
    }

    public static class TestDeadBytesV1 extends BoxWriteReadBase<SampleGroupDescriptionBox> {
        @Override
        public Class<SampleGroupDescriptionBox> getBoxUnderTest() {
            return SampleGroupDescriptionBox.class;
        }

        @Override
        public void setupProperties(Map<String, Object> addPropsHere, SampleGroupDescriptionBox box) {
            RollRecoveryEntry entry = new RollRecoveryEntry();
            entry.setRollDistance((short) 6);
            
            addPropsHere.put("defaultLength", 100);
            addPropsHere.put("version", 1);
            addPropsHere.put("groupEntries", Arrays.asList(entry, entry));
            addPropsHere.put("groupingType", "roll");
        }
    }

    public static class TestVariableLengthV1 extends BoxWriteReadBase<SampleGroupDescriptionBox> {
        @Override
        public Class<SampleGroupDescriptionBox> getBoxUnderTest() {
            return SampleGroupDescriptionBox.class;
        }

        @Override
        public void setupProperties(Map<String, Object> addPropsHere, SampleGroupDescriptionBox box) {
            UnknownEntry entry1 = new UnknownEntry("abcd");
            entry1.setContent(ByteBuffer.wrap(new byte[]{1, 2, 3}));

            UnknownEntry entry2 = new UnknownEntry("abcd");
            entry2.setContent(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6}));
            
            addPropsHere.put("defaultLength", 0);
            addPropsHere.put("version", 1);
            addPropsHere.put("groupEntries", Arrays.asList(entry1, entry2));
            addPropsHere.put("groupingType", "abcd");
        }
    }    
}

