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
package com.mercatis.lighthouse3.service.users.rest;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityRestServiceContainer;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityRestServiceContainer.CreatingServiceContainer;


@Path("/Version")
public class VersionResource {
	@CreatingServiceContainer
	protected DomainModelEntityRestServiceContainer creatingServiceContainer = null;
	
	static public String lighthouseDomainDefault = null;
	
	transient private String lighthouseDomain = null;
	
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
    /**
     * Authenticate an user with given password.
     * <br />Usage <code>/User/Authenticate</code>.
     * User code and password are expected as
     * query parameters <code>code, password</code>.
     *
     * @return a 500 code in case of an internal problem, a
     *         200 code otherwise. The body of the response contains the XML
     *         representation of the authenticated user, on authentication failure a 401 code.
     */
    @GET
    @Produces("text/plain")
    public Response getVersion(@Context UriInfo ui) {
        final String tVersion = VersionResource.class.getPackage().getImplementationVersion();
        return Response.status(Status.OK).entity(tVersion).build();
    }
    
	@GET
	@Path("/LighthouseDomain")
	@Produces("text/plain")
	public Response getLighthouseDomain(@Context HttpServletRequest servletRequest) {
		String lighthouseDomain = getLighthouseDomain();

		if (log.isDebugEnabled())
			log.debug("Returned lighthouse domain " + lighthouseDomain);

		return Response.status(Status.OK).entity(lighthouseDomain).build();
	}
    
	public String getLighthouseDomain() {
		if (lighthouseDomain==null)
			lighthouseDomain = creatingServiceContainer.getInitParameter("com.mercatis.lighthouse3.service.commons.rest.LighthouseDomain");
		
		if (lighthouseDomain==null && lighthouseDomainDefault!=null)
			lighthouseDomain = lighthouseDomainDefault;

		if (lighthouseDomain==null) {
			try {
				Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
	
				while (netInterfaces.hasMoreElements() && (lighthouseDomain == null)) {
					NetworkInterface netInterface = netInterfaces.nextElement();
	
					Enumeration<InetAddress> inetAddresses = netInterface.getInetAddresses();
	
					while (inetAddresses.hasMoreElements() && (lighthouseDomain == null)) {
						InetAddress inetAddress = inetAddresses.nextElement();
	
						if (!inetAddress.getHostAddress().startsWith("127")) {
							lighthouseDomain = inetAddress.getHostAddress();
							lighthouseDomainDefault = lighthouseDomain;
						}
					}
				}
			} catch (SocketException e) {
			}
		}
		
		return lighthouseDomain;
	}
    
}