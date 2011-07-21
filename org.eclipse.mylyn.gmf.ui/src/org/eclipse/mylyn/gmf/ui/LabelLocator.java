/*******************************************************************************
 * Copyright (c) 2011 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Obeo - initial API and implementation
 *
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.gmf.ui;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.Decoration;

/**
 * 
 * @author milesparker 
 * 
 * Cribbed from EMF compare..
 * @author <a href="mailto:stephane.bouchet@obeo.fr">Stephane Bouchet</a>
 * 
 */
public class LabelLocator implements Locator {

	private final IFigure figure;

	public LabelLocator(IFigure figure) {
		this.figure = figure;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.draw2d.Locator#relocate(org.eclipse.draw2d.IFigure)
	 */
	public void relocate(IFigure target) {
		if (target instanceof Decoration) {
			final Rectangle ref = ((IFigure) figure.getChildren().get(0))
					.getBounds().getCopy().expand(1, 0);
			((IFigure) target.getChildren().get(0)).setBounds(ref);
			target.setBounds(ref);
		}
	}

}
