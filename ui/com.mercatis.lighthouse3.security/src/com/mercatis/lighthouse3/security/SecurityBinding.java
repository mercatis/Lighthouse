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


public interface SecurityBinding {

	/**
	 * @return the name
	 */
	public abstract String getName(String context);

	/**
	 * @return the password
	 */
	public abstract char[] getPassword(String context);
	
	/**
	 * @param context
	 * @param key
	 * @return
	 */
	public abstract Object getProperty(String context, String key);

	/**
	 * @param context
	 * @param role
	 * @return
	 */
	public abstract boolean hasRole(String context, String role);
	
	
	/**
	 * @param context
	 */
	public abstract void removeContext(String context);

}