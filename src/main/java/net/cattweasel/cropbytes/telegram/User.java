package net.cattweasel.cropbytes.telegram;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "User")
@Table(name = "tg_user")
public class User implements Serializable {

	private static final long serialVersionUID = -7930373270329517774L;

	private Long userId;
	private String username;
	private String language;
	private Date lastSeen;
	private String firstname;
	private String lastname;
	private Boolean admin;
	private Boolean broadcastDisabled;
	private Boolean sleepMode;
	
	@Id
	@Column(name = "user_id", unique = true, nullable = false)
	public Long getUserId() {
		return userId;
	}
	
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "username", unique = true, nullable = true, length = 64)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "language", unique = false, nullable = true, length = 8)
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Column(name = "last_seen", unique = false, nullable = false)
	public Date getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}

	@Column(name = "firstname", unique = false, nullable = true, length = 64)
	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	@Column(name = "lastname", unique = false, nullable = true, length = 64)
	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	@Column(name = "admin", unique = false, nullable = false)
	public Boolean isAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

	@Column(name = "broadcast_disabled", unique = false, nullable = false)
	public Boolean isBroadcastDisabled() {
		return broadcastDisabled;
	}

	public void setBroadcastDisabled(Boolean broadcastDisabled) {
		this.broadcastDisabled = broadcastDisabled;
	}

	@Column(name = "sleep_mode", unique = false, nullable = false)
	public Boolean isSleepMode() {
		return sleepMode;
	}

	public void setSleepMode(Boolean sleepMode) {
		this.sleepMode = sleepMode;
	}
}
