package com.jng;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class App 
{
    static NetUtils nU = new NetUtils();
    static HashMap<String, Integer> assets = new HashMap<String, Integer>();
    // no timeout on market since we want to block

    public static void main( String[] args )
    {
        // read init assets
        try {
            if (args.length != 1)
            {
                System.out.println("need to provide initial asset file");
                return ;
            }
            readInitAssets(args[0]);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ;
        }

        try {
            final int PORT = 5001;
            InetSocketAddress myAddress  =
                new InetSocketAddress("localhost", PORT);
            SocketChannel myClient = SocketChannel.open();

            // connect to server
            myClient.connect(myAddress);
            
            // finishconn
            myClient.finishConnect();

            // read data
            byte[] serverConnectMsg = nU.readFromSocket(myClient);
            String serverConnectMsgStr = new String(serverConnectMsg, "ASCII");
            String Id = serverConnectMsgStr.split(" ", -1)[serverConnectMsgStr.split(" ", -1).length - 1];
            Id =  Id.substring(0, Id.length() - 1);   
            System.out.println("my market id is " + Id);

            while (true) {
                // will block here intentionally
                // read response from server
                byte[] readMsg = nU.readFromSocket(myClient);
                String readMsgStr = new String(readMsg, "ASCII");

                // tokenize response
                String[] tokens = readMsgStr.split(String.valueOf((char) 1), -1);

                // dont process tokens length < 3
                if (tokens.length < 3)
                {
                    System.out.println("Bad response received");
                    return ;
                }

                // check for error
                if (tokens[1].startsWith("ERROR"))
                {
                    String errMsg = tokens[1].split("=", -1)[1];
                    System.err.println("Error from router: " + errMsg);
                    continue ;
                }

                // check for restore
                if (tokens[1].startsWith("restore"))
                {
                    System.err.println("Transaction restored from router request.");
                    
                    String restoreIsntrument = tokens[1].split("=", -1)[1];
                    String restoreResponse = tokens[2].split("=", -1)[1];

                    if (restoreResponse.equalsIgnoreCase("ACCEPT"))
                    {
                        if (assets.keySet().contains(restoreIsntrument))
                            assets.put(restoreIsntrument, assets.get(restoreIsntrument) + 1);
                        else
                            assets.put(restoreIsntrument, 1);
                    }
                    else
                    {
                        if (!assets.keySet().contains(restoreIsntrument))
                            assets.put(restoreIsntrument, assets.get(restoreIsntrument) + 1);
                        else
                            assets.put(restoreIsntrument, 1);
                    }
                    continue;
                }
            

                else
                {
                    String brokerId = tokens[0];
                    String requestedInstrument = tokens[1].split("=", -1)[1];
                    String requestedIsBuy = tokens[4].split("=", -1)[1];
                    String trnxId = tokens[5].split("=", -1)[1];
                    String responseLine = "";
                    boolean isAccept = false;
                    String requestedPrice = tokens[3].split("=", -1)[1];

                    // adjust asset
                    if (requestedIsBuy.equals("true"))
                    {
                        System.out.println("Buy reuqest from " + brokerId + " for " + requestedInstrument);
                        if (assets.keySet().contains(requestedInstrument) &&
                            assets.get(requestedInstrument) > 0)
                        {
                            isAccept = true;
                            assets.put(requestedInstrument, assets.get(requestedInstrument) - 1);
                        }
                    }
                    else 
                    {
                        isAccept = true;

                        System.out.println("Sell reuqest from " + brokerId + " for " + requestedInstrument);
                        if (assets.keySet().contains(requestedInstrument))
                            assets.put(requestedInstrument, assets.get(requestedInstrument) + 1);
                        else
                            assets.put(requestedInstrument, 1);
                    }

                    responseLine = 
                    Id + "|" +
                    "response=" + (isAccept ? "ACCEPT" : "REJECT") + "|" +
                    "broker=" + brokerId + "|" +
                    "trnx=" + trnxId + "|" +
                    "instrument=" + requestedInstrument + "|" + 
                    "isBuy=" + requestedIsBuy  + "|" + 
                    "price=" + requestedPrice + "|";               

                    int checksum = nU.getFIXChecksum(
                            nU.bytesToStr(
                                nU.replacePipeWithSOH(responseLine.getBytes())
                                ), false);
                    responseLine += "10=" + checksum + "|";

                    // convert pipes to soh
                    byte[] msgToSend = nU.replacePipeWithSOH(responseLine.getBytes());
                    if (isAccept)
                        System.out.println("Accepting request...");
                    else
                        System.out.println("Rejecting request...");

                    // send data to server
                    myClient.write(ByteBuffer.wrap(msgToSend));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // reads from asset file and push it to assets
    private static void readInitAssets(String filename) throws FileNotFoundException, IOException {
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            for(String line; (line = br.readLine()) != null; ) {
                String tokens[] = line.split(" " , -1);
                String assetname = tokens[0];
                int assetCount = Integer.valueOf(tokens[1]);
                assets.put(assetname, assetCount);
            }
        }
    }
}
