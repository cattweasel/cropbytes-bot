package net.cattweasel.cropbytes.telegram;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "AuditEvent")
@Table(name = "tg_audit_event")
public class AuditEvent implements Serializable {
	
	public enum AuditAction {
		
		EXECUTE_BOT_COMMAND,
		EXECUTE_CALLBACK,
		SEND_ALERT_NOTIFICATION
	}
	
	private static final long serialVersionUID = -8991710971110703935L;
	
	private Integer id;
	private Date timestamp;
	private AuditAction action;
	private User user;
	private String source;
	private String data;
	
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

	@Enumerated(EnumType.STRING)
	@Column(name = "action", unique = false, nullable = false, length = 32)
	public AuditAction getAction() {
		return action;
	}

	public void setAction(AuditAction action) {
		this.action = action;
	}

	@ManyToOne
	@JoinColumn(name = "user", unique = false, nullable = false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Column(name = "data", unique = false, nullable = true, length = 256)
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Column(name = "source", unique = false, nullable = false, length = 32)
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
