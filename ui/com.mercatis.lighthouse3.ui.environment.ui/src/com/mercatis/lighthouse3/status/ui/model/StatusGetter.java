package com.mercatis.lighthouse3.status.ui.model;

import com.mercatis.lighthouse3.base.getterchain.GenericGetter;
import com.mercatis.lighthouse3.domainmodel.status.EventTriggeredStatusChange;
import com.mercatis.lighthouse3.domainmodel.status.ManualStatusClearance;
import com.mercatis.lighthouse3.domainmodel.status.StalenessChange;
import com.mercatis.lighthouse3.domainmodel.status.StatusChange;

public class StatusGetter extends GenericGetter {
	
	@Override
	public Object getProperty(Object pObject) {
		if(getMethodName().equals("getCausedBy")) {
			if(pObject instanceof EventTriggeredStatusChange) {
				return "Event";
			}
			if(pObject instanceof ManualStatusClearance) {
				return "Manual Clearance";
			}
			if(pObject instanceof StalenessChange) {
				return "Stale";
			}
		}
		return super.getProperty(pObject);
	}
	
	@Override
	public Class<?> getResultClass() {
		if(getMethodName().equals("getCausedBy")) {
			return StatusChange.class;
		}
		return super.getResultClass();
	}

}
