# Using the Red5 codec configuration mechanism. #
When transcoding or generating audio or video data, the Red5 PlayEngine will deliver any special configuration packets you need as well as the stream meta data for the consumer  upon subscription. Failure to set this data for decoders will keep the stream from playing. For instance, using the Comserver Demo One with AVC video results in failure for the subscriber until an AVC configuration packet is encountered.  To remedy this, Red5 provides the StreamCodecInfo object which the PlayEngine will inspect on subscription.

Here is the basic steps in how to utilize the StreamCodecInfo with a ResourceFeed containing AVC video. Using the FLVFeed from Comserver Demo One, add a field to hold the feed's IVideoStreamCodec object. As you might guess, the  StreamCodecInfo holds both  IVideoStreamCodec and IAudioStreamCodec interface objects. Since I am making an example for AVC video, I know specifically that I will need the Red5 class org.red5.server.stream.codec.AVCVideo.
```
vidInfo = new AVCVideo();
```
The IVideoStreamCodec interface is simple to use. Each Video data event is passed to the instance and it handles the storage of the key frame and codec configuration.
```
msg = msgIn.pullMessage();

if (msg instanceof RTMPMessage){	

	lMsg = (RTMPMessage) msg;

	switch (lMsg.getBody().getDataType()) {
						
		case Constants.TYPE_VIDEO_DATA:	
			VideoData data=(VideoData) lMsg.getBody();		
			vidInfo.addData(data. getData());
			break;
						

	}
}
```

The important step in this process is for your IResourceFeed to apply the IVideoStreamCodec to the ResourceStream's  StreamCodecInfo object and to set the boolean hasVideo field to true.
```
public void addResourceSink(IResourceSink arg0) {
		
	StreamCodecInfo info=(StreamCodecInfo) arg0.getStream().getCodecInfo();
	info.setVideoCodec( vidInfo);
	info.setHasVideo(true);	
	streams.add(arg0);
}
```
Once the IVideoStreamCodec or IAudioStreamCodec interface is set to the ResourceStream's  StreamCodecInfo object, the Red5 PlayEngine will provide the client with the critical packets at moment they begin playback. Setting the codec information also enables video frame dropping for more agile delivery where the network does not support the bandwidth.