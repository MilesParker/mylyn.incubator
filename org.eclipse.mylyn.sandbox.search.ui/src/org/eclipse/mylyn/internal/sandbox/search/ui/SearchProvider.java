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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * 
 * @author David Green
 */
public abstract class SearchProvider {

	public static SearchProvider instance() {
		return new MockSearchProvider(); //new Windows7SearchProvider();
	}
	
	/**
	 * perform the search
	 * @param searchSpecification
	 * @param callback
	 * @param monitor the progress monitor, can be null
	 * @throws CoreException
	 */
	public abstract void performSearch(SearchCriteria searchSpecification,SearchCallback callback,IProgressMonitor monitor) throws CoreException;
}
