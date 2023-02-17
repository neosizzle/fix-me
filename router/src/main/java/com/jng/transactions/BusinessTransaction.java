package com.jng.transactions;

import com.jng.utils.BufferUtils;

public class BusinessTransaction extends Transaction {
	private String _instrument;
	private double _quantity;
	private int 	_market;
	private double _price;
	private boolean _isBuy;

	public String getInstrument() {
		return _instrument;
	}
	public void setInstrument(String _instrument) {
		this._instrument = _instrument;
	}
	public double getQuantity() {
		return _quantity;
	}
	public void setQuantity(double _quantity) {
		this._quantity = _quantity;
	}
	public int getMarket() {
		return _market;
	}
	public void setMarket(int _market) {
		this._market = _market;
	}
	public double getPrice() {
		return _price;
	}
	public void setPrice(double _price) {
		this._price = _price;
	}
	public boolean getIsBuy() {
		return _isBuy;
	}
	public void setIsBuy(boolean _isBuy) {
		this._isBuy = _isBuy;
	}
	public BusinessTransaction(
		String instrument,
		double quantity,
		int market,
		double price,
		int toId,
		int fromId,
		String rawMsg,
		int checksum,
		boolean isBuy
		)
	{
		super(toId, fromId, rawMsg, false, false, checksum);
		this._instrument = instrument;
		this._quantity = quantity;
		this._market = market;
		this._price = price;
		this._isBuy = isBuy;
	}

	public BusinessTransaction(byte[] fixMsg) throws Exception
	{
		super(-1, -1, "", false, false, -1);
		BufferUtils bu = new BufferUtils();

		this._rawMsg = bu.bytesToStr(fixMsg);
		String changed = bu.bytesToStr(bu.replaceSOHwithPipe(fixMsg));
		String[] tokens = changed.split("\\|", -1);
		if (tokens.length != 7) throw new Exception("Invalid transaction");
		

		// get id
		String idPair = tokens[0];
		this._fromId  = Integer.valueOf(idPair);

		// instrument
		String instrumentPair = tokens[1];
		String[] instrumentTokens = instrumentPair.split("=", -1);
		if (instrumentTokens.length != 2)
			throw new Exception("Invalid transaction (instrument)");
		if (!instrumentTokens[0].equals("instrument"))
			throw new Exception("Invalid transaction (instrument)");
		this._instrument = instrumentTokens[1];

		// market
		String marketPair = tokens[2];
		String[] marketTokens = marketPair.split("=", -1);
		if (marketTokens.length != 2)
			throw new Exception("Invalid transaction (market)");
		if (!marketTokens[0].equals("market"))
			throw new Exception("Invalid transaction (market)");
		this._market = Integer.valueOf(marketTokens[1]);
		this._toId = this._market;

		// price
		String pricePair = tokens[3];
		String[] priceTokens = pricePair.split("=", -1);
		if (priceTokens.length != 2)
			throw new Exception("Invalid transaction (price)");
		if (!priceTokens[0].equals("price"))
			throw new Exception("Invalid transaction (price)");
		this._price = Double.valueOf(priceTokens[1]);

		// isBuy
		String isBuyPair = tokens[4];
		String[] isBuyTokens = isBuyPair.split("=", -1);
		if (isBuyTokens.length != 2)
			throw new Exception("Invalid transaction (isBuy)");
		if (!isBuyTokens[0].equals("isBuy"))
			throw new Exception("Invalid transaction (isBuy)");
		this._isBuy = isBuyTokens[1].equals("true");

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
