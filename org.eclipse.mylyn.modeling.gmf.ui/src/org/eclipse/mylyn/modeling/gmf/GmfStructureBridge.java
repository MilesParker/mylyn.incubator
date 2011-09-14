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

package org.eclipse.mylyn.modeling.gmf;

import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.mylyn.modeling.emf.EmfStructureBridge;

/**
 * @author Miles Parker
 */
public abstract class GmfStructureBridge extends EmfStructureBridge {

	@Override
	/**
	 * Maps the diagram object to the domain object in the most general way possible.
	 * GMF diagram implementations typically shouldn't need to override this.
	 */
	public Object getDomainObject(Object object) {
		if (object instanceof View) {
			return ((View) object).getElement();
		}
		return super.getDomainObject(object);
	}

}
