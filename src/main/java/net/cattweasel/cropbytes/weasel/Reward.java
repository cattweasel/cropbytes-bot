package net.cattweasel.cropbytes.weasel;

import java.util.Date;

import net.cattweasel.cropbytes.object.Asset;

public class Reward {

	private Long id;
	private Date timestamp;
	private Asset asset;
	private Portfolio portfolio;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Asset getAsset() {
		return asset;
	}

	public void setAsset(Asset asset) {
		this.asset = asset;
	}

	public Portfolio getPortfolio() {
		return portfolio;
	}

	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
	}
}
