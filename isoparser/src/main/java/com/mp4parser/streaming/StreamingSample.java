package com.mp4parser.streaming;

import java.nio.ByteBuffer;

/**
 *
 */
public interface StreamingSample {
    ByteBuffer getContent();

    /**
     * Gets the sample dependency as 4 times 2 bits:
     * <ul>
     * <li>unsigned int(2) is_leading;</li>
     * <li>unsigned int(2) sample_depends_on;</li>
     * <li>unsigned int(2) sample_is_depended_on; </li>
     * <li>unsigned int(2) sample_has_redundancy;</li>
     * </ul>
     * <pre>is_leading</pre> takes one of the following four values:
     * 0: the leading nature of this sample is unknown;<br/>
     * 1: this sample is a leading sample that hasa dependency before the referenced I-picture (and is therefore not decodable);<br/>
     * 2: this sample is not a leading sample; <br/>
     * 3: this sample is a leading sample that has no dependency before the referenced I-picture (and is  therefore decodable);<br/>
     * <pre>sample_depends_on</pre>takes one of the following four values:
     * 0: the dependency of this sample is unknown; <br/>
     * 1: this sample does depend on others (not an I picture);<br/>
     * 2: this sample does not depend on others (I picture); <br/>
     * 3: reserved <br/>
     * <pre>sample_is_depended_on</pre> takes one of the following four values:
     * 0: the dependency of other samples on this sample is unknown;<br/>
     * 1: other samples may depend on this one (not disposable); <br/>
     * 2: no other sample depends on this one (disposable); <br/>
     * 3: reserved<br/>
     * <pre>sample_has_redundancy</pre> takes one of the following four values:
     * 0: it is unknown whether there is redundant coding in this sample;<br/>
     * 1: there is redundant coding in this sample; <br/>
     * 2: there is no redundant coding in this sample; <br/>
     * 3: reserved <br/>
     */
    byte getSampleDependency();

    /**
     * This value provides the offset between decoding time and composition time. The offset is expressed as
     * signed long such that CT(n) = DT(n) + CTTS(n). This method is
     *
     * @return offset between decoding time and composition time.
     */
    long getCompositionTimeOffset();

    long getPresentationTime();

}
