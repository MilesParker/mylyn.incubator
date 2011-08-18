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
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.Decoration;

public class NodeLandmarkFigure extends RectangleFigure implements IRevealableFigure, Locator {

	private static final int BORDER_SIZE = 2;

	private final IFigure decorated;

	/**
	 * Constructor.
	 * 
	 * @param part
	 * @param color
	 *            the highlight color
	 * @param size
	 *            the size of the border
	 */
	public NodeLandmarkFigure(IFigure decorated) {
		this.decorated = decorated;
		setLayoutManager(new XYLayout());
		setOpaque(false);
		setFill(false);
		setOutline(true);
		setForegroundColor(ColorConstants.black);
		setSize(decorated.getSize().expand(BORDER_SIZE * 2, BORDER_SIZE * 2));
		setLineWidth(BORDER_SIZE);
		setAlpha(255);
	}

	public void relocate(IFigure target) {
		if (target instanceof Decoration) {
			//bounds may be returned by reference
			Rectangle borderBounds = decorated.getBounds().getCopy().translate(-BORDER_SIZE + 1, -BORDER_SIZE + 1);
			target.setBounds(borderBounds);
			((IFigure) target.getChildren().get(0)).setBounds(borderBounds);
		}
	}

	@Override
	public void paintFigure(Graphics graphics) {
		super.paintFigure(graphics);
	}

	public void reveal(double nearness) {
		//noop, landmarks are never hidden
	}

	public void unreveal() {
		//noop, landmarks are never hidden
	}

	public void restore() {
		//noop, landmarks are never hidden
	}
}
