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
package com.mercatis.lighthouse3.persistence.users.hibernate;

import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.domainmodel.users.GroupRegistry;
import com.mercatis.lighthouse3.domainmodel.users.UserCodeRegistration;
import com.mercatis.lighthouse3.persistence.commons.hibernate.CodedDomainModelEntityDAOImplementation;

/**
 * This class provides a Hibernate implementation of the
 * <code>GroupRegistry</code> interface.
 */
public class GroupRegistryImplementation extends CodedDomainModelEntityDAOImplementation<Group> implements GroupRegistry {

	@SuppressWarnings("unchecked")
	public List<Group> getGroupsForUserCode(String userCode) {
		Criteria criteria = unitOfWork.getCurrentSession().createCriteria(getManagedType()).createCriteria("userCodes").add(Restrictions.eq("userCode", userCode)); 
		return criteria.list();
	}

	@Override
	protected Criteria entityToCriteria(Session session, Group entityTemplate) {
		Criteria criteria = super.entityToCriteria(session, entityTemplate);

		if (entityTemplate.getLongName() != null) {
			criteria.add(Restrictions.eq("longName", entityTemplate.getLongName()));
		}
		if (entityTemplate.getContact() != null) {
			criteria.add(Restrictions.eq("contact", entityTemplate.getContact()));
		}
		if (entityTemplate.getContactEmail() != null) {
			criteria.add(Restrictions.eq("contactEmail", entityTemplate.getContactEmail()));
		}
		if (entityTemplate.getDescription() != null) {
			criteria.add(Restrictions.eq("description", entityTemplate.getDescription()));
		}
		return criteria;
	}

	@Override
	public void delete(Group entityToDelete) {
		unitOfWork.getCurrentSession().createSQLQuery("delete from GROUP_USER where GRP_ID = :groupId").setParameter("groupId", entityToDelete.getId()).executeUpdate();
		unitOfWork.getCurrentSession().createSQLQuery("delete from CONTEXT_ROLE_ASSIGNMENTS where GRP_CODE = :groupCode").setParameter("groupCode", entityToDelete.getCode()).executeUpdate();
		super.delete(entityToDelete);
	}

	@SuppressWarnings("unchecked")
	public List<String> findAllGroupCodes() {
		return unitOfWork.getCurrentSession().createQuery("select code from Group").setCacheable(true).list();
	}
	
	@Override
	public String getLighthouseDomain() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<Group> findAll() {
		return findByTemplate(new Group());
	}

	private boolean existsUserCode(String code) {
		return ((Long) unitOfWork.getCurrentSession().createQuery("select count(userCode) from UserCodeRegistration where userCode = :code").setParameter("code", code).setCacheable(true).iterate().next()).longValue() > 0l;
	}

	@Override
	public void persist(Group entityToPersist) {
		Session session = unitOfWork.getCurrentSession();
		Set<UserCodeRegistration> regs = entityToPersist.getUserCodes();
		for (UserCodeRegistration userCode : regs.toArray(new UserCodeRegistration[0])) {
			if (existsUserCode(userCode.getUserCode())) {
				regs.remove(userCode);
				userCode = (UserCodeRegistration) session.merge(userCode);
				regs.add(userCode);
			}
		}
		super.persist(entityToPersist);
	}

	@Override
	public void update(Group entityToUpdate) {
		Session session = unitOfWork.getCurrentSession();
		Set<UserCodeRegistration> regs = entityToUpdate.getUserCodes();
		for (UserCodeRegistration userCode : regs.toArray(new UserCodeRegistration[0])) {
			if (existsUserCode(userCode.getUserCode())) {
				regs.remove(userCode);
				userCode = (UserCodeRegistration) session.merge(userCode);
				regs.add(userCode);
			}
		}
		super.update(entityToUpdate);
	}
	
	public UserCodeRegistration getUserCodeRegistration(String userCode) {
		UserCodeRegistration reg = new UserCodeRegistration();
		reg.setUserCode(userCode);
		if (existsUserCode(userCode)) {
			reg = (UserCodeRegistration) unitOfWork.getCurrentSession().merge(reg);
		}
		return reg;
	}
}
