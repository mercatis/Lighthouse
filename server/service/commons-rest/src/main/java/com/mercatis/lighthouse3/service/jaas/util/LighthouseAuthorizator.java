package com.mercatis.lighthouse3.service.jaas.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignment;
import com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignmentRegistry;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.domainmodel.users.UserRegistry;
import com.mercatis.lighthouse3.persistence.commons.DAOProvider;

public class LighthouseAuthorizator {
	private LighthouseAuthorizator() {};
	
	private static UserRegistry userReg;
	private static ContextRoleAssignmentRegistry contextRoleReg;
	private static long lastWarning = 0L;
	
	private static Logger log = LoggerFactory.getLogger(LighthouseAuthorizator.class);

	public static boolean allowAccess(DAOProvider prov, String username, String role, StringBuilder context) {
		return !denyAccess(prov, username, role, context==null ? null : context.toString());
	}
	
	public static boolean allowAccess(DAOProvider prov, String username, String role, String context) {
		return !denyAccess(prov, username, role, context);
	}
	
	public static boolean denyAccess(DAOProvider prov, String username, String role, StringBuilder context) {
		return denyAccess(prov, username, role, context==null ? null : context.toString());
	}
	
	public static boolean denyAccess(DAOProvider prov, String username, String role, String context) {
		if (username==null) {
			long ts = System.currentTimeMillis();
			// 10800000 = 3h
			if (ts-lastWarning > 10800000) {
				log.warn("authorization bypassed, maybe authentication is disabled (username is null)");
				lastWarning = ts;
			}
			return false;
		}
		if (context==null) {
			log.warn("access denied for user '"+username+"' accessing resource '"+role+"' because operation is null");
			return true;
		}
		if (role==null) {
			log.info("access allowed for user '"+username+"' because role is null");
			return false;
		}

		if (userReg==null)
			userReg = prov.getDAO(UserRegistry.class);
		if (contextRoleReg==null)
			contextRoleReg = prov.getDAO(ContextRoleAssignmentRegistry.class);

		User user;

		user = userReg.findByCode(username);
		if (user==null)
			throw new PersistenceException("user not found", null);

		List <ContextRoleAssignment> creds = contextRoleReg.findAllFor(user);
		for (ContextRoleAssignment cra : creds) {
			if (cra.getRole().equals(role) && context.startsWith(cra.getContext()))
				return false;
		}

		log.warn("access denied for user '"+username+"' accessing resource '"+role+"' with operation "+context);
		return true;
	}
}
