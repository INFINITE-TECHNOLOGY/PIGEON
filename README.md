# Pigeon
> Post pigeons have been (and sometimes still are) extensively used for quick delivery of paper-based messages (such as text, drawings, maps) - thus the project name.

**Pigeon app is an HTTP Message Broker.**

It is capable to:
1) Enqueue a textual message from external source
2) Convert it into one or more HTTP messages with a specified body/query string parameters using appropriate Plugins
3) Dispatch the resulting messages ansynchronously towards one or more recipients (URLs) using a variety of HTTP connection and authentication mechanisms (such as AWS v4 signature)
4) If needed retry sending the message several times

