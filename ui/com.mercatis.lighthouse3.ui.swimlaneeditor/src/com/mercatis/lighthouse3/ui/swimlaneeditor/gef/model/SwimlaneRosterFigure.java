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
package com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;


public class SwimlaneRosterFigure extends Figure {

	public SwimlaneRosterFigure(SwimlaneRosterModel roster) {

		MarginBorder b = new MarginBorder(10);
		this.setBorder(b);

		ToolbarLayout layout = new ToolbarLayout(ToolbarLayout.VERTICAL);
		layout.setSpacing(15);
		layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		this.setLayoutManager(layout);
	}
}
