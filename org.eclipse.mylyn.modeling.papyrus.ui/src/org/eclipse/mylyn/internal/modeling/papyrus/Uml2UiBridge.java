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

package org.eclipse.mylyn.internal.modeling.papyrus;

import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionNodeEditPart;
import org.eclipse.mylyn.modeling.ui.DiagramUiBridge;
import org.eclipse.papyrus.diagram.clazz.edit.parts.ClassEditPart;
import org.eclipse.papyrus.diagram.clazz.edit.parts.PackageEditPart;
import org.eclipse.papyrus.editor.PapyrusMultiDiagramEditor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Relationship;

/**
 * @author Miles Parker
 */
public class Uml2UiBridge extends DiagramUiBridge {

	private static Uml2UiBridge INSTANCE = new Uml2UiBridge();

	public static final String UML2_CONTENT_TYPE = "uml2"; //$NON-NLS-1$

	@Override
	public boolean acceptsPart(IWorkbenchPart part) {
		return part instanceof PapyrusMultiDiagramEditor;
	}

	@Override
	public boolean acceptsViewObject(Object domainObject, Object part) {
		if (domainObject instanceof Classifier) {
			return part instanceof ClassEditPart;
		}
		if (domainObject instanceof Package) {
			return part instanceof PackageEditPart;
		}
		//Edges
		if (domainObject instanceof Relationship) {
			return part instanceof ConnectionNodeEditPart;
		}
		return false;
	}

	@Override
	public String getContentType() {
		return UML2_CONTENT_TYPE;
	}

	public static Uml2UiBridge getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Uml2UiBridge();
		}
		return INSTANCE;
	}
}
