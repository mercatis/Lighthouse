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
package com.mercatis.lighthouse3.ui.swimlaneeditor.gef;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CompoundCommand;

/**
 * This command stack checks if the executed command throws errors during execution and makes an undo if that happens.
 * 
 * @see IExtendedCommand
 */
public class ExtendedCommandStack extends CommandStack {

	private boolean enabled = true;
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(Command command) {
		if (enabled) {
			super.execute(command);
			boolean doundo = false;
			if (command instanceof CompoundCommand) {
				for (Command subCommand : (List<Command>) ((CompoundCommand) command).getCommands()) {
					if (subCommand instanceof IExtendedCommand) {
						if (!((IExtendedCommand) subCommand).executedSuccessfully()) {
							doundo = true;
						}
					}
				}
			}
			if (command instanceof IExtendedCommand) {
				if (!((IExtendedCommand) command).executedSuccessfully()) {
					doundo = true;
				}
			}
			if (doundo) {
				this.undo();
			}
		}
	}
	
	public void setEnabled(boolean enable) {
		this.enabled = enable;
	}
}
