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

import org.eclipse.core.internal.resources.ProjectNatureDescriptor;
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
import org.eclipse.mylyn.modeling.ui.EcoreDomainBridge;
import org.eclipse.mylyn.modeling.ui.IModelUIProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

public class EcoreToolsNavigatorDomainBridge extends EcoreDomainBridge {

	private static EcoreToolsNavigatorDomainBridge INSTANCE;
	
	@Override
	public boolean acceptsPart(IWorkbenchPart part) {
		return part instanceof ProjectExplorer;
	}

	@Override
	public boolean acceptsEditPart(EObject domainObject,
			EditPart part) {
		//We only support views
		return false;
	}

	public static EcoreToolsNavigatorDomainBridge getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new EcoreToolsNavigatorDomainBridge();
		}
		return INSTANCE;
	}

}
