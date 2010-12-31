/*  
 * Copyright 2008 CoreMedia AG, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an AS IS BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package com.coremedia.iso;

import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.AlbumBox;
import com.coremedia.iso.boxes.AuthorBox;
import com.coremedia.iso.boxes.BitRateBox;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ClassificationBox;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.CopyrightBox;
import com.coremedia.iso.boxes.DataEntryUrlBox;
import com.coremedia.iso.boxes.DataEntryUrnBox;
import com.coremedia.iso.boxes.DataInformationBox;
import com.coremedia.iso.boxes.DataReferenceBox;
import com.coremedia.iso.boxes.DescriptionBox;
import com.coremedia.iso.boxes.ESDescriptorBox;
import com.coremedia.iso.boxes.EditBox;
import com.coremedia.iso.boxes.EditListBox;
import com.coremedia.iso.boxes.FileTypeBox;
import com.coremedia.iso.boxes.FreeBox;
import com.coremedia.iso.boxes.FreeSpaceBox;
import com.coremedia.iso.boxes.GenreBox;
import com.coremedia.iso.boxes.HandlerBox;
import com.coremedia.iso.boxes.HintMediaHeaderBox;
import com.coremedia.iso.boxes.ItemProtectionBox;
import com.coremedia.iso.boxes.KeywordsBox;
import com.coremedia.iso.boxes.MediaBox;
import com.coremedia.iso.boxes.MediaDataBox;
import com.coremedia.iso.boxes.MediaHeaderBox;
import com.coremedia.iso.boxes.MediaInformationBox;
import com.coremedia.iso.boxes.MetaBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.coremedia.iso.boxes.OmaDrmAccessUnitFormatBox;
import com.coremedia.iso.boxes.OriginalFormatBox;
import com.coremedia.iso.boxes.PerformerBox;
import com.coremedia.iso.boxes.ProtectionSchemeInformationBox;
import com.coremedia.iso.boxes.RatingBox;
import com.coremedia.iso.boxes.RecordingYearBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.SampleSizeBox;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.SampleToChunkBox;
import com.coremedia.iso.boxes.SchemeInformationBox;
import com.coremedia.iso.boxes.SchemeTypeBox;
import com.coremedia.iso.boxes.SoundMediaHeaderBox;
import com.coremedia.iso.boxes.StaticChunkOffsetBox;
import com.coremedia.iso.boxes.SyncSampleBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TitleBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.coremedia.iso.boxes.TrackReferenceBox;
import com.coremedia.iso.boxes.TrackReferenceTypeBox;
import com.coremedia.iso.boxes.UnknownBox;
import com.coremedia.iso.boxes.UserBox;
import com.coremedia.iso.boxes.UserDataBox;
import com.coremedia.iso.boxes.VideoMediaHeaderBox;
import com.coremedia.iso.boxes.apple.AppleAlbumArtistBox;
import com.coremedia.iso.boxes.apple.AppleAlbumBox;
import com.coremedia.iso.boxes.apple.AppleArtistBox;
import com.coremedia.iso.boxes.apple.AppleCommentBox;
import com.coremedia.iso.boxes.apple.AppleCompilationBox;
import com.coremedia.iso.boxes.apple.AppleCopyrightBox;
import com.coremedia.iso.boxes.apple.AppleCoverBox;
import com.coremedia.iso.boxes.apple.AppleCustomGenreBox;
import com.coremedia.iso.boxes.apple.AppleDataBox;
import com.coremedia.iso.boxes.apple.AppleDataRateBox;
import com.coremedia.iso.boxes.apple.AppleDataReferenceBox;
import com.coremedia.iso.boxes.apple.AppleDescriptionBox;
import com.coremedia.iso.boxes.apple.AppleEncoderBox;
import com.coremedia.iso.boxes.apple.AppleGaplessPlaybackBox;
import com.coremedia.iso.boxes.apple.AppleGenericBox;
import com.coremedia.iso.boxes.apple.AppleGroupingBox;
import com.coremedia.iso.boxes.apple.AppleIdBox;
import com.coremedia.iso.boxes.apple.AppleItemListBox;
import com.coremedia.iso.boxes.apple.AppleLosslessSpecificBox;
import com.coremedia.iso.boxes.apple.AppleMeanBox;
import com.coremedia.iso.boxes.apple.AppleMediaTypeBox;
import com.coremedia.iso.boxes.apple.AppleNameBox;
import com.coremedia.iso.boxes.apple.AppleNetworkBox;
import com.coremedia.iso.boxes.apple.ApplePurchaseDateBox;
import com.coremedia.iso.boxes.apple.AppleRatingBox;
import com.coremedia.iso.boxes.apple.AppleRecordingYearBox;
import com.coremedia.iso.boxes.apple.AppleReferenceMovieBox;
import com.coremedia.iso.boxes.apple.AppleReferenceMovieDescriptorBox;
import com.coremedia.iso.boxes.apple.AppleShowBox;
import com.coremedia.iso.boxes.apple.AppleSortAlbumBox;
import com.coremedia.iso.boxes.apple.AppleStandardGenreBox;
import com.coremedia.iso.boxes.apple.AppleStoreAccountTypeBox;
import com.coremedia.iso.boxes.apple.AppleStoreCountryCodeBox;
import com.coremedia.iso.boxes.apple.AppleSynopsisBox;
import com.coremedia.iso.boxes.apple.AppleTempBox;
import com.coremedia.iso.boxes.apple.AppleTrackAuthorBox;
import com.coremedia.iso.boxes.apple.AppleTrackNumberBox;
import com.coremedia.iso.boxes.apple.AppleTrackTitleBox;
import com.coremedia.iso.boxes.apple.AppleTvEpisodeBox;
import com.coremedia.iso.boxes.apple.AppleTvEpisodeNumberBox;
import com.coremedia.iso.boxes.apple.AppleTvSeasonBox;
import com.coremedia.iso.boxes.fragment.MovieExtendsBox;
import com.coremedia.iso.boxes.fragment.MovieExtendsHeaderBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentHeaderBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentRandomAccessBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentRandomAccessOffsetBox;
import com.coremedia.iso.boxes.fragment.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.fragment.TrackExtendsBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentHeaderBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentRandomAccessBox;
import com.coremedia.iso.boxes.fragment.TrackRunBox;
import com.coremedia.iso.boxes.h264.AvcConfigurationBox;
import com.coremedia.iso.boxes.odf.MutableDrmInformationBox;
import com.coremedia.iso.boxes.odf.OmaDrmCommonHeadersBox;
import com.coremedia.iso.boxes.odf.OmaDrmContainerBox;
import com.coremedia.iso.boxes.odf.OmaDrmContentIdBox;
import com.coremedia.iso.boxes.odf.OmaDrmContentObjectBox;
import com.coremedia.iso.boxes.odf.OmaDrmCoverUriBox;
import com.coremedia.iso.boxes.odf.OmaDrmDiscreteHeadersBox;
import com.coremedia.iso.boxes.odf.OmaDrmGroupIdBox;
import com.coremedia.iso.boxes.odf.OmaDrmIconUriBox;
import com.coremedia.iso.boxes.odf.OmaDrmInfoUrlBox;
import com.coremedia.iso.boxes.odf.OmaDrmKeyManagenentSystemBox;
import com.coremedia.iso.boxes.odf.OmaDrmLyricsUriBox;
import com.coremedia.iso.boxes.odf.OmaDrmRightsObjectBox;
import com.coremedia.iso.boxes.odf.OmaDrmTransactionTrackingBox;
import com.coremedia.iso.boxes.rtp.HintInformationBox;
import com.coremedia.iso.boxes.rtp.HintPacketsSentBox;
import com.coremedia.iso.boxes.rtp.HintSampleEntry;
import com.coremedia.iso.boxes.rtp.HintStatisticBoxes;
import com.coremedia.iso.boxes.rtp.HintStatisticsBox;
import com.coremedia.iso.boxes.rtp.LargestHintPacketBox;
import com.coremedia.iso.boxes.rtp.LargestHintPacketDurationBox;
import com.coremedia.iso.boxes.rtp.LargestRelativeTransmissionTimeBox;
import com.coremedia.iso.boxes.rtp.MaximumDataRateBox;
import com.coremedia.iso.boxes.rtp.PayloadTypeBox;
import com.coremedia.iso.boxes.rtp.RtpMovieHintInformationBox;
import com.coremedia.iso.boxes.rtp.RtpTrackSdpHintInformationBox;
import com.coremedia.iso.boxes.rtp.SmallestRelativeTransmissionTimeBox;
import com.coremedia.iso.boxes.rtp.TimeScaleEntry;
import com.coremedia.iso.boxes.sampleentry.AmrSpecificBox;
import com.coremedia.iso.boxes.sampleentry.AudioSampleEntry;
import com.coremedia.iso.boxes.sampleentry.TextSampleEntry;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;
import com.coremedia.iso.boxes.threegpp26244.LocationInformationBox;
import com.coremedia.iso.boxes.vodafone.AlbumArtistBox;
import com.coremedia.iso.boxes.vodafone.ContentDistributorIdBox;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 *
 */
public class OldBoxFactoryImpl extends AbstractBoxParser {
    private static Logger LOG = Logger.getLogger(OldBoxFactoryImpl.class.getName());


    //TODO there are better ways than one millions if-statements,  I'm sure --sma
    public AbstractBox createBox(byte[] type, byte[] userType, byte[] parent, Box lastMovieFragmentBox) {
        //  System.err.println("Box: " + IsoFile.bytesToFourCC(type) + " Parent: " + ((parent!=null&&parent.length==4)?IsoFile.bytesToFourCC(parent):"IsoFile"));

        if (Arrays.equals(parent, IsoFile.fourCCtoBytes(TrackReferenceTypeBox.TYPE1)) ||
                (Arrays.equals(parent, IsoFile.fourCCtoBytes(TrackReferenceTypeBox.TYPE2)))) {
            return new TrackReferenceTypeBox(type);
        }
        if (Arrays.equals(parent, IsoFile.fourCCtoBytes(MetaBox.TYPE))) {
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleItemListBox.TYPE))) {
                return new AppleItemListBox();
            }
        }
        if (Arrays.equals(parent, IsoFile.fourCCtoBytes(AppleGenericBox.TYPE))) {
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleNameBox.TYPE))) {
                return new AppleNameBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleMeanBox.TYPE))) {
                return new AppleMeanBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleDataBox.TYPE))) {
                return new AppleDataBox();
            }
        }


        if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleReferenceMovieBox.TYPE))) {
            return new AppleReferenceMovieBox();
        }

        if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleReferenceMovieDescriptorBox.TYPE))) {
            return new AppleReferenceMovieDescriptorBox();
        }

        if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleDataRateBox.TYPE))) {
            return new AppleDataRateBox();
        }

        if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleDataReferenceBox.TYPE))) {
            return new AppleDataReferenceBox();
        }


        if (Arrays.equals(parent, IsoFile.fourCCtoBytes(AppleItemListBox.TYPE))) {

            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleCopyrightBox.TYPE))) {
                return new AppleCopyrightBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleGenericBox.TYPE))) {
                return new AppleGenericBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleRatingBox.TYPE))) {
                return new AppleRatingBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleGaplessPlaybackBox.TYPE))) {
                return new AppleGaplessPlaybackBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleGroupingBox.TYPE))) {
                return new AppleGroupingBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleAlbumArtistBox.TYPE))) {
                return new AppleAlbumArtistBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleGroupingBox.TYPE))) {
                return new AppleGroupingBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleTvEpisodeBox.TYPE))) {
                return new AppleTvEpisodeBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleCommentBox.TYPE))) {
                return new AppleCommentBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleTempBox.TYPE))) {
                return new AppleTempBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleEncoderBox.TYPE))) {
                return new AppleEncoderBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleTvSeasonBox.TYPE))) {
                return new AppleTvSeasonBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleCompilationBox.TYPE))) {
                return new AppleCompilationBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleTrackTitleBox.TYPE))) {
                return new AppleTrackTitleBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleAlbumBox.TYPE))) {
                return new AppleAlbumBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleCoverBox.TYPE))) {
                return new AppleCoverBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleArtistBox.TYPE))) {
                return new AppleArtistBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleTrackAuthorBox.TYPE))) {
                return new AppleTrackAuthorBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleTrackNumberBox.TYPE))) {
                return new AppleTrackNumberBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleStandardGenreBox.TYPE))) {
                return new AppleStandardGenreBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleCustomGenreBox.TYPE))) {
                return new AppleCustomGenreBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleRecordingYearBox.TYPE))) {
                return new AppleRecordingYearBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleIdBox.TYPE))) {
                return new AppleIdBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleNetworkBox.TYPE))) {
                return new AppleNetworkBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleShowBox.TYPE))) {
                return new AppleShowBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleDescriptionBox.TYPE))) {
                return new AppleDescriptionBox();
            }

            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleSynopsisBox.TYPE))) {
                return new AppleSynopsisBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(ApplePurchaseDateBox.TYPE))) {
                return new ApplePurchaseDateBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleTvEpisodeNumberBox.TYPE))) {
                return new AppleTvEpisodeNumberBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleSortAlbumBox.TYPE))) {
                return new AppleSortAlbumBox();
            }

            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleStoreAccountTypeBox.TYPE))) {
                return new AppleStoreAccountTypeBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleStoreCountryCodeBox.TYPE))) {
                return new AppleStoreCountryCodeBox();
            }

            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleMediaTypeBox.TYPE))) {
                return new AppleMediaTypeBox();
            }


        }
        if (Arrays.equals(parent, IsoFile.fourCCtoBytes(UserDataBox.TYPE))) {
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(OmaDrmContentIdBox.TYPE))) {
                return new OmaDrmContentIdBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(RecordingYearBox.TYPE))) {
                return new RecordingYearBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(HintInformationBox.TYPE))) {
                return new HintInformationBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(HintStatisticsBox.TYPE))) {
                return new HintStatisticsBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(TitleBox.TYPE))) {
                return new TitleBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(DescriptionBox.TYPE))) {
                return new DescriptionBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(OmaDrmIconUriBox.TYPE))) {
                return new OmaDrmIconUriBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(OmaDrmInfoUrlBox.TYPE))) {
                return new OmaDrmInfoUrlBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AlbumBox.TYPE))) {
                return new AlbumBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(CopyrightBox.TYPE))) {
                return new CopyrightBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(GenreBox.TYPE))) {
                return new GenreBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(PerformerBox.TYPE))) {
                return new PerformerBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AuthorBox.TYPE))) {
                return new AuthorBox();
            }

            if (Arrays.equals(type, IsoFile.fourCCtoBytes(KeywordsBox.TYPE))) {
                return new KeywordsBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(LocationInformationBox.TYPE))) {
                return new LocationInformationBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(RatingBox.TYPE))) {
                return new RatingBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(ClassificationBox.TYPE))) {
                return new ClassificationBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(ContentDistributorIdBox.TYPE))) {
                return new ContentDistributorIdBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AlbumArtistBox.TYPE))) {
                return new AlbumArtistBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(OmaDrmCoverUriBox.TYPE))) {
                return new OmaDrmCoverUriBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(OmaDrmLyricsUriBox.TYPE))) {
                return new OmaDrmLyricsUriBox();
            }
        }
        if (Arrays.equals(parent, IsoFile.fourCCtoBytes(HintInformationBox.TYPE))) {
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(RtpTrackSdpHintInformationBox.TYPE))) {
                return new RtpTrackSdpHintInformationBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(RtpMovieHintInformationBox.TYPE))) {
                return new RtpMovieHintInformationBox();
            }
        }
        if (Arrays.equals(parent, IsoFile.fourCCtoBytes(HintStatisticsBox.TYPE))) {
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(HintPacketsSentBox.TYPE1))) {
                return new HintPacketsSentBox(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(HintPacketsSentBox.TYPE2))) {
                return new HintPacketsSentBox(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(LargestHintPacketDurationBox.TYPE))) {
                return new LargestHintPacketDurationBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(LargestHintPacketBox.TYPE))) {
                return new LargestHintPacketBox();
            }

            if (Arrays.equals(type, IsoFile.fourCCtoBytes(SmallestRelativeTransmissionTimeBox.TYPE))) {
                return new SmallestRelativeTransmissionTimeBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(LargestRelativeTransmissionTimeBox.TYPE))) {
                return new LargestRelativeTransmissionTimeBox();
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(MaximumDataRateBox.TYPE))) {
                return new MaximumDataRateBox();
            }

            if (Arrays.equals(type, IsoFile.fourCCtoBytes(HintStatisticBoxes.TYPE1))) {
                return new HintStatisticBoxes(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(HintStatisticBoxes.TYPE2))) {
                return new HintStatisticBoxes(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(HintStatisticBoxes.TYPE3))) {
                return new HintStatisticBoxes(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(HintStatisticBoxes.TYPE4))) {
                return new HintStatisticBoxes(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(HintStatisticBoxes.TYPE5))) {
                return new HintStatisticBoxes(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(HintStatisticBoxes.TYPE6))) {
                return new HintStatisticBoxes(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(HintStatisticBoxes.TYPE7))) {
                return new HintStatisticBoxes(type);
            }

            if (Arrays.equals(type, IsoFile.fourCCtoBytes(PayloadTypeBox.TYPE))) {
                return new PayloadTypeBox();
            }
        }
        if (Arrays.equals(parent, IsoFile.fourCCtoBytes(SampleDescriptionBox.TYPE))) {
            /*
            *  Sample Entries are parsed here
            ************/

            /*
            * Hint Samples
            ******************/
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(HintSampleEntry.TYPE1))) {
                return new HintSampleEntry(type);
            }

            /*
            * Text Samples
            ******************/
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(TextSampleEntry.TYPE1))) {
                return new TextSampleEntry(type);
            }

            /*
            * Audio Samples
            ******************/
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AudioSampleEntry.TYPE1))) {
                return new AudioSampleEntry(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AudioSampleEntry.TYPE2))) {
                return new AudioSampleEntry(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AudioSampleEntry.TYPE3))) {
                return new AudioSampleEntry(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AudioSampleEntry.TYPE4))) {
                return new AudioSampleEntry(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AudioSampleEntry.TYPE5))) {
                return new AudioSampleEntry(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AudioSampleEntry.TYPE7))) {
                return new AudioSampleEntry(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(AudioSampleEntry.TYPE_ENCRYPTED))) {
                return new AudioSampleEntry(type);
            }

            /*
            * Video Samples
            ******************/
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(VisualSampleEntry.TYPE1))) {
                return new VisualSampleEntry(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(VisualSampleEntry.TYPE2))) {
                return new VisualSampleEntry(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(VisualSampleEntry.TYPE3))) {
                return new VisualSampleEntry(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(VisualSampleEntry.TYPE4))) {
                return new VisualSampleEntry(type);
            }
            if (Arrays.equals(type, IsoFile.fourCCtoBytes(VisualSampleEntry.TYPE_ENCRYPTED))) {
                return new VisualSampleEntry(type);
            }
        }


        if (Arrays.equals(type, IsoFile.fourCCtoBytes(ESDescriptorBox.TYPE))) {
            return new ESDescriptorBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(AvcConfigurationBox.TYPE))) {
            return new AvcConfigurationBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleLosslessSpecificBox.TYPE))) {
            return new AppleLosslessSpecificBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(BitRateBox.TYPE))) {
            return new BitRateBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(FileTypeBox.TYPE))) {
            return new FileTypeBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(MediaDataBox.TYPE))) {
            return lastMovieFragmentBox != null ?
                    new MediaDataBox<TrackFragmentBox>((MovieFragmentBox) lastMovieFragmentBox) : new MediaDataBox<TrackBox>(null);
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(MovieBox.TYPE))) {
            return new MovieBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(MovieHeaderBox.TYPE))) {
            return new MovieHeaderBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(TrackBox.TYPE))) {
            return new TrackBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(TrackHeaderBox.TYPE))) {
            return new TrackHeaderBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(EditBox.TYPE))) {
            return new EditBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(EditListBox.TYPE))) {
            return new EditListBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(MediaBox.TYPE))) {
            return new MediaBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(MediaHeaderBox.TYPE))) {
            return new MediaHeaderBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(HandlerBox.TYPE))) {
            return new HandlerBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(MediaInformationBox.TYPE))) {
            return new MediaInformationBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(VideoMediaHeaderBox.TYPE))) {
            return new VideoMediaHeaderBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(SoundMediaHeaderBox.TYPE))) {
            return new SoundMediaHeaderBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(HintMediaHeaderBox.TYPE))) {
            return new HintMediaHeaderBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(DataInformationBox.TYPE))) {
            return new DataInformationBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(DataReferenceBox.TYPE))) {
            return new DataReferenceBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(DataEntryUrlBox.TYPE))) {
            return new DataEntryUrlBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(DataEntryUrnBox.TYPE))) {
            return new DataEntryUrnBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(SampleTableBox.TYPE))) {
            return new SampleTableBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(CompositionTimeToSample.TYPE))) {
            return new CompositionTimeToSample();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(SampleDescriptionBox.TYPE))) {
            return new SampleDescriptionBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(TimeToSampleBox.TYPE))) {
            return new TimeToSampleBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(SyncSampleBox.TYPE))) {
            return new SyncSampleBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(SampleToChunkBox.TYPE))) {
            return new SampleToChunkBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(SampleSizeBox.TYPE))) {
            return new SampleSizeBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(StaticChunkOffsetBox.TYPE))) {
            return new StaticChunkOffsetBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(UserDataBox.TYPE))) {
            return new UserDataBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(FreeSpaceBox.TYPE))) {
            return new FreeSpaceBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(TrackReferenceBox.TYPE))) {
            return new TrackReferenceBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(TimeScaleEntry.TYPE))) {
            return new TimeScaleEntry();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(OmaDrmContainerBox.TYPE))) {
            return new OmaDrmContainerBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(MutableDrmInformationBox.TYPE))) {
            return new MutableDrmInformationBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(OmaDrmTransactionTrackingBox.TYPE))) {
            return new OmaDrmTransactionTrackingBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(OmaDrmRightsObjectBox.TYPE))) {
            return new OmaDrmRightsObjectBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(OmaDrmDiscreteHeadersBox.TYPE))) {
            return new OmaDrmDiscreteHeadersBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(OmaDrmContentObjectBox.TYPE))) {
            return new OmaDrmContentObjectBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(OmaDrmCommonHeadersBox.TYPE))) {
            return new OmaDrmCommonHeadersBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(OmaDrmGroupIdBox.TYPE))) {
            return new OmaDrmGroupIdBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(AmrSpecificBox.TYPE))) {
            return new AmrSpecificBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(MetaBox.TYPE))) {
            return new MetaBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(ItemProtectionBox.TYPE))) {
            return new ItemProtectionBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(ProtectionSchemeInformationBox.TYPE))) {
            return new ProtectionSchemeInformationBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(OriginalFormatBox.TYPE))) {
            return new OriginalFormatBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(SchemeInformationBox.TYPE))) {
            return new SchemeInformationBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(OmaDrmKeyManagenentSystemBox.TYPE))) {
            return new OmaDrmKeyManagenentSystemBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(OmaDrmAccessUnitFormatBox.TYPE))) {
            return new OmaDrmAccessUnitFormatBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(SchemeTypeBox.TYPE))) {
            return new SchemeTypeBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(SchemeTypeBox.TYPE))) {
            return new SchemeTypeBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(UserBox.TYPE))) {
            return new UserBox(userType);
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(FreeBox.TYPE))) {
            return new FreeBox();
        }

        //fragmented mp4

        //moov contents
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(MovieExtendsBox.TYPE))) {
            return new MovieExtendsBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(MovieExtendsHeaderBox.TYPE))) {
            return new MovieExtendsHeaderBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(TrackExtendsBox.TYPE))) {
            return new TrackExtendsBox();
        }

        //moof
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(MovieFragmentBox.TYPE))) {
            return new MovieFragmentBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(MovieFragmentHeaderBox.TYPE))) {
            return new MovieFragmentHeaderBox();
        }

        if (Arrays.equals(type, IsoFile.fourCCtoBytes(TrackFragmentBox.TYPE))) {
            return new TrackFragmentBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(TrackFragmentHeaderBox.TYPE))) {
            return new TrackFragmentHeaderBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(TrackRunBox.TYPE))) {
            return new TrackRunBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(SampleDependencyTypeBox.TYPE))) {
            return new SampleDependencyTypeBox();
        }

        //mfra index
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(MovieFragmentRandomAccessBox.TYPE))) {
            return new MovieFragmentRandomAccessBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(TrackFragmentRandomAccessBox.TYPE))) {
            return new TrackFragmentRandomAccessBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(MovieFragmentRandomAccessOffsetBox.TYPE))) {
            return new MovieFragmentRandomAccessOffsetBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleReferenceMovieBox.TYPE))) {
            return new AppleReferenceMovieBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleReferenceMovieDescriptorBox.TYPE))) {
            return new AppleReferenceMovieDescriptorBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleDataRateBox.TYPE))) {
            return new AppleDataRateBox();
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(AppleDataReferenceBox.TYPE))) {
            return new AppleDataReferenceBox();
        }
        /* if (Arrays.equals(type, IsoFile.fourCCtoBytes(NameBox.TYPE))) {
            return new NameBox();
        }*/

        String hexType = Integer.toHexString((type[0] >> 4) & 0xf) + Integer.toHexString(type[0] & 0xf) +
                Integer.toHexString((type[1] >> 4) & 0xf) + Integer.toHexString(type[1] & 0xf) +
                Integer.toHexString((type[2] >> 4) & 0xf) + Integer.toHexString(type[2] & 0xf) +
                Integer.toHexString((type[3] >> 4) & 0xf) + Integer.toHexString(type[3] & 0xf);


        LOG.info("Unknown box found: " + IsoFile.bytesToFourCC(type) + " 0x" + hexType + " parent is: " + IsoFile.bytesToFourCC(parent));
        return new UnknownBox(type);
    }


}
