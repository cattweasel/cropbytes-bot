package net.cattweasel.cropbytes.object;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity(name = "Asset")
@Table(name = "cb_asset")
public class Asset implements Serializable {

	public enum AssetType {
		
		ANIMAL,
		BUILDING,
		CROPLAND,
		EXTRACT,
		FEED,
		GENERAL,
		SEED,
		TREE
	}
	
	private static final long serialVersionUID = 1514028180592367744L;
	
	private String code;
	private String name;
	private AssetType assetType;
	private Boolean tradeable;
	private Boolean trialAsset;
	private Boolean proAsset;
	private Integer duration;
	private Asset origin;
	private Double grindingFees;
	private List<Requirement> requirements;
	private List<Extract> extracts;
	
	@Id
	@Column(name = "code", unique = true, nullable = false, length = 8)
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	@Column(name = "name", unique = true, nullable = false, length = 32)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name = "asset_type", unique = false, nullable = false)
	@Enumerated(EnumType.STRING)
	public AssetType getAssetType() {
		return assetType;
	}

	public void setAssetType(AssetType assetType) {
		this.assetType = assetType;
	}

	@Column(name = "tradeable", unique = false, nullable = false)
	public Boolean isTradeable() {
		return tradeable;
	}

	public void setTradeable(Boolean tradeable) {
		this.tradeable = tradeable;
	}

	@Column(name = "trial_asset", unique = false, nullable = false)
	public Boolean isTrialAsset() {
		return trialAsset;
	}

	public void setTrialAsset(Boolean trialAsset) {
		this.trialAsset = trialAsset;
	}

	@Column(name = "pro_asset", unique = false, nullable = false)
	public Boolean isProAsset() {
		return proAsset;
	}

	public void setProAsset(Boolean proAsset) {
		this.proAsset = proAsset;
	}

	@Column(name = "duration", unique = false, nullable = true)
	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	@ManyToOne
	@JoinColumn(name = "origin", unique = false, nullable = true)
	public Asset getOrigin() {
		return origin;
	}

	public void setOrigin(Asset origin) {
		this.origin = origin;
	}

	@Column(name = "grinding_fees", unique = false, nullable = true)
	public Double getGrindingFees() {
		return grindingFees;
	}

	public void setGrindingFees(Double grindingFees) {
		this.grindingFees = grindingFees;
	}

	@OneToMany(mappedBy = "origin", cascade = CascadeType.ALL)
	public List<Requirement> getRequirements() {
		return requirements;
	}

	public void setRequirements(List<Requirement> requirements) {
		this.requirements = requirements;
	}
	
	public void addRequirement(Requirement requirement) {
		if (requirements == null) {
			requirements = new ArrayList<Requirement>();
		}
		requirements.add(requirement);
	}

	@OneToMany(mappedBy = "origin", cascade = CascadeType.ALL)
	public List<Extract> getExtracts() {
		return extracts;
	}

	public void setExtracts(List<Extract> extracts) {
		this.extracts = extracts;
	}
	
	public void addExtract(Extract extract) {
		if (extracts == null) {
			extracts = new ArrayList<Extract>();
		}
		extracts.add(extract);
	}
}
