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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.sandbox.search.ui.SearchProvider;

/**
 * A way to get search providers that have been registered via the extension point.
 * 
 * @author David Green
 */
public class SearchProviders {
	private static final String EXTENSION_POINT_NAME_SEARCH_PROVIDER = "searchProvider"; //$NON-NLS-1$

	public static List<SearchProvider> getSearchProviders() {
		List<SearchProvider> providers = new ArrayList<SearchProvider>();

		final String pluginId = SearchPlugin.getDefault().getBundle().getSymbolicName();

		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(pluginId,
				EXTENSION_POINT_NAME_SEARCH_PROVIDER);
		if (extensionPoint != null) {
			IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
			for (IConfigurationElement element : configurationElements) {
				SearchProvider provider;
				try {
					provider = (SearchProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
					providers.add(provider);
				} catch (CoreException e) {
					SearchPlugin.getDefault()
							.getLog()
							.log(new Status(IStatus.ERROR, pluginId,
									"Cannot instantiate search provider " + element.getAttribute("class"), e)); //$NON-NLS-1$//$NON-NLS-2$
				}
			}
		}

		return providers;
	}

	public static SearchProvider getSearchProvider() throws CoreException {
		List<SearchProvider> searchProviders = getSearchProviders();
		if (searchProviders.size() > 1) {
			return new CompositeSearchProvider(searchProviders);
		} else if (!searchProviders.isEmpty()) {
			return searchProviders.get(0);
		}
		throw new CoreException(new Status(IStatus.ERROR, SearchPlugin.BUNDLE_ID,
				Messages.SearchProviders_NoSearchProvidersAvailable));
	}
}
