package com.jng;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class NetUtils {
	public byte[] readFromSocket(SocketChannel socket) throws IOException
	{

		ByteBuffer readBuffer = ByteBuffer.allocate(1024 * 1000);
		readBuffer.clear();
		int read = socket.read(readBuffer);
		if (read < 0) return new byte[0];
		byte[] arr = new byte[read];
		readBuffer.flip();
		readBuffer.get(arr);
		return arr;
	}

	public String bytesToStr(byte[] bytes)
	{
		try {
			return new String(bytes, "ASCII");
		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR";
		}
	}

	public byte[] strToBytes(String str)
	{
		return str.getBytes();
	}

	public byte[] replacePipeWithSOH(byte[] bytes)
	{
		byte[] res = new byte[bytes.length];

		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == '|')
			{
				res[i] = 1;
				continue;
			}
			res[i] = bytes[i];
		}
		return res;
	}

	public byte[] replaceSOHwithPipe(byte[] bytes)
	{
		byte[] res = new byte[bytes.length];

		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == 1)
			{
				res[i] = '|';
				continue;
			}
			res[i] = bytes[i];
		}
		return res;
	}

	public int getFIXChecksum(String message, boolean isPipe)
	{
		int res = 0;
		String soh = isPipe ? "\\|" : String.valueOf((char)1);
		String[] tokens = message.split(soh, -1);
		String[] tokensNoCs = message.indexOf("10=") > -1 ?  Arrays.copyOf(tokens, tokens.length - 2) :  Arrays.copyOf(tokens, tokens.length - 1);
		String messageNoCs = "";
		for (String token : tokensNoCs) {
			messageNoCs += (token + (isPipe? "|" : soh));
		}
		byte[] bytes = messageNoCs.getBytes();
		
		// System.out.println("bytes : -");
		// for (byte b : bytes) {
		// 	System.out.print((char)b);
		// }
		// System.out.println();

		for (byte b : bytes) {
			res += b;
		}
		res %= 256;
		return res;
	}
}
