package com.spaniard.dao;

import java.util.List;
import com.spaniard.db.Person;

/**
 *
 * @author alex
 */
public interface PersonDao {

    Long getMaxId();

    List<Person> getList(Long lastId);
}
