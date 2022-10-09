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
import javax.persistence.UniqueConstraint;

import net.cattweasel.cropbytes.object.Asset;

@Entity(name = "FarmAsset")
@Table(name = "tg_farm_asset", uniqueConstraints = @UniqueConstraint(columnNames={"farm", "target", "seeds"}))
public class FarmAsset implements Serializable {

	private static final long serialVersionUID = -8313286398781670240L;

	private Integer id;
	private Farm farm;
	private Asset target;
	private Integer amount;
	private Asset seeds;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name = "farm", unique = false, nullable = false)
	public Farm getFarm() {
		return farm;
	}

	public void setFarm(Farm farm) {
		this.farm = farm;
	}

	@ManyToOne
	@JoinColumn(name = "target", unique = false, nullable = false)
	public Asset getTarget() {
		return target;
	}

	public void setTarget(Asset target) {
		this.target = target;
	}

	@Column(name = "amount", unique = false, nullable = false)
	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	@ManyToOne
	@JoinColumn(name = "seeds", unique = false, nullable = true)
	public Asset getSeeds() {
		return seeds;
	}

	public void setSeeds(Asset seeds) {
		this.seeds = seeds;
	}
}
