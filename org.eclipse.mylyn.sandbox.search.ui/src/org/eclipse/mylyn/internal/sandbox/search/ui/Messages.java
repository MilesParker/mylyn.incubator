/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
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
	public static String DesktopSearchQuery_Label;
	public static String DesktopSearchQuery_NFilesMatching;
	public static String DesktopSearchQuery_NoFilesMatching;
	public static String DesktopSearchQuery_OneFileMatches;
	public static String DesktopSearchResultPage_EmptyLabel;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
