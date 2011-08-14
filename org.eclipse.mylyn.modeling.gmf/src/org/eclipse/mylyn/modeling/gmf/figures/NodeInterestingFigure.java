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

package org.eclipse.mylyn.modeling.gmf.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.swt.graphics.Color;

public class NodeInterestingFigure extends RectangleFigure {
	/**
	 * Constructor.
	 * @param part 
	 * 
	 * @param color
	 *            the highlight color
	 * @param size
	 *            the size of the border
	 */
	public NodeInterestingFigure(IGraphicalEditPart part) {
		setLayoutManager(new XYLayout());
		setOpaque(true);
		setFill(true);
		setOutline(false);
		IFigure partFigure = part.getFigure();
		setSize(partFigure.getSize().expand(10, 10));
		if (partFigure.getParent() != null) {
			partFigure = partFigure.getParent();
		}
		setBackgroundColor(ColorConstants.yellow);
		setAlpha(200);
	}
}