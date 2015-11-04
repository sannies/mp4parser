package org.mp4parser.boxes.iso14496.part15;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;
import org.mp4parser.ParsableBox;
import org.mp4parser.boxes.samplegrouping.SampleGroupDescriptionBox;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class Part15GroupDescriptionBoxesTest extends BoxRoundtripTest {

    public Part15GroupDescriptionBoxesTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        StepwiseTemporalLayerEntry stsa = new StepwiseTemporalLayerEntry();
        SyncSampleEntry sync = new SyncSampleEntry();
        sync.setNalUnitType(5);
        sync.setReserved(1);

        TemporalLayerSampleGroup tscl = new TemporalLayerSampleGroup();
        tscl.setTemporalLayerId(23);
        tscl.setTlAvgBitRate(203);
        tscl.setTlAvgFrameRate(28);
        tscl.setTlConstantFrameRate(12);
        tscl.setTlconstraint_indicator_flags(23442324);
        tscl.setTllevel_idc(75);
        tscl.setTlMaxBitRate(23467);
        tscl.setTlprofile_compatibility_flags(26726378);
        tscl.setTlprofile_idc(12);
        tscl.setTlprofile_space(1);
        tscl.setTltier_flag(true);
        TemporalSubLayerSampleGroup tsas = new TemporalSubLayerSampleGroup();


        return Arrays.asList(
                new Object[]{new SampleGroupDescriptionBox(),
                        new Map.Entry[]{
                                new E("groupingType", StepwiseTemporalLayerEntry.TYPE),
                                new E("groupEntries", Arrays.asList(stsa))}},

                new Object[]{new SampleGroupDescriptionBox(),
                        new Map.Entry[]{
                                new E("groupingType", SyncSampleEntry.TYPE),
                                new E("groupEntries", Arrays.asList(sync))}},
                new Object[]{new SampleGroupDescriptionBox(),
                        new Map.Entry[]{
                                new E("groupingType", TemporalLayerSampleGroup.TYPE),
                                new E("groupEntries", Arrays.asList(tscl))}},
                new Object[]{new SampleGroupDescriptionBox(),
                        new Map.Entry[]{
                                new E("groupingType", TemporalSubLayerSampleGroup.TYPE),
                                new E("groupEntries", Arrays.asList(tsas))}}
        );
    }
}