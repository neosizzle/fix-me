# Fix-me
This document is a writeup for the 42 subject: fix-me. We will be writing a program that simulates stock exchanges and deals with trading algorithms, with networking and socket implementations.

We will need to write 3 main components / applications to complete our system
- Router
- Market server
- Broker client

We also need to implement the following features 
- None blocking sockets
- Java executor framework features
- Maven multi module (we need to build multiple applications)

This document wont cover anything networking related, this would just be an overview about the design and the rationales behind each design choice.

## FIX protocol
>https://en.wikipedia.org/wiki/Financial_Information_eXchange

Similar to HTTP, FIX protocol is a text-based transfer protocol that follows a set of rules and is understood by FIX clients which also follows the same rules to parse the messages received.

In a nutshell, the FIX protocol uses
- SOH (ASCII 1) character as deliminiters
- Each delimited field consists of a `key=value` pair
- The first field will specify the protocol and its version
- The second field will specify the length of the entire message
- The last field will contain a checksum for all the characters before the field.

Since the system we are building does have some customizations, We wont follow exactly the codes in the protocol, but we will stil adapt to the structure and the conventions otherwise.

## Rationales
### Router
This component will be the core of our system, and most of the load will be bear by this application. Its purposes will be : 
- Maintain a routing table to keep track of all market and broker identities. the routing table should contain accurate information of the mapping between the network sockets and the business representation of those entities and will serve as a reference for other classes in the router.
- Generating IDs for market and broker applications and manage the population of those entities upon connection / disconnection.
- Able to determine the type of client being connected.
- Acts as a bridge for message sending, able to preprocess and validate messages before forwarding it to destination application.
- Be the "scribe" in transactions and store all transactional activity in a database where its connected
- ~~Checks for any non-completed transactions upon launch~~  scrapped, the there is no way to determine which broker and market is which if any of the applications break. A new connection will represent a new broker or market. A reconencting broker or market would be treated as a new one as it will be generating a new ID. And I dont plan to implement ID tracking so theres that

### Broker
This component will be our front end application, where users can place orders and manipulate the market, literally. Its job would be
- Provide a user interface to send buy / sell instructions
- Provide a storage state to keep track of assets and money 
- Receive rejected / approve messages from router by market
- Able to rollback transaction if rollback is requested from server

### Market
This component will connect to the router and listen for incoming transaction requests, and process them in memory before sending back the response. It is responsible for 
- Containing buyable instruments
- Sending rejected / approve messages to router for broker
- Change state of instruments depending on buy / sell requests
- Able to rollback transaction if rollback is requested from server

![](https://hackmd.io/_uploads/HJp8MYmNn.png)
Above defines the infrastructure of our system, and what we will model out applications on.

## Assumptions and constraints
I made some assumptions about some unspecified parameters, these assumptions are made because it affects certain behaviour of our programs. 

- Market and Broker will only launch on new instances (no forking in application)
- The definition of fallback is the restoration of the values of money / assets in the broker and market applications
- When a router restarts, all previously connected entities are treated as now newly connected entities.

Of course, you can define have different assumptions on how to handle the above situations, as long as your system adapts and the results are expected, any assumed definitions would be fine.

## Message formats
This section will define how the messages are sent, and the adjustments made to suit the requirments and our assumptions

### Buy / sell request from Broker to Market
```
Broker -> Router
BrokerID | instrument | marketId | quantity=1 | price | action | checksum |

Router -> Market
BrokerID | instrument | marketId | quantity=1 | price | action | trnxId | checksum |
```

### Error message from router to broker
```
Router -> Broker
BrokerID | ErrMsg | checksum |
```

### Restore message from router to broker
```
Router -> Broker
BrokerID | RestoreAmount | action | checksum |
```

### Response message from market to broker
```
Market -> Router -> Broker
MarketID | Response | BrokerID | TrnxID | instrument | action | price | checksum |
```

### Restore message from router to market
```
Router -> Broker
BrokerID | RestoreInstrument | quantity=1 | checksum |
```


### Router connection handshake
```
Router -> Market
Router -> Broker
MarketID/BrokerID | checksum |
```

## Router
![](https://hackmd.io/_uploads/B1x0XC74n.png)

As we can see, the router class is the core component of this application, where it is the main consumer of other classes and objects.

The **Database** class is responsible to handling database initialization as well as providing read / write interfaces for other sercives to use.

The **RouterState** class will be our routing table, where is it responsible to store and cache all our incoming clients and map them to their sockets.

The **MarketServer** and **BrokerServer** represents one instance of a Market server and a broker client. They do not need to exist in pairs. They inherit from the **NetworkServer** interface to extend networking capabilities which also allows them to have custom handlers. An improvement can be made here by using a Factory instead of instantiating them on the go.

The **ResponseTransaction** and **BusinessTransaction** classes represent the data model for transactions and they implement from **Transaction** which is the preprocesses and intagged transaction type. Both of these classes will be used when getting or pushing data to the database.

## Broker
![](https://hackmd.io/_uploads/ry_di0XEh.png)


## Market
![](https://hackmd.io/_uploads/HJPNA0QN2.png)


## Automated testing
I had pondered implementing a tester for this system, but ultimately did not commit to the things I have planned becuase of time constraints. Hence, I will talk about what I have in mind for the tester and hopefully, someone else or some time else this would be implemented.

The test is model after end-to-end testing, which means we will be testing the application itself externally and not in the code.

Some of the requirements are like such -
- Make sure components can connect to each other
- Make sure IDs are generated
- Broker client must be able to carry out transactions
- Broker clients 'list' command must be accurate and updated
- Broker client must validate transactions before sending them on network
- Markets are affected by pass transactions (they may run out of assets or gain new assets)

Here are some ideas which I thought of, implemented partially, and scrapped -
- A script that launches markets, gets their ID, generate test inputs based on IDs and redirect data to broker shell ([Shell dies upon first redirection](https://stackoverflow.com/questions/5724646/how-to-pipe-input-to-java-program-with-bash), we want to keep the shell alive for subsequent calls and we need to check the values in between)
- Using named pipes instead of bash redirection, and use a new process for input population (Does not work for the same reason above, regardless of redirection method)
- If I cant redirect, I would just write into the TTY of the different broker shells. This seemed like the correct solution, I did some initial testing and it seemed okay, just that the final product will take time to implement, so thats that.


###### tags: `OOP`

