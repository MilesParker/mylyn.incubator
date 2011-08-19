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

import org.eclipse.mylyn.modeling.gmf.MylynDecoratorProvider;
import org.eclipse.mylyn.modeling.ui.IModelUIProvider;

/**
 * @author Miles Parker
 */
public class EcoreDiagramDecoratorProvider extends MylynDecoratorProvider {

	@Override
	public IModelUIProvider getDomainUIBridge() {
		return EcoreDiagramDomainBridge.getInstance();
	}

}
