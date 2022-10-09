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
import javax.persistence.UniqueConstraint;

@Entity(name = "Requirement")
@Table(name = "cb_requirement", uniqueConstraints = @UniqueConstraint(columnNames={"origin", "target"}))
public class Requirement implements Serializable {

	private static final long serialVersionUID = -6015710019622798739L;
	
	private Integer id;
	private Asset origin;
	private Asset target;
	private Double amount;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "amount", unique = false, nullable = false)
	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	@ManyToOne
	@JoinColumn(name = "origin", unique = false, nullable = false)
	public Asset getOrigin() {
		return origin;
	}

	public void setOrigin(Asset origin) {
		this.origin = origin;
	}

	@ManyToOne
	@JoinColumn(name = "target", unique = false, nullable = false)
	public Asset getTarget() {
		return target;
	}

	public void setTarget(Asset target) {
		this.target = target;
	}
}
