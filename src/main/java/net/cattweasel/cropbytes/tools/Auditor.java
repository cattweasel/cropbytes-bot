package net.cattweasel.cropbytes.tools;

import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;

import net.cattweasel.cropbytes.telegram.AuditEvent;
import net.cattweasel.cropbytes.telegram.AuditEvent.AuditAction;
import net.cattweasel.cropbytes.telegram.User;

/**
 * Central utility class for creating audit events.
 * 
 * @author cattweasel
 *
 */
public class Auditor {

	/**
	 * Creates a new audit event based on the given input arguments.
	 * 
	 * @param session The database session to be used
	 * @param user The user (origin) who caused the event
	 * @param action The action which were performed by the user
	 * @param source An optionally source to describe the peace of code
	 * @param data Optionally data which was processed during the event
	 */
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
