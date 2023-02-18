package com.jng.transactions;

import com.jng.utils.BufferUtils;

public class ResponseTransaction extends Transaction {
	private String _response;
	private String _instrument;
	private int _trxnId;
	private int _broker;

	public String getInstrument() {
		return _instrument;
	}
	public void setInstrument(String _instrument) {
		this._instrument = _instrument;
	}
	public int getBroker() {
		return _broker;
	}
	public void setBroker(int _broker) {
		this._broker = _broker;
	}
	public int getTrxnId() {
		return _trxnId;
	}
	public void setTrxnId(int _trxnId) {
		this._trxnId = _trxnId;
	}
	public String getResponse() {
		return _response;
	}
	public void setResponse(String _response) {
		this._response = _response;
	}

	ResponseTransaction(
		String status,
		int trnxId,
		String instrument,
		int toId,
		int fromId,
		String rawMsg,
		int checksum
	)
	{
		super(toId, fromId, rawMsg, true, false, checksum);
		this._response = status;
		this._trxnId = trnxId;
		this._instrument = instrument;
	}

	public ResponseTransaction(byte[] fixMsg) throws Exception
	{
		super(-1, -1, "", true, false, -1);
		BufferUtils bu = new BufferUtils();

		this._rawMsg = bu.bytesToStr(fixMsg);
		String rawMsgStr =  bu.bytesToStr(fixMsg);
		String[] tokens = rawMsgStr.split(String.valueOf((char) 1), -1);
		if (tokens.length != 7) throw new Exception("Invalid transaction");

		// get id
		String idPair = tokens[0];
		this._fromId  = Integer.valueOf(idPair);

		// get response
		String responsePair = tokens[1];
		String[] responseTokens = responsePair.split("=", -1);
		if (responseTokens.length != 2)
			throw new Exception("Invalid transaction (response)");
		if (!responseTokens[0].equals("response"))
			throw new Exception("Invalid transaction (response)");
		if (!responseTokens[1].equals("ACCEPT") && !responseTokens[1].equals("REJECT"))
			throw new Exception("Invalid transaction (response)");
		this._response = responseTokens[1];

		// broker
		String brokerPair = tokens[2];
		String[] brokerTokens = brokerPair.split("=", -1);
		if (brokerTokens.length != 2)
			throw new Exception("Invalid transaction (broker)");
		if (!brokerTokens[0].equals("broker"))
			throw new Exception("Invalid transaction (broker)");
		this._broker = Integer.valueOf(brokerTokens[1]);
		this._toId = this._broker;

		// trnxid
		String trnxPair = tokens[3];
		String[] trnxTokens = trnxPair.split("=", -1);
		if (trnxTokens.length != 2)
			throw new Exception("Invalid transaction (trnx)");
		if (!trnxTokens[0].equals("trnx"))
			throw new Exception("Invalid transaction (trnx)");
		this._trxnId = Integer.valueOf(trnxTokens[1]);

		String instrumentPair = tokens[4];
		String[] instrument = instrumentPair.split("=", -1);
		if (instrument.length != 2)
			throw new Exception("Invalid transaction (instrument)");
		if (!instrument[0].equals("instrument"))
			throw new Exception("Invalid transaction (instrument)");
		this._instrument = instrument[1];

		// checksum
		String checksumPair = tokens[5];
		String[] checksumTokens = checksumPair.split("=", -1);
		if (checksumTokens.length != 2)
			throw new Exception("Invalid transaction (checksum)");
		if (!checksumTokens[0].equals("10"))
			throw new Exception("Invalid transaction (checksum)");
		this._checksum = Integer.valueOf(checksumTokens[1]);
	}
}
