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

package org.eclipse.mylyn.internal.emf.ui;

import java.util.List;

import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.context.ui.AbstractContextUiBridge;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * @author Benjamin Muskalla
 */
public class EmfUiBridge extends AbstractContextUiBridge {

	@Override
	public void open(IInteractionElement element) {
		// ignore

	}

	@Override
	public void close(IInteractionElement element) {
		// ignore

	}

	@Override
	public boolean acceptsEditor(IEditorPart editorPart) {
		// ignore
		return false;
	}

	@Override
	public IInteractionElement getElement(IEditorInput input) {
		// ignore
		return null;
	}

	@Override
	public List<TreeViewer> getContentOutlineViewers(IEditorPart editorPart) {
		// ignore
		return null;
	}

	@Override
	public Object getObjectForTextSelection(TextSelection selection, IEditorPart editor) {
		// ignore
		return null;
	}

	@Override
	public String getContentType() {
		// ignore
		return null;
	}
}
