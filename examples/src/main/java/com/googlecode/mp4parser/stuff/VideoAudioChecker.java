package com.googlecode.mp4parser.stuff;


import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.HandlerBox;

import java.io.IOException;
import java.util.List;

public class VideoAudioChecker {

    public static void main(String[] args) throws IOException {
        IsoFile isoFile = new IsoFile(VideoAudioChecker.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/count-video.mp4");
        System.err.println(getType(isoFile));
    }

    public static TYPE getType(IsoFile isoFile) {

        List<HandlerBox> handlerBoxes =
                isoFile.getBoxes(HandlerBox.class, true);
        for (HandlerBox handlerBox : handlerBoxes) {
            if ("vide".equals(handlerBox.getHandlerType())) {
                return TYPE.VIDEO;
            } else if ("soun".equals(handlerBox.getHandlerType())) {
                return TYPE.AUDIO;
            } else {
                System.err.println("unknown");
            }

        }
        return TYPE.AUDIO;
    }

    private enum TYPE {
        AUDIO,
        VIDEO
    }

}
