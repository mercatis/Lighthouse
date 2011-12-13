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
package com.mercatis.lighthouse3.persistence.events.hibernate;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.BlobType;
import org.hibernate.type.BooleanType;
import org.hibernate.type.DateType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.FloatType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import com.mercatis.lighthouse3.commons.commons.Ranger;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.domainmodel.events.aggregation.Aggregation;
import com.mercatis.lighthouse3.domainmodel.events.aggregation.AggregationCommand;
import com.mercatis.lighthouse3.domainmodel.events.aggregation.AggregationIntervalResult;
import com.mercatis.lighthouse3.domainmodel.events.aggregation.Aggregator;
import com.mercatis.lighthouse3.persistence.commons.hibernate.DomainModelEntityDAOImplementation;

/**
 * This class provides a Hibernate-based implementation of the
 * <code>EventRegistry</code> interface.
 */
public class EventRegistryImplementation extends DomainModelEntityDAOImplementation<Event> implements EventRegistry {

	public void log(Event event) {
		this.persist(event);
	}

	public void log(Deployment context, Event event) {
		event.setContext(context);
		this.log(event);
	}

	public void log(DeploymentRegistry deploymentRegistry,
			SoftwareComponentRegistry softwareComponentRegistry,
			String location, String componentCode, Event event) {

		SoftwareComponent softwareComponent = softwareComponentRegistry
				.findByCode(componentCode);
		if (softwareComponent == null)
			throw new PersistenceException(
					"Could not find event issuing software component with code.",
					null);

		Deployment deployment = deploymentRegistry.findByComponentAndLocation(
				softwareComponent, location);
		if (deployment == null)
			throw new PersistenceException(
					"Could not find deployment of event issuing software component at location.",
					null);

		this.log(deployment, event);
	}
	
	@Override
	public boolean alreadyPersisted(Event event) {
		return find(event.getId())!=null;
	}

	@Override
	protected Criteria entityToCriteria(Session session, Event entityTemplate) {
		Criteria criteria = generateOrderingCriteria(session, entityTemplate,
				true);

		return criteria;
	}

	/**
	 * This method generates criteria for a given event template that also
	 * contain an ordering clause on the date of occurrence.
	 * 
	 * @param session
	 *            the Hibernate session to use for criteria generation
	 * @param entityTemplate
	 *            the template for which to generate the criteria
	 * @param descending
	 *            <code>true</code> if descending order is wanted (the default)
	 *            or <code>false</code> for ascending order.
	 * @return
	 */
	public Criteria generateOrderingCriteria(Session session,
			Event entityTemplate, boolean descending) {
		Criteria criteria = super.entityToCriteria(session, entityTemplate);

		if (entityTemplate.getContext() != null) {
			if (!Ranger.isEnumerationRange(entityTemplate.getContext()))
				criteria.add(Restrictions.eq("context", entityTemplate
						.getContext()));
			else
				criteria.add(Restrictions.in("context", Ranger
						.castToEnumerationRange(entityTemplate.getContext())
						.getEnumeration()));

		}

		if (entityTemplate.getCode() != null) {
			if (!Ranger.isEnumerationRange(entityTemplate.getCode()))
				criteria.add(Restrictions.eq("code", entityTemplate.getCode()));
			else
				criteria.add(Restrictions.in("code", Ranger
						.castToEnumerationRange(entityTemplate.getCode())
						.getEnumeration()));

		}

		if (entityTemplate.getLevel() != null) {
			if (!Ranger.isEnumerationRange(entityTemplate.getLevel()))
				criteria.add(Restrictions
						.eq("level", entityTemplate.getLevel()));
			else
				criteria.add(Restrictions.in("level", Ranger
						.castToEnumerationRange(entityTemplate.getLevel())
						.getEnumeration()));
		}

		if (entityTemplate.getMachineOfOrigin() != null)
			criteria.add(Restrictions.eq("machineOfOrigin", entityTemplate
					.getMachineOfOrigin()));

		if (entityTemplate.getMessage() != null) {
			criteria.add(Restrictions.ilike("message", "%"
					+ entityTemplate.getMessage() + "%"));
		}

		if (entityTemplate.getStackTrace() != null) {
			if (this.unitOfWork.getSqlDialect() instanceof org.hibernate.dialect.MySQL5InnoDBDialect)
				criteria.add(Restrictions.sqlRestriction(
						"match ({alias}.STACK_TRACE) against (?)",
						entityTemplate.getStackTrace(), StringType.INSTANCE));
			else
				criteria.add(Restrictions.ilike("stackTrace", "%"
						+ entityTemplate.getStackTrace() + "%"));
		}

		if (entityTemplate.getDateOfOccurrence() != null) {
			if (!Ranger.isIntervalRange(entityTemplate.getDateOfOccurrence())) {
				criteria.add(Restrictions.eq("dateOfOccurrence", entityTemplate
						.getDateOfOccurrence()));
			} else {
				Date lowerBound = Ranger.castToIntervalRange(
						entityTemplate.getDateOfOccurrence()).getLowerBound();
				Date upperBound = Ranger.castToIntervalRange(
						entityTemplate.getDateOfOccurrence()).getUpperBound();

				if ((lowerBound == null) && (upperBound != null))
					criteria.add(Restrictions
							.le("dateOfOccurrence", upperBound));
				else if ((lowerBound != null) && (upperBound == null))
					criteria.add(Restrictions
							.ge("dateOfOccurrence", lowerBound));
				else if ((lowerBound != null) && (upperBound != null)) {
					criteria.add(Restrictions
							.le("dateOfOccurrence", upperBound));
					criteria.add(Restrictions
							.ge("dateOfOccurrence", lowerBound));
				}
			}
		}

		if (!entityTemplate.getTransactionIds().isEmpty()) {
			Set<Criterion> transactionRestrictions = new HashSet<Criterion>();
			for (String transactionId : entityTemplate.getTransactionIds())
				transactionRestrictions
						.add(Restrictions
								.sqlRestriction(
										"exists (select lti.* from EVENT_TRANSACTION_IDS lti where {alias}.EVT_ID = lti.EVT_ID and lti.TRANSACTION_ID = ?)",
										transactionId, StringType.INSTANCE));

			if (transactionRestrictions.size() == 1) {
				criteria.add(transactionRestrictions.iterator().next());
			} else {
				Iterator<Criterion> restrictions = transactionRestrictions
						.iterator();
				Criterion orCriterion = restrictions.next();

				while (restrictions.hasNext()) {
					orCriterion = Restrictions.or(orCriterion, restrictions
							.next());
				}

				criteria.add(orCriterion);
			}
		}

		for (String tag : entityTemplate.getTags())
			criteria
					.add(Restrictions
							.sqlRestriction(
									"exists (select lt.* from EVENT_TAGS lt where {alias}.EVT_ID = lt.EVT_ID and lt.TAG = ?)",
									tag, StringType.INSTANCE));

		for (String udf : entityTemplate.getUdfs().keySet()) {
			Object value = entityTemplate.getUdf(udf);

                        if (udf.equals("eventRESTResourceLimitRestriction")) {
                            criteria.setMaxResults((Integer) value);
                            break;
                        }

			String columnName = "";
			Type valueType = StringType.INSTANCE;

			if (value instanceof Boolean) {
				columnName = "BOOLEAN_VAL";
				valueType = BooleanType.INSTANCE;
			}

			if (value instanceof Integer) {
				columnName = "INTEGER_VAL";
				valueType = IntegerType.INSTANCE;
			}

			if (value instanceof Long) {
				columnName = "LONG_VAL";
				valueType = LongType.INSTANCE;
			}

			if (value instanceof Float) {
				columnName = "FLOAT_VAL";
				valueType = FloatType.INSTANCE;
			}

			if (value instanceof Double) {
				columnName = "DOUBLE_VAL";
				valueType = DoubleType.INSTANCE;
			}

			if (value instanceof Date) {
				columnName = "DATE_VAL";
				valueType = DateType.INSTANCE;
			}

			if (value instanceof byte[]) {
				columnName = "BINARY_VAL";
				valueType = BlobType.INSTANCE;
			}

			if (value instanceof String) {
				columnName = "STRING_VAL";
				valueType = StringType.INSTANCE;
			}

			criteria
					.add(Restrictions
							.sqlRestriction(
									"exists (select lu.* from EVENT_UDFS lu where {alias}.EVT_ID = lu.EVT_ID and lu.UDF = ? and lu."
											+ columnName + " = ?)",
									new Object[] { udf, value }, new Type[] {
											StringType.INSTANCE, valueType }));

		}

		if (descending)
			criteria.addOrder(Order.desc("dateOfOccurrence"));
		else
			criteria.addOrder(Order.asc("dateOfOccurrence"));

		return criteria;
	}

	public Aggregation aggregate(AggregationCommand command) {
		command.setRegistry(this);
		Aggregation result = new Aggregation();
		Aggregator aggregator = command.getAggregator();

		for (AggregationIntervalResult intervalResult : aggregator) {
			result.add(intervalResult);
		}

		return result;
	}

	public Deployment[] getAllDeployments() {
		Session session = this.unitOfWork.getCurrentSession();

		Query query = session
				.createQuery("select distinct event.context from Event event");

		Deployment[] result = new Deployment[query.list().size()];

		for (int x = 0; x < query.list().size(); x++)
			result[x] = (Deployment) query.list().get(x);

		return result;
	}

	public String[] getAllUdfNames() {
		Session session = this.unitOfWork.getCurrentSession();
		session.flush();

		Query query = session
				.createSQLQuery("select distinct UDF from EVENT_UDFS");

		String[] result = new String[query.list().size()];

		for (int x = 0; x < query.list().size(); x++)
			result[x] = (String) query.list().get(x);

		return result;
	}
}
