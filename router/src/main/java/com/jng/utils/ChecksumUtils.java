package com.jng.utils;

import java.util.Arrays;

public class ChecksumUtils {
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

	public boolean validateFIXChecksum(String message)
	{
		// extract checksum number from message
		if (message.indexOf("10=") == -1)
			return false;
		
		String soh = String.valueOf((char)1);
		String[] tokens = message.split(soh, -1);
		if (tokens.length < 2) return false;
		String checksumField = tokens[tokens.length - 2];
		String[] checksumTokens = checksumField.split("=", 2);
		if (checksumTokens.length != 2)
			return false;

		try {
			return getFIXChecksum(message, false) == Integer.valueOf(checksumTokens[1].trim());
		} catch (Exception e) {
			return getFIXChecksum(message, false) == -1;
		}
	}
}
