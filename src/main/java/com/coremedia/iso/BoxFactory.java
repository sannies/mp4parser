package com.coremedia.iso;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.BoxInterface;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 30.12.10
 * Time: 17:54
 * To change this template use File | Settings | File Templates.
 */
public interface BoxFactory {
    Box parseBox(IsoBufferWrapper in, BoxInterface parent, Box lastMovieFragmentBox) throws IOException;
}
