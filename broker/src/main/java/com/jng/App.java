package com.jng;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

// TODO implement selector for read
public class App 
{
    static NetUtils nU = new NetUtils();
    static HashMap<String, Integer> assets = new HashMap<String, Integer>();
    static double monies = 69420;
    static Selector _selector;
    // timeout works partially, subject to tell me to handle perfect timeout so :|
    // does not cascade responses
    static int TIMEOUT = 10000;

    public static void main( String[] args )
    {
        try {
            final int PORT = 5000;
            InetSocketAddress myAddress  =
                new InetSocketAddress("localhost", PORT);
            SocketChannel myClient = SocketChannel.open();
            myClient.configureBlocking(false);
            
            // init select
            _selector = Selector.open();

            // set client to connect
            myClient.register(_selector, SelectionKey.OP_CONNECT);

            // wait for select to return
            _selector.select(TIMEOUT);

            // catch connect timeout
            Iterator<SelectionKey> keys = _selector.selectedKeys().iterator();
            if (!keys.hasNext())
            {
                System.err.println("Connection timeout");
                return ;
            }

            SelectionKey selectionKey = keys.next();
            keys.remove();

            if (!selectionKey.isValid()){
                System.err.println("Server disconnected");
                return ;
            }
            if (!selectionKey.isConnectable())
            {
                System.err.println("Connection failure");
                return ;   
            }
            
            // connect to server
            myClient.connect(myAddress);

            // finishconn
            myClient.finishConnect();
                            
            // set client to read
            myClient.register(_selector, SelectionKey.OP_READ);          

            // wait for select to return
            _selector.select(TIMEOUT);

            // catch read timeout
            keys = _selector.selectedKeys().iterator();
            if (!keys.hasNext())
            {
                System.err.println("Read timeout");
                return ;
            }

            selectionKey = keys.next();
            keys.remove();

            if (!selectionKey.isValid()){
                System.err.println("Server disconnected");
                return ;
            }
            if (!selectionKey.isReadable())
            {
                System.err.println("Unable to read data from server");
                return ;
            }

            // read data
            byte[] serverConnectMsg = nU.readFromSocket(myClient);
            String serverConnectMsgStr = new String(serverConnectMsg, "ASCII");
            String Id = serverConnectMsgStr.split(" ", -1)[serverConnectMsgStr.split(" ", -1).length - 1];
            Id =  Id.substring(0, Id.length() - 1);   
            System.out.println("my broker id is " + Id);
            System.out.println("use 'help' to get commands");

            while (true) {
                try {
                    // read line
                    String line = System.console().readLine(">");

                    // help command
                    if (line.equals("help"))
                    {
                        System.out.println("help    -   display help");
                        System.out.println("ls    -   list assets");
                        System.out.println("trnx    -   make transaction");
                        System.out.println("id    -   get broker id");
                        System.out.println("exit    -   exit program");
                    }

                    // exit command
                    else if (line.equals("exit"))
                        return ;

                    // transaction command
                    else if (line.equalsIgnoreCase("trnx"))
                    {
                        // input instrument
                        String instrument = System.console().readLine("instrument:");

                        // input marketid
                        String marketId = System.console().readLine("marketId:");

                        // input price
                        Boolean isValidPrice;
                        isValidPrice = false;
                        Double price = 0.0;

                        while (!isValidPrice) {
                            String pricestr = System.console().readLine("price:");
                            
                            try {
                                price = Double.valueOf(pricestr);
                                isValidPrice = true;
                            } catch (Exception e) {
                                System.out.println("Invalid price");
                            }
                        }

                        // input buy/sell
                        Boolean isValidAction;
                        isValidAction = false;
                        String action = "";

                        while (!isValidAction) {
                            action = System.console().readLine("action (buy/sell):");
                            if (action.equalsIgnoreCase("buy") || action.equalsIgnoreCase("sell"))
                                break;
                        }

                        // validate price < monies
                        if (price > monies)
                        {
                            System.out.println("You do not have enough funds");
                            continue ;
                        }

                        // validate instrument exists and > 1
                        if (action.equals("sell")  && (assets.get(instrument) == null || assets.get(instrument) < 1))
                        {
                            System.out.println("Asset invalid");
                            continue ;
                        }

                        String trnxLine =
                        Id + "|" +
                        "instrument=" + instrument + "|" +
                        "market=" + marketId + "|" +
                        "price=" + price + "|" +
                        "isBuy=" + action.equals("buy") + "|" 
                        ;

                        int checksum = nU.getFIXChecksum(
                            nU.bytesToStr(
                                nU.replacePipeWithSOH(trnxLine.getBytes())
                                ), false);

                        trnxLine += "10=" + checksum + "|";

                        // convert pipes to soh
                        byte[] msgToSend = nU.replacePipeWithSOH(trnxLine.getBytes());

                        // adjust assets
                        if (action.equals("buy"))
                        {
                            monies -= price;
                            if (assets.keySet().contains(instrument))
                                assets.put(instrument, assets.get(instrument) + 1);
                            else
                                assets.put(instrument, 1);
                        }
                        else
                        {
                            monies += price;
                            if (assets.keySet().contains(instrument))
                                assets.put(instrument, assets.get(instrument) - 1);
                        }

                        // set client to write
                        myClient.register(_selector, SelectionKey.OP_WRITE);          

                        // wait for select to return
                        _selector.select(TIMEOUT);

                        // catch write timeout
                        keys = _selector.selectedKeys().iterator();
                        if (!keys.hasNext())
                        {
                            System.err.println("Write timeout");
                            continue ;
                        }

                        selectionKey = keys.next();
                        keys.remove();

                        if (!selectionKey.isValid()){
                            System.err.println("Server disconnected");
                            return ;
                        }
                        if (!selectionKey.isWritable())
                        {
                            System.err.println("Unable to Write data to server");
                            continue ;
                        }

                        // send data to server
                        myClient.write(ByteBuffer.wrap(msgToSend));

                        // set client to read
                        myClient.register(_selector, SelectionKey.OP_READ);          

                        // wait for select to return
                        _selector.select(TIMEOUT);

                        // catch read timeout
                        keys = _selector.selectedKeys().iterator();
                        if (!keys.hasNext())
                        {
                            System.err.println("Read timeout");
                            continue ;
                        }

                        selectionKey = keys.next();
                        keys.remove();

                        if (!selectionKey.isValid()){
                            System.err.println("Server disconnected");
                            return ;
                        }
                        if (!selectionKey.isReadable())
                        {
                            System.err.println("Unable to read data from server");
                            continue ;
                        }

                        // read response from server
                        byte[] readMsg = nU.readFromSocket(myClient);
                        String readMsgStr = new String(readMsg, "ASCII");

                        // tokenize response
                        String[] tokens = readMsgStr.split(String.valueOf((char) 1), -1);

                        // dont process tokens length < 3
                        if (tokens.length < 3)
                        {
                            System.out.println("Bad response received");
                            continue ;
                        }

                        // check for error
                        if (tokens[1].startsWith("ERROR"))
                        {
                            String errMsg = tokens[1].split("=", -1)[1];
                            
                            System.err.println("Error from router: " + errMsg);

                            // adjust assets
                            if (action.equalsIgnoreCase("buy"))
                            {
                                try {
                                monies += price;
                                if (assets.keySet().contains(instrument))
                                    assets.put(instrument, assets.get(instrument) - 1);
                                } catch (Exception e) {
                                e.printStackTrace();
                                }
                            }
                            else
                            {
                                monies -= price;
                                if (assets.keySet().contains(instrument))
                                    assets.put(instrument, assets.get(instrument) + 1);
                                else
                                    assets.put(instrument, 1);
                            }
                        }

                        // check for restore
                        else if (tokens[1].startsWith("restore"))
                        {
                            String restoreAmount = tokens[1].split("=", -1)[1];
                            String restoreAction = tokens[2].split("=", -1)[1];
                            String restoreInstrument = tokens[3].split("=", -1)[1];

                            // adjust assets
                            if (restoreAction.equalsIgnoreCase("buy"))
                            {
                                try {
                                monies += Double.valueOf(restoreAmount);
                                if (assets.keySet().contains(restoreInstrument))
                                    assets.put(restoreInstrument, assets.get(restoreInstrument) - 1);
                                } catch (Exception e) {
                                e.printStackTrace();
                                }
                            }
                            else
                            {
                                monies -= Double.valueOf(restoreAmount);
                                if (assets.keySet().contains(restoreInstrument))
                                    assets.put(restoreInstrument, assets.get(restoreInstrument) + 1);
                                else
                                    assets.put(restoreInstrument, 1);
                            }
                        }

                        else if (tokens[1].startsWith("response"))
                        {
                            String instrumentResponse = tokens[4].split("=", -1)[1];
                            String isBuyResponse = tokens[5].split("=", -1)[1];
                            String priceResponse = tokens[6].split("=", -1)[1];

                            if (tokens[1].split("=", -1)[1].equals("REJECT"))
                            {
                                System.out.println("Transaction rejected for " + instrumentResponse + " at price " + priceResponse);
                                // adjust assets
                                if (isBuyResponse.equalsIgnoreCase("true"))
                                {
                                try {
                                    monies += Double.valueOf(priceResponse);
                                    if (assets.keySet().contains(instrumentResponse))
                                        assets.put(instrumentResponse, assets.get(instrumentResponse) - 1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                }
                                else
                                {
                                    monies -= Double.valueOf(Double.valueOf(priceResponse));
                                    if (assets.keySet().contains(instrumentResponse))
                                        assets.put(instrumentResponse, assets.get(instrumentResponse) + 1);
                                    else
                                        assets.put(instrumentResponse, 1);
                                }
                            }
                            else
                                System.out.println("Transaction accepted for " + instrumentResponse + " at price " + priceResponse);

                        }
                        // print to console
                        // System.out.println(readMsgStr);
                    }

                    // list commnd
                    else if (line.equalsIgnoreCase("ls"))
                    {
                        System.out.println("Asset           Quantity");
                        for (String key : assets.keySet()) {
                            int value = assets.get(key);
                            System.out.println(key + "          " + value);
                        }
                        if (assets.keySet().isEmpty())
                            System.out.println("-          -");
                        System.out.println("\nmonies: "+ monies);
                    }

                    // id command
                    else if (line.equalsIgnoreCase("id"))
                    {
                        System.out.println(Id);
                    }

                    else
                        System.out.println("8=====D");

                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
