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
package com.mercatis.lighthouse3.base.actions;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Simple to use implementation to provide actions with attached dropdown menu.
 * 
 */
public abstract class DropDownAction<T> extends Action {
	
	/**
	 * Possible sorting modes for the menu items.
	 * <br /><code>NONE</code> - items will be displayed in the order they are added
	 * <br /><code>ASCENDING</code> - items will be sorted ascending by name
	 * <br /><code>DESCENDING</code> - items will be sorted descending by name
	 */
	public static enum SortingMode {NONE, ASCENDING, DESCENDING}
	
	/**
	 * The currently selected soring mode.
	 */
	protected SortingMode sortingMode = SortingMode.NONE;
	
	/**
	 * The comparator used for sorting.
	 */
	private Comparator<DropDownItem> comparator;
	
	/**
	 * The {@link ILabelProvider} for the menu items.
	 */
	private ILabelProvider labelProvider;
	
	/**
	 * This list conains all elements to be displayed in the dropdownmenu.
	 */
	private final LinkedList<DropDownItem> menuItems = new LinkedList<DropDownItem>();
	
	/**
	 * The menu creator is taking care of creating/disposing the dropdownmenu.
	 */
	private IMenuCreator menuCreator = new DropDownMenuCreator();
	
	/**
	 * This methid will be invoked when an item from the menu is selected.
	 * 
	 * @param name The Name of menu item
	 * @param data The data carried by the item - may be null
	 */
	abstract public void runFromMenuItem(String name, T data);

	/**
	 * Creates a new DropDownAction and sets the menu creator.
	 * When overriding this constructor, be sure to call super() or no menu will be shown.
	 */
	public DropDownAction() {
		setMenuCreator(menuCreator);
		labelProvider = toStringLabelProvider;
	}
	
	/**
	 * Sets the {@link ILabelProvider} for the menu. This provider will be used for menu entries without given name.
	 * <br />The default is a simple toString labelprovider.
	 * 
	 * @param labelProvider The new {@link ILabelProvider}. <code>null</code> will set the default provider.
	 */
	public void setLabelProvider(ILabelProvider labelProvider) {
		if (labelProvider == null) {
			this.labelProvider = toStringLabelProvider;
		} else {
			this.labelProvider = labelProvider;
		}
		rebuildMenu();
	}
	
	/**
	 * Set the sorting mode to one of these values:
	 * <ul>
	 * <li>{@link SortingMode.NONE}</li>
	 * <li>{@link SortingMode.ASCENDING}</li>
	 * <li>{@link SortingMode.DESCENDING}</li>
	 * </ul>
	 * 
	 * @param sortingMode
	 */
	public void setSortingMode(SortingMode sortingMode) {
		this.sortingMode = sortingMode;
		rebuildMenu();
	}
	
	/**
	 * Set the comparator to be used for sorting.
	 * 
	 * @param comparator May be null to disable any comparators.
	 */
	public void setComparator(Comparator<DropDownItem> comparator) {
		this.comparator = comparator;
		rebuildMenu();
	}
	
	/**
	 * Resorts the menu items and disposes the current menu.
	 * <br />The menu will be recreated when it is about to be displayed.
	 */
	private void rebuildMenu() {
		if (sortingMode != SortingMode.NONE) {
			if (comparator == null) {
				Collections.sort(menuItems);
			} else {
				Collections.sort(menuItems, comparator);
			}
		}
		menuCreator.dispose();
	}
	
	/**
	 * Add a new item to the menu.
	 * 
	 * @param name The name to be displayed
	 */
	public void addMenuItem(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Name must be set.");
		}
		addMenuItem(name, null);
	}
	
	/**
	 * Add a new item with payload to the menu.
	 * <br />When selecting this item, the payload will be delivered to {@link #runFromMenuItem(String, Object)}
	 * <br />Note that the name will override any labelproviders if set.
	 * 
	 * @param name The name to be displayed
	 * @param data The payload this item should carry around - may be null
	 */
	public void addMenuItem(String name, T data) {
		DropDownItem item = new DropDownItem(name, data);
		menuItems.add(item);
		rebuildMenu();
	}
	
	/**
	 * Add a new item to this menu. If there is no label provider given, the default toString label provider will bw used.
	 * 
	 * @param data The data to be represented by a menu item.
	 */
	public void addMenuItem(T data) {
		if (data == null) {
			throw new IllegalArgumentException("Data object must be set.");
		}
		addMenuItem(null, data);
	}
	
	/**
	 * Remove an item from the menu with the given name.
	 * 
	 * @param name The display name of the item to remove.
	 */
	public void removeMenuItem(String name) {
		DropDownItem itemToRemove = null;
		for (DropDownItem item : menuItems) {
			if (item.getName().equals(name)) {
				itemToRemove = item;
				break;
			}
		}
		menuItems.remove(itemToRemove);
		rebuildMenu();
	}
	
	/**
	 * Remove an item from the menu with the given payload.
	 * 
	 * @param data The payload of the item to remove.
	 */
	public void removeMenuItem(T data) {
		DropDownItem itemToRemove = null;
		for (DropDownItem item : menuItems) {
			if (item.getData().equals(data)) {
				itemToRemove = item;
				break;
			}
		}
		menuItems.remove(itemToRemove);
		rebuildMenu();
	}
	
	/**
	 * Removes all menu items from the menu.
	 */
	public void clearMenu() {
		menuItems.clear();
		rebuildMenu();
	}
	
	/**
	 * Retieve the first menu item.
	 * @return The first item in menu or null if the menu is empty.
	 */
	public DropDownItem getFirstMenuItem() {
		return menuItems.peek();
	}
	
	private ILabelProvider toStringLabelProvider = new ILabelProvider() {

		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			return element.toString();
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	};
	
	/**
	 * Internal class to store the menu items.
	 * <br />Provides payload and sorting functionalitiy
	 */
	protected class DropDownItem implements Comparable<DropDownItem> {
		
		/**
		 * Displayed name - sorting criteria
		 */
		String name;
		
		/**
		 * Payload
		 */
		T data;
		
		/**
		 * Creates a new DropDownItem with given name and payload.
		 * <br />Note that the name will override any labelproviders if set.
		 * 
		 * @param name The name of the DropDownItem
		 * @param data The payload my be null or any other object
		 */
		private DropDownItem(String name, T data) {
			this.name = name;
			this.data = data;
		}
		
		public String getName() {
			if (name != null)
				return name;
			return labelProvider.getText(data);
		}
		
		public T getData() {
			return data;
		}

		/**
		 * Implementation of the Comparable interface respecting the selected sorting mode.
		 */
		public int compareTo(DropDownItem other) {
			if (sortingMode.equals(SortingMode.ASCENDING))
				return this.getName().compareTo(other.getName());
			else
				return other.getName().compareTo(this.getName());
		}
		
		public SortingMode getSortingMode() {
			return sortingMode;
		}
	}
	
	/**
	 * Internal implementation of {@link IMenuCreator}.
	 */
	private class DropDownMenuCreator implements IMenuCreator {

		Menu menu;

		/**
		 * Dispose the menu and set the variable to null. It will be recreated on demand.
		 */
		public void dispose() {
			if (menu != null)
				menu.dispose();
			menu = null;
		}

		public Menu getMenu(Control parent) {
			if (menu == null) {
				menu = new Menu(parent);
				fillMenu();
			}
			return menu;
		}

		public Menu getMenu(Menu parent) {
			if (menu == null) {
				menu = new Menu(parent);
				fillMenu();
			}
			return menu;
		}
		
		/**
		 * This method takes the menuItems from the action and (re)fills the dropdownmenu.
		 * <br />Each menu item will invoke {@link #DropDownAction.runFromMenuItem(String, Object)}
		 */
		private void fillMenu() {
			int i = 0;
			for (final DropDownItem item : menuItems) {
				final MenuItem menuItem = new MenuItem(menu, SWT.CASCADE, i++);
				menuItem.setText(item.getName() != null ? item.getName() : labelProvider.getText(item.getData()));
				menuItem.setImage(labelProvider.getImage(item.getData()));
				menuItem.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						runFromMenuItem(item.getName(), item.getData());
					}
				});
			}
		}
	}
}
