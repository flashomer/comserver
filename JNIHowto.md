# Introduction #

Here is a collection of code snippets from my jni experiments.


# Details #

You'll need a good handle on writing c++ and java to enjoy the jni to its full extent or find it practical for any particular task.

## Starting the Red5 Server ##
There are two ways to start the red5 server from c++. The obvious method of using the startup scripts is one, and creating it manually in the same way through the creation of a local JVM is the second.

Method 2:
Assume the pointer to the jni is initialized already.
```
class Red5Server : public ICallbackCapable
{
public:
	JediNativeInterface * pJni;
	jmethodID mid ;
	jobject client; 

	jclass cls;

	Red5Server(JediNativeInterface * jni)
	{		
		client=0;

		this->pJni=jni;
		
	}
	void boot()
	{
		cls = (*pJni->getEnv()).FindClass( "org/red5/server/Bootstrap");
		
		mid = (*pJni->getEnv()).GetStaticMethodID( cls, "main","([Ljava/lang/String;)V");
		
		(*pJni->getEnv()).CallStaticVoidMethod( cls, mid,0);	


	}
```

## Stopping the Red5 Server ##
Stopping the red5 server is done by creating a a new jvm and talking to it over an admin port. Lets cheat and use the script since this is a shut-down and we will not need to talk to the new jvm after it has done the task.

```
	void shutdown()
	{
		::ShellExecute(0,0,"red5-shutdown.bat",0,RED5_HOME,0);
		
	}
```

## Initiate RPC Calls From Local Java VMs to C++ Jedi Native Interface ##
Whether you need the JVM that you have created to call back on it's own threads, or if you need arbitrary JVMs to call your native process, it is supported on Windows through traditional JNI and named pipes.

On the native side, a class extends the ICallbackCapable base class. This reserves a channel number, currently supporting 1024 asynchronous  callback channels.

### Setting the channel id's ###
The id's are created automatically, but if you want to use them, you must make the jvm aware of the channel number for a particular class instance. For example, the RTMPClient in c++ interfaces with the Java class 'ChannelOut' which has a class field 'id'. When the c++ process creates this ChannelOut class in the jvm, it sets the id of it's channel. If the client is registered as a handler with the JNI interface, it can receive calls initiated by the JVM.

Setting RPC channel Id to java instance, as implemented in the RTMPClient class. After it is set, any java thread can reference the ChannelOut's id field, create a JNICallback object with that channel id, and invoke an rpc to that c++ class instance.
```
//get the method id
jmethodID setId = (*pJni->getEnv()).GetMethodID( cls, "setId","(I)V");
//set it with the c++ base class 'getId' value.
(*pJni->getEnv()).CallVoidMethod(client,setId,getId());

```



C++ Callback Registration: Use the method 'addCallbackHandler'.
```

	JediNativeInterface * test= new JediNativeInterface();
	
	test->createJNI();

	Red5Server * server= new Red5Server(test);

	test->addCallbackHandler(server);//Add the native handler

	server->boot();
```

For simplicity, this example only prints out the request, and returns the RPC channel id that processed the call as "Process by idNum".

In the C++ handler implementation, the data buffer contains the request and the results are placed into the 'results' buffer. The method returns the actual length of the data placed into the buffer. This is scrammed up and marshaled back to Java.

```
	int processCall(char * data,char * results)
	{
		int i= this->getId();

		sprintf(results,"Processed by %d",i);
		
		printf("The request is %s\r\n",data);

		return (lstrlen(results))*sizeof(CHAR);
	}
```

On the Java side, you need the JediNativeCallback.dll in the directory where you are executing the c++ from, or in your system LD\_PATH. And of course you need the JediNativeInterface.jar in the particular jvm's library.

The JNICallback class takes the channel id in the constructor, and the method signature is :

call(String callParameter, int rpcChannelNumber);

```

		JNICallback caller= new JNICallback(1);

		String ret = caller.call("foo", caller.getId());
		
		System.out.println("return was "+ ret);

```

The callback is synchronous and will block while c++ responds, or it will quickly timeout if the pipe server is not available. On the same token, the c++ process that calls the jvm can also be blocked by sleeping/waiting the Thread on the java side.


## Setting an Environment Variable ##
Can't get your app to start? Maybe a variable was not set on the JVM you created.

At the Header:
```
#define RED5_HOME "C:\\workspaces\\eclipse\\ComServer\\red5"
```
In your runtime implementation:
```
::SetEnvironmentVariable("RED5_HOME",RED5_HOME);
```


## Adding Arguments to the JVM ##
Look at the JediNativeInterface class in c++. Scroll down to where the arguments are created. Add some more new strings, copy the codes, and dont forget to delete the new strings you made in the 'detstoyJNI' method.
```


options[3].optionString = new char[MAX_PATH];
sprintf(options[3].optionString,"-Dlogback.ContextSelector=org.red5.logging.LoggingContextSelector -Dcatalina.useNaming=true");

```

Adding to destroyJNI method.

```

delete options[3].optionString;

```