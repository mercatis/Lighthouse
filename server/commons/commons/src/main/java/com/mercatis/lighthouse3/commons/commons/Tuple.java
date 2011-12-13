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

/**
 * This class models tuples (i.e., pairs) of data. This is mainly a vehicle to
 * circumvent Hibernate's restriction to map sets of sets of values.
 */
public class Tuple<A, B> {

	/**
	 * This property captures the first element of the tuple.
	 */
	private A a = null;

	/**
	 * This method returns the first element of the tuple.
	 * 
	 * @return the first element of the tuple.
	 */
	public A getA() {
		return this.a;
	}

	/**
	 * This sets the first element of the tuple.
	 * 
	 * @param a
	 *            the new first element of the tuple.
	 */
	public void setA(A a) {
		this.a = a;
	}

	/**
	 * This property captures the second element of the tuple.
	 */
	private B b = null;

	/**
	 * This method returns the second element of the tuple.
	 * 
	 * @return the second element of the tuple.
	 */
	public B getB() {
		return this.b;
	}

	/**
	 * This sets the second element of the tuple.
	 * 
	 * @param a
	 *            the new second element of the tuple.
	 */
	public void setB(B b) {
		this.b = b;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Tuple))
			return false;

		Tuple<?,?> that = (Tuple<?,?>) obj;

		if (this == that)
			return true;

		if (this.getA() == null && that.getA() != null)
			return false;

		if (this.getA() != null && !this.getA().equals(that.getA()))
			return false;

		if (this.getB() == null && that.getB() != null)
			return false;

		if (this.getB() != null && !this.getB().equals(that.getB()))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int hash = 0;

		if (this.getA() != null)
			hash += this.getA().hashCode();

		if (this.getB() != null)
			hash += 11 * this.getB().hashCode();

		return hash;
	}

	/**
	 * The default constructor for tuples.
	 */
	public Tuple() {
	}

	/**
	 * 
	 * The value constructor for tuples. Immediately assigns elements.
	 * 
	 * @param a
	 *            the first element of the constructed tuple.
	 * @param b
	 *            the second element of the constructed tuple.
	 */
	public Tuple(A a, B b) {
		this.setA(a);
		this.setB(b);
	}

}
