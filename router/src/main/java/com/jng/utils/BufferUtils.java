package com.jng.utils;

public class BufferUtils {
	// Function to insert x in arr at position pos
    public String[] insertX(int n, String arr[],
                                String x, int pos)
    {
        int i;
 
        // create a new array of size n+1
        String newarr[] = new String[n + 1];
 
        // insert the elements from
        // the old array into the new array
        // insert all elements till pos
        // then insert x at pos
        // then insert rest of the elements
        for (i = 0; i < n + 1; i++) {
            if (i < pos - 1)
                newarr[i] = arr[i];
            else if (i == pos - 1)
                newarr[i] = x;
            else
                newarr[i] = arr[i - 1];
        }
        return newarr;
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
}
