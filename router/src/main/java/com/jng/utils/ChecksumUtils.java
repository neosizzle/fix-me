package com.jng.utils;

import java.util.Arrays;

public class ChecksumUtils {
	public int getFIXChecksum(String message)
	{
		int res = 0;
		String soh = String.valueOf((char)1);
		String[] tokens = message.split(soh, -1);
		String[] tokensNoCs = message.indexOf("10=") > -1 ?  Arrays.copyOf(tokens, tokens.length - 2) :  Arrays.copyOf(tokens, tokens.length - 1);
		String messageNoCs = "";
		for (String token : tokensNoCs) {
			messageNoCs += (token + soh);
		}
		byte[] bytes = messageNoCs.getBytes();
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
		String checksumField = tokens[tokens.length - 2];
		String[] checksumTokens = checksumField.split("=", 2);
		if (checksumTokens.length != 2)
			return false;

		return getFIXChecksum(message) == Integer.valueOf(checksumTokens[1]);
	}
}
