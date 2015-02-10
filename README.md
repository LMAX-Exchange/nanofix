Nanofix    [![Build Status](https://travis-ci.org/LMAX-Exchange/nanofix.svg)](https://travis-ci.org/LMAX-Exchange/nanofix)
==============

A fix client for testing FIX servers.

Nanofix was created with the following objectives:

- Easily construct invalid FIX messages.
- Run within the same JVM as other FIX engines.
- Have control over FIX session level messages.

This is a testing tool, it is not intended for production use.

Maintainer
==========

[Judd Gaddie] (https://github.com/juddgaddie)

Changelog
==========

## 1.0.0 Released

- Initial release


Example Usage
==========
Usage as a TCP connection initiator.
```java

        FixClient client = FixClientFactory.createFixClient("hostname", 2000);
        client.subscribeToAllMessages(new FixMessageHandler() {
            @Override
            public void onFixMessage(FixMessage fixMessage) {
                System.out.println("Received fix message " + fixMessage.toFixString());
            }
        });

        client.connect();

        client.registerTransportObserver(new ConnectionObserver() {
            ...
            @Override
            public void connectionClosed() {
                System.out.println("TCP Connection Closed.");
            }
        });

        client.send(new FixMessageBuilder().messageType(MsgType.LOGIN).username("hello").build());


```

Usage as a TCP connection listener
```java

        final FixClient listeningClient = FixClientFactory.createFixClient(2000);
        listeningClient.subscribeToAllMessages(new FixMessageHandler() {
            @Override
            public void onFixMessage(FixMessage fixMessage) {
                System.out.println("Received fix message " + fixMessage.toFixString());
                listeningClient.send(new FixMessageBuilder().messageType(MsgType.LOGIN).username("hello back").build());
            }
        });

        listeningClient.registerTransportObserver(new ConnectionObserver() {
            @Override
            public void connectionEstablished() {
                System.out.println("TCP Connection Established.");
            }

            @Override
            public void connectionClosed() {
                System.out.println("TCP Connection Closed.");
            }
        });

```


