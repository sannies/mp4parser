package org.mp4parser.boxes.apple;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import org.mp4parser.IsoFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;


public class TimeCodeBoxTest extends BoxWriteReadBase<TimeCodeBox> {
    String tcmd = "00000026746D6364000000000000" +
            "0001000000000000000000005DC00000" +
            "03E918B200000000";

    @Test
    public void checkRealLifeBox() throws IOException, DecoderException {
        File f = File.createTempFile("TimeCodeBoxTest", "checkRealLifeBox");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(Hex.decodeHex(tcmd.toCharArray()));
        fos.close();

        IsoFile isoFile = new IsoFile(new FileInputStream(f).getChannel());
        TimeCodeBox tcmd = (TimeCodeBox) isoFile.getBoxes().get(0);
        System.err.println(tcmd);
        isoFile.close();
        f.delete();
    }


    @Override
    public Class<TimeCodeBox> getBoxUnderTest() {
        return TimeCodeBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, TimeCodeBox box) {
        addPropsHere.put("dataReferenceIndex", 666);
        addPropsHere.put("frameDuration", 1001);
        addPropsHere.put("numberOfFrames", 24);
        addPropsHere.put("reserved1", 0);
        addPropsHere.put("reserved2", 0);
        addPropsHere.put("timeScale", 24000);
        addPropsHere.put("rest", new byte[]{4, 5});
    }

}
