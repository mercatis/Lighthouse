package com.mercatis.lighthouse3.base.ui.widgets.chooser;

public interface SecuritySelectionListModificationListener<T> {
	void onListItemModified(T item, boolean enabled, boolean modified);
}
