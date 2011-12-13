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
package com.mercatis.lighthouse3.persistence.commons.hibernate;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.BlobType;
import org.hibernate.type.BooleanType;
import org.hibernate.type.ClobType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.FloatType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;

/**
 * This class provides an immutable composite Hibernate custom type for the
 * polymorphic mapping of <code>Object</code> properties that may be of any of
 * the following simple value classes:
 * 
 * <ul>
 * <li><code>Boolean</code>
 * <li><code>Integer</code>
 * <li><code>Long</code>
 * <li><code>Float</code>
 * <li><code>Double</code>
 * <li><code>Date</code>
 * <li><code>true</code><code>byte[]</code>
 * <li><code>String</code>
 * <li><code>true</code><code>byte[]</code>
 * </ul>
 * 
 * The property names of the composite custom type corresponding to these
 * possible instantiations are:
 * 
 * <ul>
 * <li><code>booleanVal</code>
 * <li><code>integerVal</code>
 * <li><code>longVal</code>
 * <li><code>floatVal</code>
 * <li><code>doubleVal</code>
 * <li><code>dateVal</code>
 * <li><code>binaryVal</code>
 * <li><code>stringVal</code>
 * <li><code>clobVal</code>
 * 
 * This custom type generally is useful for mapping UDF-like property structures
 * within domain classes.
 */
public class ValueObjectUserType implements CompositeUserType {

	public Class<Object> returnedClass() {
		return Object.class;
	}

	public boolean isMutable() {
		return false;
	}

	public String[] getPropertyNames() {
		return new String[] { "booleanVal", "integerVal", "longVal", "floatVal", "doubleVal", "dateVal", "binaryVal", "stringVal", "clobVal" };
	}

	public Type[] getPropertyTypes() {
		return new Type[] { BooleanType.INSTANCE, IntegerType.INSTANCE, LongType.INSTANCE, FloatType.INSTANCE, DoubleType.INSTANCE, TimestampType.INSTANCE,
				BlobType.INSTANCE, StringType.INSTANCE, ClobType.INSTANCE };
	}

	public Object getPropertyValue(Object component, int property) throws HibernateException {
		if ((property == 0) && (component instanceof Boolean))
			return component;

		if ((property == 1) && (component instanceof Integer))
			return component;

		if ((property == 2) && (component instanceof Long))
			return component;

		if ((property == 3) && (component instanceof Float))
			return component;

		if ((property == 4) && (component instanceof Double))
			return component;

		if ((property == 5) && (component instanceof Date))
			return component;

		if ((property == 6) && (component instanceof byte[]))
			return component;

		if ((property == 7) && (component instanceof String))
			return component;

		if ((property == 8) && (component instanceof char[]))
			return component;

		return null;
	}

	public void setPropertyValue(Object component, int property, Object value) throws HibernateException {
		throw new UnsupportedOperationException("Object values are immutable.");
	}

	public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {

		Object result = null;

		result = resultSet.getBoolean(names[0]);
		if (!resultSet.wasNull())
			return result;

		result = resultSet.getInt(names[1]);
		if (!resultSet.wasNull())
			return result;

		result = resultSet.getLong(names[2]);
		if (!resultSet.wasNull())
			return result;

		result = resultSet.getFloat(names[3]);
		if (!resultSet.wasNull())
			return result;

		result = resultSet.getDouble(names[4]);
		if (!resultSet.wasNull())
			return result;

		result = resultSet.getDate(names[5]);
		if (!resultSet.wasNull())
			return result;

		Blob blob = resultSet.getBlob(names[6]);
		if (!resultSet.wasNull()) {
			result = blob.getBytes(1, (int) blob.length());
			return result;
		}

		result = resultSet.getString(names[7]);
		if (!resultSet.wasNull())
			return result;

		Clob clob = resultSet.getClob(names[8]);
		if (!resultSet.wasNull()) {
			char[] buffer = new char[(int) clob.length()];
			try {
				clob.getCharacterStream().read(buffer);
				result = buffer;
			} catch (IOException ex) {
				Logger.getLogger(ValueObjectUserType.class.getName()).log(Level.WARN, null, ex);
			}
			return result;
		}

		return result;
	}

	public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
		Dialect d = session.getFactory().getDialect();
		boolean isSqlServer = d instanceof org.hibernate.dialect.SQLServerDialect || d instanceof org.hibernate.dialect.SQLServer2008Dialect || d instanceof org.hibernate.dialect.SQLServer2008Dialect;
		@SuppressWarnings("deprecation")
		boolean isOracle = d instanceof org.hibernate.dialect.OracleDialect || d instanceof org.hibernate.dialect.Oracle8iDialect ||  d instanceof org.hibernate.dialect.Oracle9Dialect ||  d instanceof org.hibernate.dialect.Oracle9iDialect ||  d instanceof org.hibernate.dialect.Oracle10gDialect;

		if (value instanceof Boolean)
			statement.setBoolean(index, (Boolean) value);
		else
			statement.setNull(index, BooleanType.INSTANCE.sqlType());

		if (value instanceof Integer)
			statement.setInt(index + 1, (Integer) value);
		else
			statement.setNull(index + 1, IntegerType.INSTANCE.sqlType());

		if (value instanceof Long)
			statement.setLong(index + 2, (Long) value);
		else
			statement.setNull(index + 2, LongType.INSTANCE.sqlType());

		if (value instanceof Float)
			statement.setFloat(index + 3, (Float) value);
		else
			statement.setNull(index + 3, FloatType.INSTANCE.sqlType());

		if (value instanceof Double)
			statement.setDouble(index + 4, (Double) value);
		else
			statement.setNull(index + 4, DoubleType.INSTANCE.sqlType());

		if (value instanceof java.sql.Timestamp)
			statement.setTimestamp(index + 5, (java.sql.Timestamp) value);
		else if (value instanceof java.util.Date)
			statement.setTimestamp(index + 5, new java.sql.Timestamp(((java.util.Date) value).getTime()));
		else
			statement.setNull(index + 5, TimestampType.INSTANCE.sqlType());

		if (value instanceof byte[] && isOracle)
			statement.setObject(index + 6, (byte[]) value);
		else if (value instanceof byte[])
			statement.setBlob(index + 6, new SerialBlob((byte[]) value));
		else
			statement.setNull(index + 6, java.sql.Types.BLOB);

		if (value instanceof String)
			statement.setString(index + 7, (String) value);
		else
			statement.setNull(index + 7, StringType.INSTANCE.sqlType());

		if (value instanceof char[] && (isOracle || isSqlServer))
			statement.setObject(index + 8, new String((char[]) value).getBytes());
		else if (value instanceof char[])
			statement.setClob(index + 8, new SerialClob((char[]) value));
		else
			statement.setNull(index + 8, java.sql.Types.CLOB);
	}

	public Object deepCopy(Object object) throws HibernateException {
		return object;
	}

	public Serializable disassemble(Object object, SessionImplementor session) throws HibernateException {
		return (Serializable) object;
	}

	public Object assemble(Serializable cached, SessionImplementor session, Object owner) throws HibernateException {
		return cached;
	}

	public Object replace(Object original, Object target, SessionImplementor session, Object owner) throws HibernateException {
		return original;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == y)
			return true;
		if (x == null || y == null)
			return false;
		return x.equals(y);
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

}
