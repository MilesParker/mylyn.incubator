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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.swt.graphics.Color;

/**
 * @author Miles Parker
 */
class FigureChild {
	IFigure figure;

	Color color;

	Color hideColor;

	public FigureChild(IFigure figure, Color color, Color hideColor) {
		super();
		this.figure = figure;
		this.color = color;
		this.hideColor = hideColor;
	}

	void restore() {
		figure.setForegroundColor(color);
		if (figure instanceof Shape) {
			((Shape) figure).setAlpha(255);
		}
	}

	void reveal() {
		figure.setForegroundColor(color);
		if (figure instanceof Shape) {
			((Shape) figure).setAlpha(150);
		}
	}

	void hide() {
		figure.setForegroundColor(hideColor);
		if (figure instanceof Shape) {
			((Shape) figure).setAlpha(255);
		}
	}
}