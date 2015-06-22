# Version 1.0 #

Note: The java jar is not included in the demo folders.

### Demo 1 ###
Demo one server code will load all the flv files in the top level of the streams directory and use them to create a global feed for all live flv streams you target with the client.

The client shows how to create a resource, a proxy , and attach the underlying netstream to a video object. Updated and improved by using creation time delta rather than Sleep time. To integrate A/V streams, the creation-times of the output and input media must be closely monitored, or the a/v may stutter. This demo gives you a starting point.
### Demo 2 ###
Demo two client code shows how to send messages and configure the flv buffer. The server code shows how to use the room ticker feed which broadcasts node data, and also how to configure metadata for the resource streams. The room ticker code is inside the framework source.
### Demo 3 ###
Demo 3 includes only flash code and can run on the demo 2 server code. It shows how you might add shared objects to the mix.
### Demo 4 ###
Pass the Mic! Video conference sample using shared objects to control which client live stream is viewed in a singular comserver Resource stream. The java demo shows a bit of the IConnectionNode usage. The flex demo passes a flex form-document as node data.
Requires camera and microphone for broadcast, or requires you to disable device access to view stream without.
### Demo 5 ###
Uses a Jinngine physics based stream feed, with away3dlite flash rendering to create a shared physics simulation. The data is delivered to the clients through the comserver flv stream in real time. The view of the simulation is synchronized between clients and based of their flv stream buffer length. The server-demo does not use a global feed, so every unique stream name in every room you create at run-time will be it's own simulation. Only a stream's own subscribers will be sharing that particular stream's physics simulation. When a client closes the browser, the node data shape is also removed. When new clients join, their shape is added to the stream sim.

Update: Rotation angles fixed, and server side Jinngine api usage corrected.

Jinngine: [http://code.google.com/p/jinngine/](http://code.google.com/p/jinngine/)

away3dlite: [http://away3d.com/](http://away3d.com/)
### Demo 6 ###
Identity and authorization demo using facebook user id. The demo uses the flash facebook api in an IFrame, verifies the facebook parameters, and encodes the user id, and user type into a token for the red5 ComServer framework. I'll assume you are familiar with the facebook application platform and can set up your own IFrame app at facebook.com.

The demo uses the Facebook api to request permissions. Once given, the IFrame is reloaded, and the token generated for red5. Next the user and list of friend id's are retrieved. The user then creates a set of properties to share, connects and joins a shared object. Once fully connected and shared object data set, for each user defined in the shared object, the user's image is loaded onto a sprite and place onstage.

This swf file is based on the IFrame demo from the facebook flash API

facebook flash API:http://code.google.com/p/facebook-actionscript-api/
### Demo 7 ###
IP cam monitor in it's most basic form. A resource feed connects to some ip cam image address, converts to screen video codec, and streams to clients through resource proxy comstream. Also works with some regular jpegs. Included is the original java ip cam code to experiment with. Included is a jpeg sample image in case you want to try without having an ip cam. Not all ip cam image formats will work 'out of the box' with this code. It is intended as only a starting point. I hope to revisit ip cams in the future to explore cams with pre-compressed formats besides jpeg.
### Jacuda Encoder ###
Hardware H264 encoder sample. Requires a webcam, windows 7 and a 'Tesla' or 'Fermi' GPU. The demo consists of a red5 webapp, a publishing client, and a playback client. The publisher will load a camera via jni interface, create the nVidia Hardware encoder, and begin streaming to the application , or any other application you specify.
While not exactly a comserver application, this demo shares many aspects of the internal core of the comserver.
### Light ###
The 'light' demo show how to use the ApplicationLight class. This is for more traditional red5 programming where you do not have a central flv acting as the main data point for consumers. The benefits of this class are simple spring configuration of identity / authentication modules.