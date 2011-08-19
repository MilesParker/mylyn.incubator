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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.Shape;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * Allows us to handle all of the various combinations of figures in a consistent way.
 * 
 * @author Miles Parker
 */
public class FigureManagerHelper {

	public static FigureManagerHelper INSTANCE = new FigureManagerHelper();

	// TODO We need to revisit to make sure that we're not creating a bazillion colors here. We can do an indexed
	// version
	// if that is the case. Also, we need to be sure to dispose these properly.
	protected Map<RGB, Color> colorCache = new HashMap<RGB, Color>();

	public FigureManagerHelper() {
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
			figure.setForegroundColor(getColor(color1, color2, (float) nearness));
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

	public final Color create(RGB rgb) {
		Color color = colorCache.get(rgb);
		if (color == null) {
			color = new Color(org.eclipse.swt.widgets.Display.getCurrent(), rgb.red, rgb.green, rgb.blue);
			colorCache.put(rgb, color);
		}
		return color;
	}

	public Color getColor(Color color1, Color color2, float distance) {
		RGB rgb1 = new RGB(color1.getRed(), color1.getGreen(), color1.getBlue());
		float[] hsb1 = rgb1.getHSB();
		RGB rgb2 = new RGB(color2.getRed(), color2.getGreen(), color2.getBlue());
		float[] hsb2 = rgb2.getHSB();

		float[] hsb = new float[3];

		for (int i = 0; i < hsb.length; i++) {
			hsb[i] = hsb1[i] + (((hsb2[i] - hsb1[i]) * distance));
		}
		return create(new RGB(hsb[0], hsb[1], hsb[2]));
	}
}
