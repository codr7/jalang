# DO NOT EDIT THIS FILE - See: https://eclipse.dev/jetty/documentation/

[description]
Expose the Jetty WebSocket Client classes to deployed web applications.

[environment]
ee8

[tags]
websocket

[depend]
client
ee8-annotations

[lib]
lib/jetty-websocket-core-common-${jetty.version}.jar
lib/jetty-websocket-core-client-${jetty.version}.jar
lib/ee8-websocket/jetty-ee8-websocket-jetty-api-${jetty.version}.jar
lib/ee8-websocket/jetty-ee8-websocket-jetty-common-${jetty.version}.jar
lib/ee8-websocket/jetty-ee8-websocket-jetty-client-${jetty.version}.jar
lib/ee8-websocket/jetty-ee8-websocket-jetty-client-webapp-${jetty.version}.jar
