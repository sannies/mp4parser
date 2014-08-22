package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.Utf8;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.SubSampleInformationBox;
import com.coremedia.iso.boxes.sampleentry.XMLSubtitleSampleEntry;
import com.googlecode.mp4parser.authoring.AbstractTrack;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.TrackMetaData;
import com.googlecode.mp4parser.util.Iso639;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.lang.Override;
import java.lang.String;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMPTETTTrackImpl extends AbstractTrack {

    public static final String SMPTE_TT_NAMESPACE = "http://www.smpte-ra.org/schemas/2052-1/2010/smpte-tt";
    TrackMetaData trackMetaData = new TrackMetaData();
    SampleDescriptionBox sampleDescriptionBox = new SampleDescriptionBox();
    XMLSubtitleSampleEntry XMLSubtitleSampleEntry = new XMLSubtitleSampleEntry();

    List<Sample> samples = new ArrayList<Sample>();
    SubSampleInformationBox subSampleInformationBox = new SubSampleInformationBox();

    boolean containsImages;

    private long[] sampleDurations;

    static long toTime(String expr) {
        Pattern p = Pattern.compile("([0-9][0-9]):([0-9][0-9]):([0-9][0-9])([\\.:][0-9][0-9]?[0-9]?)?");
        Matcher m = p.matcher(expr);
        if (m.matches()) {
            String hours = m.group(1);
            String minutes = m.group(2);
            String seconds = m.group(3);
            String fraction = m.group(4);
            if (fraction == null) {
                fraction = ".000";
            }
            fraction = fraction.replace(":", ".");
            long ms = Long.parseLong(hours) * 60 * 60 * 1000;
            ms += Long.parseLong(minutes) * 60 * 1000;
            ms += Long.parseLong(seconds) * 1000;
            ms += Double.parseDouble("0" + fraction) * 1000;
            return ms;
        } else {
            throw new RuntimeException("Cannot match " + expr + " to time expression");
        }
    }

    public static String getLanguage(Document document) {
        return document.getDocumentElement().getAttribute("xml:lang");
    }

    public static long earliestTimestamp(Document document) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        NamespaceContext ctx = new TextTrackNamespaceContext();
        XPath xpath = xPathfactory.newXPath();
        xpath.setNamespaceContext(ctx);

        try {
            XPathExpression timedNodesXpath = xpath.compile("//*[@begin]");
            NodeList timedNodes = (NodeList) timedNodesXpath.evaluate(document, XPathConstants.NODESET);

            long earliestTimestamp = 0;
            for (int i = 0; i < timedNodes.getLength(); i++) {
                Node n = timedNodes.item(i);
                String begin = n.getAttributes().getNamedItem("begin").getNodeValue();
                earliestTimestamp = Math.min(toTime(begin), earliestTimestamp);
            }
            return earliestTimestamp;
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

    }

    public static long latestTimestamp(Document document) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        NamespaceContext ctx = new TextTrackNamespaceContext();
        XPath xpath = xPathfactory.newXPath();
        xpath.setNamespaceContext(ctx);

        try {
            XPathExpression timedNodesXpath = xpath.compile("//*[@begin]");

            NodeList timedNodes = (NodeList) timedNodesXpath.evaluate(document, XPathConstants.NODESET);

            long lastTimeStamp = 0;
            for (int i = 0; i < timedNodes.getLength(); i++) {
                Node n = timedNodes.item(i);
                String begin = n.getAttributes().getNamedItem("begin").getNodeValue();
                long end;
                if (n.getAttributes().getNamedItem("dur") != null) {
                    end = toTime(begin) + toTime(n.getAttributes().getNamedItem("dur").getNodeValue());
                } else if (n.getAttributes().getNamedItem("end") != null) {
                    end = toTime(n.getAttributes().getNamedItem("end").getNodeValue());
                } else {
                    throw new RuntimeException("neither end nor dur attribute is present");
                }
                lastTimeStamp = Math.max(end, lastTimeStamp);
            }
            return lastTimeStamp;
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

    }

    public SMPTETTTrackImpl(File... files) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        super(files[0].getName());
        sampleDurations = new long[files.length];
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        long startTime = 0;
        String firstLang = null;
        for (int sampleNo = 0; sampleNo < files.length; sampleNo++) {
            final File file = files[sampleNo];
            SubSampleInformationBox.SubSampleEntry subSampleEntry = new SubSampleInformationBox.SubSampleEntry();
            subSampleInformationBox.getEntries().add(subSampleEntry);
            subSampleEntry.setSampleDelta(1);

            Document doc = dBuilder.parse(file);
            String lang = getLanguage(doc);
            if (firstLang == null) {
                firstLang = lang;
            } else if (!firstLang.equals(lang)) {
                throw new RuntimeException("Within one Track all sample documents need to have the same language");
            }

            XPathFactory xPathfactory = XPathFactory.newInstance();
            NamespaceContext ctx = new TextTrackNamespaceContext();
            XPath xpath = xPathfactory.newXPath();
            xpath.setNamespaceContext(ctx);

            long lastTimeStamp = latestTimestamp(doc);
            sampleDurations[sampleNo] = lastTimeStamp - startTime;
            startTime = lastTimeStamp;

            XPathExpression expr = xpath.compile("/ttml:tt/ttml:body/ttml:div/@smpte:backgroundImage");
            NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            HashMap<String, String> internalName2Original = new HashMap<String, String>();
            Collection<String> originalNames = new HashSet<String>();
            for (int i = 0; i < nl.getLength(); i++) {
                originalNames.add(nl.item(i).getNodeValue());
            }
            originalNames = new ArrayList<String>(originalNames);
            Collections.sort((List<String>) originalNames);

            int p = 1;
            for (String originalName : originalNames) {
                String ext = originalName.substring(originalName.lastIndexOf("."));
                internalName2Original.put(originalName, "urn:dece:container:subtitleimageindex:" + p++ + ext);
            }
            if (!originalNames.isEmpty()) {
                String xml = new String(streamToByteArray(new FileInputStream(file)));
                for (Map.Entry<String, String> stringStringEntry : internalName2Original.entrySet()) {
                    xml = xml.replace(stringStringEntry.getKey(), stringStringEntry.getValue());
                }
                final String finalXml = xml;
                final List<File> pix = new ArrayList<File>();
                samples.add(new Sample() {
                    public void writeTo(WritableByteChannel channel) throws IOException {
                        channel.write(ByteBuffer.wrap(Utf8.convert(finalXml)));
                        for (File file1 : pix) {
                            FileInputStream fis = new FileInputStream(file1);
                            byte[] buffer = new byte[8096];
                            int n = 0;
                            while (-1 != (n = fis.read(buffer))) {
                                channel.write(ByteBuffer.wrap(buffer, 0, n));
                            }
                        }
                    }

                    public long getSize() {
                        long l = Utf8.convert(finalXml).length;
                        for (File file1 : pix) {
                            l += file1.length();
                        }
                        return l;
                    }

                    public ByteBuffer asByteBuffer() {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try {
                            writeTo(Channels.newChannel(baos));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return ByteBuffer.wrap(baos.toByteArray());
                    }
                });

                SubSampleInformationBox.SubSampleEntry.SubsampleEntry xmlEntry =
                        new SubSampleInformationBox.SubSampleEntry.SubsampleEntry();
                xmlEntry.setSubsampleSize(Utf8.utf8StringLengthInBytes(finalXml));
                subSampleEntry.getSubsampleEntries().add(xmlEntry);
                for (String originalName : originalNames) {
                    File pic = new File(file.getParentFile(), originalName);
                    pix.add(pic);
                    SubSampleInformationBox.SubSampleEntry.SubsampleEntry sse =
                            new SubSampleInformationBox.SubSampleEntry.SubsampleEntry();
                    sse.setSubsampleSize(pic.length());
                    subSampleEntry.getSubsampleEntries().add(sse);
                }
            } else {
                samples.add(new Sample() {
                    public void writeTo(WritableByteChannel channel) throws IOException {
                        Channels.newOutputStream(channel).write(streamToByteArray(new FileInputStream(file)));
                    }

                    public long getSize() {
                        return file.length();
                    }

                    public ByteBuffer asByteBuffer() {
                        try {
                            return ByteBuffer.wrap(streamToByteArray(new FileInputStream(file)));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        }
        trackMetaData.setLanguage(Iso639.convert2to3(firstLang));
        XMLSubtitleSampleEntry.setNamespace(SMPTE_TT_NAMESPACE);
        XMLSubtitleSampleEntry.setSchemaLocation(SMPTE_TT_NAMESPACE);
        if (containsImages) {
            XMLSubtitleSampleEntry.setAuxiliaryMimeTypes("image/png");
        } else {
            XMLSubtitleSampleEntry.setAuxiliaryMimeTypes("");
        }
        sampleDescriptionBox.addBox(XMLSubtitleSampleEntry);
        trackMetaData.setTimescale(30000);
        trackMetaData.setLayer(65535);


    }

    private byte[] streamToByteArray(InputStream input) throws IOException {
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

    private static class TextTrackNamespaceContext implements NamespaceContext {
        public String getNamespaceURI(String prefix) {
            if (prefix.equals("ttml")) {
                return "http://www.w3.org/ns/ttml";
            }
            if (prefix.equals("smpte")) {
                return "http://www.smpte-ra.org/schemas/2052-1/2010/smpte-tt";
            }
            return null;
        }

        public Iterator getPrefixes(String val) {
            return Arrays.asList("ttml", "smpte").iterator();
        }

        public String getPrefix(String uri) {
            if (uri.equals("http://www.w3.org/ns/ttml")) {
                return "ttml";
            }
            if (uri.equals(SMPTE_TT_NAMESPACE)) {
                return "smpte";
            }
            return null;
        }
    }
}
