package org.mp4parser.muxer.tracks.ttml;

import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TtmlHelpers {

    public static final String SMPTE_TT_NAMESPACE = "http://www.smpte-ra.org/schemas/2052-1/2010/smpte-tt";
    public static final String TTML_NAMESPACE = "http://www.w3.org/ns/ttml";
    public static final NamespaceContext NAMESPACE_CONTEXT = new TextTrackNamespaceContext();
    static byte[] namespacesStyleSheet1 = ("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
            "    <xsl:output method=\"text\"/>\n" +
            "    <xsl:key name=\"kElemByNSURI\"\n" +
            "             match=\"*[namespace::*[not(. = ../../namespace::*)]]\"\n" +
            "              use=\"namespace::*[not(. = ../../namespace::*)]\"/>\n" +
            "    <xsl:template match=\"/\">\n" +
            "        <xsl:for-each select=\n" +
            "            \"//namespace::*[not(. = ../../namespace::*)]\n" +
            "                           [count(..|key('kElemByNSURI',.)[1])=1]\">\n" +
            "            <xsl:value-of select=\"concat(.,'&#xA;')\"/>\n" +
            "        </xsl:for-each>\n" +
            "    </xsl:template>\n" +
            "</xsl:stylesheet>").getBytes();

    public static void main(String[] args) throws URISyntaxException, ParserConfigurationException, IOException, SAXException, XPathExpressionException, TransformerException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse("C:\\dev\\mp4parser\\a.xml");
        List<Document> split = TtmlSegmenter.split(doc, 60);
/*        for (Document document : split) {
            pretty(document, System.out, 4);
        }*/
        Track t = new TtmlTrackImpl("a.xml", split);
        Movie m = new Movie();
        m.addTrack(t);
        Container c = new DefaultMp4Builder().build(m);
        c.writeContainer(new FileOutputStream("output.mp4").getChannel());
    }

    public static String[] getAllNamespaces(Document doc) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer(new StreamSource(new ByteArrayInputStream(namespacesStyleSheet1)));
            StringWriter sw = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            List<String> r = new ArrayList<String>(new LinkedHashSet<String>(Arrays.asList(sw.getBuffer().toString().split("\n"))));
            return r.toArray(new String[r.size()]);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }

    }

    public static String toTimeExpression(long ms) {
        return toTimeExpression(ms, -1);
    }

    public static String toTimeExpression(long ms, int frames) {
        String minus = ms >= 0 ? "" : "-";
        ms = Math.abs(ms);

        long hours = ms / 1000 / 60 / 60;
        ms -= hours * 1000 * 60 * 60;

        long minutes = ms / 1000 / 60;
        ms -= minutes * 1000 * 60;

        long seconds = ms / 1000;
        ms -= seconds * 1000;
        if (frames >= 0) {
            return String.format("%s%02d:%02d:%02d:%d", minus, hours, minutes, seconds, frames);
        } else {
            return String.format("%s%02d:%02d:%02d.%03d", minus, hours, minutes, seconds, ms);
        }
    }

    public static long toTime(String expr) {
        Pattern p = Pattern.compile("(-?)([0-9][0-9]):([0-9][0-9]):([0-9][0-9])([\\.:][0-9][0-9]?[0-9]?)?");
        Matcher m = p.matcher(expr);
        if (m.matches()) {
            String minus = m.group(1);
            String hours = m.group(2);
            String minutes = m.group(3);
            String seconds = m.group(4);
            String fraction = m.group(5);
            if (fraction == null) {
                fraction = ".000";
            }

            fraction = fraction.replace(":", ".");
            long ms = Long.parseLong(hours) * 60 * 60 * 1000;
            ms += Long.parseLong(minutes) * 60 * 1000;
            ms += Long.parseLong(seconds) * 1000;
            if (fraction.contains(":")) {
                ms += Double.parseDouble("0" + fraction.replace(":", ".")) * 40 * 1000; // 40ms == 25fps - simplifying assumption should be ok for here
            } else {
                ms += Double.parseDouble("0" + fraction) * 1000;
            }

            return ms * ("-".equals(minus) ? -1 : 1);
        } else {
            throw new RuntimeException("Cannot match '" + expr + "' to time expression");
        }
    }

    public static void pretty(Document document, OutputStream outputStream, int indent) throws IOException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        if (indent > 0) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));
        }
        Result result = new StreamResult(outputStream);
        Source source = new DOMSource(document);
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

    public static long getStartTime(Node p) {
        long time = 0;
        Node current = p;
        while ((current = current.getParentNode()) != null) {
            if (current.getAttributes() != null && current.getAttributes().getNamedItem("begin") != null) {
                time += toTime(current.getAttributes().getNamedItem("begin").getNodeValue());
            }
        }

        if (p.getAttributes() != null && p.getAttributes().getNamedItem("begin") != null) {
            return time + toTime(p.getAttributes().getNamedItem("begin").getNodeValue());
        }
        return time;
    }

    public static long getEndTime(Node p) {
        long time = 0;
        Node current = p;
        while ((current = current.getParentNode()) != null) {
            if (current.getAttributes() != null && current.getAttributes().getNamedItem("begin") != null) {
                time += toTime(current.getAttributes().getNamedItem("begin").getNodeValue());
            }
        }

        if (p.getAttributes() != null && p.getAttributes().getNamedItem("end") != null) {
            return time + toTime(p.getAttributes().getNamedItem("end").getNodeValue());
        }
        return time;
    }

    public static void deepCopyDocument(Document ttml, File target) throws IOException {
        try {
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("//*/@backgroundImage");
            NodeList nl = (NodeList) expr.evaluate(ttml, XPathConstants.NODESET);
            for (int i = 0; i < nl.getLength(); i++) {
                Node backgroundImage = nl.item(i);
                URI backgroundImageUri = URI.create(backgroundImage.getNodeValue());
                if (!backgroundImageUri.isAbsolute()) {
                    copyLarge(new URI(ttml.getDocumentURI()).resolve(backgroundImageUri).toURL().openStream(), new File(target.toURI().resolve(backgroundImageUri).toURL().getFile()));
                }
            }
            copyLarge(new URI(ttml.getDocumentURI()).toURL().openStream(), target);

        } catch (XPathExpressionException e) {
            throw new IOException(e);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private static long copyLarge(InputStream input, File outputFile)
            throws IOException {
        byte[] buffer = new byte[16384];
        long count = 0;
        int n = 0;
        outputFile.getParentFile().mkdirs();
        FileOutputStream output = new FileOutputStream(outputFile);
        try {
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
                count += n;
            }
        } finally {
            output.close();
        }
        return count;
    }

    private static class TextTrackNamespaceContext implements NamespaceContext {


        public String getNamespaceURI(String prefix) {
            if (prefix.equals("ttml")) {
                return TTML_NAMESPACE;
            }
            if (prefix.equals("smpte")) {
                return SMPTE_TT_NAMESPACE;
            }
            return null;
        }

        public Iterator getPrefixes(String val) {
            return Arrays.asList("ttml", "smpte").iterator();
        }

        public String getPrefix(String uri) {
            if (uri.equals(TTML_NAMESPACE)) {
                return "ttml";
            }
            if (uri.equals(SMPTE_TT_NAMESPACE)) {
                return "smpte";
            }
            return null;
        }
    }
}
