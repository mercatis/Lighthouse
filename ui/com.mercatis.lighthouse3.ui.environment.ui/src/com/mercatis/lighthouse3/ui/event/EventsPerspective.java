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
package com.mercatis.lighthouse3.ui.event;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


public class EventsPerspective implements IPerspectiveFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui
	 * .IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
		layout.addShowViewShortcut("lighthouse3.events.view.eventdetailview");
		layout.addPerspectiveShortcut("lighthouse3.events.perspective.eventperspective");

		IFolderLayout right = layout.createFolder("eventDetailView", IPageLayout.RIGHT, 0.75f, layout.getEditorArea());
		right.addView("lighthouse3.events.view.eventdetailview");
	}

}
