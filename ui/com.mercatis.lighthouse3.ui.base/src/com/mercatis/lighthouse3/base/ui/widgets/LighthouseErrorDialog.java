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
package com.mercatis.lighthouse3.base.ui.widgets;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class LighthouseErrorDialog extends IconAndMessageDialog {

	private boolean stackTraceExpanded = false;
	
	private String title;
	private IStatus status;
    private Button detailsButton;
    private Text stackTraceText;

    public LighthouseErrorDialog(Shell parent, String title, String message, IStatus status, boolean stackTraceVisible) {
		super(parent);
		this.title = title;
		this.message = message;
		this.status = status;
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/**Opens an error dialog
	 * @param parent 
	 * @param title The title of the dialog
	 * @param message The message of the dialog 
	 * @param status The status which should be displayed
	 * @param stackTraceVisible true if the stacktrace should be visible
	 * @return
	 */
	public static int openError(Shell parent, String title, String message, IStatus status, boolean stackTraceVisible) {
		LighthouseErrorDialog dialog = new LighthouseErrorDialog(parent, title, message, status, stackTraceVisible);
		return dialog.open();
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Details buttons
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createDetailsButton(parent);
    }
	
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    protected void buttonPressed(int id) {
    	if (id == IDialogConstants.DETAILS_ID) {
            toggleDetailsArea();
        } else {
            super.buttonPressed(id);
        }
    }
    
    protected Text createStackTraceText(Composite parent) {
    	stackTraceText= new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
        GridData data = new GridData(	GridData.HORIZONTAL_ALIGN_FILL |
                						GridData.GRAB_HORIZONTAL | 
                						GridData.VERTICAL_ALIGN_FILL |
                						GridData.GRAB_VERTICAL);
        data.horizontalSpan = 2;
        stackTraceText.setLayoutData(data);
        stackTraceText.setFont(parent.getFont());
        
        fillStackTrace(stackTraceText);
        
        return stackTraceText;
    }
    
    /**
     * @param stackTraceText
     */
    private void fillStackTrace(Text stackTraceText) {
    	if(status.getException() != null) {
    		
    		StringWriter sw = new StringWriter();
 			PrintWriter pw = new PrintWriter(sw);
 			status.getException().printStackTrace(pw);
 			
 	        String message = sw.getBuffer().toString();
    		stackTraceText.setText(message);
    	}
    }
    
    /**
     * 
     */
    private void toggleDetailsArea() {
        Point windowSize = getShell().getSize();
        Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        if (stackTraceExpanded) {
            stackTraceText.dispose();
            stackTraceExpanded = false;
            detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
        } else {
            stackTraceText = createStackTraceText((Composite) getContents());
            detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
            stackTraceExpanded = true;
        }
        Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
    }
	
    
	/**
	 * @param parent
	 */
	private void createDetailsButton(Composite parent) {
		detailsButton = createButton(parent, IDialogConstants.DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL, false);
	}
	
	/**
	 * 
	 */
	protected final void showDetailsArea() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(title);
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IconAndMessageDialog#getImage()
	 */
	@Override
	protected Image getImage() {
		return getErrorImage();
	}

}
