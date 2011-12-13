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

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.UnitOfWork;
import com.mercatis.lighthouse3.persistence.commons.DAOProvider;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

/**
 * This abstract base class provides a common service container for RESTful web
 * services representing domain model entities. It more or less implements a
 * singleton pattern and provides methods for retrieving the appropriate
 * registries within the resources of the service.
 * 
 * It must be subclassed and implemented for each persistence mechanism and
 * service.
 */
public abstract class DomainModelEntityRestServiceContainer extends ServletContainer implements DAOProvider {

	private static final long serialVersionUID = -246381922438917872L;

	/**
	 * Keeps a reference to the running service container
	 */
	protected static DomainModelEntityRestServiceContainer instance = null;

	/**
	 * This method returns a reference to the environment service container
	 * running. This is useful for tests but should not be called within
	 * production code since different service containers may interfere with
	 * each other.
	 * 
	 * @return the environment service container.
	 */
	public static DomainModelEntityRestServiceContainer getServiceContainer() {
		return instance;
	}

	public DomainModelEntityRestServiceContainer() {
		super();
		if (log.isDebugEnabled())
			log.debug("Starting service container: " + this.toString());
	}

	/**
	 * This property keeps a logger.
	 */
	protected Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * This method must be implemented for each persistence mechanism to return
	 * an instance of the given domain model DAO interface attached to the
	 * current unit of work.
	 * 
	 * @param registryInterface
	 *            the class of the DAO interface to return
	 * @return the instance of the DAO interface or <code>null</code> in case
	 *         that the instance could not be created
	 */
	public abstract <R extends DomainModelEntityDAO<? extends DomainModelEntity>> R getDAO(Class<R> registryInterface);

	/**
	 * This method must be implemented by subclasses in order to produce the
	 * current unit of work for the present persistence mechanism.
	 * 
	 * @return the current unit of work
	 */
	protected abstract UnitOfWork getCurrentUnitOfWork();

	/*
	 * Overridden to provide fall through behaviour if the filter cannot fulfill
	 * the request. TODO An even better approach would be to check the URL
	 * upfront against the registered ServiceContainers, if we are able to
	 * fulfill the request and if we are, interrupt the filter chain even if we
	 * get a non 200 (OK) response.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.jersey.spi.container.servlet.ServletContainer#doFilter(javax.
	 * servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
	 * javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		String servletPath = request.getServletPath();

		// if we match the static content regular expression lets delegate to
		// the filter chain
		// to use the default container servlets & handlers
		Pattern p = getStaticContentPattern();
		if (p != null && p.matcher(servletPath).matches()) {
			chain.doFilter(request, response);
			return;
		}

		this.service(request, response);
		if (!response.toString().contains("HTTP/1.1 200")) {
			if (log.isDebugEnabled()) {
				log.debug("Unknown Service Response. Delegating to filter chain. Response: " + response.toString());
			}

			try {
				response.reset();
				chain.doFilter(request, response);
			} catch (IllegalStateException ex) {
				log.warn("Response is already committed. Cannot delegate request to filter chain.");
			}
		}
	}

	@Override
	public void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {

		if (log.isDebugEnabled())
			log.debug("Received web service request: " + httpRequest.getMethod() + " " + httpRequest.getRequestURL());

		if (log.isDebugEnabled())
			log.debug("Fetching current unit of work");

		UnitOfWork unitOfWork = this.getCurrentUnitOfWork();

		if (log.isDebugEnabled())
			log.debug("Fetched current unit of work: " + unitOfWork.toString());

		boolean commit = false;

		try {
			if (log.isDebugEnabled()) {
				log.debug("Delegating request to REST resource");
			}

			super.service(httpRequest, httpResponse);
			commit = httpResponse.toString().contains("HTTP/1.1 200");

			if (log.isDebugEnabled()) {
				if (commit) {
					log.debug("Sending web service OK response: " + httpResponse.toString());
				} else {
					log.debug("Sending web service error response: " + httpResponse.toString());
				}
			}
		} catch (ServletException servletException) {
			log.error("Caught servlet exception", servletException);

			throw servletException;
		} catch (IOException ioException) {
			log.error("Caught IO exception", ioException);

			throw ioException;
		} catch (RuntimeException anythingElse) {
			log.error("Caught exception", anythingElse);

			throw anythingElse;
		} finally {
			try {
				if (commit) {
					if (log.isDebugEnabled())
						log.debug("Committing database");

					unitOfWork.commit();
				} else {
					if (log.isDebugEnabled()) 
						log.debug("Rolling back database");

					unitOfWork.rollback();
				}
			} catch (Exception anything) {
				log.error("Database transaction handling failed", anything);
			}
		}
	}

	/**
	 * The <code>@CreatingServiceContainer</code> annotation can be applied
	 * within Jersey resource classes to a property. This property must be an
	 * instance of <code>DomainModelEntityRestServiceContainer</code>. Into this
	 * property, the <code>DomainModelEntityRestServiceContainer</code> that
	 * created the resource will be injected.
	 */
	@Target(value = ElementType.FIELD)
	@Retention(value = RetentionPolicy.RUNTIME)
	@Documented
	public static @interface CreatingServiceContainer {
	}

	/**
	 * This method sets up LH3-specific Jersey annotation extensions.
	 * 
	 * @param rc
	 *            the resource configuration of the Jersey configuration
	 *            routine.
	 */
	protected void setUpJerseyAnnotationExtensions(ResourceConfig rc) {
		final DomainModelEntityRestServiceContainer currentContainer = this;

		rc.getSingletons().add(new InjectableProvider<CreatingServiceContainer, Class<?>>() {

			private List<Class<?>> getSuperClassesOf(Class<?> clazz) {
				List<Class<?>> superClasses = new ArrayList<Class<?>>();
				Class<?> currentClass = clazz;

				while (currentClass != null) {
					superClasses.add(currentClass);
					currentClass = currentClass.getSuperclass();
				}

				return superClasses;
			}

			public ComponentScope getScope() {
				return ComponentScope.Singleton;
			}

			public Injectable<DomainModelEntityRestServiceContainer> getInjectable(ComponentContext componentContext, CreatingServiceContainer annotation,
					Class<?> clazz) {
				if (this.getSuperClassesOf(clazz).contains(DomainModelEntityRestServiceContainer.class)) {
					return new Injectable<DomainModelEntityRestServiceContainer>() {

						public DomainModelEntityRestServiceContainer getValue() {
							return currentContainer;
						}

					};
				} else
					return null;
			}

		});
	}

	protected void setUpLighthouseDomain(Map<String, String> initParams) {
		String lighthouseDomain = initParams.get("com.mercatis.lighthouse3.service.commons.rest.LighthouseDomain");
		if (lighthouseDomain != null)
			DomainModelEntity.lighthouseDomainDefault = lighthouseDomain;
	}

	protected Map<String, String> initParameters;

	@SuppressWarnings("unchecked")
	@Override
	protected final void configure(ServletConfig sc, ResourceConfig rc, WebApplication wa) {
		super.configure(sc, rc, wa);

		if (log.isDebugEnabled()) {
			log.debug("Starting up REST service container with servlet config: ");
		}

		this.initParameters = new HashMap<String, String>();
		Enumeration<String> initParameterNames = sc.getInitParameterNames();
		while (initParameterNames.hasMoreElements()) {
			String initParameterName = initParameterNames.nextElement();
			String initParameterValue = sc.getInitParameter(initParameterName);
			initParameters.put(initParameterName, initParameterValue);
			if (log.isDebugEnabled())
				log.debug(initParameterName + "=" + initParameterValue);
		}

		configure(initParameters, rc, wa);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.jersey.spi.container.servlet.ServletContainer#configure(javax
	 * .servlet.FilterConfig, com.sun.jersey.api.core.ResourceConfig,
	 * com.sun.jersey.spi.container.WebApplication)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected final void configure(FilterConfig fc, ResourceConfig rc, WebApplication wa) {
		super.configure(fc, rc, wa);

		if (log.isDebugEnabled()) {
			log.debug("Starting up REST service container with filter config: ");
		}

		this.initParameters = new HashMap<String, String>();
		Enumeration<String> initParameterNames = fc.getInitParameterNames();
		while (initParameterNames.hasMoreElements()) {
			String initParameterName = initParameterNames.nextElement();
			String initParameterValue = fc.getInitParameter(initParameterName);
			initParameters.put(initParameterName, initParameterValue);
			if (log.isDebugEnabled())
				log.debug(initParameterName + "=" + initParameterValue);
		}

		configure(initParameters, rc, wa);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#getInitParameter(java.lang.String)
	 */
	@Override
	public String getInitParameter(String name) {
		if (this.initParameters == null)
			return super.getInitParameter(name);

		return this.initParameters.get(name);
	}

	protected void configure(Map<String, String> initParams, ResourceConfig rc, WebApplication wa) {
		this.setUpLighthouseDomain(initParams);
		this.setUpJerseyAnnotationExtensions(rc);

		instance = this;
	}
}
