package com.jng.transactions;

public class Transaction {
	protected int _id;
	protected int _toId;
	protected int _fromId;
	protected String _rawMsg;
	protected boolean _isConfirmed;
	protected boolean _isResponse;
	protected int _checksum;

	public int getId() {
		return _id;
	}
	public void setId(int _id) {
		this._id = _id;
	}
	public int getToId() {
		return _toId;
	}
	public void setToId(int _toId) {
		this._toId = _toId;
	}
	public int getFromId() {
		return _fromId;
	}
	public void setFromId(int _fromId) {
		this._fromId = _fromId;
	}
	public String getRawMsg() {
		return _rawMsg;
	}
	public void setRawMsg(String _rawMsg) {
		this._rawMsg = _rawMsg;
	}
	public int getChecksum() {
		return _checksum;
	}
	public void setChecksum(int _checksum) {
		this._checksum = _checksum;
	}
	public boolean getIsResponse() {
		return _isResponse;
	}
	public void setIsResponse(boolean _isRes) {
		this._isResponse = _isRes;
	}
	public boolean getIsConfirmed()
	{
		return this._isConfirmed;
	}
	public void setIsConfirmed(boolean isconfirmed)
	{
		this._isConfirmed = isconfirmed;
	}

	public boolean validateChecksum()
	{
		return true;
	}

	public Transaction(int toId, int fromId, String rawMsg, boolean isResponse, boolean isConfirmed, int checksum)
	{
		this._id = -1;
		this._fromId = fromId;
		this._toId = toId;
		this._rawMsg = rawMsg;
		this._isResponse = isResponse;
		this._isConfirmed = isConfirmed;
		this._checksum = checksum;

	}
}
