package com.mp4parser.boxes.iso23009.part1;

import com.mp4parser.ParsableBox;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class EventMessageBoxTest extends BoxRoundtripTest {


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        String schemeIdUri;
        String value;
        long timescale;
        long presentationTimeDelta;
        long eventDuration;
        long id;
        byte[] messageData;


        return Collections.singletonList(
                new Object[]{new EventMessageBox(),
                        new Map.Entry[]{
                                new E("schemeIdUri", "sjkfsdhjklfhskj"),
                                new E("value", "sdjsfhksdhddd"),
                                new E("timescale", 1L),
                                new E("presentationTimeDelta", 2L),
                                new E("eventDuration", 3L),
                                new E("id", 4L),
                                new E("messageData", new byte[]{1, 1, 2, 3, 4, 5, 6, 7})}
                });
    }


    public EventMessageBoxTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }
}