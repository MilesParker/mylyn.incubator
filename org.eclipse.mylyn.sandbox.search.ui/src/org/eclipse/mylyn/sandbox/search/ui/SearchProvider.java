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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An implementation of a search capability
 * 
 * @author David Green
 */
public abstract class SearchProvider {

	/**
	 * perform the search
	 * 
	 * @param searchSpecification
	 * @param callback
	 * @param monitor
	 *            the progress monitor, can be null
	 * @throws CoreException
	 */
	public abstract void performSearch(SearchCriteria searchSpecification, SearchCallback callback,
			IProgressMonitor monitor) throws CoreException;
}
