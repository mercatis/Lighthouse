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
package com.mercatis.lighthouse3.domainmodel.status;

import java.util.List;
import java.util.Map;

import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;

/**
 * This interface provides DAO / registry services for status. Status are loaded
 * with all their data and with the status carrier forming their context.
 */
public interface StatusRegistry extends CodedDomainModelEntityDAO<Status> {

	/**
	 * This method returns the status with the given code. The status change
	 * history will be paginated, however. For pagination, you specify the page
	 * size in terms of status changes and the number of the page you are
	 * interested in.
	 * 
	 * Note that the latest status change (i.e., the first status change in the
	 * history) is always returned. The pages are counted from this latest
	 * change on. I.e., requesting the x-th page of size ten always returns 11
	 * status changes in the history, with the latest change being the first in
	 * the history.
	 * 
	 * @param code
	 *            the code of the status to retrieve.
	 * @param pageSize
	 *            the granularity of pagination
	 * @param pageNo
	 *            the page to return, counting from 1
	 * @return the status with the paginated history. <code>null</code> in case
	 *         a status with the given code does not exist.
	 */
	public Status findByCode(String code, int pageSize, int pageNo);

	/**
	 * This method returns all status which are attached to a given carrier,
	 * i.e., a deployment, an environment, or a process task.
	 * 
	 * @param carrier
	 *            the carrier
	 * @return the status for the carrier.
	 */
	public List<Status> getStatusForCarrier(StatusCarrier carrier);

	/**
	 * This method returns all status which are attached to a given carrier,
	 * i.e., a deployment, an environment, or a process task.
	 * 
	 * The change histories of those status will be paginated, however. For
	 * pagination, you specify the page size in terms of status changes and the
	 * number of the page you are interested in.
	 * 
	 * Note that the latest status change (i.e., the first status change in the
	 * history) is always returned. The pages are counted from this latest
	 * change on. I.e., requesting the x-th page of size ten always returns 11
	 * status changes in the history, with the latest change being the first in
	 * the history.
	 * 
	 * @param carrier
	 *            the carrier
	 * @param pageSize
	 *            the granularity of pagination
	 * @param pageNo
	 *            the page to return, counting from 1
	 * @return the status for the carrier.
	 */
	public List<Status> getStatusForCarrier(StatusCarrier carrier, int pageSize, int pageNo);

	/**
	 * This method manually clears a status with a given code. I.e., it inserts
	 * a manual status clearance into the history.
	 * 
	 * @param code
	 *            the code of the status to clear
	 * @param clearer
	 *            the clearer who performed the clearance
	 * @param reason
	 *            the reason associated with the clearance.
	 * @throws PersistenceException
	 *             in case a status with the given code does not exist.
	 */
	public void clearStatusManually(String code, String clearer, String reason);

	/**
	 * This method looks up all status attached to the given carrier and its sub
	 * carriers. The current status of those are counted up into a histogram.
         * <br />Attached deployments are not considered.
	 * 
	 * @param carrier
	 *            the carrier for which to calculate the current status.
	 * @return the aggregated current status.
	 */
	public StatusHistogram getAggregatedCurrentStatusForCarrier(StatusCarrier carrier);

	/**
	 * This method looks up all status attached to the given carrier and its sub
	 * carriers. The current status of those are counted up into a histogram.
	 *
	 * @param carrier
	 *            the carrier for which to calculate the current status.
         * @param withDeployments true if you want to consider attached deployments during aggregation
	 * @return the aggregated current status.
	 */
	public StatusHistogram getAggregatedCurrentStatusForCarrier(StatusCarrier carrier, boolean withDeployments);

	/**
	 * This method looks up all status attached to the given carrierClass and its sub
	 * carriers. The current status of those are counted up into a histogram.
         * <br />Attached deployments are not considered.
	 *
	 * @param carrierClass
	 *            the carrierClass for which to calculate the current statuses.
	 * @return the aggregated current status mapped by code of the entities found for the given carrierClass.
	 */
	public <T extends StatusCarrier> Map<String, StatusHistogram> getAggregatedCurrentStatusesForCarrierClass(Class<T> carrierClass);

	/**
	 * This method looks up all status attached to the given carrierClass and its sub
	 * carriers. The current status of those are counted up into a histogram.
	 *
	 * @param carrierClass
	 *            the carrierClass for which to calculate the current statuses.
         * @param withDeployments true if you want to consider attached deployments during aggregation
	 * @return the aggregated current status mapped by code of the entities found for the given carrierClass.
	 */
	public <T extends StatusCarrier> Map<String, StatusHistogram> getAggregatedCurrentStatusesForCarrierClass(Class<T> carrierClass, boolean withDeployments);
}
