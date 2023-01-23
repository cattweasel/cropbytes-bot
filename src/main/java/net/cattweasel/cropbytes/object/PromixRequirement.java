package net.cattweasel.cropbytes.object;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "PromixRequirement")
@Table(name = "cb_promix_requirement"/*, uniqueConstraints = @UniqueConstraint(columnNames={"promix_asset", "regular_asset"})*/)
public class PromixRequirement implements Serializable {

	private static final long serialVersionUID = 3951016137777921140L;

	private Integer id;
	private Asset promixAsset;
	private Integer promixAmount;
	private Asset regularAsset;
	private Integer regularAmount;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name = "promix_asset", unique = false, nullable = false)
	public Asset getPromixAsset() {
		return promixAsset;
	}

	public void setPromixAsset(Asset promixAsset) {
		this.promixAsset = promixAsset;
	}

	@Column(name = "promix_amount", unique = false, nullable = false)
	public Integer getPromixAmount() {
		return promixAmount;
	}

	public void setPromixAmount(Integer promixAmount) {
		this.promixAmount = promixAmount;
	}

	@ManyToOne
	@JoinColumn(name = "regular_asset", unique = false, nullable = false)
	public Asset getRegularAsset() {
		return regularAsset;
	}

	public void setRegularAsset(Asset regularAsset) {
		this.regularAsset = regularAsset;
	}

	@Column(name = "regular_amount", unique = false, nullable = false)
	public Integer getRegularAmount() {
		return regularAmount;
	}

	public void setRegularAmount(Integer regularAmount) {
		this.regularAmount = regularAmount;
	}
}
