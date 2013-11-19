package com.googlecode.mp4parser.boxes.apple;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import org.junit.Test;

import java.util.Date;
import java.util.Map;

/**
 * Created by sannies on 10/22/13.
 */
public class AppleRecordingYearBoxTest extends BoxWriteReadBase<AppleRecordingYearBox> {
    @Override
    public Class<AppleRecordingYearBox> getBoxUnderTest() {
        return AppleRecordingYearBox.class  ;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, AppleRecordingYearBox box) {
        addPropsHere.put("date", new Date(1000000) );
    }

    @Test
    public void testDateFormat() throws Exception {
        System.err.println(new AppleRecordingYearBox().df.format(new Date()));
        new AppleRecordingYearBox().df.parse( AppleRecordingYearBox.iso8601toRfc822Date("2013-09-29T07:00:00Z"));
        new AppleRecordingYearBox().df.parse( AppleRecordingYearBox.iso8601toRfc822Date("2013-09-29T07:00:00+04:30"));

    }
}
