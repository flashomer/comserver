# Setting Up #

I recommend to get the red5 source. It contains the dependencies and is a great reference for understanding AMF and RTMP.

The comserver jar is in the svn under the java source branch. The demos do not contain the jar itself.

I compile the apps with eclipse in the main red5/webapps folder, then I copy it to the dist/webapps directory after build. Typically I do use an ant script, but it is only the copy operation that runs run post build and pre-launch.

Once you have the comserver jar in your lib folder and have added it to the external jars list, you can change your existing app, or oflaDemo app to extend ComServer instead of MultithreadedApplicationAdapter. You then need to add the abstract methods of ComServer.

# Setting up the facebook demo to develop locally #

You can run the facebook demo on a local server for rapid development.

Edit your hosts file so that the facebook application canvas url domain is localhost.

Edit your local webserver configuration to be that domain.

Set up the facebook application at facebook.com to target a canvas url on that localhost domain.

Set up flashbuilder so that the output directory is in your localhost domain, and that the html at your designated canvas url will load the flash document.

Set up the flashbuilder run/debug url to be your facebook application page at facebook.com.

Run the facebook application using the run/debug buttons in flashbuilder. The browser should open up the facebook application page at facebook.com, and it should use the files on your localhost domain.