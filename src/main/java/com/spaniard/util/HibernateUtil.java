package com.spaniard.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 *
 * @author alex
 */
public class HibernateUtil {

    public static SessionFactory buildSessionFactory() {
        SessionFactory sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml").buildSessionFactory();
        return sessionFactory;
    }
    
}
