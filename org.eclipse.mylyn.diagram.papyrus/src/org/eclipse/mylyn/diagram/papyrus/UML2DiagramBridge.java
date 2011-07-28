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

package org.eclipse.mylyn.diagram.papyrus;

import javax.lang.model.element.Element;

import org.eclipse.mylyn.emf.context.IDiagramContextBridge;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.NamedElement;

/**
 * @author milesparker
 */
public class UML2DiagramBridge implements IDiagramContextBridge {

	public static final String UML2_CONTENT_TYPE = "uml2"; //$NON-NLS-1$


	@Override
	public String getContentType() {
		return UML2_CONTENT_TYPE;
	}
	
	@Override
	public String getLabel(Object object) {
		if (object instanceof NamedElement) {
			return ((NamedElement) object).getName();
		}
		return null;
	}
	
	@Override
	public Class<?> getDomainBaseClass() {
		return Element.class;
	}

	@Override
	public Class<?>[] getDomainNodeClasses() {
		return new Class[]{Classifier.class};
	}
	
}
