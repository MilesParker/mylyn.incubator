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

package org.eclipse.mylyn.modeling.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.context.ui.AbstractContextUiBridge;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public abstract class DiagramUIBridge extends AbstractContextUiBridge {

	boolean initialized;

	@Override
	public void open(IInteractionElement element) {
	}

	@Override
	public void close(IInteractionElement element) {
	}

	@Override
	public IInteractionElement getElement(IEditorInput input) {
		return null;
	}

	@Override
	public List<TreeViewer> getContentOutlineViewers(IEditorPart editorPart) {
		if (editorPart == null) {
			return null;
		}
		List<TreeViewer> viewers = new ArrayList<TreeViewer>();
		Object out = editorPart.getAdapter(IContentOutlinePage.class);
		if (out instanceof Page) {
			Page page = (Page) out;
			if (page.getControl() != null) {
				IWorkbenchSite site = page.getSite();
				if (site != null) {
					ISelectionProvider provider = site.getSelectionProvider();
					if (provider instanceof TreeViewer) {
						viewers.add((TreeViewer) provider);
					}
				}
			}
		}
		return viewers;
	}

	/**
	 * NB: Text is probably not appropriate for EMF models until we look at supporting DSLs. See:
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=343195
	 */
	@Override
	public Object getObjectForTextSelection(TextSelection selection, IEditorPart editor) {
		return null;
	}

	@Override
	public boolean acceptsEditor(IEditorPart editorPart) {
		return getDomainUIBridge().acceptsPart(editorPart);
	}

	@Override
	public String getContentType() {
		return getDomainUIBridge().getContentType();
	}

	public abstract IModelUIProvider getDomainUIBridge();
}