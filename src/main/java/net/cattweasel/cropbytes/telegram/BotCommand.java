package net.cattweasel.cropbytes.telegram;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "BotCommand")
@Table(name = "tg_bot_command")
public class BotCommand implements Serializable {

	private static final long serialVersionUID = 4076875224762173972L;

	private String command;
	private String executor;
	private Boolean disabled;
	private Boolean development;
	private Boolean admin;
	
	@Id
	@Column(name = "command", unique = true, nullable = false, length = 16)
	public String getCommand() {
		return command;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}

	@Column(name = "executor", unique = false, nullable = false, length = 128)
	public String getExecutor() {
		return executor;
	}

	public void setExecutor(String executor) {
		this.executor = executor;
	}

	@Column(name = "disabled", unique = false, nullable = false)
	public Boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	@Column(name = "development", unique = false, nullable = false)
	public Boolean isDevelopment() {
		return development;
	}

	public void setDevelopment(Boolean development) {
		this.development = development;
	}

	@Column(name = "admin", unique = false, nullable = false)
	public Boolean isAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}
}
