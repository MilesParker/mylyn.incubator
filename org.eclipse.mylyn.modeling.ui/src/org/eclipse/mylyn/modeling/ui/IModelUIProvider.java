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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.mylyn.modeling.context.IModelStructureProvider;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Miles Parker
 */
public interface IModelUIProvider extends IModelStructureProvider {

	boolean acceptsPart(IWorkbenchPart part);

	boolean acceptsEditPart(EObject object, EditPart part);
}
