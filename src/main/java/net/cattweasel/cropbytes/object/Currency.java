package net.cattweasel.cropbytes.object;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "Currency")
@Table(name = "cb_currency")
public class Currency implements Serializable {

	private static final long serialVersionUID = 1106995570770190293L;

	private String code;
	private String name;
	
	@Id
	@Column(name = "code", unique = true, nullable = false, length = 5)
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
}
