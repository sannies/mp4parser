<a href='https://pledgie.com/campaigns/28717'><img alt='Click here to lend your support to: MP4Parser and make a donation at pledgie.com !' src='https://pledgie.com/campaigns/28717.png?skin_name=chrome' border='0' ></a> 
[![Build Status](https://travis-ci.org/sannies/mp4parser.svg?branch=master)](https://travis-ci.org/sannies/mp4parser)

Java MP4 Parser
====================

A Java API to read, write and create MP4 container. Manipulating containers is different from encoding and decoding videos and audio. 

Using the library
------------------

The library is published to Maven repositories. Each release is pushed to a staging repository which is published on the release page. On request specific releases can be pushed to maven central. 

```
  <dependency>
    <groupId>com.googlecode.mp4parser</groupId>
    <artifactId>isoparser</artifactId>
    <version>1.0.5.4</version>
  </dependency>
```

For projects that do not use a dependency management tool each release's artifacts (jar, javadoc-jar, source-jar) are attached to the release page. Please be aware that the project requires the aspectj-rt.jar library. 


What can you do?
--------------------

Typical tasks for the MP4 Parser are: 

- Muxing audio/video into an MP4 file
- Append recordings that use same encode settings
- Adding/Changing metadata
- Shorten recordings by omitting frames

My examples will all use H264 and AAC as these two codecs are most typical for MP4 files. AC-3 is also not uncommon as the codec is well known from DVD. 
There are also MP4 files with H263/MPEG-2 video tracks but they are no longer used widespread as most android phones. You can also

Muxing Audio/Video
--------------------

The API and the process is straight-forward:

1. You wrap each raw format file into an appropriate Track object. 
  ```java
H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl("video.h264"));
AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl("audio.aac"));
  ```

2. These Track object are then added to a Movie object
  ```java
Movie movie = new Movie();
movie.addTrack(h264Track);
movie.addTrack(aacTrack);
  ```

3. The Movie object is fed into an MP4Builder to create the container. 
  ```java
Container mp4file = new DefaultMp4Builder().build(movie);
  ```

4. Write the container to an appropriate sink.
  ```java
FileChannel fc = new FileOutputStream(new File("output.mp4")).getChannel();
mp4file.writeContainer(fc);
fc.close();
  ```

There are cases where the frame rate is signalled out of band or is known in advance so that the H264 doesn't contain it literally. 
In this case you will have to supply it to the constructor. 

There are Track implementations for the following formats: 

 * H264
 * AAC
 * AC3
 * EC3 

and additionally two subtitle tracks that do not directly wrap a raw format but they are conceptually similar.

Typical Issues
--------------------

Audio and video are not in sync. Whenever there are problems with timing possible make sure to start 

Audio starts before video
--------------------

In AAC there are always samplerate/1024 sample/s so each sample's duration is 1000 * 1024 / samplerate milliseconds. 

 * 48KHz => ~21.3ms
 * 44.1KHz => ~23.2ms

By omitting samples from the start you can easily shorten the audio track. Remove as many as you need. You will not be able 
to match audio and video exactly with that but the human perception is more sensible to early audio than to late audio. 

Remember: If someone is only 10 meters away the delay between audio and video is >30ms. The brain is used to that!

```java
AACTrackImpl aacTrackOriginal = new AACTrackImpl(new FileDataSourceImpl("audio.aac"));
// removes the first sample and shortens the AAC track by ~22ms
CroppedTrack aacTrackShort = new CroppedTrack(aacTrackOriginal, 1, aacTrack.getSamples().size());
```




Append Recordings with Same Encode Settings 
-------------------------------------------

It is important to emphasize that you cannot append any two tracks with: 
 
 * Different resolutions 
 * Different frame-rates

What can't you do?
--------------------

Create JPEGs from a movie. No - this is no decoder. The MP4 Parser doesn't know how to do that. 
Create a movie from JPEGs
