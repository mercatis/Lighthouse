package com.mercatis.lighthouse3.service.jaas;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.security.Credential;
import org.eclipse.jetty.plus.jaas.spi.AbstractLoginModule;
import org.eclipse.jetty.plus.jaas.spi.UserInfo;

import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.domainmodel.users.UserRegistry;
import com.mercatis.lighthouse3.service.jaas.util.RegistryResolver;


/**
 * PropertyFileLoginModule
 *
 *
 */
public class LighthouseLoginModule extends AbstractLoginModule {
 
	private static String realm = "LighthouseAuthenticated";
	private Logger log = Logger.getLogger(LighthouseLoginModule.class);
	
	private static UserRegistry userRegistry; 
	
	public LighthouseLoginModule() {
		if (userRegistry==null) {
			userRegistry = new RegistryResolver<UserRegistry>("com.mercatis.lighthouse3.persistence.users.hibernate.UserRegistryImplementation").resolve();
		}
	}
	
	@Override
	public UserInfo getUserInfo(String username) throws Exception {
		if (userRegistry==null) {
			log.fatal("user registry not available");
			throw new PersistenceException("user registry not available");
		}
		User user = userRegistry.findByCode(username);
		if (user==null) {
			log.warn("failed authentication attempt for unknown user '"+username+"'");
			return null;
		}
		if (log.isDebugEnabled())
			log.debug("trying to authenticate user "+username);
		List<String> roles = new ArrayList<String>(1);
		roles.add(realm);
		Credential c = new LighthouseCredential(user.getPassword());
		return new UserInfo(username, c, roles);
	}
}
