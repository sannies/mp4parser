package com.googlecode.mp4parser.h264;

import com.googlecode.mp4parser.h264.model.PictureParameterSet;
import com.googlecode.mp4parser.h264.model.SeqParameterSet;

/**
 * An interface for retrieving stream parameters from a place that has it
 *
 * @author Stanislav Vitvitskiy
 */
public interface StreamParams {
    SeqParameterSet getSPS(int id);

    PictureParameterSet getPPS(int id);
}
