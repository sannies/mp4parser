package com.mp4parser.streaming.rawformats.aac;

/**
 * Created by sannies on 01.09.2015.
 */
public enum AudioObjectTypes {


    NOT_APPLICABLE("n/a"),
    AAC_Main("AAC Main"),
    AAC_LC("AAC LC (Low Complexity)"),
    AAC_SSR("AAC SSR (Scalable Sample Rate)"),
    AAC_LTP("AAC LTP (Long Term Prediction)"),
    SBR("SBR (Spectral Band Replication)"),
    AAC_Scalable("AAC Scalable"),
    TwinVQ("TwinVQ"),
    CELP("CELP (Code Excited Linear Prediction)"),
    HXVC("HXVC (Harmonic Vector eXcitation Coding)"),
    Reserved1("Reserved"),
    Reserved2("Reserved"),
    TTSI("TTSI (Text-To-Speech Interface)"),
    Main_Synthesis("Main Synthesis"),
    Wavetable_Synthesis("Wavetable Synthesis"),
    General_MIDI("General MIDI"),
    Algorithmic_Synthesis_and_Audio_Effects("Algorithmic Synthesis and Audio Effects"),
    ER_AAC_LC("ER (Error Resilient) AAC LC"),
    Reserved3("Reserved"),
    ER_AAC_LTP("ER AAC LTP"),
    ER_AAC_Scalable("ER AAC Scalable"),
    ER_TwinVQ("ER TwinVQ"),
    ER_BSAC("ER BSAC (Bit-Sliced Arithmetic Coding)"),
    ER_AAC_LD("ER AAC LD (Low Delay)"),
    ER_CELP("ER CELP"),
    ER_HVXC("ER HVXC"),
    ER_HILN("ER HILN (Harmonic and Individual Lines plus Noise)"),
    ER_Parametric("ER Parametric"),
    SSC("SSC (SinuSoidal Coding)"),
    PS("PS (Parametric Stereo)"),
    MPEG_Surround("MPEG Surround"),
    Escape_value("(Escape value)"),
    Layer_1("Layer-1"),
    Layer_2("Layer-2"),
    Layer_3("Layer-3"),
    DST("DST (Direct Stream Transfer)"),
    ALS("ALS (Audio Lossless)"),
    SLS("SLS (Scalable LosslesS)"),
    SLS_non_core("SLS non-core"),
    ER_AAC_ELD("ER AAC ELD (Enhanced Low Delay)"),
    SMR_Simple("SMR (Symbolic Music Representation) Simple"),
    SMR_Main("SMR Main"),
    USAC_NO_SBR("USAC (Unified Speech and Audio Coding) (no SBR)"),
    SAOC("SAOC (Spatial Audio Object Coding)"),
    LD_MPEG_Surround("LD MPEG Surround"),
    USAC("USAC");


    String name;

    AudioObjectTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
