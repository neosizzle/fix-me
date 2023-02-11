package com.jng.transactions;

public class BusinessTransaction extends Transaction {
	private String _instrument;
	private double _quantity;
	private String _market;
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
	public String getMarket() {
		return _market;
	}
	public void setMarket(String _market) {
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
		String market,
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
}
