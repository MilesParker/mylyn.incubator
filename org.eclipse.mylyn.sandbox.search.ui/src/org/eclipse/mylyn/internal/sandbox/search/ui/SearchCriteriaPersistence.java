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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.mylyn.sandbox.search.ui.SearchCriteria;

/**
 * a mechanism for persisting {@link SearchCriteria}.
 * 
 * @author David Green
 */
public class SearchCriteriaPersistence {

	private static final String KEY_FILE_NAME_PATTERNS = "filenamePatterns"; //$NON-NLS-1$

	private static final String KEY_TEXT = "text"; //$NON-NLS-1$

	public void load(SearchCriteria criteria, IDialogSettings settings) {
		criteria.setText(settings.get(KEY_TEXT));
		criteria.setFilenamePatterns(settings.getArray(KEY_FILE_NAME_PATTERNS));
	}

	public void save(SearchCriteria criteria, IDialogSettings settings) {
		settings.put(KEY_TEXT, criteria.getText());
		settings.put(KEY_FILE_NAME_PATTERNS, criteria.getFilenamePatterns());
	}

}
