/**
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
package com.mercatis.lighthouse3.domainmodel.events.aggregation;

import java.util.Iterator;
import java.util.Map;

import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventBuilder;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;

/**
 * The aggregation command is used to describe an aggregation of events.
 */
public class AggregationCommand {

	/**
	 * The aggregation type defines what operation should be used
	 */
	public enum Type {
		AVERAGE, COUNT, MAXIMUM, MINIMUM, SUM
	}

	/**
	 * The group determines in which groups an aggregation should be divided
	 */
	public enum Group {
		EVENTS, CONTEXT, LEVEL, CODE, TRANSACTION_IDS, TAGS
	}

	/**
	 * A set of possible time intervals for an aggregation
	 */
	public enum Interval {
		HOURS, DAYS, WEEKS, MONTHS, QUARTERS, YEARS
	}

	/**
	 * Class constructor specifying the details of the aggregation.
	 * 
	 * @param registry
	 *            the event registry to receive the list of events with
	 * @param type
	 *            the type of aggregation, e.g. count, min, max, ...
	 * @param template
	 *            the template of an <code>Event</code> to use in order to find
	 *            all matching events
	 * @param target
	 *            the target property of the <code>Event</code> to use the
	 *            aggregation on
	 * @param interval
	 *            the time interval of the aggregation, e.g. hours, weeks, ...
	 * @param group
	 *            how the aggregation should be grouped, e.g. to events,
	 *            context, ...
	 */
	public AggregationCommand(EventRegistry registry, Type type, Event template, AggregationTarget target,
			Interval interval, Group group) {
		this.type = type;
		this.template = template;
		this.target = target;
		this.interval = interval;
		this.group = group;
		this.registry = registry;
	}

	/**
	 * Class constructor specifying the details of the aggregation.
	 * 
	 * @param type
	 *            the type of aggregation, e.g. count, min, max, ...
	 * @param template
	 *            the template of an <code>Event</code> to use in order to find
	 *            all matching events
	 * @param target
	 *            the target property of the <code>Event</code> to use the
	 *            aggregation on
	 * @param interval
	 *            the time interval of the aggregation, e.g. hours, weeks, ...
	 * @param group
	 *            how the aggregation should be grouped, e.g. to events,
	 *            context, ...
	 */
	public AggregationCommand(Type type, Event template, AggregationTarget target, Interval interval, Group group) {
		this(null, type, template, target, interval, group);
	}

	/**
	 * Returns the correct <code>Aggregator</code> for this Aggregation. In the
	 * same breath it chooses the correct <code>GroupByGrouper</code> and
	 * <code>IntervalGrouper</code> as well.
	 * 
	 * @return the correct <code>Aggregator</code>
	 */
	public Aggregator getAggregator() {
		Aggregator aggregator = null;
		if (this.type.equals(Type.COUNT)) {
			aggregator = new AggregatorCount();
		} else if (this.type.equals(Type.SUM)) {
			aggregator = new AggregatorSum();
		} else if (this.type.equals(Type.AVERAGE)) {
			aggregator = new AggregatorAverage();
		} else if (this.type.equals(Type.MAXIMUM)) {
			aggregator = new AggregatorMaximum();
		} else if (this.type.equals(Type.MINIMUM)) {
			aggregator = new AggregatorMinimum();
		}

		aggregator.init(this.getGroupByGrouper(), this.target);

		return aggregator;
	}

	/**
	 * Returns the correct <code>GroupByGrouper</code> for this Aggregation. In
	 * the same breath it chooses the correct <code>IntervalGrouper</code> to
	 * instantiate the <code>GroupByGrouper</code> as well.
	 * 
	 * @return the correct <code>GroupByGrouper</code>.
	 */
	private GroupByGrouper getGroupByGrouper() {
		GroupByGrouper groupByGrouper = null;
		if (this.group.equals(Group.EVENTS)) {
			groupByGrouper = new GroupByGrouperEvents();
		} else if (this.group.equals(Group.CODE)) {
			groupByGrouper = new GroupByGrouperCode();
		} else if (this.group.equals(Group.CONTEXT)) {
			groupByGrouper = new GroupByGrouperContext();
		} else if (this.group.equals(Group.LEVEL)) {
			groupByGrouper = new GroupByGrouperLevel();
		} else if (this.group.equals(Group.TAGS)) {
			groupByGrouper = new GroupByGrouperTags();
		} else if (this.group.equals(Group.TRANSACTION_IDS)) {
			groupByGrouper = new GroupByGrouperTransactionIds();
		}

		groupByGrouper.init(this.getIntervalGrouper());

		return groupByGrouper;
	}

	/**
	 * Returns the correct <code>IntervalGrouper</code> for this Aggregation
	 * 
	 * @return the correct <code>IntervalGrouper</code>
	 */
	private IntervalGrouper getIntervalGrouper() {
		IntervalGrouper intervalGrouper = null;
		if (this.interval.equals(Interval.YEARS)) {
			intervalGrouper = new IntervalGrouperYears();
		} else if (this.interval.equals(Interval.QUARTERS)) {
			intervalGrouper = new IntervalGrouperQuarters();
		} else if (this.interval.equals(Interval.MONTHS)) {
			intervalGrouper = new IntervalGrouperMonths();
		} else if (this.interval.equals(Interval.WEEKS)) {
			intervalGrouper = new IntervalGrouperWeeks();
		} else if (this.interval.equals(Interval.DAYS)) {
			intervalGrouper = new IntervalGrouperDays();
		} else if (this.interval.equals(Interval.HOURS)) {
			intervalGrouper = new IntervalGrouperHours();
		}

		intervalGrouper.init(this.getEventIterator());

		return intervalGrouper;
	}

	/**
	 * The <code>EventRegistry</code> to use findByTemplate() with.
	 */
	private EventRegistry registry;

	/**
	 * Sets the EventRegistry for this command.
	 * 
	 * @param registry
	 *            the EventRegistry to be set.
	 */
	public void setRegistry(EventRegistry registry) {
		this.registry = registry;
	}

	/**
	 * Returns the iterator over the list of found events.
	 * 
	 * @return the iterator of the found events
	 */
	public Iterator<Event> getEventIterator() {
		return registry.findByTemplate(template).iterator();
	}

	/**
	 * The type of aggregation, e.g. count, min, max, ...
	 */
	private Type type;

	/**
	 * Returns the type of the aggregation
	 * 
	 * @return the type of the aggregation
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * The template of an <code>Event</code> to use in order to search for
	 * matching events
	 */
	private Event template;

	/**
	 * Returns the template of an <code>Event</code> to use in order to search
	 * for matching events
	 * 
	 * @return the template of an <code>Event</code>
	 */
	public Event getTemplate() {
		return this.template;
	}

	/**
	 * The target property of an <code>Event</code>, on which the aggregation
	 * should be used
	 */
	private AggregationTarget target;

	/**
	 * Returns the target property of an <code>Event</code>, on which the
	 * aggregation should be used
	 * 
	 * @return the target property
	 */
	public AggregationTarget getTarget() {
		return this.target;
	}

	/**
	 * The interval, in which the events should be divided
	 */
	private Interval interval;

	/**
	 * Returns the interval, in which the events should be divided
	 * 
	 * @return the interval of the aggregation
	 */
	public Interval getInterval() {
		return this.interval;
	}

	/**
	 * The group that specifies in which parts the aggregation should be grouped
	 */
	private Group group;

	/**
	 * Returns the group that specifies in which parts the aggregation should be
	 * grouped
	 * 
	 * @return the group
	 */
	public Group getGroup() {
		return this.group;
	}

	/**
	 * Returns the query parameter representation of this instance.
	 * 
	 * @return the query parameters as Map
	 */
	public Map<String, String> toQueryParameters() {
		Map<String, String> map = this.template.toQueryParameters();

		map.put("aType", this.type.toString());
		map.put("aInterval", this.interval.toString());
		map.put("aGroup", this.group.toString());
		map.put("aTarget", this.target.getType().toString());
		map.put("aIdentification", this.target.getIdentification());

		return map;
	}

	/**
	 * Builds up this class by using the information from fitting query
	 * parameters.
	 * 
	 * @param parameters
	 *            the parameters that hold the command info
	 */
	public void fromQueryParameters(Map<String, String> parameters) {
		this.type = Type.valueOf(parameters.get("aType"));
		this.interval = Interval.valueOf(parameters.get("aInterval"));
		this.group = Group.valueOf(parameters.get("aGroup"));
		this.target = new AggregationTarget().setType(AggregationTarget.Type.valueOf(parameters.get("aTarget")))
				.setIdentification(parameters.get("aIdentification"));

		parameters.remove("aType");
		parameters.remove("aInterval");
		parameters.remove("aGroup");
		parameters.remove("aTarget");
		parameters.remove("aIdentification");

		this.template = EventBuilder.template().done();
		this.template.fromQueryParameters(parameters);
	}
}
