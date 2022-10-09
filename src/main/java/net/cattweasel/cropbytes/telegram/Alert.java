package net.cattweasel.cropbytes.telegram;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Currency;

@Entity(name = "Alert")
@Table(name = "tg_alert")
public class Alert implements Serializable {

	private static final long serialVersionUID = 9048581841536503491L;

	private Integer id;
	private User user;
	private Double factor;
	private Boolean allAssets;
	private Boolean allCurrencies;
	private Asset customAsset;
	private Currency customCurrency;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name = "user", unique = false, nullable = false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Column(name = "factor", unique = false, nullable = false)
	public Double getFactor() {
		return factor;
	}

	public void setFactor(Double factor) {
		this.factor = factor;
	}

	@Column(name = "all_assets", unique = false, nullable = false)
	public Boolean isAllAssets() {
		return allAssets;
	}

	public void setAllAssets(Boolean allAssets) {
		this.allAssets = allAssets;
	}

	@Column(name = "all_currencies", unique = false, nullable = false)
	public Boolean isAllCurrencies() {
		return allCurrencies;
	}

	public void setAllCurrencies(Boolean allCurrencies) {
		this.allCurrencies = allCurrencies;
	}

	@ManyToOne
	@JoinColumn(name = "custom_asset", unique = false, nullable = true)
	public Asset getCustomAsset() {
		return customAsset;
	}

	public void setCustomAsset(Asset customAsset) {
		this.customAsset = customAsset;
	}

	@ManyToOne
	@JoinColumn(name = "custom_currency", unique = false, nullable = true)
	public Currency getCustomCurrency() {
		return customCurrency;
	}

	public void setCustomCurrency(Currency customCurrency) {
		this.customCurrency = customCurrency;
	}
}
