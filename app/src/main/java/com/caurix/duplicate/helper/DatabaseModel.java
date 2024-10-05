package com.caurix.duplicate.helper;

public class DatabaseModel {

	// Model Class for Database (SMS Database to store messages in the App)

	private int id;
	private String sender;
	private String client_number;
	private String amount;
	private String trx_type;
	private String received_time;
	private String sent_time;
	private String is_duplicate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getClient_number() {
		return client_number;
	}

	public void setClient_numberr(String client_number) {
		this.client_number = client_number;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getTrx_type() {
		return trx_type;
	}

	public void setTrx_type(String trx_type) {
		this.trx_type = trx_type;
	}

	public String getReceived_time() {
		return received_time;
	}

	public void setReceived_time(String received_time) {
		this.received_time = received_time;
	}

	public String getSent_time() {
		return sent_time;
	}

	public void setSent_time(String sent_time) {
		this.sent_time = sent_time;
	}
	public String getIs_duplicate() {
		return is_duplicate;
	}

	public void setIs_duplicate(String is_duplicate) {
		this.is_duplicate = is_duplicate;
	}
}
