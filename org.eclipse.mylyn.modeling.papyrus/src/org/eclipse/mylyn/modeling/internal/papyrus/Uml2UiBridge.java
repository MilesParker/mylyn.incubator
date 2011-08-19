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

package org.eclipse.mylyn.modeling.internal.papyrus;

import org.eclipse.mylyn.modeling.papyrus.Uml2DomainBridge;
import org.eclipse.mylyn.modeling.ui.DiagramUiBridge;
import org.eclipse.mylyn.modeling.ui.IModelUiProvider;

/**
 * @author Miles Parker
 */
public class Uml2UiBridge extends DiagramUiBridge {

	@Override
	public IModelUiProvider getDomainUIBridge() {
		return Uml2DomainBridge.getInstance();
	}

}
