Java MP4 Parser
====================

A Java API to read, write and create MP4 container. Manipulating containers is different from encoding and decoding videos and audio. 


What can you do?
--------------------

Typical tasks for the MP4 Parser are: 

- Muxing audio/video into an MP4 file
- Append recordings that use same encode settings
- Adding/Changing metadata
- Shorten recordings by ommiting frames. 

My examples will all use H264 and AAC as these to codecs are most typical for MP4 files. AC-3 is also not uncommon as the codec is well known from DVD. 
There are also MP4 files with H263/MPEG-2 video tracks but they are no longer used widespread as most android phones   You can also

Muxing Audio/Video
--------------------

The API and the process is straight-forward:

1. You wrap each raw format file into an appropriate Track object. 
{code}
        H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl("video.h264"));
        AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl("audio.aac"));
{code}
2. These Track object are then added to a Movie object
{code}
        Movie movie = new Movie();
        movie.addTrack(h264Track);
        movie.addTrack(aacTrack);
{code}
3. The Movie object is fed into an MP4Builder to create the container. 
{code}
        Container mp4file = new DefaultMp4Builder().build(movie);
{code}
4. Write the container to an appropriate sink.
{code}
        FileChannel fc = new FileOutputStream(new File("output.mp4")).getChannel();
        mp4file.writeContainer(fc);
        fc.close();
{code}

There are cases where the frame rate is signalled out of band or is known in advance so that the H264 doesn't contain it literally. 
In this case you will have to supply it to the constructor. 

There Track implementations for the following formats: 

 * H264
 * AAC
 * AC3
 * EC3 

and additionally two subtitle tracks that do not directly wrap a raw format but they are conceptually similar.


Append Recordings with Same Encode Settings 
-------------------------------------------

It is important to emphasize that you cannot append any two tracks with: 
 
 * Different resultions 
 * Different framerates
 

 
 as this leads to d 
What can't you do?
--------------------

Create JPEGs from a movie. No - this is no decoder. The MP4 Parser doesn't know how to do that. 
Create a movie from JPEGs
