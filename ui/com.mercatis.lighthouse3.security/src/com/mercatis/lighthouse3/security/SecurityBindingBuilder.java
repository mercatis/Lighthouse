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
package com.mercatis.lighthouse3.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mercatis.lighthouse3.security.internal.Context;
import com.mercatis.lighthouse3.security.internal.SecurityBindingImpl;


public class SecurityBindingBuilder {

	private Map<Context,Set<String>> roles;
	
	private Map<Context,String> usernames;
	
	private Map<Context,char[]> passwords;
	
	private Map<Context, Map<String, Object>> properties;
	
	/**
	 * 
	 */
	public SecurityBindingBuilder() {
		this.roles = new HashMap<Context,Set<String>>();
		this.usernames = new HashMap<Context,String>();
		this.passwords = new HashMap<Context,char[]>();
		this.properties = new HashMap<Context, Map<String,Object>>();
	}
	
	/**
	 * @param context
	 * @param role
	 * @return
	 */
	private SecurityBindingBuilder role(Context context, String role) {
		Set<String> rolesForContext = this.roles.get(context);
		if (rolesForContext == null) {
			rolesForContext = new HashSet<String>();
			this.roles.put(context, rolesForContext);
		}
		rolesForContext.add(role);
		
		return this;
	}
	
	/**
	 * @param context
	 * @param role
	 * @return
	 */
	public SecurityBindingBuilder role(String context, String role) {
		Context ctx = new Context(context);
		this.role(ctx, role);
		
		return this;
	}
	
	public SecurityBindingBuilder username(String context, String name) {
		Context ctx = new Context(context);
		this.usernames.put(ctx, name);
		
		return this;
	}
	
	public SecurityBindingBuilder password(String context, char[] password) {
		Context ctx = new Context(context);
		if (password == null) {
			password = new char[0];
		}
		
		this.passwords.put(ctx, password.clone());
		
		return this;
	}
	
	private SecurityBindingBuilder property(Context context, String key, Object value) {
		Map<String, Object> propertiesForContext = this.properties.get(context);
		if (propertiesForContext == null) {
			propertiesForContext = new HashMap<String, Object>();
			this.properties.put(context, propertiesForContext);
		}
		propertiesForContext.put(key, value);
		
		return this;
	}
	
	public SecurityBindingBuilder property(String context, String key, Object value) {
		return property(new Context(context), key, value);
	}
	
	/**
	 * @return
	 */
	public SecurityBinding build() {
		SecurityBinding credential = new SecurityBindingImpl(this.roles, this.usernames, this.passwords, this.properties);
		this.roles = null;
		this.usernames = null;
		this.passwords = null;
		this.properties = null;
		
		return credential;
	}
}
