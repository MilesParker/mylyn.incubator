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

import org.eclipse.emf.ecore.presentation.EcoreEditor;
import org.eclipse.mylyn.modeling.emf.ecore.EcoreDomainBridge;
import org.eclipse.mylyn.modeling.ui.DiagramUiBridge;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

/**
 * @author Miles Parker
 */
public class EcoreToolsNavigatorUiBridge extends DiagramUiBridge {

	private static EcoreToolsNavigatorUiBridge INSTANCE;

	@Override
	public boolean acceptsPart(IWorkbenchPart part) {
		return part instanceof ProjectExplorer || part instanceof EcoreEditor;
	}

	@Override
	public boolean acceptsViewObject(Object domainObject, Object part) {
		//We only support views
		return false;
	}

	public static EcoreToolsNavigatorUiBridge getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new EcoreToolsNavigatorUiBridge();
		}
		return INSTANCE;
	}

	@Override
	public String getContentType() {
		return EcoreDomainBridge.ECORE_CONTENT_TYPE;
	}
}
