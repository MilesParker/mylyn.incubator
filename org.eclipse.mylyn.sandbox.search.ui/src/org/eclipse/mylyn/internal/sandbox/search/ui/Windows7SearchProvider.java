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
import org.eclipse.core.runtime.SubMonitor;

/**
 * 
 * @author David Green
 */
public class Windows7SearchProvider extends SearchProvider {

	@Override
	public void performSearch(SearchCriteria searchSpecification,
			SearchCallback callback, IProgressMonitor m)
			throws CoreException {
		SubMonitor monitor = SubMonitor.convert(m);
		monitor.beginTask("TEST", 10000);
		callback.searchInitiated();
		try {
			// monitor.worked(1);
			// monitor.newChild(100);
			
			// FIXME: implement
			
		} finally {
			callback.searchCompleted();
			monitor.done();
		}

	}

}
