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

package org.eclipse.mylyn.internal.modeling.ecoretools;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EAttributeEditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EClass2EditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EClassEditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EDataType2EditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EDataTypeEditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EEnum2EditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EEnumEditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EOperationEditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EPackage2EditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EPackageEditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EReferenceEditPart;
import org.eclipse.emf.ecoretools.diagram.part.EcoreDiagramEditor;
import org.eclipse.mylyn.modeling.emf.ecore.EcoreDomainBridge;
import org.eclipse.mylyn.modeling.ui.DiagramUiBridge;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Miles Parker
 */
public class EcoreDiagramUiBridge extends DiagramUiBridge {

	private static EcoreDiagramUiBridge INSTANCE;

	@Override
	public boolean acceptsPart(IWorkbenchPart part) {
		return part instanceof EcoreDiagramEditor;
	}

	@Override
	public boolean acceptsViewObject(Object domainObject, Object part) {
		//Nodes
		if (domainObject instanceof EClass) {
			return part instanceof EClassEditPart || part instanceof EClass2EditPart;
		}
		if (domainObject instanceof EEnum) {
			return part instanceof EEnumEditPart || part instanceof EEnum2EditPart;
		}
		if (domainObject instanceof EDataType) {
			return part instanceof EDataTypeEditPart || part instanceof EDataType2EditPart;
		}
		if (domainObject instanceof EAttribute) {
			return part instanceof EAttributeEditPart;
		}
		if (domainObject instanceof EOperation) {
			return part instanceof EOperationEditPart;
		}
		//We don't want the root-most package or we'll get the whole diagram!
		if (domainObject instanceof EPackage && ((EPackage) domainObject).eContainer() != null) {
			return part instanceof EPackageEditPart || part instanceof EPackage2EditPart;
		}
		//Edges
		if (domainObject instanceof EReference) {
			return part instanceof EReferenceEditPart;
		}
		return false;
	}

	public static EcoreDiagramUiBridge getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new EcoreDiagramUiBridge();
		}
		return INSTANCE;
	}

	@Override
	public String getContentType() {
		return EcoreDomainBridge.ECORE_CONTENT_TYPE;
	}
}
