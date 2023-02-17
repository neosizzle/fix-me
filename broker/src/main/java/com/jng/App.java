package com.jng;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;


/**
 * Hello world!
 *
 */
public class App 
{
    static NetUtils nU = new NetUtils();
    static HashMap<String, Integer> assets = new HashMap<String, Integer>();
    static double monies = 69420;

    // TODO make broker program 
    public static void main( String[] args )
    {
        try {
            final int PORT = 5000;
            InetSocketAddress myAddress  =
                new InetSocketAddress("localhost", PORT);
            SocketChannel myClient = SocketChannel.open();
            
            // connect to server
            myClient.connect(myAddress);

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

                        System.out.println(nU.bytesToStr(msgToSend));

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

                        // send data to server
                        myClient.write(ByteBuffer.wrap(msgToSend));

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

                        // TODO check for market response

                        // print to console
                        System.out.println(readMsgStr);
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
                    {
                        assets.put("sex", 123);
                        System.out.println("8=====D");
                    }

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
