/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.internal.sandbox.search.ui;

import org.eclipse.osgi.util.NLS;

class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.mylyn.internal.sandbox.search.ui.messages"; //$NON-NLS-1$

	public static String CompositeSearchProvider_SearchingTask;

	public static String DesktopSearchActionGroup_MenuOpenWith;

	public static String DesktopSearchPage_CaseSensitive;

	public static String DesktopSearchPage_FilenamePatterns;

	public static String DesktopSearchPage_SearchFailedMessage;

	public static String DesktopSearchPage_SearchFailedTitle;

	public static String DesktopSearchPage_SpecifyFilenamePatternsPrompt;

	public static String DesktopSearchPage_SpecifyTextPrompt;

	public static String DesktopSearchPage_TextLabel;

	public static String DesktopSearchQuery_Label;

	public static String DesktopSearchQuery_NFilesMatching;

	public static String DesktopSearchQuery_NoFilesMatching;

	public static String DesktopSearchQuery_OneFileMatches;

	public static String DesktopSearchResultPage_EmptyLabel;

	public static String OpenFileAction_CannotOpenFile;

	public static String OpenFileAction_OpenFileErrorMessage;

	public static String OpenFileAction_OpenFileErrorTitle;

	public static String SearchProviders_NoSearchProvidersAvailable;

	public static String SearchResultLabelProvider_QualifierFormat;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
