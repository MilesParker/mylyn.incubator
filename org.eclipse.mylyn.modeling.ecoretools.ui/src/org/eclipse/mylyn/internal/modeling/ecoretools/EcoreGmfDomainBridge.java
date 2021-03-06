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

import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.mylyn.modeling.emf.ecore.EcoreDomainBridge;

/**
 * @author Miles Parker
 */
public class EcoreGmfDomainBridge extends EcoreDomainBridge {

	@Override
	public Object getDomainObject(Object object) {
		if (object instanceof View) {
			return ((View) object).getElement();
		}
		return super.getDomainObject(object);
	}
}