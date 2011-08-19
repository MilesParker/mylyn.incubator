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

package org.eclipse.mylyn.modeling.papyrus;

import javax.management.relation.Relation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.mylyn.modeling.context.IModelStructureProvider;
import org.eclipse.mylyn.modeling.ui.IModelUIProvider;
import org.eclipse.papyrus.diagram.clazz.edit.parts.ClassEditPart;
import org.eclipse.papyrus.editor.PapyrusMultiDiagramEditor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;

/**
 * @author Miles Parker
 */
public class UML2DomainBridge implements IModelStructureProvider, IModelUIProvider {

	private static UML2DomainBridge INSTANCE = new UML2DomainBridge();

	public static final String UML2_CONTENT_TYPE = "uml2"; //$NON-NLS-1$

	public boolean acceptsPart(IWorkbenchPart part) {
		return part instanceof PapyrusMultiDiagramEditor;
	}

	public String getContentType() {
		return UML2_CONTENT_TYPE;
	}

	public String getLabel(Object object) {
		if (object instanceof NamedElement) {
			return ((NamedElement) object).getName();
		}
		return null;
	}

	public Class<?> getDomainBaseNodeClass() {
		return Element.class;
	}

	public Class<?>[] getDomainNodeClasses() {
		return new Class[] { Classifier.class };
	}

	public Class<?> getDomainBaseEdgeClass() {
		return Relation.class;
	}

	public Class<?>[] getDomainEdgeClasses() {
		return new Class[] { Relation.class };
	}

	public static UML2DomainBridge getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new UML2DomainBridge();
		}
		return INSTANCE;
	}

	public boolean acceptsEditPart(EObject domainObject, EditPart part) {
		if (domainObject instanceof Classifier) {
			return part instanceof ClassEditPart;
		}
		return false;
	}

}
