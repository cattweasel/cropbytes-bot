package net.cattweasel.cropbytes.weasel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Snapshot {

	private Date timestamp;
	private Portfolio portfolio;
	private Map<String, Double> currencies;
	private Map<String, Double> assets;
	
	public Snapshot() {
	}
	
	public Snapshot(Snapshot current) {
		if (current != null) {
			this.currencies = current.getCurrencies();
			this.assets = current.getAssets();
		}
	}
	
	@Override
	public String toString() {
		return String.format("Snapshot[timestamp: %s - currencies: %s - assets: %s]",
				timestamp, currencies, assets);
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

	public Map<String, Double> getCurrencies() {
		return currencies == null ? new HashMap<String, Double>() : currencies;
	}

	public void setCurrencies(Map<String, Double> currencies) {
		this.currencies = currencies;
	}

	public Map<String, Double> getAssets() {
		return assets == null ? new HashMap<String, Double>() : assets;
	}

	public void setAssets(Map<String, Double> assets) {
		this.assets = assets;
	}
}
