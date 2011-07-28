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

package org.eclipse.mylyn.diagram.papyrus;

import org.eclipse.mylyn.emf.context.EcoreDiagramBridge;
import org.eclipse.mylyn.emf.ui.GenericUIBridge;
import org.eclipse.papyrus.editor.PapyrusMultiDiagramEditor;
import org.eclipse.ui.IEditorPart;

/**
 * @author milesparker
 */
public class UML2UIBridge extends GenericUIBridge {

	@Override
	public String getContentType() {
		return UML2DiagramBridge.UML2_CONTENT_TYPE;
	}

	@Override
	public boolean acceptsEditor(IEditorPart editorPart) {
		return editorPart instanceof PapyrusMultiDiagramEditor;
	}

}
