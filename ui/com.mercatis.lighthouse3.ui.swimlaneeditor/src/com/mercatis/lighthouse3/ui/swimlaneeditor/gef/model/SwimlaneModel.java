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

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to fill the SwimlaneEditPart with data.
 * It carries a roster that is used to autolayout ProcessTasks.
 * 
 */
public class SwimlaneModel implements Comparable<SwimlaneModel> {
	public static enum SwimlaneType {
		DEFAULT, DRYLANE, NEWLANE
	}

	private String name;
	private ArrayList<SwimlaneRosterModel> roster = new ArrayList<SwimlaneRosterModel>();
	private SwimlaneType type;

	public SwimlaneModel(SwimlaneType type, String name) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addTaskToRoster(int rosterNumber, ProcessTaskModel task) {
		while (rosterNumber >= roster.size()) {
			roster.add(new SwimlaneRosterModel());
		}
		roster.get(rosterNumber).addTask(task);
	}

	public List<SwimlaneRosterModel> getRoster() {
		if (roster.isEmpty())
			roster.add(new SwimlaneRosterModel());
		return roster;
	}

	public SwimlaneType getType() {
		return this.type;
	}

	public int compareTo(SwimlaneModel other) {
		if (this.type == SwimlaneType.DRYLANE && other.type != SwimlaneType.DRYLANE)
			return -1;
		if (this.type != SwimlaneType.DRYLANE && other.type == SwimlaneType.DRYLANE)
			return 1;
		if (this.type == SwimlaneType.NEWLANE && other.type != SwimlaneType.NEWLANE)
			return 1;
		if (this.type != SwimlaneType.NEWLANE && other.type == SwimlaneType.NEWLANE)
			return -1;
		return this.name.compareTo(other.name);
	}
}
