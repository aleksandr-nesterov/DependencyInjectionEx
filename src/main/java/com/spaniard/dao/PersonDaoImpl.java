package com.spaniard.dao;

import java.util.List;
import org.hibernate.Session;
import com.spaniard.db.Person;
import com.spaniard.di.annotations.Inject;
import com.spaniard.di.annotations.Repository;
import com.spaniard.di.annotations.Transactional;

/**
 *
 * @author alex
 */
@Repository
public class PersonDaoImpl implements PersonDao {

    @Inject
    private Session session;

    private final static String MAX_ID_QUERY
            = "select max(e.id) from EsbJournError as e ";
    private final static String LIST_QUERY = "from EsbJournError as e where :lastId < e.id order by e.id desc";
    
    @Override
    @Transactional(readOnly = true)
    public Long getMaxId() {
        return (Long) session.createQuery(MAX_ID_QUERY).uniqueResult();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Person> getList(Long lastId) {
        return session.createQuery(LIST_QUERY).setParameter("lastId", lastId).list();
    }

}
