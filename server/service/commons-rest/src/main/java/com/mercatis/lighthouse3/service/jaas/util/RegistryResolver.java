package com.mercatis.lighthouse3.service.jaas.util;

import java.lang.reflect.Method;

import javax.persistence.PersistenceException;

import org.hibernate.SessionFactory;

import com.mercatis.lighthouse3.service.commons.rest.HibernateDomainModelEntityRestServiceContainer;

public class RegistryResolver<T> {
	private final String classname;
	
	public RegistryResolver(String classname) {
		this.classname = classname;
	}
	
	@SuppressWarnings("unchecked")
	public T resolve() {
		// create session factory
		SessionFactory factory = HibernateDomainModelEntityRestServiceContainer.getSessionFactory();
		
		// instantiate registry
		try {
			Class<?> registryClass;
			Method setSessionFactory;
			registryClass = Class.forName(classname);
			T registry = (T) registryClass.newInstance();
			setSessionFactory = registryClass.getMethod("setSessionFactory", org.hibernate.SessionFactory.class);
			setSessionFactory.invoke(registry, factory);
			return registry;
		} catch (Exception e) {
			throw new PersistenceException("error getting registry "+classname);
		}
	}
}
