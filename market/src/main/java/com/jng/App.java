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

    public static void main( String[] args )
    {
        try {
            final int PORT = 5001;
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
            System.out.println("my market id is " + Id);

            while (true) {
                // read response from server
                byte[] readMsg = nU.readFromSocket(myClient);
                String readMsgStr = new String(readMsg, "ASCII");

                System.out.println("read message" + readMsgStr);

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
                // TODO test restore
                if (tokens[1].startsWith("restore"))
                {
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
            

                // TODO check for business transaction
                else
                {
                    String brokerId = tokens[0];
                    String requestedInstrument = tokens[1].split("=", -1)[1];
                    String requestedIsBuy = tokens[4].split("=", -1)[1];
                    String trnxId = tokens[5].split("=", -1)[1];
                    String responseLine = "";
                    boolean isAccept = false;

                    // adjust asset
                    if (requestedIsBuy.equals("true"))
                    {
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
                    "instrument=" + requestedInstrument + "|";

                    int checksum = nU.getFIXChecksum(
                            nU.bytesToStr(
                                nU.replacePipeWithSOH(responseLine.getBytes())
                                ), false);
                    responseLine += "10=" + checksum + "|";

                    // convert pipes to soh
                    byte[] msgToSend = nU.replacePipeWithSOH(responseLine.getBytes());
                    System.out.println(nU.bytesToStr(msgToSend));

                    // send data to server
                    myClient.write(ByteBuffer.wrap(msgToSend));
                }

            }

        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
