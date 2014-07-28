package com.googlecode.mp4parser;

import com.coremedia.iso.BoxReplacer;
import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.boxes.dece.BaseLocationBox;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class ModifyBoxExample {
    public static void main(String[] args) throws IOException {
        File fc = new File("D:\\PKG - Paramount UVU files Star Trek, MI4\\Mission_Impossible_Ghost_Protocol_Feature_SDUV_480p_16avg192max.uvu");
        BoxReplacer.replace(Collections.<String, Box>singletonMap("/bloc", new BaseLocationBox("baselocation", "purchaselocation")), fc);
    }

}
