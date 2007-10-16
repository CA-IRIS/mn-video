The server applications listed below are Tomcat applications 
and are run as a service.  The service script is in the root of this 
module and named, (appropriately) tomcat.  They require a properties
file for configuration.  This should be located in:
/etc/tms and named video.properties.

There are multiple artifacts of the video module. They are:

NVR war file:
The nvr war file can be run on an NVR server.  It is designed to check with the
Video backend server for it's configuration.  This server application handles
requests for information about camera recordings such as start and end time 
on the disk.  It also serves the video files.

BACKEND war file:
The backend war file is a server application that runs inside the network.
It serves as a service for images and streams.  Client requests for images
and streams are all routed through this server.  This server will then
connect to the appropriate encoder and retrieve the requested image/stream.
The only other direct connection to the encoders is from the NVR server.

REPEATER war file:
The reapeater war file is a server application that acts as a relay to
the backend.  This application resides in the public domain and handles
requests for images and streams.  Image buffering is done in this server to reduce
the traffic inside the network and speed up response time to clients.  For
multiple concurrent stream requests for the same video, the repeater will make
a single request to the backend and distribute that stream to all listeners.  
This server must be setup to accept connections on port 80 since many
corporate firewalls block 8080.  Getting tomcat to bind to 80 proved more
difficult than simply setting up a firewall rule to forward port 80 traffic
to port 8080.  The following command will do the port forwarding:
/sbin/iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080
I haven't found a way to set it up so that this redirect is active when the
iptables service is restarted.  I just execute the command if I have to restart
iptables.
The repeater needs to connect to a database for authentication. This requires
that the jdbc jar be placed on the path ($CATALINA_HOME/server/lib).

NVR-CLIENT jar file:
The nvr client application is distributed via JavaWebStart and is the interface
for the user to obtain video from the DiVAS (DIstributed Video Archive System).
Requests for information about available video are sent to the backend server
which will redirect the client to the appropriate NVR server which ultimately
will fulfill the request.

VIDEO-CLIENT jar file:
The video client provides access to real-time MJPEG video streams.  There is a
standalone application, an applet and a Swing widget which can be embedded
in other applications.