# FLV Files #

One could record cue points into an flv file to use as a feed node. The difference between playing the file normally and making a node is providing the invocation and IComserverNode interface as shared data to all subscribers. A group of users can experience synchronized actions initiated by the server.

So, what we can do is to encode the data to be used in the IComserverNode into a cue point, create a node with that data and then encode the actual cue points as usual. The life cycle of the node is that it has an Id, and data and then it is added to a resource sink. When it has performed its purpose, it is removed from the sink.

For simplicity, the code below has removed all time information which should be read and processed for accurate time-based playback.

Open the flv through the vod provider service.

```
public class FLVNode extends Thread implements IComserverNode , IConsumer{
        Public String flv_name = "robot.flv";

	@Override
	public void setResource(IResourceSink resource) {
		this.resource=resource;
		IContext context =resource.getStream().getScope().getContext();
		
		providerService = (IProviderService) context.getBean(IProviderService.BEAN_NAME);
		
		
		msgIn = providerService.getVODProviderInput(resource.getStream().getScope(), flv_name);	
	

	}
```

Within a do/while loop , continue to get the next message from the vod provider and check if it is a Notify. We are looking for our init cue point to retrieve our node data, and our add cue point to start our subscription to the resource stream.

```
                        IMessage msg;

			try {
				
				msg = msgIn.pullMessage();
			
			
			RTMPMessage lMsg = null;

			if (msg instanceof RTMPMessage) 
			{
				
				lMsg = (RTMPMessage) msg;	
				
				if( lMsg.getBody() instanceof Notify){
						
```

Check the method names for your initiation cue points and send the others.

```
					
					Input reader = new Input(((Notify)lMsg.getBody()).getData());
					
					reader.readDataType();//string
					method=reader.readString(null);
					reader.readDataType();//object
				
					Map invokeData=  (Map) reader.readMap(new Deserializer(), null);

						if(method.equals("init")){	
							//here is our Node data
							data=invokeData;	
							
						}

						if(method.equals("remove")){//remove node
							resource.removeSubscriber(this);
							
						}else if(method.equals("add")){//add node
							resource.addSubscriber(this);
							
						}else if(method.equals("change")){
							//Replace Node Data
							data=invokeData;
						}//else send invoke as is.{
                                                        resource.getStream().dispatchEvent(lMsg.getBody());}

```

Continue to read the flv file to the end, and loop as required. You could even replay sections as desired.

Pre-recorded data feeds are very handy in games, and could even be the source of flash quiz questions. Creating the FLV recorder app to insert the initiation cue points is not difficult and they can be recorded directly to the target web-context with red5.

## Recording cue points with the flash client ##
...
```

```