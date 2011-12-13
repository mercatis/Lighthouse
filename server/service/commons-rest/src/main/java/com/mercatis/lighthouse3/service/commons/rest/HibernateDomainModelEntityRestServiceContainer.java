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
package com.mercatis.lighthouse3.service.commons.rest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.UnitOfWork;
import com.mercatis.lighthouse3.persistence.commons.DAOProvider;
import com.mercatis.lighthouse3.persistence.commons.hibernate.UnitOfWorkImplementation;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;

/**
 * This base class provides a common service container for RESTful web services
 * representing domain model entities based on Hibernate.
 * 
 * It can be subclassed for each service to implement the method
 * 
 * <code>public abstract <R extends DomainModelEntityDAO<? extends DomainModelEntity>> R getDAO(Class<R> registryInterface)</code>
 * in a manner suitable for the service.
 * 
 * The default implementation of
 * <code>public abstract <R extends DomainModelEntityDAO<? extends DomainModelEntity>> R getDAO(Class<R> registryInterface)</code>
 * does the following: given an interface
 * <code>com.mercatis.lighthouse3.domainmodel.x.Y</code> it tries to construct
 * an instance of the class
 * <code>com.mercatis.lighthouse3.persistence.x.hibernate.YImplementation</code>
 * by
 * <ul>
 * <li>calling the default constructor
 * <li>and calling the method
 * <code>setSessionFactory(SessionFactory sessionFactory)</code> on the created
 * instance.
 * </ul>
 * 
 * The container supports additional init parameters for loading Hibernate
 * configurations:
 * 
 * <ul>
 * <li>
 * <code>com.mercatis.lighthouse3.service.commons.rest.HibernateConfigFileLocation</code>
 * for passing the location of the config file via a file path.
 * <code>com.mercatis.lighthouse3.service.commons.rest.HibernateConfigResource</code>
 * for passing the resource name of the configuration if it is contained in the
 * class path.
 * <code>com.mercatis.lighthouse3.service.commons.rest.HibernateDbConnectionUrl</code>
 * for passing the name of the database to override the hibernate settings with.
 * </ul>
 */
public class HibernateDomainModelEntityRestServiceContainer extends DomainModelEntityRestServiceContainer {

	private static final long serialVersionUID = -6484733341631474040L;

	public static final String HIBERNATE_DB_CONNECTION_URL = "com.mercatis.lighthouse3.service.commons.rest.HibernateDbConnectionUrl";

	public static final String HIBERNATE_CONFIG_FILE_LOCATION = "com.mercatis.lighthouse3.service.commons.rest.HibernateConfigFileLocation";

	public static final String HIBERNATE_CONFIG_RESOURCE = "com.mercatis.lighthouse3.service.commons.rest.HibernateConfigResource";

	public static final String HIBERNATE_DEFAULT_RESOURCE = "com/mercatis/lighthouse3/service/commons/rest/hibernate.mem.cfg.xml";

	public static final String HIBERNATE_LIGHTHOUSE_TEST_RESOURCE = "com/mercatis/lighthouse3/service/commons/rest/hibernate.lighthouse.test.cfg.xml";

	public static final String HIBERNATE_CONFIGURATION_SERVERKEY = "HibernateConfiguration";

	public static final String CONFIGURATION_SERVER_URL = "CONFIGURATION_SERVER_URL";

	public static final String CONFIGURATION_NAME = "Configuration_Name";

	private Map<DAORequest, Object> daoCache = new HashMap<DAORequest, Object>();

	/**
	 * Keeps the name of desired configuration in the Configuration Server
	 */
	protected String configurationName;

	/**
	 * Keeps the configuration loaded from the configuration server
	 */
	protected Properties configuration;

	/**
	 * This property keeps an overriding hibernate DB connection URL.
	 */
	private String hibernateDbConnectionUrl = null;

	/**
	 * This property keeps a reference to the initialized hibernate session
	 * factory to use for for the various registry implementations for the
	 * environment service.
	 */
	protected static SessionFactory sessionFactory = null;

	/**
	 * This method returns the configured hibernate session factory.
	 *
	 * @return the hibernate session factory.
	 */
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * This method sets up the hibernate session factory using the passed
	 * configuration file. It is invoked when init parameter
	 * <code>com.mercatis.lighthouse3.service.environment.rest.HibernateConfigFileLocation</code>
	 * is set.
	 *
	 * @param hibernateConfigFilePath
	 *            the path to the Hibernate config file.
	 */
	public Configuration setUpHibernateSessionFactoryFromFile(String hibernateConfigFilePath) {
		Configuration hibernateConfiguration = new Configuration().configure(new File(hibernateConfigFilePath));
		overwriteHibernateDBConnectionURL(hibernateConfiguration);
		return hibernateConfiguration;
	}

	/**
	 * This method sets up the hibernate session factory using the passed
	 * resource containing the hibernate configuration. It is invoked when init
	 * parameter
	 * <code>com.mercatis.lighthouse3.service.environment.rest.HibernateConfigResource</code>
	 * is set.
	 *
	 * @param hibernateConfigFilePath
	 *            the path to the config resource.
	 */
	public Configuration setUpHibernateSessionFactoryFromResource(String hibernateConfigResourcePath) {
		Configuration hibernateConfiguration = new Configuration().configure(hibernateConfigResourcePath);
		overwriteHibernateDBConnectionURL(hibernateConfiguration);
		return hibernateConfiguration;
	}

	/**
	 * This property keeps a reference to the current unit of work.
	 */
	protected UnitOfWorkImplementation unitOfWork = null;

	@Override
	protected synchronized void configure(Map<String, String> initParams, ResourceConfig rc, WebApplication wa) {
		super.configure(initParams, rc, wa);

		if (sessionFactory == null) {
			Configuration hibernateConfiguration = getHibernateConfiguration();
			sessionFactory = hibernateConfiguration.buildSessionFactory();
			if (log.isDebugEnabled()) {
				log.debug("Creating new Hibernate Session Factory.");
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Using already established Hibernate Session Factory.");
			}
		}
		
		unitOfWork = new UnitOfWorkImplementation();
		unitOfWork.setSessionFactory(getSessionFactory());
	}

	@Override
	protected UnitOfWork getCurrentUnitOfWork() {
		return unitOfWork;
	}

	public <R extends DomainModelEntityDAO<? extends DomainModelEntity>> R getDAO(Class<R> registryInterface) {
		return getDAO(registryInterface, false);
	}

	@SuppressWarnings("unchecked")
	public <R extends DomainModelEntityDAO<? extends DomainModelEntity>> R getDAO(Class<R> registryInterface, boolean forceHibnerate) {
		String baseName = registryInterface.getCanonicalName().replace("com.mercatis.lighthouse3.domainmodel.", "");
		String moduleName = baseName.split("\\.")[0];
		String daoName = baseName.split("\\.")[1];

		DAORequest request = new DAORequest(registryInterface.getName(), forceHibnerate);
		R result = (R) daoCache.get(request);

		if (result == null) {
			try {
				Class<?> implementationClass;
				if (!forceHibnerate && initParameters != null && initParameters.containsKey(registryInterface.getCanonicalName())) {
					implementationClass = Class.forName(initParameters.get(registryInterface.getCanonicalName()));
					result = (R) implementationClass.newInstance();
				} else {
					implementationClass = Class.forName("com.mercatis.lighthouse3.persistence." + moduleName
							+ ".hibernate." + daoName + "Implementation");
					result = (R) implementationClass.newInstance();
					Method setSessionFactory = implementationClass.getMethod("setSessionFactory",
							org.hibernate.SessionFactory.class);
					setSessionFactory.invoke(result, getSessionFactory());
				}

				Method setDAOProvider = implementationClass.getMethod("setDAOProvider", DAOProvider.class);
				setDAOProvider.invoke(result, this);

				if (initParameters != null) {
					Method setInitParams = implementationClass.getMethod("setInitParams", Map.class);
					setInitParams.invoke(result, initParameters);
					if (initParameters.containsKey(implementationClass.getCanonicalName() + ".configuration")) {
						Properties daoConfiguration = new Properties();
						try {
							daoConfiguration.load(HibernateDomainModelEntityRestServiceContainer.class.getResourceAsStream(
									initParameters.get(implementationClass.getCanonicalName() + ".configuration")));
						} catch (IOException ex) {
							Logger.getLogger(HibernateDomainModelEntityRestServiceContainer.class.getName()).log(Level.ERROR, null, ex);
						}
						Method configure = implementationClass.getMethod("configure", Properties.class);
						configure.invoke(result, daoConfiguration);
					}
				}
				Logger.getLogger(HibernateDomainModelEntityRestServiceContainer.class.getName()).debug(String.format("Putting dao for %s into DAOCache", daoName));
				daoCache.put(request, result);
			} catch (Throwable e) {
				log.error(e.getMessage());
				result = null;
			}
		} else {
			Logger.getLogger(HibernateDomainModelEntityRestServiceContainer.class.getName()).debug(String.format("Returning dao for %s from cache", daoName));
		}

		return (R) result;
	}

	/**
	 * Returns a Hibernate Configuration object depending on the initialization parameters in the web.xml file.
	 * @return
	 */
	private Configuration getHibernateConfiguration() {
		Configuration hibernateConfiguration;
		if (getInitParameter(HIBERNATE_DB_CONNECTION_URL) != null) {
			hibernateDbConnectionUrl = getInitParameter(HIBERNATE_DB_CONNECTION_URL);
		}

		if (getInitParameter(HIBERNATE_CONFIG_FILE_LOCATION) != null) {
			hibernateConfiguration = setUpHibernateSessionFactoryFromFile(getInitParameter(HIBERNATE_CONFIG_FILE_LOCATION));
		} else if (getInitParameter(HIBERNATE_CONFIG_RESOURCE) != null) {
			hibernateConfiguration = setUpHibernateSessionFactoryFromResource(getInitParameter(HIBERNATE_CONFIG_RESOURCE));
		} else {
			hibernateConfiguration = setUpHibernateSessionFactoryFromResource(HIBERNATE_DEFAULT_RESOURCE);
		}

		return hibernateConfiguration;
	}

	private void overwriteHibernateDBConnectionURL(Configuration hibernateConfiguration) {
		if (hibernateDbConnectionUrl != null) {
			hibernateConfiguration.setProperty("hibernate.connection.url", hibernateDbConnectionUrl);
		}
	}

	/**
	 * Returns the loaded configuration from the Configuration server
	 * @return
	 */
	public Properties getConfiguration() {
		return configuration;
	}

	private class DAORequest {

		private String className;
		private boolean forceHibernate;

		public DAORequest(String className, boolean forceHibernate) {
			this.className = className;
			this.forceHibernate = forceHibernate;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final DAORequest other = (DAORequest) obj;
			if ((className == null) ? (other.className != null) : !className.equals(other.className)) {
				return false;
			}
			if (forceHibernate != other.forceHibernate) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 17 * hash + (className != null ? className.hashCode() : 0);
			hash = 17 * hash + (forceHibernate ? 1 : 0);
			return hash;
		}
	}

	private void shutdownHibernateSessionfactory() {
		if (sessionFactory != null) {
			sessionFactory.close();
			sessionFactory = null;
		}
	}
	
	@Override
	public void destroy() {
		shutdownHibernateSessionfactory();
		super.destroy();
	}
	
	@Override
	public void reload() {
		shutdownHibernateSessionfactory();
		super.reload();
	}
}
