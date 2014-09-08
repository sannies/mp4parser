package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.googlecode.mp4parser.authoring.AbstractTrack;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.TrackMetaData;

import java.io.IOException;
import java.util.List;

/**
 * Created by sannies on 08.09.2014.
 */
public class H265TrackImpl extends AbstractTrack {
    public H265TrackImpl(String name) {
        super(name);
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return null;
    }

    public long[] getSampleDurations() {
        return new long[0];
    }

    public TrackMetaData getTrackMetaData() {
        return null;
    }

    public String getHandler() {
        return null;
    }

    public List<Sample> getSamples() {
        return null;
    }

    public void close() throws IOException {

    }

    /**
     * 8.4.3  Sync sample
     An	HEVC	sample	is	considered	as	a	sync	sample	if	the	VCL	NAL	units	 in	 the	 sample	 indicate	 that	 the
     coded	 picture	 contained	 in	 the	 sample	 is	 an	 Instantaneous	 Decoding	Refresh	(IDR)	picture,	a	Clean
     picture.	 Random	Access	(CRA)	picture,	or	a	Broken	Link	Access	(BLA)
     When	the	sample	entry	name	is	'hev1',	the	following	applies:
       If	the	sample	is	a	random	access	point,	all	parameter	sets	needed	for	decoding	that	sample	shall
     be	included	either	in	the	sample	entry	or	in	the	sample	itself.
       Otherwise	(the	sample	is	not	a	random	access	point),	all	parameter	sets	needed	for	decoding	the
     sample	shall	be	included	either	in	the	sample	entry	or	in	any	of	the	samples	since	the	previous
     random	access	point	to	the	sample	itself,	inclusive.
     For	signalling	of	various	types	of	random	access	points,	the	following	guidelines	are	recommended:
       The	sync	sample	table	(and	the	equivalent	flag	in	movie	fragments)	must	be	used	in	an	HEVC
     track	unless	all	samples	are	sync	samples.	Note	that	track	fragment	random	access	box	refers	to
     the	presence	of	signalled	sync	samples	in	a	movie	fragment.
       The	 'roll'	 sample	 group	 is	 recommended	 to	 be	 used	 only	 for	 gradual	 decoding	 refresh	 (GDR)
     based	random	access	points,	i.e.	those	that	contain	non‐intra	coded	slices.
       The	use	of	the	'rap	'	or	'sync'	sample	group	is	optional,	depending	 on	 the	 need	 of	 either	 the
     information	on	leading	samples	associated	with	the	random	access	points	or	the	picture	types
     (e.g.	IDR,	CRA,	or	BLA)	of	the	random	access	points.
       The	use	of	the	Alternative	Startup	Sequences	(ISO/IEC	14496‐12	section	10.3)	sample	group	is
     recommended	to	be	used	only	with	random	access	points	consisting	of	CRA	and	BLA	pictures.
     In	the	context	of	this	clause,	the	leading	samples,	defined	as	part	of	the	definition	of	the	'rap	'	sample
     group	in	ISO/IEC	14496‐2,	contain	Random	Access	Skipped	Leading	(RASL)	access	units	as	defined	in
     ISO/IEC	23008‐2.
     * @return
     */
    public long[] getSyncSamples() {
        return super.getSyncSamples();
    }
}
