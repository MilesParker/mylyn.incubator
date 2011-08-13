package org.eclipse.mylyn.modeling.gmf.figures;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.swt.graphics.Color;

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