package org.eclipse.mylyn.modeling.gmf;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.swt.graphics.Color;

public class FadedFigure extends RectangleFigure {
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
	public FadedFigure(IGraphicalEditPart part) {
		setLayoutManager(new XYLayout());
		setOpaque(true);
		IFigure parentFigure = part.getFigure();
		Color backgroundColor = parentFigure.getBackgroundColor();
		setSize(parentFigure.getSize().expand(10, 10));
		setBackgroundColor(ColorConstants.blue);
		setAlpha(255);
	}
}
