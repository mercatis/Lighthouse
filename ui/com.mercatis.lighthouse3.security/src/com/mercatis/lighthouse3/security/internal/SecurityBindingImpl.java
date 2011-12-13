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
package com.mercatis.lighthouse3.security.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mercatis.lighthouse3.security.SecurityBinding;

public class SecurityBindingImpl implements SecurityBinding {

	private final Map<Context, Set<String>> roles;

	private final Map<Context, String> names;

	private final Map<Context, char[]> passwords;
	
	private final Map<Context, Map<String, Object>> properties;
	
	/**
	 * @param roles
	 */
	public SecurityBindingImpl(Map<Context,Set<String>> roles) {
		this.roles = new HashMap<Context,Set<String>>(roles);
		this.names = new HashMap<Context,String>();
		this.passwords = new HashMap<Context,char[]>();
		this.properties = new HashMap<Context, Map<String, Object>>();
	}
	
	public SecurityBindingImpl(Map<Context,Set<String>> roles, Map<Context, String> names, Map<Context, char[]> passwords, Map<Context, Map<String, Object>> properties) {
		this.roles = new HashMap<Context,Set<String>>(roles);
		this.names = new HashMap<Context, String>(names);
		this.properties = new HashMap<Context, Map<String, Object>>(properties);
		
		// deep copy passwords
		this.passwords = new HashMap<Context, char[]>(passwords.size());
		for (Map.Entry<Context, char[]> entry : passwords.entrySet()) {
			this.passwords.put(entry.getKey(), entry.getValue().clone());
		}
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.internal.ISecurityBinding#getName(com.mercatis.lighthouse3.security.internal.Context)
	 */
	public String getName(Context context) {
		do {
			String name = names.get(context);
			if (name != null)
				return name;
			context = context.getParentContext();
		} while (context != null);
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.SecurityBinding#getName(java.lang.String)
	 */
	public String getName(String context) {
		return getName(new Context(context));
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.internal.ISecurityBinding#getPassword(com.mercatis.lighthouse3.security.internal.Context)
	 */
	public char[] getPassword(Context context) {
		do {
			char[] password = passwords.get(context);
			if (password != null)
				return password.clone();
			context = context.getParentContext();
		} while (context != null);
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.SecurityBinding#getPassword(java.lang.String)
	 */
	public char[] getPassword(String context) {
		return getPassword(new Context(context));
	}
	
	/**
	 * @param context
	 * @param role
	 * @return
	 */
	public boolean hasRole(Context context, String role) {
		do {
			Set<String> rolesForContext = roles.get(context);
			if (rolesForContext != null && rolesForContext.contains(role))
				return true;
			context = context.getParentContext();
		} while (context != null);
		return false;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.internal.ISecurityBinding#hasRole(java.lang.String, java.lang.String)
	 */
	public boolean hasRole(String context, String role) {
		Context ctx = new Context(context);
		Context ctx2 = new Context("//LH3/" + context); //TODO dirty things here...testing only
		return hasRole(ctx, role) || hasRole(ctx2, role);
	}

	/**
	 * @param ctx
	 * @return
	 */
	public SecurityBinding merge(SecurityBindingImpl ctx) {
		SecurityBindingImpl merged = new SecurityBindingImpl(this.roles, this.names, this.passwords, this.properties);
		
		if (ctx != null) {
			merged.roles.putAll(ctx.roles);
			merged.names.putAll(ctx.names);
			merged.passwords.putAll(ctx.passwords);
			merged.properties.putAll(ctx.properties);
		}
		
		return merged;
	}
	
	/**
	 * @param context
	 * @param key
	 * @return
	 */
	public Object getProperty(Context context, String key) {
		do {
			Map<String, Object> propertiesForContext = properties.get(context);
			if (propertiesForContext != null && propertiesForContext.containsKey(key))
				return propertiesForContext.get(key);
			context = context.getParentContext();
		} while (context != null);
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.SecurityBinding#getProperty(java.lang.String, java.lang.String)
	 */
	public Object getProperty(String context, String key) {
		return getProperty(new Context(context), key);
	}
	
	public void removeContext(Context context) {
		List<Context> contextKeysToRemove = new ArrayList<Context>();
		
		// names
		for (Context contextKey : names.keySet()) {
			if (context.isParentContext(contextKey))
				contextKeysToRemove.add(contextKey);
		}
		for (Context key : contextKeysToRemove) {
			names.remove(key);
		}
		contextKeysToRemove.clear();
		
		// passwords
		for (Context contextKey : passwords.keySet()) {
			if (context.isParentContext(contextKey))
				contextKeysToRemove.add(contextKey);
		}
		for (Context key : contextKeysToRemove) {
			passwords.remove(key);
		}
		contextKeysToRemove.clear();
		
		// properties
		for (Context contextKey : properties.keySet()) {
			if (context.isParentContext(contextKey))
				contextKeysToRemove.add(contextKey);
		}
		for (Context key : contextKeysToRemove) {
			properties.remove(key);
		}
		contextKeysToRemove.clear();
		
		// roles
		for (Context contextKey : roles.keySet()) {
			if (context.isParentContext(contextKey))
				contextKeysToRemove.add(contextKey);
		}
		for (Context key : contextKeysToRemove) {
			roles.remove(key);
		}
		contextKeysToRemove.clear();
		System.out.println(String.format("After: properties: %s, roles: %s", properties, roles));
	}
	
	public void removeContext(String context) {
		removeContext(new Context(context));
	}
	
}
