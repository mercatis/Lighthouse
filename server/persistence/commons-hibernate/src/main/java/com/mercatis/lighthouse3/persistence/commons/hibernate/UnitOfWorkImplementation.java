/*
 * Copyright 2011 mercatis Technologies AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mercatis.lighthouse3.persistence.commons.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionFactoryImplementor;

import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.commons.UnitOfWork;

public class UnitOfWorkImplementation implements UnitOfWork {
	
	protected Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * This field maintains the reference to the Hibernate session factory
	 * underlying the current unit of work.
	 */
	private static SessionFactory sessionFactory = null;

	/**
	 * This property keeps a reference to the SQL dialect of the database
	 * underlying the DAO implementation
	 */
	private Dialect sqlDialect = null;

	/**
	 * This method return the Hibernate SQL dialect of the database underlying
	 * the unit of work implementation.
	 * 
	 * @return the Hibernate SQL dialect
	 */
	public Dialect getSqlDialect() {
		if ((this.sqlDialect == null) && (sessionFactory != null))
			this.sqlDialect = ((SessionFactoryImplementor) sessionFactory).getDialect();

		return this.sqlDialect;
	}

	/**
	 * This map maintains closing-safe thread local Hibernate sessions. As one
	 * may instantiate several Hibernate-based DAOs pointing to the same session
	 * factory, it is important that this map is global.
	 */
	private static ThreadLocal<Session> threadLocalSessions = null;
	
	/**
	 * Set the Hibernate session factory to use for the current unit of work.
	 * 
	 * @param sessionFactory
	 *            the session factory to use for the current unit of work
	 */
	public void setSessionFactory(SessionFactory factory) {
		if (factory != sessionFactory) {
			threadLocalSessions = new ThreadLocal<Session>();
			sessionFactory = factory;
		}
	}

	/**
	 * This method returns the Hibernate session factory used for the current
	 * unit of work.
	 * 
	 * @return the session factory.
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * Returns the current hibernate session for the unit of work.
	 * 
	 * @return the hibernate session
	 */
	public Session getCurrentSession() {
		Session session = threadLocalSessions.get();
		
		if (session == null) {
			try {
				if (log.isDebugEnabled()) {
					log.debug("UnitOfWork: Open Session, SessionFactory: " + System.identityHashCode(this.getSessionFactory()));
				}
				session = this.getSessionFactory().openSession();
				session.setDefaultReadOnly(true);

				if (!session.getTransaction().isActive())
					session.beginTransaction();

				threadLocalSessions.set(session);
			} catch (Exception e1) {
				try {
					session.close();
					threadLocalSessions.set(null);
				} catch (Exception e2) {
					log.error(e2.getMessage(), e2);
				}
				
				throw new PersistenceException("Could not open new session", e1);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("UnitOfWork: Providing Session: " + System.identityHashCode(session));
		}
		return session;
	}

	public void commit() {
		Session session = null;
		try {
			session = threadLocalSessions.get();
			if (session == null) {
				return;
			}
			
			if (log.isDebugEnabled()) {
				log.debug("UnitOfWork: Commit Session: " + System.identityHashCode(session));
			}
			
			session.getTransaction().commit();
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			throw new PersistenceException("Could not commit transaction", e1);
		} finally {
			try {
				if (session != null) {
					session.close();
					threadLocalSessions.set(null);
				}
			} catch (Exception e2) {
				log.error(e2.getMessage(), e2);
			}
		}
	}

	public void flush() {
		Session session = null;
		try {
			session = threadLocalSessions.get();
			if (session == null) {
				return;
			}
			
			if (log.isDebugEnabled()) {
				log.debug("UnitOfWork: Flush Session: " + System.identityHashCode(session));
			}
			
			session.flush();
			session.clear();
		} catch (Exception cause) {
			throw new PersistenceException("Could not flush unit of work", cause);
		}
	}

	public void rollback() {
		Session session = null;
		try {
			session = threadLocalSessions.get();
			if (session == null) {
				return;
			}
			
			if (log.isDebugEnabled()) {
				log.debug("UnitOfWork: Rollback Session: " + System.identityHashCode(session));
			}
				
			session.getTransaction().rollback();
		}	 catch (Exception e1) {
				log.error(e1.getMessage(), e1);
				throw new PersistenceException("Could not commit transaction", e1);
			} finally {
				try {
					if (session != null) {
						session.close();
						threadLocalSessions.set(null);
					}
				} catch (Exception e2) {
					log.error(e2.getMessage(), e2);
				}
			}
	}
	
	public void setReadOnly(Object o, boolean readOnly) {
		Session session = threadLocalSessions.get();
		if (session == null) {
			return;
		}
		
		if (session.contains(o) && session.isReadOnly(o) != readOnly) {
			if (log.isDebugEnabled()) {
				log.debug("UnitOfWork: SetReadOnly: " + readOnly + ", Session: " + System.identityHashCode(session));
			}
			session.setReadOnly(o, readOnly);
		}
	}
}
