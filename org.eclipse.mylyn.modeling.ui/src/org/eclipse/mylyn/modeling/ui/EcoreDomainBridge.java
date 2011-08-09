/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.modeling.ui;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

/**
 * @author milesparker
 */
public abstract class EcoreDomainBridge implements IModelUIProvider {

	public static final String ECORE_CONTENT_TYPE = "ecore"; //$NON-NLS-1$

	public String getContentType() {
		return ECORE_CONTENT_TYPE;
	}

	public Class<?> getDomainBaseClass() {
		return EObject.class;
	}

	public Class<?>[] getDomainNodeClasses() {
		return new Class[] { EClass.class, EEnum.class, EPackage.class };
	}

	public String getLabel(Object object) {
		if (object instanceof ENamedElement) {
			return ((ENamedElement) object).getName();
		}
		return object.toString();
	}
}