package net.cattweasel.cropbytes.weasel;

import java.util.Date;

import net.cattweasel.cropbytes.object.Currency;

public class FiatTrade {

	private Long id;
	private Long orderId;
	private Double price;
	private Double amount;
	private Double total;
	private Currency feeCurrency;
	private Double fees;
	private Currency baseCurrency;
	private Currency targetCurrency;
	private Date timestamp;
	private Portfolio portfolio;
	
	@Override
	public String toString() {
		return String.format("FiatTrade[baseCurrency: %s - targetCurrency: %s - price: %s - amount: %s - total: %s - fees: %s]",
				baseCurrency.getCode(), targetCurrency.getCode(), price, amount, total, fees);
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Double getTotal() {
		return total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}

	public Currency getFeeCurrency() {
		return feeCurrency;
	}

	public void setFeeCurrency(Currency feeCurrency) {
		this.feeCurrency = feeCurrency;
	}

	public Double getFees() {
		return fees;
	}

	public void setFees(Double fees) {
		this.fees = fees;
	}

	public Currency getBaseCurrency() {
		return baseCurrency;
	}

	public void setBaseCurrency(Currency baseCurrency) {
		this.baseCurrency = baseCurrency;
	}

	public Currency getTargetCurrency() {
		return targetCurrency;
	}

	public void setTargetCurrency(Currency targetCurrency) {
		this.targetCurrency = targetCurrency;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Portfolio getPortfolio() {
		return portfolio;
	}

	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
	}
}
