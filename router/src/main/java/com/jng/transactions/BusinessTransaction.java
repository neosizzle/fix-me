package com.jng.transactions;

import com.jng.utils.BufferUtils;

public class BusinessTransaction extends Transaction {
	private String _instrument;
	private double _quantity;
	private int 	_market;
	private double _price;

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
	public double get_price() {
		return _price;
	}
	public void set_price(double _price) {
		this._price = _price;
	}
	public BusinessTransaction(
		String instrument,
		double quantity,
		int market,
		double price,
		int toId,
		int fromId,
		String rawMsg,
		int checksum
		)
	{
		super(toId, fromId, rawMsg, false, false, checksum);
		this._instrument = instrument;
		this._quantity = quantity;
		this._market = market;
		this._price = price;
	}

	public BusinessTransaction(byte[] fixMsg) throws Exception
	{
		super(-1, -1, "", false, false, -1);
		BufferUtils bu = new BufferUtils();

		String changed = bu.bytesToStr(bu.replaceSOHwithPipe(fixMsg));
		String[] tokens = changed.split("|", -1);
		if (tokens.length != 5) throw new Exception("Invalid transaction");
		
		// String Pair = tokens[];
		// String[] Tokens = Pair.split("=", -1);
		// if (Tokens.length != 2)
		// 	throw new Exception("Invalid transaction ()");
		// if (Tokens[0] != "ID")
		// 	throw new Exception("Invalid transaction ()");
		// this._ = Integer.valueOf(Tokens[1]);

		// get id
		String idPair = tokens[0];
		String[] idTokens = idPair.split("=", -1);
		if (idTokens.length != 2)
			throw new Exception("Invalid transaction (id)");
		if (idTokens[0] != "ID")
			throw new Exception("Invalid transaction (id)");
		this._id = Integer.valueOf(idTokens[1]);

		// instrument
		String instrumentPair = tokens[1];
		String[] instrumentTokens = instrumentPair.split("=", -1);
		if (instrumentTokens.length != 2)
			throw new Exception("Invalid transaction (instrument)");
		if (instrumentTokens[0] != "instrument")
			throw new Exception("Invalid transaction (instrument)");
		this._instrument = instrumentTokens[1];

		// market
		String marketPair = tokens[2];
		String[] marketTokens = marketPair.split("=", -1);
		if (marketTokens.length != 2)
			throw new Exception("Invalid transaction (market)");
		if (marketTokens[0] != "market")
			throw new Exception("Invalid transaction (market)");
		this._market = Integer.valueOf(marketTokens[1]);

		// price
		String pricePair = tokens[3];
		String[] priceTokens = pricePair.split("=", -1);
		if (priceTokens.length != 2)
			throw new Exception("Invalid transaction (price)");
		if (priceTokens[0] != "price")
			throw new Exception("Invalid transaction (price)");
		this._price = Integer.valueOf(priceTokens[1]);

		// checksum
		String checksumPair = tokens[4];
		String[] checksumTokens = checksumPair.split("=", -1);
		if (checksumTokens.length != 2)
			throw new Exception("Invalid transaction (checksum)");
		if (checksumTokens[0] != "10")
			throw new Exception("Invalid transaction (checksum)");
		this._checksum = Integer.valueOf(checksumTokens[1]);
	}
}
