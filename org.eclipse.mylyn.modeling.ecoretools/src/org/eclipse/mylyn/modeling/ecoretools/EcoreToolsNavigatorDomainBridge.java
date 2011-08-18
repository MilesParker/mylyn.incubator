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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.mylyn.modeling.ui.EcoreDomainBridge;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

public class EcoreToolsNavigatorDomainBridge extends EcoreDomainBridge {

	private static EcoreToolsNavigatorDomainBridge INSTANCE;

	public boolean acceptsPart(IWorkbenchPart part) {
		return part instanceof ProjectExplorer;
	}

	@Override
	public boolean acceptsEditPart(EObject domainObject, EditPart part) {
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
