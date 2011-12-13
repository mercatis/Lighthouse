package com.mercatis.lighthouse3.ui.environment.providers;

import java.util.Comparator;

import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;

public class GenericLabelComparator implements Comparator<Object> {

	private final static GenericLabelComparator INSTANCE;
	
	static {
		INSTANCE = new GenericLabelComparator();
	}
	
	public int compare(Object o1, Object o2) {		
		return LabelConverter.getLabel(o1).compareTo(LabelConverter.getLabel(o2));
	}

	public static GenericLabelComparator getInstance(){
		return INSTANCE;
	}
}

	
