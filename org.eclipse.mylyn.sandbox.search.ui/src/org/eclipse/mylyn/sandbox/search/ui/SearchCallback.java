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


/**
 * A callback for use with {@link SearchProvider}
 * 
 * @author David Green
 */
public abstract class SearchCallback {

	public abstract void searchResult(SearchResult item);

}
