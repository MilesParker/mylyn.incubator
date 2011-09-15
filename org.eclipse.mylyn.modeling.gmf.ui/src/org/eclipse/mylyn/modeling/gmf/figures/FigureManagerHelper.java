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
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.Shape;
import org.eclipse.swt.graphics.Color;

/**
 * Allows us to handle all of the various combinations of figures in a consistent way.
 * 
 * @author Miles Parker
 */
public class FigureManagerHelper {

	public static FigureManagerHelper INSTANCE = new FigureManagerHelper();

	/**
	 * Private to ensure that we can only access through singleton.
	 */
	private FigureManagerHelper() {
	}

	/**
	 * Note: We are working with a masking figure. Therefore "revealed" is when this mask is fully transparent, i.e.
	 * alpha is 0.
	 * 
	 * @param figure
	 * @param nearness
	 *            bounded in [0,1] where 1.0 is touching and 0.0 is furtherest away
	 */
	public void reveal(IFigure figure, double nearness) {
		// We don't actually want to fully reveal.
		nearness *= .75;
		int alpha = (int) (255 - (nearness * 255.0));
		if (figure instanceof Shape) {
			((Shape) figure).setAlpha(alpha);
		}
	}

	/**
	 * Note: We are working with a masking figure. Therefore "revealed" is when this mask is fully transparent, i.e.
	 * alpha is 0.
	 * 
	 * @param figure
	 * @param nearness
	 *            bounded in [0,1] with
	 */
	public void reveal(IFigure figure, Color color1, Color color2, double nearness) {
		if (figure instanceof Shape && !(figure instanceof PolylineConnection)) {
			reveal(figure, nearness);
		} else {
			// We don't actually want to fully reveal.
			nearness *= .75;
			figure.setForegroundColor(GradiatedColorRegistry.INSTANCE.getColor(color1, color2, (float) nearness));
		}
	}

	public void unreveal(IFigure figure) {
		if (figure instanceof Shape && !(figure instanceof PolylineConnection)) {
			((Shape) figure).setAlpha(255);
		}
		// if (figure instanceof EdgeMaskingFigure) {
		// ((EdgeMaskingFigure) figure).unreveal();
		// }
	}

	public void unreveal(IFigure figure, Color maskingColor) {
		if (figure instanceof Shape && !(figure instanceof PolylineConnection)) {
			unreveal(figure);
		} else {
			figure.setForegroundColor(maskingColor);
		}
	}
}
