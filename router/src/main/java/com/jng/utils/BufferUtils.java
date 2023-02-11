package com.jng.utils;

public class BufferUtils {
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
}
