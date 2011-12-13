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
package com.mercatis.lighthouse3.ui.environment.base.services;


public interface DomainConfiguration {

	/**
	 * @return
	 */
	public abstract String getUsername();

	/**
	 * @return
	 */
	public abstract String getPassword();

	/**
	 * @return
	 */
	public abstract String getUrl();

	/**
	 * @param url
	 */
	public abstract void setUrl(String url);

	/**
	 * @param password
	 */
	public abstract void setPassword(String password);

	/**
	 * @param username
	 */
	public abstract void setUsername(String username);

	/**
	 * @param serverDomainKey
	 */
	public abstract void setServerDomainKey(String serverDomainKey);

	/**
	 * @return
	 */
	public abstract String getServerDomainKey();
}