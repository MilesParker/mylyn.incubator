/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.sandbox.web;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Property editor dialog
 * 
 * @author Eugene Kuleshov
 */
public class ParameterEditorDialog extends Dialog {
	private String title;
	private String name;
	private String value;
	

	private Text valueText;
	private Text nameText;

	
	public ParameterEditorDialog(Shell parent) {
		super(parent);
		this.title = "New Property";
	}
	
	public ParameterEditorDialog(Shell parent, String name, String value) {
		super(parent);
		this.title = "Edit Property";
		this.name = name;
		this.value = value;
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);

		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setText("&Name:");

		nameText = new Text(composite, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.widthHint = 300;
		nameText.setLayoutData(gridData);

		Label valueLabel = new Label(composite, SWT.NONE);
		valueLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		valueLabel.setText("&Value:");

		valueText = new Text(composite, SWT.BORDER);
		GridData gridData_1 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData_1.widthHint = 300;
		valueText.setLayoutData(gridData_1);

		if(name!=null) {
			nameText.setText(name.trim());
			valueText.setFocus();
		}
		if(value!=null) {
			valueText.setText(value);
			valueText.setSelection(0, value.length());
		}
		
		ModifyListener updateListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtons();
			}
		};
		nameText.addModifyListener(updateListener);
		valueText.addModifyListener(updateListener);
		
		return composite;
	}
	
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
	}

	public void create() {
		super.create();
		updateButtons();
	}
	
	private void updateButtons() {
	    name = nameText.getText().trim();
	    value = valueText.getText();
		
		getButton(IDialogConstants.OK_ID).setEnabled(name.length()>0);
	}

	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}

}

