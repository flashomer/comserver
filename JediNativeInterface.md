# Introduction #

The Jedi Native Interface plays on the jni acronym with the star wars reference. Its purpose is to reuse the java code in c++ applications. Inspired by the jni demo that uses the GPU to compress video, this library aims to allow the same operation, but controlled by c++ directly.


Basically I want to use the red5 rtmp client directly from my directshow applications to produce broadcast encoding, and along the way, learn about hard-core jni/java class loading.

# Details #

The interface consists of a red5 plugin that resides in the plugins folder of your red5 installation, and a c++ class that loads this jar into a VM. Now, it does not start red5, it just makes all the classes available. You could call for the red5 'Bootstrap' class and start red5 if you wanted too.

Once loaded, the jar sets up the system class path the same manner as red5 would, including config folder, spring logging, and all other jars included in the lib folder.

The safest way to use the classes is to build a handler inside the jni plugin and deal with the object lifecycle that way. Theoretically, a hard-core programmer could use the 'findClass' JNI method directly if he/she knew how to handle the java class.

I created a class called 'ChannelOut' in the plugin that safely wraps the RTMPClient using code from the StreamingProxy red5 class as a start. The Class is registered in the JNI Bootstrap jar, and a corresponding  c++ class is created to interface with it in a nice, somewhat OOP way.

Note: 'pushFrame' of the c++ rtmp client in this first version only accepts h264 avc bytestream, which is parsed into flv video packets and pushed out of the rtmp client to the target server.

```
	JediNativeInterface * test= new JediNativeInterface();

	test->createJNI();

	RTMPClient * client=0;

	client = new RTMPClient(test);
	client->createClient();		
	client->setModeVal(0);
	client->setAppName("cuda");
	client->setHostName("127.0.0.1");
	client->setPortNum(1935);
	client->setPublishedName("test");
	client->initiateCapture();

	...

	client->pushFrame(pBuffer,size,time);
	
	...

	if(client){
		client->close();
		delete client;
	}

	...

	test->destroyJNI();

	delete test;

```