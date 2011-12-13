/**
 * (c) Copyright 2010 mercatis technologies AG
 *
 * All rights reserved.
 *
 * Part of Lighthouse 3
 *
 * This source code is proprietary trade secret information of
 * mercatis information systems GmbH. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * mercatis information systems GmbH.
 */
package com.mercatis.lighthouse3.services.internal.registries;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;


public interface LateBindingDomainModelEntity<Entity extends DomainModelEntity> {

	public Entity getDelegateEntity();
	
	public LateBindingDomainModelEntity<Entity> bind();
	
}
