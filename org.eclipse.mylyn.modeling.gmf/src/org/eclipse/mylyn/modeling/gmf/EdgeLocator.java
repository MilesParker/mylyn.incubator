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
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.Decoration;

public class EdgeLocator implements Locator {

	private final IFigure decorated;

	public EdgeLocator(IFigure decorated) {
		this.decorated = decorated;
	}

	@Override
	public void relocate(IFigure target) {
		if (target instanceof Decoration
				&& decorated instanceof PolylineConnection) {
			PolylineConnection edge = (PolylineConnection) decorated;
			PolylineConnection decerator = (PolylineConnection) target
					.getChildren().get(0);
			decerator.setPoints(edge.getPoints().getCopy());
			target.setBounds(decerator.getBounds().getCopy());
		}
	}

}
