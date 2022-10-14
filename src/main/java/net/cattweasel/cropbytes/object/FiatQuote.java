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

@Entity(name = "FiatQuote")
@Table(name = "cb_fiat_quote", uniqueConstraints = @UniqueConstraint(columnNames={"base_currency", "target_currency"}))
public class FiatQuote implements Serializable {

	private static final long serialVersionUID = -1780363418892144708L;

	private Integer id;
	private Date timestamp;
	private Currency baseCurrency;
	private Currency targetCurrency;
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
		return timestamp == null ? null : new Date(timestamp.getTime());
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp == null ? null : new Date(timestamp.getTime());
	}

	@ManyToOne
	@JoinColumn(name = "base_currency", unique = false, nullable = false)
	public Currency getBaseCurrency() {
		return baseCurrency;
	}

	public void setBaseCurrency(Currency baseCurrency) {
		this.baseCurrency = baseCurrency;
	}

	@ManyToOne
	@JoinColumn(name = "target_currency", unique = false, nullable = false)
	public Currency getTargetCurrency() {
		return targetCurrency;
	}

	public void setTargetCurrency(Currency targetCurrency) {
		this.targetCurrency = targetCurrency;
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
