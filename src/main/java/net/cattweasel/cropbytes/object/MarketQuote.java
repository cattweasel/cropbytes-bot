package net.cattweasel.cropbytes.object;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = "MarketQuote")
@Table(name = "cb_market_quote", uniqueConstraints = @UniqueConstraint(columnNames={"currency", "asset"}))
public class MarketQuote implements Serializable {

	private static final long serialVersionUID = -8837614747238131223L;

	private Integer id;
	private Date timestamp;
	private Currency currency;
	private Asset asset;
	private Double price;
	private Double avgPrice;
	private Double priceChange;
	private Double volume;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	@Column(name = "timestamp", unique = false, nullable = false)
	public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@ManyToOne
	@JoinColumn(name = "currency", unique = false, nullable = false)
	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	@ManyToOne
	@JoinColumn(name = "asset", unique = false, nullable = false)
	public Asset getAsset() {
		return asset;
	}

	public void setAsset(Asset asset) {
		this.asset = asset;
	}

	@Column(name = "price", unique = false, nullable = false)
	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	@Column(name = "avg_price", unique = false, nullable = false)
	public Double getAvgPrice() {
		return avgPrice;
	}

	public void setAvgPrice(Double avgPrice) {
		this.avgPrice = avgPrice;
	}

	@Column(name = "price_change", unique = false, nullable = false)
	public Double getPriceChange() {
		return priceChange;
	}

	public void setPriceChange(Double priceChange) {
		this.priceChange = priceChange;
	}

	@Column(name = "volume", unique = false, nullable = false)
	public Double getVolume() {
		return volume;
	}

	public void setVolume(Double volume) {
		this.volume = volume;
	}
}