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
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionNodeEditPart;
import org.eclipse.mylyn.modeling.context.IModelStructureProvider;
import org.eclipse.mylyn.modeling.ui.IModelUiProvider;
import org.eclipse.papyrus.diagram.clazz.edit.parts.ClassEditPart;
import org.eclipse.papyrus.diagram.clazz.edit.parts.PackageEditPart;
import org.eclipse.papyrus.editor.PapyrusMultiDiagramEditor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Relationship;

/**
 * @author Miles Parker
 */
public class Uml2DomainBridge implements IModelStructureProvider, IModelUiProvider {

	private static Uml2DomainBridge INSTANCE = new Uml2DomainBridge();

	public static final String UML2_CONTENT_TYPE = "uml2"; //$NON-NLS-1$

	public boolean acceptsPart(IWorkbenchPart part) {
		return part instanceof PapyrusMultiDiagramEditor;
	}

	public boolean acceptsEditPart(EObject domainObject, EditPart part) {
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

	public String getLabel(Object object) {
		if (object instanceof NamedElement) {
			return ((NamedElement) object).getName();
		}
		return null;
	}

	public String getContentType() {
		return UML2_CONTENT_TYPE;
	}

	public static Uml2DomainBridge getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Uml2DomainBridge();
		}
		return INSTANCE;
	}

}
