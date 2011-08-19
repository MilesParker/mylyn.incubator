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

import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.mylyn.modeling.gmf.figures.NodeLandmarkFigure;
import org.eclipse.mylyn.modeling.gmf.figures.NodeMaskingFigure;

/**
 * @author Miles Parker
 */
public class NodeDecorator extends ContextDecorator {

	private final EObject domainObject;

	public NodeDecorator(MylynDecoratorProvider provider, IDecoratorTarget target, EObject domainObject) {
		super(provider, target);
		this.domainObject = domainObject;
	}

	@Override
	protected void createDecoration() {
		removeDecorations();
		if (!isInteresting()) {
			IGraphicalEditPart part = (IGraphicalEditPart) getTarget().getAdapter(IGraphicalEditPart.class);
			final IFigure decorated = part.getFigure();
			addDecoration(new NodeMaskingFigure(decorated));
		} else if (isLandmark()) {
			IGraphicalEditPart part = (IGraphicalEditPart) getTarget().getAdapter(IGraphicalEditPart.class);
			final IFigure decorated = part.getFigure();
			addDecoration(new NodeLandmarkFigure(decorated));
		} //we don't do anything with "just interesting" case right now. See http://bugs.eclipse.org/bugs/show_bug.cgi?id=343218
	}

	@Override
	public boolean isInteresting() {
		return getProvider().isInteresting(domainObject) || getProvider().isLandmark(domainObject);
	}

	@Override
	public boolean isLandmark() {
		return getProvider().isLandmark(domainObject);
	}

}