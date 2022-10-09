package net.cattweasel.cropbytes.tools;

import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;

import net.cattweasel.cropbytes.telegram.AuditEvent;
import net.cattweasel.cropbytes.telegram.AuditEvent.AuditAction;
import net.cattweasel.cropbytes.telegram.User;

public class Auditor {

	public static void audit(Session session, User user, AuditAction action, String source, String data) {
		Transaction tx = session.beginTransaction();
		AuditEvent event = new AuditEvent();
		event.setAction(action);
		event.setData(data);
		event.setTimestamp(new Date());
		event.setUser(user);
		event.setSource(source);
		session.save(event);
		tx.commit();
	}
}
