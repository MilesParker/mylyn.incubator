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

package org.eclipse.mylyn.modeling.ecoretools;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecoretools.diagram.part.EcoreDiagramEditor;
import org.eclipse.mylyn.emf.ui.IDomainUIBridge;
import org.eclipse.ui.IEditorPart;

public class EcoreDiagramDomainBridge implements IDomainUIBridge {

	private static EcoreDiagramDomainBridge INSTANCE;

	public static final String ECORE_CONTENT_TYPE = "ecore";

	@Override
	public String getContentType() {
		return ECORE_CONTENT_TYPE;
	}

	@Override
	public boolean acceptsEditor(IEditorPart editorPart) {
		return editorPart instanceof EcoreDiagramEditor;
	}

	@Override
	public Class<?> getDomainBaseClass() {
		return EObject.class;
	}

	@Override
	public Class<?>[] getDomainNodeClasses() {
		return new Class[] { EClassifier.class };
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof ENamedElement) {
			return ((ENamedElement) object).getName();
		}
		return null;
	}

	public static EcoreDiagramDomainBridge getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new EcoreDiagramDomainBridge();
		}
		return INSTANCE;
	}
}
