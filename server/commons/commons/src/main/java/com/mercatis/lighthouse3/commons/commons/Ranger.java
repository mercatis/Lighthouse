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
package com.mercatis.lighthouse3.commons.commons;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * This is a helper class allowing one to dynamically generate
 * classes marrying almost arbitrary Java value classes with the
 * <code>Range</code> interface.
 * 
 * This is useful when one wants to specify value range queries in query by
 * example templates in addition to point queries.
 * 
 * Sadly, this only works for non-final value classes. For the following final
 * classes, special treatment is provided:
 * 
 * <ul>
 * <li><code>java.lang.String</code>
 * </ul>
 */
public class Ranger {

	/**
	 * This predicate checks if the given object is an enumeration range.
	 * 
	 * @param o
	 *            the object to test
	 * @return <code>true</code> iff the given object is an enumeration range
	 */
	static public boolean isEnumerationRange(Object o) {
		if (o instanceof EnumerationRange)
			return true;
		if (o instanceof String)
			return ((String) o).contains(XmlMuncher.VALUE_ENUMERATION_SEPARATOR);
		return false;
	}

	/**
	 * This method casts a given object to an enumeration range.
	 * 
	 * @param o
	 *            the object to cast
	 * @return the casted object
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public <V> EnumerationRange<V> castToEnumerationRange(V o) {
		if (o instanceof EnumerationRange)
			return (EnumerationRange<V>) o;

		if (String.class.isInstance(o)) {
			List<String> enumerationElements = XmlMuncher.getValueEnumerationElements((String) o);
			return (EnumerationRange<V>) new EnumerationRangeImplementation<String>(new HashSet(enumerationElements));
		}

		return null;
	}

	/**
	 * This predicate checks if the given object is an interval range.
	 * 
	 * @param o
	 *            the object to test
	 * @return <code>true</code> iff the given object is an interval range
	 */
	static public boolean isIntervalRange(Object o) {
		if (o instanceof IntervalRange)
			return true;
		if (o instanceof String)
			return ((String) o).contains(XmlMuncher.VALUE_INTERVAL_SEPARATOR);
		return false;
	}

	/**
	 * This method casts a given object to an interval range.
	 * 
	 * @param o
	 *            the object to cast
	 * @return the casted object
	 */
	@SuppressWarnings("unchecked")
	static public <V extends Comparable<V>> IntervalRange<V> castToIntervalRange(V o) {
		if (o instanceof IntervalRange)
			return (IntervalRange<V>) o;

		if (String.class.isInstance(o)) {
			Object workAround = o;
			String s = (String) workAround;
			List<String> intervalBounds = XmlMuncher.getValueIntervalElements(s);

			if (intervalBounds.size() == 2) {
				workAround = new IntervalRangeImplementation<String>(intervalBounds.get(0), intervalBounds.get(1));
				return (IntervalRange<V>) workAround;
			} else if (s.startsWith(XmlMuncher.VALUE_INTERVAL_SEPARATOR)) {
				workAround = new IntervalRangeImplementation<String>(null, intervalBounds.get(0));
				return (IntervalRange<V>) workAround;
			} else if (s.endsWith(XmlMuncher.VALUE_INTERVAL_SEPARATOR)) {
				workAround = new IntervalRangeImplementation<String>(intervalBounds.get(0), null);
				return (IntervalRange<V>) workAround;
			}
		}

		return null;
	}

	/**
	 * This method creates an enumeration range from a set of values
	 * 
	 * @param values
	 *            the set of values making up the enumeration
	 * @return the enumeration range
	 */
	@SuppressWarnings("unchecked")
	static public <V> V enumeration(final Set<V> values) {
		V result = null;

		if (values.isEmpty())
			return result;

		if (values.iterator().next() instanceof String)
			result = (V) new EnumerationRangeImplementation<V>(values).asString();
		else
			result = createDynamicEnumerationProxy(values);

		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <V> V createDynamicEnumerationProxy(final Set<V> values) {
		Class valueClass = (Class) values.iterator().next().getClass();
		Set<Class> subClassInterfaces = new HashSet<Class>();
		subClassInterfaces.addAll((Collection<? extends Class>) Arrays.asList(valueClass.getInterfaces()));
		subClassInterfaces.add((Class) EnumerationRange.class);

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(valueClass);
		enhancer.setInterfaces(subClassInterfaces.toArray(new Class[0]));

		enhancer.setCallback(new MethodInterceptor() {

			private EnumerationRange<V> range = new EnumerationRangeImplementation<V>(values);

			public Object intercept(Object proxy, Method method, Object[] arguments, MethodProxy methodProxy)
					throws Throwable {

				if ("contains".equals(method.getName()))
					return this.range.contains((V) arguments[0]);
				else if ("overlaps".equals(method.getName()))
					return this.range.overlaps((EnumerationRange<V>) arguments[0]);
				else if ("getEnumeration".equals(method.getName()))
					return this.range.getEnumeration();
				else if ("asString".equals(method.getName()))
					return this.range.asString();
				else
					return methodProxy.invokeSuper(proxy, arguments);
			}
		});

		V result = (V) enhancer.create();
		return result;
	}

	/**
	 * This method creates an interval range from a start and end value of type
	 * Comparable.
	 * 
	 * @param from
	 *            the begin of the interval
	 * 
	 * @param to
	 *            the end of the interval
	 * 
	 * @return the interval range
	 */
	@SuppressWarnings("unchecked")
	static public <V extends Comparable<V>> V interval(final V from, final V to) {
		V result = null;

		if (String.class.isInstance(from) || String.class.isInstance(to)) {
			Object workAround = new IntervalRangeImplementation<V>(from, to).asString();
			result = (V) workAround;
		} else
			result = createDynamicIntervalProxy(from, to);

		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <V extends Comparable<V>> V createDynamicIntervalProxy(final V from, final V to) {
		Set<Class> subClassInterfaces = new HashSet<Class>();
		Class baseClass = null;

		if (from != null)
			baseClass = from.getClass();
		else if (to != null)
			baseClass = to.getClass();
		else
			return null;

		subClassInterfaces.addAll(Arrays.asList(baseClass.getInterfaces()));
		subClassInterfaces.add(IntervalRange.class);

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(baseClass);
		enhancer.setInterfaces(subClassInterfaces.toArray(new Class[0]));

		enhancer.setCallback(new MethodInterceptor() {

			private IntervalRange<V> range = new IntervalRangeImplementation<V>(from, to);

			public Object intercept(Object proxy, Method method, Object[] arguments, MethodProxy methodProxy)
					throws Throwable {

				if ("contains".equals(method.getName()))
					return this.range.contains((V) arguments[0]);
				else if ("overlaps".equals(method.getName()))
					return this.range.overlaps((IntervalRange<V>) arguments[0]);
				else if ("getLowerBound".equals(method.getName()))
					return this.range.getLowerBound();
				else if ("getUpperBound".equals(method.getName()))
					return this.range.getUpperBound();
				else if ("asString".equals(method.getName()))
					return this.range.asString();
				else
					return methodProxy.invokeSuper(proxy, arguments);
			}
		});

		V result = (V) enhancer.create();
		return result;
	}
}
