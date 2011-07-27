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
package org.eclipse.mylyn.internal.sandbox.search.ui.windows;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.mylyn.sandbox.search.ui.SearchCallback;
import org.eclipse.mylyn.sandbox.search.ui.SearchCriteria;
import org.eclipse.mylyn.sandbox.search.ui.SearchProvider;
import org.eclipse.osgi.util.NLS;

/**
 * a search provider that implements search over the Windows search APIs.
 * 
 * @author David Green
 * @author Raymond Lam
 */
public class WindowsSearchProvider extends SearchProvider {

	private native void performNativeSearch(SearchCriteria searchSpecification, SearchCallback callback,
			IProgressMonitor monitor);

	//Load the library
	static {
		String dllName = "Windows7Search"; //$NON-NLS-1$
		String suffix = "32"; //$NON-NLS-1$
		try {
			String osArch = Platform.getOSArch();
			if (Platform.ARCH_X86.equals(osArch)) {
				suffix = "32"; //$NON-NLS-1$
			} else if (Platform.ARCH_X86_64.equals(osArch)) {
				suffix = "64"; //$NON-NLS-1$
			}
		} catch (Throwable t) {
		}
		dllName += suffix;
		System.loadLibrary(dllName);
	}

	@Override
	public void performSearch(SearchCriteria searchSpecification, SearchCallback callback, IProgressMonitor m)
			throws CoreException {
		SubMonitor monitor = SubMonitor.convert(m);
		monitor.beginTask(NLS.bind(Messages.WindowsSearchProvider_SearchingTask, searchSpecification.getText()),
				IProgressMonitor.UNKNOWN);
		try {
			// monitor.worked(1);
			// monitor.newChild(100);

			performNativeSearch(searchSpecification, callback, monitor);
		} finally {
			monitor.done();
		}

	}
//
//	public static void main(String[] args) {
//		SearchCallback callback = new SearchCallback() {
//
//			@Override
//			public void searchResult(SearchResult item) {
//				System.out.println("Found: " + item.getFile().getAbsolutePath());
//			}
//		};
//		SearchCriteria spec = new SearchCriteria();
//		spec.setText("SIG");
//		spec.setFilenamePatterns(new String[] {});
//		try {
//			new WindowsSearchProvider().performSearch(spec, callback, new NullProgressMonitor());
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//	}
}
