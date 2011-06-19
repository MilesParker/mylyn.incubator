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
package org.eclipse.mylyn.sandbox.search.ui;

import java.util.Arrays;

/**
 * a search specification that defines criteria for a search
 * 
 * @author David Green
 */
public final class SearchCriteria {

	private String text;

	private boolean caseSensitive;

	private String[] filenamePatterns;

	public SearchCriteria() {
	}

	public SearchCriteria(String text, String[] filenamePatterns) {
		this.text = text;
		this.filenamePatterns = filenamePatterns;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public String[] getFilenamePatterns() {
		return filenamePatterns;
	}

	public void setFilenamePatterns(String[] filenamePatterns) {
		this.filenamePatterns = filenamePatterns;
	}

	public String getFilenamePatternsAsText() {
		String[] patterns = getFilenamePatterns();
		String text = ""; //$NON-NLS-1$
		for (String pattern : patterns) {
			pattern = pattern.trim();
			if (pattern.length() != 0) {
				if (text.length() > 0) {
					text += ", "; //$NON-NLS-1$
				}
				text += pattern;
			}
		}
		return text;
	}

	public void setFilenamePatternsAsText(String text) {
		filenamePatterns = text == null || text.trim().length() == 0 ? new String[0] : text.trim().split(
				"(\\s*,\\s*)|(\\s+)"); //$NON-NLS-1$
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	/**
	 * equality based on text only
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SearchCriteria other = (SearchCriteria) obj;
		if (text == null) {
			if (other.text != null) {
				return false;
			}
		} else if (!text.equals(other.text)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SearchCriteria [text=" + text + ", filenamePatterns=" //$NON-NLS-1$ //$NON-NLS-2$
				+ Arrays.toString(filenamePatterns) + "]"; //$NON-NLS-1$
	}

}
