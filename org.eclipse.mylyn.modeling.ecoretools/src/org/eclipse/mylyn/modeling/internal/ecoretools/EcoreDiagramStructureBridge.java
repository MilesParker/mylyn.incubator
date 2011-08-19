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

package org.eclipse.mylyn.modeling.internal.ecoretools;

import org.eclipse.mylyn.modeling.context.IModelStructureProvider;
import org.eclipse.mylyn.modeling.ecoretools.EcoreDiagramDomainBridge;
import org.eclipse.mylyn.modeling.gmf.GmfStructureBridge;

/**
 * @author Miles Parker
 */
public class EcoreDiagramStructureBridge extends GmfStructureBridge {

	@Override
	public IModelStructureProvider getDomainContextBridge() {
		return EcoreDiagramDomainBridge.getInstance();
	}

}
