package com.jng.utils;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class HandlerUtils {
	ChecksumUtils csU = new ChecksumUtils();
	BufferUtils bU = new BufferUtils();

	public String generateErrMsg(String message, int ID, Boolean isCheckSumPipe)
	{
		String errMsg =
					ID +
					message
					;
		int checksumGenerated;

		if(!isCheckSumPipe)
			checksumGenerated = csU.getFIXChecksum(errMsg, isCheckSumPipe);
		else
			checksumGenerated = csU.getFIXChecksum(bU.bytesToStr(
				bU.replacePipeWithSOH(bU.strToBytes(errMsg))
				), false);
		errMsg += ("10="+checksumGenerated+ String.valueOf((char) 1));
		return errMsg;
	}

	public void putStringToClient(Map<SocketChannel, byte[]> map, SocketChannel socket, String message)
	{
		map.put(
			socket,
			bU.replacePipeWithSOH(bU.strToBytes(message)));
	}
}
