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

import org.eclipse.mylyn.modeling.gmf.ContextDecoratorProvider;
import org.eclipse.mylyn.modeling.ui.DiagramUiBridge;

/**
 * @author Miles Parker
 */
public class EcoreDiagramDecoratorProvider extends ContextDecoratorProvider {

	@Override
	public DiagramUiBridge getDomainUIBridge() {
		return EcoreDiagramUiBridge.getInstance();
	}

}
