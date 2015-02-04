package com.spaniard.example;

import com.spaniard.handler.DependencyInjectionHandler;
import com.spaniard.dao.PersonDao;
import com.spaniard.di.annotations.Inject;

/**
 *
 * @author alex
 */
public class ObjectInstanceExample {

    @Inject
    private PersonDao esbJournErrorDao;

    public static void main(String[] args) {
        String packageName = "com.spaniard.dao";
        ObjectInstanceExample instance = DependencyInjectionHandler
                .processInstance(packageName, new ObjectInstanceExample());
        instance.f();       
    }

    private void f() {
        Long maxId = esbJournErrorDao.getMaxId();
        System.out.println(maxId);        
        System.out.println(esbJournErrorDao.getList(maxId).size());
    }

}
