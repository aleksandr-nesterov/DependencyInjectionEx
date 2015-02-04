package com.spaniard.handler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import com.spaniard.di.annotations.Inject;
import com.spaniard.di.annotations.Repository;
import com.spaniard.di.annotations.Transactional;
import static com.spaniard.util.HibernateUtil.buildSessionFactory;

/**
 *
 * @author alex
 */
public class DependencyInjectionHandler {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    /**
     * <p>
     * Use to inject into object instance
     *
     * @param <T> -- the type of the object instance
     * @param packageName -- your package name
     * @param obj -- proxy object instance
     * @return
     */
    public static <T> T processInstance(String packageName, T obj) {
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false),
                new TypeAnnotationsScanner(), new FieldAnnotationsScanner());
        Set<Class<? extends Object>> set = reflections.getSubTypesOf(Object.class);
        for (Field injectField : obj.getClass().getDeclaredFields()) {
            if (injectField.isAnnotationPresent(Inject.class)) {
                for (Class clazz : set) {
                    try {
                        if (!clazz.isInterface() && injectField.getType().isAssignableFrom(clazz)) {
                            injectField.setAccessible(true);
                            Object instance = clazz.newInstance();
                            if (injectField.getType().isInterface()) {
                                Object newInstance = getProxy(injectField.getType(), instance);
                                injectField.set(obj, newInstance);
                            } else {
                                throw new RuntimeException("Could not inject into field [" + injectField.getName() + "]. Declare an interface type.");
                            }
                        }
                    } catch (SecurityException | InstantiationException | IllegalAccessException ex) {
                        Logger.getLogger(DependencyInjectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return obj;
    }

    private static <T> T getProxy(Class intf, final T obj) {
        return (T) Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[]{intf}, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
                if (obj.getClass().isAnnotationPresent(Repository.class)) {
                    Method declaredMethod = obj.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
                    if (declaredMethod.isAnnotationPresent(Transactional.class)) {
                        Session session = sessionFactory.openSession();
                        try {
                            for (Field field : obj.getClass().getDeclaredFields()) {
                                if (field.isAnnotationPresent(Inject.class)) {
                                    field = obj.getClass().getDeclaredField("session");
                                    field.setAccessible(true);
                                    field.set(obj, session);
                                }
                            }
                        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                            Logger.getLogger(DependencyInjectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        Object result = null;
                        Transaction tx = null;
                        try {
                            if (declaredMethod.getDeclaredAnnotation(Transactional.class).readOnly()) {
                                try {
                                    result = method.invoke(obj, args);
                                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                    Logger.getLogger(DependencyInjectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } else {
                                tx = session.beginTransaction();
                                try {
                                    result = method.invoke(obj, args);
                                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                    Logger.getLogger(DependencyInjectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                tx.commit();
                            }
                        } catch (HibernateException e) {
                            if (tx != null) {
                                tx.rollback();
                            }
                        } finally {
                            session.close();
                        }
                        return result;
                    }
                }
                return method.invoke(obj, args);
            }
        }
        );
    }

}
