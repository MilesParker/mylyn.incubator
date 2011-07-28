package org.eclipse.mylyn.gmf.ui;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.swt.graphics.Color;

public class MaskingFigure extends RectangleFigure {
	/**
	 * Constructor.
	 * 
	 * @param part
	 * 
	 * @param color
	 *            the highlight color
	 * @param size
	 *            the size of the border
	 */
	public MaskingFigure(IGraphicalEditPart part) {
		setLayoutManager(new XYLayout());
		setOpaque(true);
		IFigure figure = part.getFigure();
		Color backgroundColor = figure.getBackgroundColor();
		setBackgroundColor(backgroundColor);
		setAlpha(255);
	}
}
