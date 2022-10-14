package net.cattweasel.cropbytes.telegram;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity(name = "Farm")
@Table(name = "tg_farm")
public class Farm implements Serializable {

	private static final long serialVersionUID = 4787784719999210240L;

	private Integer id;
	private User user;
	private List<FarmAsset> farmAssets;
	
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

	@OneToMany(mappedBy = "farm")
	public List<FarmAsset> getFarmAssets() {
		return farmAssets;
	}

	public void setFarmAssets(List<FarmAsset> farmAssets) {
		this.farmAssets = farmAssets;
	}
	
	public void addFarmAsset(FarmAsset farmAsset) {
		if (farmAssets == null) {
			farmAssets = new ArrayList<FarmAsset>();
		}
		farmAssets.add(farmAsset);
	}
}
