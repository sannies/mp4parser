package com.googlecode.mp4parser.authoring.tracks.ttml;

import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.SubSampleInformationBox;
import com.googlecode.mp4parser.authoring.AbstractTrack;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.TrackMetaData;
import com.mp4parser.iso14496.part30.XMLSubtitleSampleEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.*;

import static com.googlecode.mp4parser.authoring.tracks.ttml.TtmlHelpers.*;

public class TtmlTrackImpl extends AbstractTrack {


    TrackMetaData trackMetaData = new TrackMetaData();
    SampleDescriptionBox sampleDescriptionBox = new SampleDescriptionBox();
    XMLSubtitleSampleEntry xmlSubtitleSampleEntry = new XMLSubtitleSampleEntry();

    List<Sample> samples = new ArrayList<Sample>();
    SubSampleInformationBox subSampleInformationBox = new SubSampleInformationBox();


    private long[] sampleDurations;


    public static String getLanguage(Document document) {
        return document.getDocumentElement().getAttribute("xml:lang");
    }

    protected long firstTimestamp(Document document) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        xpath.setNamespaceContext(TtmlHelpers.NAMESPACE_CONTEXT);

        try {
            XPathExpression xp = xpath.compile("//*[@begin]");
            NodeList timedNodes = (NodeList) xp.evaluate(document, XPathConstants.NODESET);

            long firstTimestamp = Long.MAX_VALUE;
            for (int i = 0; i < timedNodes.getLength(); i++) {
                firstTimestamp = Math.min(getStartTime(timedNodes.item(i)), firstTimestamp);
            }
            return firstTimestamp;
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

    }

    protected long lastTimestamp(Document document) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        xpath.setNamespaceContext(TtmlHelpers.NAMESPACE_CONTEXT);

        try {
            XPathExpression xp = xpath.compile("//*[@end]");
            NodeList timedNodes = (NodeList) xp.evaluate(document, XPathConstants.NODESET);

            long lastTimeStamp = 0;
            for (int i = 0; i < timedNodes.getLength(); i++) {
                lastTimeStamp = Math.max(getEndTime(timedNodes.item(i)), lastTimeStamp);
            }
            return lastTimeStamp;
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

    }

    protected void extractLanguage(List<Document> ttmls) {
        String firstLang = null;
        for (Document ttml : ttmls) {

            String lang = getLanguage(ttml);
            if (firstLang == null) {
                firstLang = lang;
                trackMetaData.setLanguage(Locale.forLanguageTag(lang).getISO3Language());
            } else if (!firstLang.equals(lang)) {
                throw new RuntimeException("Within one Track all sample documents need to have the same language");
            }

        }
    }

    protected List<String> extractMimeTypes(Document ttml) throws XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();

        XPath xpath = xPathfactory.newXPath();

        XPathExpression expr = xpath.compile("//*/@smpte:backgroundImage");
        NodeList nl = (NodeList) expr.evaluate(ttml, XPathConstants.NODESET);

        Set<String> mimeTypes = new LinkedHashSet<String>();

        int p = 1;
        for (int i = 0; i < nl.getLength(); i++) {
            Node bgImageNode = nl.item(i);
            String uri = bgImageNode.getNodeValue();
            String ext = uri.substring(uri.lastIndexOf("."));
            if (ext.contains("jpg") || ext.contains("jpeg")) {
                mimeTypes.add("image/jpeg");
            } else if (ext.contains("png")) {
                mimeTypes.add("image/png");
            }
        }
        return new ArrayList<String>(mimeTypes);
    }

    protected static List<byte[]> extractImages(Document ttml) throws XPathExpressionException, URISyntaxException, IOException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//*/@backgroundImage");
        NodeList nl = (NodeList) expr.evaluate(ttml, XPathConstants.NODESET);

        LinkedHashMap<String, String> internalNames2Original = new LinkedHashMap<String, String>();

        int p = 1;
        for (int i = 0; i < nl.getLength(); i++) {
            Node bgImageNode = nl.item(i);
            String uri = bgImageNode.getNodeValue();
            String ext = uri.substring(uri.lastIndexOf("."));

            String internalName = internalNames2Original.get(uri);
            if (internalName == null) {
                internalName = "urn:mp4parser:" + p++ + ext;
                internalNames2Original.put(internalName, uri);
            }
            bgImageNode.setNodeValue(internalName);

        }
        List<byte[]> images = new ArrayList<byte[]>();
        if (!internalNames2Original.isEmpty()) {
            for (Map.Entry<String, String> internalName2Original : internalNames2Original.entrySet()) {

                URI pic = new URI(ttml.getDocumentURI()).resolve(internalName2Original.getValue());
                images.add(streamToByteArray(pic.toURL().openStream()));

            }
        }
        return images;
    }

    long extractDuration(Document ttml) {
         return lastTimestamp(ttml) - firstTimestamp(ttml);
    }

    public TtmlTrackImpl(String name, List<Document> ttmls) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, URISyntaxException {
        super(name);
        extractLanguage(ttmls);
        Set<String> mimeTypes = new HashSet<String>();
        sampleDurations = new long[ttmls.size()];
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        xpath.setNamespaceContext(TtmlHelpers.NAMESPACE_CONTEXT);
        long startTime = 0;

        for (int sampleNo = 0; sampleNo < ttmls.size(); sampleNo++) {
            final Document ttml = ttmls.get(sampleNo);
            SubSampleInformationBox.SubSampleEntry subSampleEntry = new SubSampleInformationBox.SubSampleEntry();
            subSampleInformationBox.getEntries().add(subSampleEntry);
            subSampleEntry.setSampleDelta(1);
            sampleDurations[sampleNo] = extractDuration(ttml);

            List<byte[]> images = extractImages(ttml);
            mimeTypes.addAll(extractMimeTypes(ttml));

            // No changes of XML after this point!
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TtmlHelpers.pretty(ttml, baos, 4);
            SubSampleInformationBox.SubSampleEntry.SubsampleEntry xmlEntry =
                    new SubSampleInformationBox.SubSampleEntry.SubsampleEntry();
            xmlEntry.setSubsampleSize(baos.size());

            subSampleEntry.getSubsampleEntries().add(xmlEntry);
            for (byte[] image : images) {
                baos.write(image);
                SubSampleInformationBox.SubSampleEntry.SubsampleEntry imageEntry =
                        new SubSampleInformationBox.SubSampleEntry.SubsampleEntry();
                imageEntry.setSubsampleSize(image.length);
                subSampleEntry.getSubsampleEntries().add(imageEntry);

            }

            final byte[] finalSample = baos.toByteArray();
            samples.add(new Sample() {
                public void writeTo(WritableByteChannel channel) throws IOException {
                    channel.write(ByteBuffer.wrap(finalSample));
                }

                public long getSize() {
                    return finalSample.length;
                }

                public ByteBuffer asByteBuffer() {
                    return ByteBuffer.wrap(finalSample);
                }
            });
        }


        xmlSubtitleSampleEntry.setNamespace(join(",", getAllNamespaces(ttmls.get(0))));
        xmlSubtitleSampleEntry.setSchemaLocation("");
        xmlSubtitleSampleEntry.setAuxiliaryMimeTypes(join(",", new ArrayList<String>(mimeTypes).toArray(new String[mimeTypes.size()])));
        sampleDescriptionBox.addBox(xmlSubtitleSampleEntry);
        trackMetaData.setTimescale(30000);
        trackMetaData.setLayer(65535);


    }

    private static String join(String joiner, String[] i) {
        StringBuilder result = new StringBuilder();
        for (String s : i) {
            result.append(s).append(joiner);
        }
        result.setLength(result.length()-1);
        return result.toString();
    }


    private static long latestTimestamp(Document document) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        xpath.setNamespaceContext(TtmlHelpers.NAMESPACE_CONTEXT);

        try {
            XPathExpression xp = xpath.compile("//*[name()='p']");
            NodeList timedNodes = (NodeList) xp.evaluate(document, XPathConstants.NODESET);
            long lastTimeStamp = 0;
            for (int i = 0; i < timedNodes.getLength(); i++) {
                lastTimeStamp = Math.max(getEndTime(timedNodes.item(i)), lastTimeStamp);
            }
            return lastTimeStamp;
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

    }

    private static byte[] streamToByteArray(InputStream input) throws IOException {
        byte[] buffer = new byte[8096];
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return sampleDescriptionBox;
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
        return "subt";
    }

    public List<Sample> getSamples() {
        return samples;
    }

    @Override
    public SubSampleInformationBox getSubsampleInformationBox() {
        return subSampleInformationBox;

    }

    public void close() throws IOException {

    }
}
