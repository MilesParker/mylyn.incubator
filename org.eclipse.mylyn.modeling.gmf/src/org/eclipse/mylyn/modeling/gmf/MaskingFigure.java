package org.eclipse.mylyn.modeling.gmf;

import org.eclipse.draw2d.ColorConstants;
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
		setFill(true);
		setOutline(false);
		IFigure partFigure = part.getFigure();
		setSize(partFigure.getSize().expand(10, 10));
		if (partFigure.getParent() != null) {
			partFigure = partFigure.getParent();
		}
		Color backgroundColor = partFigure.getBackgroundColor();
		setBackgroundColor(backgroundColor);
		setAlpha(255);
	}
}
