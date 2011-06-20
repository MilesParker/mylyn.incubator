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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.mylyn.sandbox.search.ui.SearchResult;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class SearchResultLabelProvider extends LabelProvider implements IStyledLabelProvider {

	private WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();

	@Override
	public Image getImage(Object element) {
		return labelProvider.getImage(element);
	}

	@Override
	public String getText(Object element) {
		return getStyledText(element).toString();
	}

	@Override
	public void dispose() {
		labelProvider.dispose();
		labelProvider = null;
		super.dispose();
	}

	public StyledString getStyledText(Object element) {
		String name = null;
		String qualifier = null;
		if (element instanceof SearchResult) {
			SearchResult result = (SearchResult) element;
			name = result.getFile().getName();
			IResource resource = result.getResource();
			if (resource != null) {
				element = resource;
			}
			qualifier = result.getFile().getParentFile().getPath();
		}
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			name = resource.getName();
			qualifier = resource.getParent().getFullPath().toString();
		}
		if (name != null) {
			StyledString string = new StyledString(name);
			String decorated = NLS.bind(Messages.SearchResultLabelProvider_QualifierFormat,
					new String[] { string.toString(), qualifier });

			StyledCellLabelProvider.styleDecoratedString(decorated, StyledString.QUALIFIER_STYLER, string);
			return string;
		}
		return new StyledString(labelProvider.getText(element));
	}

}
