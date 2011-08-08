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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EClass2EditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EClassEditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EEnum2EditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EEnumEditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EPackage2EditPart;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EPackageEditPart;
import org.eclipse.emf.ecoretools.diagram.part.EcoreDiagramEditor;
import org.eclipse.gef.EditPart;
import org.eclipse.mylyn.modeling.ui.IModelUIProvider;
import org.eclipse.ui.IWorkbenchPart;

public class EcoreDiagramDomainBridge extends EcoreDomainBridge {

	private static EcoreDiagramDomainBridge INSTANCE;
	
	@Override
	public boolean acceptsPart(IWorkbenchPart part) {
		return part instanceof EcoreDiagramEditor;
	}

	@Override
	public boolean acceptsEditPart(EObject domainObject,
			EditPart part) {
		if (domainObject instanceof EClass) {
			return part instanceof EClassEditPart || part instanceof EClass2EditPart;
		}
		if (domainObject instanceof EEnum) {
			 return part instanceof EEnumEditPart || part instanceof EEnum2EditPart;
		}		
		//We don't want the root-most package or we'll get the whole diagram!
		if (domainObject instanceof EPackage && ((EPackage) domainObject).eContainer() != null) {
			 return part instanceof EPackageEditPart || part instanceof EPackage2EditPart;
		}		
		return false;
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
