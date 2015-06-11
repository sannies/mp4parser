package com.coremedia.iso.boxes.apple;

import com.googlecode.mp4parser.AbstractContainerBox;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * siDecompressionParam Atom ('wave')
 * <p>The siDecompressionParam atom provides the ability to store data specific to a given audio decompressor in the
 * SoundDescription record. As example, some audio decompression algorithms, such as Microsoft’s ADPCM, require a
 * set of out-of-band values to configure the decompressor. These are stored in an atom of this type.</p>
 * <p>This atom contains other atoms with audio decompressor settings and is a required extension to the sound sample
 * description for MPEG-4 audio. A 'wave' chunk for 'mp4a' typically contains (in order) at least a 'frma' atom, an
 * 'mp4a' atom, an 'esds' atom, and a “Terminator Atom (0x00000000)” atom.</p>
 * <p>The contents of other siDecompressionParam atoms are dependent on the audio decompressor.
 * <ul>
 * <li>Size - An unsigned 32-bit integer holding the size of the decompression parameters atom</li>
 * <li>Type - An unsigned 32-bit field containing the four-character code 'wave'</li>
 * <li>TrackExtension atoms - Atoms containing the necessary out-of-band decompression parameters for the sound decompressor.
 * For MPEG-4 audio ('mp4a'), this includes elementary stream descriptor ('esds'), format ('frma'), and terminator atoms.</li>
 * </ul>
 * <p>Possible paths: /moov/trak/mdia/minf/stbl/stsd/mp4a/wave/esds or /moov/trak/mdia/minf/stbl/stsd/mp4a/wave/mp4a/esds</p>
 *
 * @author Paul Gregoire (mondain@gmail.com)
 */
public final class AppleWaveBox extends AbstractContainerBox {
    public static final String TYPE = "wave";

    public AppleWaveBox() {
        super(TYPE);
    }

}
