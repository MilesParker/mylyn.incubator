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

package org.eclipse.mylyn.modeling.ui;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.gef.EditPart;
import org.eclipse.mylyn.modeling.context.IModelStructureProvider;

/**
 * @author Miles Parker
 */
public abstract class EcoreDomainBridge implements IModelStructureProvider, IModelUIProvider {

	private static final Class<?>[] NODE_CLASSES = new Class[] { EClass.class, EEnum.class, EPackage.class };

	private static final Class<?>[] EDGE_CLASSES = new Class[] { EReference.class };

	public static final String ECORE_CONTENT_TYPE = "ecore"; //$NON-NLS-1$

	public String getContentType() {
		return ECORE_CONTENT_TYPE;
	}

	public Class<?> getDomainBaseNodeClass() {
		return EObject.class;
	}

	public Class<?>[] getDomainNodeClasses() {
		return NODE_CLASSES;
	}

	public Class<?>[] getDomainEdgeClasses() {
		return EDGE_CLASSES;
	}

	public Class<?> getDomainBaseEdgeClass() {
		return EReference.class;
	}

	public String getLabel(Object object) {
		if (object instanceof ENamedElement) {
			return ((ENamedElement) object).getName();
		}
		return object.toString();
	}

	public boolean acceptsEditPart(EObject domainObject, EditPart part) {
		// ignore
		return false;
	}
}