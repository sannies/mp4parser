package com.mp4parser.rtp2dash;

import mpeg.dash.schema.mpd._2011.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.*;

public class ManifestHelper {

    JAXBContext jaxbContext;

    {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public String getManifest(List<DashFragmentedMp4Writer> tracks, GregorianCalendar availabilityStartTime) throws JAXBException, DatatypeConfigurationException, IOException {
        MPDtype mpd = new MPDtype();
        mpd.setAvailabilityStartTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(availabilityStartTime));

        GregorianCalendar publishTime = GregorianCalendar.from(ZonedDateTime.now());
        publishTime.setTimeZone(TimeZone.getTimeZone("GMT"));
        mpd.setPublishTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(publishTime));
        mpd.setSuggestedPresentationDelay(DatatypeFactory.newInstance().newDuration(20000));
        PeriodType periodType = new PeriodType();
        periodType.setId("1");
        mpd.getPeriod().add(periodType);
        periodType.setStart(DatatypeFactory.newInstance().newDuration(0));
        boolean closed = true;
        for (DashFragmentedMp4Writer trackWriter : tracks) {
            closed &= trackWriter.isClosed();
        }
        mpd.setMinBufferTime(DatatypeFactory.newInstance().newDuration(2000));
        mpd.setProfiles("urn:mpeg:dash:profile:isoff-live:2011");
        Map<Long, AdaptationSetType> adaptationSetsMap = new HashMap<Long, AdaptationSetType>();
        for (DashFragmentedMp4Writer track : tracks) {
            AdaptationSetType adaptationSetType = adaptationSetsMap.get(track.getAdaptationSetId());
            if (adaptationSetType == null) {
                adaptationSetType = new AdaptationSetType();
                adaptationSetsMap.put(track.getAdaptationSetId(), adaptationSetType);
                adaptationSetType.setId(track.getAdaptationSetId());
                String hdlr = track.getSource().getHandler();
                if ("soun".equals(hdlr)) {
                    adaptationSetType.setContentType("audio");
                } else if ("vide".equals(hdlr)) {
                    adaptationSetType.setContentType("video");
                } else if ("text".equals(hdlr) || "subt".equals(hdlr)) {
                    adaptationSetType.setContentType("text");
                } else {
                    throw new IOException("Unknown handler " + hdlr);
                }
                adaptationSetType.setSegmentAlignment("true");
            }

            adaptationSetType.getRepresentation().add(track.getRepresentation());
        }
        for (AdaptationSetType adaptationSetType : adaptationSetsMap.values()) {
            periodType.getAdaptationSet().add(adaptationSetType);
        }

        double duration = 0;
        for (AdaptationSetType adaptationSet : periodType.getAdaptationSet()) {

            for (RepresentationType representation : adaptationSet.getRepresentation()) {
                double repDuration = 0;
                for (SegmentTimelineType.S s : representation.getSegmentTemplate().getSegmentTimeline().getS()) {
                    repDuration += s.getD().longValue();
                    if (s.getR() != null) {
                        repDuration += s.getR().longValue() * s.getD().longValue();
                    }
                }
                repDuration /= representation.getSegmentTemplate().getTimescale();
                duration = Math.max(duration, repDuration);
            }
        }
        mpd.setMediaPresentationDuration(DatatypeFactory.newInstance().newDuration((long) (duration * 1000)));

        if (closed) {
            mpd.setType(PresentationType.STATIC);

        } else {
            mpd.setType(PresentationType.DYNAMIC);
            mpd.setMinimumUpdatePeriod(DatatypeFactory.newInstance().newDuration(4000));
        }


        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter sw = new StringWriter();
        marshaller.marshal(new ObjectFactory().createMPD(mpd), sw);
        return sw.toString();
    }
}
