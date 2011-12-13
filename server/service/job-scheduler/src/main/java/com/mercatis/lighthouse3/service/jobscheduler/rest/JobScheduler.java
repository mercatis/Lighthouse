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
package com.mercatis.lighthouse3.service.jobscheduler.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * This is a REST resource capturing the job monitor service itself. So far,
 * this resource does nothing. It is merely needed for bootstrapping the job
 * scheduler service.
 */
@Path("/JobScheduler")
public class JobScheduler {
	/**
	 * This method provides a dummy OK response to a GET request.
	 * 
	 * @return
	 */
	@GET
	@Consumes("application/xml")
	@Produces("text/plain")
	public Response get() {
		return Response.status(Status.OK).build();
	}
}
