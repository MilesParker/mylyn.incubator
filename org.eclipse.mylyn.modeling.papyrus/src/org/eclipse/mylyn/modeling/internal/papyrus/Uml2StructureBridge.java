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

import javax.management.relation.Relation;

import org.eclipse.mylyn.modeling.gmf.GmfStructureBridge;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;

/**
 * @author Miles Parker
 */
public class Uml2StructureBridge extends GmfStructureBridge {

	public static final String UML2_CONTENT_TYPE = "uml2"; //$NON-NLS-1$

	@Override
	public Class<?> getDomainBaseNodeClass() {
		return Element.class;
	}

	@Override
	public Class<?>[] getDomainNodeClasses() {
		return new Class[] { Classifier.class };
	}

	@Override
	public Class<?> getDomainBaseEdgeClass() {
		return Relation.class;
	}

	@Override
	public Class<?>[] getDomainEdgeClasses() {
		return new Class[] { Relation.class };
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof NamedElement) {
			return ((NamedElement) object).getName();
		}
		return null;
	}

	@Override
	public String getContentType() {
		return UML2_CONTENT_TYPE;
	}
}
