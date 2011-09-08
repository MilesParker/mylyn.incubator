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
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * Provides support for defining Mylyn views, editors and their components for arbitrary model implementations.
 * Consumers should typically only need to override abstract methods.
 * 
 * @author Miles Parker
 */
public abstract class DiagramUiBridge extends AbstractContextUiBridge {

	boolean initialized;

	/**
	 * Shouldn't need to override.
	 */
	@Override
	public void open(IInteractionElement element) {
	}

	/**
	 * Implementors should not need to override.
	 */
	@Override
	public void close(IInteractionElement element) {
	}

	/**
	 * Implementors should not need to override.
	 */
	@Override
	public IInteractionElement getElement(IEditorInput input) {
		return null;
	}

	/**
	 * Simply returns any outline views that can adapt to the part. Implementors should not need to override.
	 */
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
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=343195 Implementors should not need to override.
	 */
	@Override
	public Object getObjectForTextSelection(TextSelection selection, IEditorPart editor) {
		return null;
	}

	/**
	 * Does the bridge provide support for the provider part? Override to define editors and views that Mylyn should
	 * manage.
	 * 
	 * @param part
	 *            an arbitrary workbench part
	 * @return
	 */
	public abstract boolean acceptsPart(IWorkbenchPart part);

	/**
	 * Is the provided view object appropriate for the given model object? (This is initially intended to support GEF
	 * Edit Parts but could be used in other circumstances such as by a structured viewer.) Override to define where
	 * apprpriate mappings exist between the view and model objects.
	 * 
	 * @param modelObject
	 * @param viewObject
	 * @return
	 */
	public abstract boolean acceptsViewObject(Object modelObject, Object viewObject);

	/**
	 * Delegates to more generic part method.
	 */
	@Override
	public final boolean acceptsEditor(IEditorPart editorPart) {
		return acceptsPart(editorPart);
	}
}