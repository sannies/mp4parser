package org.mp4parser.muxer.tracks.ttml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.List;

import static org.mp4parser.muxer.tracks.ttml.TtmlHelpers.*;


public class TtmlSegmenter {

    public static List<Document> split(Document doc, int splitTimeInSeconds) throws XPathExpressionException {
        int splitTime = splitTimeInSeconds * 1000;
        XPathFactory xPathfactory = XPathFactory.newInstance();

        XPath xpath = xPathfactory.newXPath();
        XPathExpression xp = xpath.compile("//*[name()='p']");

        boolean thereIsMore;

        List<Document> subDocs = new ArrayList<Document>();


        do {
            long segmentStartTime = subDocs.size() * splitTime;
            long segmentEndTime = (subDocs.size() + 1) * splitTime;
            Document d = (Document) doc.cloneNode(true);
            NodeList timedNodes = (NodeList) xp.evaluate(d, XPathConstants.NODESET);
            thereIsMore = false;

            for (int i = 0; i < timedNodes.getLength(); i++) {
                Node p = timedNodes.item(i);
                long startTime = getStartTime(p);
                long endTime = getEndTime(p);
                //p.appendChild(d.createComment(toTimeExpression(startTime) + " -> " + toTimeExpression(endTime)));
                if (startTime < segmentStartTime && endTime > segmentStartTime) {
                    changeTime(p, "begin", segmentStartTime - startTime);
                    startTime = segmentStartTime;

                }

                if (startTime >= segmentStartTime && startTime < segmentEndTime && endTime > segmentEndTime) {
                    changeTime(p, "end", segmentEndTime - endTime);
                    startTime = segmentStartTime;
                    endTime = segmentEndTime;
                }

                if (startTime > segmentEndTime) {
                    thereIsMore = true;
                }

                if (!(startTime >= segmentStartTime && endTime <= segmentEndTime)) {
                    Node parent = p.getParentNode();
                    parent.removeChild(p);
                } else {
                    changeTime(p, "begin", -segmentStartTime);
                    changeTime(p, "end", -segmentStartTime);
                }

            }
            trimWhitespace(d);

            XPathExpression bodyXP = xpath.compile("/*[name()='tt']/*[name()='body'][1]");
            Element body = (Element) bodyXP.evaluate(d, XPathConstants.NODE);
            String beginTime = body.getAttribute("begin");
            String endTime = body.getAttribute("end");
            if (beginTime == null || "".equals(beginTime)) {
                body.setAttribute("begin", toTimeExpression(segmentStartTime));
            } else {
                changeTime(body, "begin", segmentStartTime);
            }
            if (endTime == null || "".equals(endTime)) {
                body.setAttribute("end", toTimeExpression(segmentEndTime));
            } else {
                changeTime(body, "end", segmentEndTime);
            }
            subDocs.add(d);
        } while (thereIsMore);

        return subDocs;
    }

    public static void changeTime(Node p, String attribute, long amount) {
        if (p.getAttributes() != null && p.getAttributes().getNamedItem(attribute) != null) {
            String oldValue = p.getAttributes().getNamedItem(attribute).getNodeValue();
            long nuTime = toTime(oldValue) + amount;
            int frames = 0;
            if (oldValue.contains(".")) {
                frames = -1;
            } else {
                // todo more precision! 44 ~= 23 frames per second.
                // that should be ok for non high framerate content
                // actually I'd have to get the ttp:frameRateMultiplier
                // and the ttp:frameRate attribute to calculate at which frame to show the sub
                frames = (int) (nuTime - (nuTime / 1000) * 1000) / 44;
            }
            p.getAttributes().getNamedItem(attribute).setNodeValue(toTimeExpression(nuTime, frames));
        }

    }


    public static Document normalizeTimes(Document doc) throws XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();

        XPath xpath = xPathfactory.newXPath();
        xpath.setNamespaceContext(TtmlHelpers.NAMESPACE_CONTEXT);
        XPathExpression xp = xpath.compile("//*[name()='p']");
        NodeList timedNodes = (NodeList) xp.evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < timedNodes.getLength(); i++) {
            Node p = timedNodes.item(i);
            pushDown(p);

        }
        for (int i = 0; i < timedNodes.getLength(); i++) {
            Node p = timedNodes.item(i);
            removeAfterPushDown(p, "begin");
            removeAfterPushDown(p, "end");

        }
        return doc;
    }

    private static void pushDown(Node p) {
        long time = 0;

        Node current = p;
        while ((current = current.getParentNode()) != null) {
            if (current.getAttributes() != null && current.getAttributes().getNamedItem("begin") != null) {
                time += toTime(current.getAttributes().getNamedItem("begin").getNodeValue());
            }
        }

        if (p.getAttributes() != null && p.getAttributes().getNamedItem("begin") != null) {
            p.getAttributes().getNamedItem("begin").setNodeValue(toTimeExpression(time + toTime(p.getAttributes().getNamedItem("begin").getNodeValue())));
        }
        if (p.getAttributes() != null && p.getAttributes().getNamedItem("end") != null) {
            p.getAttributes().getNamedItem("end").setNodeValue(toTimeExpression(time + toTime(p.getAttributes().getNamedItem("end").getNodeValue())));
        }

    }

    private static void removeAfterPushDown(Node p, String begin) {
        Node current = p;
        while ((current = current.getParentNode()) != null) {
            if (current.getAttributes() != null && current.getAttributes().getNamedItem(begin) != null) {
                current.getAttributes().removeNamedItem(begin);
            }
        }
    }

    public static void trimWhitespace(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                child.setTextContent(child.getTextContent().trim());
            }
            trimWhitespace(child);
        }
    }


}
