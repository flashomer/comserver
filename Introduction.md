# Framework Introduction #

The connection between flash and red5 is through rtmp protocol.
The client connects and plays a live FLV target stream.
The root path, which includes the URI and Application name,
is combined with a formatted string to produce a full context
path and stream name.
Since the goal of this framework is to share and inject script data into FLV streams,
the target uri and stream name are formalized into a class object.

Flash class:
com.thebitstream.comserver.Resource

ctor parameter format:

Root-
"rtmp://domain.com:1935/applicationName/"

Context-
"roomName:roomName:streamName"
or
"roomName:streamName"
or
"roomName:"  (no stream subscribe, for advanced 'invoke only' connection.)

Any components or multi-user-tools  that are meant to share data should join the same context path.
Component base classes should implement an interface to provide the context at run-time.

## On The Server ##

On the server, a stream is played where no other file or live published stream resides.
The server creates an empty stream, and notifies the subscriber.
It broadcasts until the room is destroyed.
As an application developer, you create feed objects and add them to the resource.
The feed objects can be more or less sophisticated as you please,
and should pump script data into the FLV stream.

On the server is another associated object type with a reference to the FLV stream.
The Node data has an id and a map of name/value pairs.
A subscribing flash client is represented as a Node.
Feeds are notified of node additions and removals, so a feed that broadcasts the
presence of connected nodes can be developed and likely included in a release soon.
There is no limit to the types of feeds and nodes you can develop.

Included framework feeds/nodes:

Static Node: basic minimum node form.

IConnectionNode- Subscribing flash clients. Managed by the framework.

SimpleFlVFeed- looping or not, pushes script-data/audio/video into one or more FLV resources.

Presence- broadcasts presence of nodes and the associated node data.

Potential Feeds or Nodes you could make:

Physics engine singleton or 2d grid singleton for game players.

Stream interceptor/switcher , 'for passing the mic', or broadcast hubs.

Stream trans-coders from alternate sources.

A/V re-muxer, combine audio and video from separate existing flash streams.

Playback of prerecorded user input.

Shared object data listener injecting into FLV.