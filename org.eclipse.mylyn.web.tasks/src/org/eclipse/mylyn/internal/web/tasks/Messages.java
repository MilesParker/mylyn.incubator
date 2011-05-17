/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.web.tasks;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.mylyn.internal.web.tasks.messages"; //$NON-NLS-1$

	public static String ParameterEditorDialog_New_Property;

	public static String ParameterEditorDialog_Edit_Property;

	public static String ParameterEditorDialog_Name_;

	public static String ParameterEditorDialog_Value_;

	public static String ParameterEditorDialog_Name_should_be_Java_identifier;

	public static String ParametersEditor_Parameter;

	public static String ParametersEditor_Value;

	public static String ParametersEditor_Add_;

	public static String ParametersEditor_Remove_;

	public static String ParametersEditor_Edit_;

	public static String WebQueryWizardPage_Advanced_Configuration;

	public static String WebQueryWizardPage_complete;

	public static String WebQueryWizardPage_Create_web_query;

	public static String WebQueryWizardPage_Description;

	public static String WebQueryWizardPage_Id;

	public static String WebQueryWizardPage_incomplete;

	public static String WebQueryWizardPage_New_web_query;

	public static String WebQueryWizardPage_No_matching_results_Check_query_regexp;

	public static String WebQueryWizardPage_Open;

	public static String WebQueryWizardPage_Opening_Browser;

	public static String WebQueryWizardPage_Owner;

	public static String WebQueryWizardPage_Parsing_error_;

	public static String WebQueryWizardPage_Preview;

	public static String WebQueryWizardPage_Query_Pattern_;

	public static String WebQueryWizardPage_Query_result;

	public static String WebQueryWizardPage_Query_Title_;

	public static String WebQueryWizardPage_Query_URL_;

	public static String WebQueryWizardPage_Specify_query_parameters_for_X;

	public static String WebQueryWizardPage_Status;

	public static String WebQueryWizardPage_Type;

	public static String WebQueryWizardPage_Unable_to_fetch_resource_;

	public static String WebQueryWizardPage_Updating_preview;

	public static String WebRepositoryConnector_Require_two_matching_groups;

	public static String WebRepositoryConnector_Web_Template_Advanced_;

	public static String WebRepositoryConnector_Failed_to_parse_RSS_feed;

	public static String WebRepositoryConnector_Could_not_fetch_resource;

	public static String WebRepositorySettingsPage_Web_Repository_Settings;

	public static String WebRepositorySettingsPage_Select_server_template_example_and_modify_to_match_settings_for_your_project;

	public static String WebRepositorySettingsPage_Login_Form_URL_;

	public static String WebRepositorySettingsPage_Login_Token_Pattern;

	public static String WebRepositorySettingsPage_Advanced_Configuration;

	public static String WebRepositorySettingsPage_Task_URL_;

	public static String WebRepositorySettingsPage_New_Task_URL_;

	public static String WebRepositorySettingsPage_Query_Request_URL_;

	public static String WebRepositorySettingsPage_Query_Pattern_;

	public static String WebRepositorySettingsPage_Login_Request_URL_;

	public static String WebTaskEditorPageFactory_Browser;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
