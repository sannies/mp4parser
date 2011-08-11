import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;
import com.coremedia.iso.boxes.mdat.SampleList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 7/10/11
 * Time: 10:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class Test {
    public static void main5(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Properties properties = new Properties();
        Properties names = new Properties();
        properties.load(Test.class.getResourceAsStream("/isoparser-default.properties"));
        for (Object o : properties.values()) {
            String clazzName = o.toString().substring(0, o.toString().indexOf("("));
            if (
                    clazzName.equals("com.coremedia.iso.boxes.UserBox") ||
                            clazzName.equals("com.coremedia.iso.boxes.sampleentry.AudioSampleEntry") ||
                            clazzName.equals("com.coremedia.iso.boxes.rtp.RtpHintSampleEntry") ||
                            clazzName.equals("com.coremedia.iso.boxes.sampleentry.VisualSampleEntry") ||
                            clazzName.equals("com.coremedia.iso.boxes.UnknownBox") ||
                            clazzName.equals("com.coremedia.iso.boxes.sampleentry.MpegSampleEntry") ||
                            clazzName.equals("com.coremedia.iso.boxes.sampleentry.TextSampleEntry") ||
                            clazzName.equals("com.coremedia.iso.boxes.TrackReferenceTypeBox") ||
                            clazzName.equals("com.coremedia.iso.boxes.rtp.HintStatisticBoxes") ||
                            clazzName.equals("com.coremedia.iso.boxes.rtp.HintPacketsSentBox")
                    ) {
                continue;
            }
            Class clazz = Class.forName(clazzName);
            AbstractBox box = (AbstractBox) clazz.newInstance();
            //names.setProperty(IsoFile.bytesToFourCC(box.getType()), box.getDisplayName());
        }
        System.err.println(names.toString());
        FileWriter fw = new FileWriter("/home/sannies/scm/svn/mp4parser/isoviewer/src/main/resources/names.properties");

        names.store(fw, "");
        fw.close();

    }


    public static void main2(String[] args) throws IOException {
        IsoBufferWrapper ibw = new IsoBufferWrapperImpl(new File("/home/sannies/pe_ksfo_0195_224x112_0.ismv"));
        IsoFile isoFile = new IsoFile(ibw);
        isoFile.parse();
        for (MovieFragmentBox movieFragmentBox : isoFile.getBoxes(MovieFragmentBox.class)) {
            List<IsoBufferWrapper> l = new SampleList(movieFragmentBox);
            for (IsoBufferWrapper isoBufferWrapper : l) {
                long size = isoBufferWrapper.size();
                assert size < Integer.MAX_VALUE;

                byte[] sampleContent = new byte[(int) size];
                isoBufferWrapper.read(sampleContent);

                System.err.println(Arrays.asList(sampleContent));
            }

        }
    }

    public static void main3(String[] args) throws IOException {
        IsoBufferWrapper ibw = new IsoBufferWrapperImpl(new File("/home/sannies/pe_ksfo_0195_224x112_0.ismv"));
        IsoFile isoFile = new IsoFile(ibw);
        isoFile.parse();
        TrackBox trackBox = isoFile.getBoxes(MovieBox.class).get(0).getBoxes(TrackBox.class).get(0);
        List<IsoBufferWrapper> l = new SampleList(trackBox);

        for (IsoBufferWrapper isoBufferWrapper : l) {
            long size = isoBufferWrapper.size();
            assert size < Integer.MAX_VALUE;

            byte[] sampleContent = new byte[(int) size];
            isoBufferWrapper.read(sampleContent);

            System.err.println(sampleContent.length);
        }

    }

    public static void main4(String[] args) throws IOException {
        IsoBufferWrapper ibw = new IsoBufferWrapperImpl(new File("/home/sannies/suckerpunch-distantplanet_h1080p.mov"));
        IsoFile isoFile = new IsoFile(ibw);
        isoFile.parse();
        TrackBox trackBox = isoFile.getBoxes(MovieBox.class).get(0).getBoxes(TrackBox.class).get(0);
        List<IsoBufferWrapper> l = new SampleList(trackBox);
        for (IsoBufferWrapper isoBufferWrapper : l) {
            long size = isoBufferWrapper.size();
            assert size < Integer.MAX_VALUE;

            byte[] sampleContent = new byte[(int) size];
            isoBufferWrapper.read(sampleContent);

            System.err.println(sampleContent.length);
        }
    }
}
