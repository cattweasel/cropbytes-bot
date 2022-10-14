package net.cattweasel.cropbytes.tools;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Utility class for providing database sessions.
 * 
 * @author cattweasel
 *
 */
public class HibernateUtil {

	private static SessionFactory factory;
	
	static {
		
		factory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
	}
	
	/**
	 * Get a new database session. Keep in mind to close it after usage!
	 * 
	 * @return A new database session
	 */
	public static Session openSession() {
		return factory.openSession();
	}
}
