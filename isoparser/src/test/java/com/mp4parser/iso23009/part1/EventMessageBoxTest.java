package com.mp4parser.iso23009.part1;

import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

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


    public EventMessageBoxTest(Box boxUnderTest, Map.Entry<String, Object>... properties) {
        super(boxUnderTest, properties);
    }
}