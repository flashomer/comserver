# ComServer #

The purpose to use the FLV as the main container rather than remote shared object or other technologies is that it is readily transmitted between rtmp stream servers.
The FLV format is well documented, and supports embedded script cue points,
which provide an easy and natural path for the server to invoke functions on the client.

In another light,it is possible to have a game involving a group of players while broadcasting the FLV containing their interaction through any commercial flash CDN to a scalable global audience.

The resource stream can be recorded and played back at a later time, or you can have the recorded interaction integrated into another interactive stream.

Another benefit that I like to imagine is that the script-data events are cued within the netstream buffer.