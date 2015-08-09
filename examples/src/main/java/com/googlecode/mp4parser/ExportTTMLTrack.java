package com.googlecode.mp4parser;

import com.mp4parser.boxes.iso14496.part12.SubSampleInformationBox;
import com.mp4parser.muxer.Movie;
import com.mp4parser.muxer.Track;
import com.mp4parser.muxer.builder.DefaultMp4Builder;
import com.mp4parser.muxer.container.mp4.MovieCreator;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import static com.mp4parser.tools.CastUtils.l2i;

/**
 * Created by user on 06.08.2014.
 */
public class ExportTTMLTrack {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        //Movie m = MovieCreator.build("C:\\dev\\mp4parser-github\\ttml-example\\subs.uvu");
        Movie m = MovieCreator.build("C:\\dev\\mp4parser-github\\output.mp4");
        for (Track track : m.getTracks()) {
            if (track.getHandler().endsWith("vide")) {
                Movie vide = new Movie(Collections.singletonList(track));
                DefaultMp4Builder builder = new DefaultMp4Builder();
                builder.build(vide).writeContainer(new RandomAccessFile("vide_" + track.getTrackMetaData().getTrackId() + ".mp4", "rw").getChannel());
            }
            if (track.getHandler().endsWith("soun")) {
                Movie vide = new Movie(Collections.singletonList(track));
                DefaultMp4Builder builder = new DefaultMp4Builder();
                builder.build(vide).writeContainer(new RandomAccessFile("soun_" + track.getTrackMetaData().getTrackId() +".mp4", "rw").getChannel());
            }
            if (track.getHandler().endsWith("subt")) {
                for (int i = 0; i < track.getSamples().size(); i++) {
                    File f = new File("subtitle_" + track.getTrackMetaData().getTrackId() + "_" + i + ".xml");
                    f.delete();
                    RandomAccessFile raf = new RandomAccessFile(f, "rw");
                    SubSampleInformationBox subs = track.getSubsampleInformationBox();
                    int j = 0;
                    ByteBuffer xmlSamplePart = null;
                    for (SubSampleInformationBox.SubSampleEntry subSampleEntry : subs.getEntries()) {
                        j += subSampleEntry.getSampleDelta();
                        if (j == (i + 1)) {
                            // found sample entry
                            Iterator<SubSampleInformationBox.SubSampleEntry.SubsampleEntry> subsampleIter =
                                    subSampleEntry.getSubsampleEntries().iterator();
                            if (subsampleIter.hasNext()) {
                                SubSampleInformationBox.SubSampleEntry.SubsampleEntry xmlSubSampleEntry = subsampleIter.next();
                                ByteBuffer sample = track.getSamples().get(i).asByteBuffer();
                                xmlSamplePart = (ByteBuffer) sample.slice().limit(l2i(xmlSubSampleEntry.getSubsampleSize()));
                                raf.getChannel().write(xmlSamplePart);
                                raf.close();
                                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                factory.setNamespaceAware(true);
                                DocumentBuilder builder = factory.newDocumentBuilder();
                                Document document = builder.parse(f);

                                XPathFactory xPathfactory = XPathFactory.newInstance();
                                NamespaceContext ctx = new NamespaceContext() {
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
                                        if (uri.equals("http://www.smpte-ra.org/schemas/2052-1/2010/smpte-tt")) {
                                            return "smpte";
                                        }
                                        return null;
                                    }
                                };
                                XPath xpath = xPathfactory.newXPath();
                                xpath.setNamespaceContext(ctx);
                                XPathExpression expr = xpath.compile("/ttml:tt/ttml:body/ttml:div/@smpte:backgroundImage");
                                NodeList nl = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
                                HashSet<String> names = new HashSet<String>();
                                for (int n = 0; n < nl.getLength(); n++) {
                                    names.add(nl.item(n).getNodeValue());
                                }
                                List<String> imageNames = new ArrayList<String>(names);
                                Collections.sort(imageNames);
                                System.out.println(nl);
                                System.out.println(document.getFirstChild().getTextContent());


                                sample = ((ByteBuffer) sample.position(l2i(xmlSubSampleEntry.getSubsampleSize()))).slice();
                                int p = 0;
                                while (subsampleIter.hasNext()) {
                                    SubSampleInformationBox.SubSampleEntry.SubsampleEntry picSubSampleEntry = subsampleIter.next();
                                    ByteBuffer pic = (ByteBuffer) sample.slice().limit(l2i(picSubSampleEntry.getSubsampleSize()));
                                    sample = ((ByteBuffer) sample.position(l2i(picSubSampleEntry.getSubsampleSize()))).slice();
                                    FileOutputStream fosPic = new FileOutputStream(
                                            "subtitle_" + track.getTrackMetaData().getTrackId() + "_" + i + "_" + imageNames.get(p++).replace(":", "_"));
                                    fosPic.getChannel().write(pic);
                                }
                                String content = IOUtils.toString(new FileInputStream(f));
                                for (String imageName : imageNames) {
                                    content = content.replaceAll(imageName,
                                            "subtitle_" + track.getTrackMetaData().getTrackId() + "_" + i + "_" + imageName.replace(":", "_"));
                                }
                                IOUtils.write(content, new FileOutputStream(f));
                            }
                        }
                    }
                    if (xmlSamplePart == null) {
                        xmlSamplePart = track.getSamples().get(i).asByteBuffer();
                        raf.getChannel().write(xmlSamplePart);
                    }
                }
            }
        }

    }
}
