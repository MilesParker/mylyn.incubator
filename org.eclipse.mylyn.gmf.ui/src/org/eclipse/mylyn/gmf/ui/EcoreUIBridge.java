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

package org.eclipse.mylyn.gmf.ui;

import org.eclipse.emf.ecoretools.diagram.part.EcoreDiagramEditor;
import org.eclipse.mylyn.emf.context.EcoreDiagramBridge;
import org.eclipse.mylyn.emf.ui.GenericUIBridge;
import org.eclipse.ui.IEditorPart;


public class EcoreUIBridge extends GenericUIBridge {

	@Override
	public String getContentType() {
		return EcoreDiagramBridge.ECORE_CONTENT_TYPE;
	}

	@Override
	public boolean acceptsEditor(IEditorPart editorPart) {
		return editorPart instanceof EcoreDiagramEditor;
	}
}
