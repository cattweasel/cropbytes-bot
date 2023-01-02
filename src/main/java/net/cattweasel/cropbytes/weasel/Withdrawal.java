package net.cattweasel.cropbytes.weasel;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.cattweasel.cropbytes.object.Currency;

public class Withdrawal {

	private Long id;
	private Portfolio portfolio;
	private Date timestamp;
	private Currency currency;
	private Double amount;
	private Double fees;
	
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return String.format("Withdrawal[timestamp: %s - currency: %s - amount: %s - fees: %s]",
				sdf.format(timestamp), currency.getCode(), amount, fees);
	}
	
	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public Currency getCurrency() {
		return currency;
	}
	
	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Portfolio getPortfolio() {
		return portfolio;
	}

	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
	}

	public Double getFees() {
		return fees;
	}

	public void setFees(Double fees) {
		this.fees = fees;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
