package org.eclipse.mylyn.modeling.gmf;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.swt.graphics.Color;

public class MaskingEdgeFigure extends PolylineConnection {
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
	public MaskingEdgeFigure(IGraphicalEditPart part) {
		IFigure partFigure = part.getFigure();
		if (partFigure.getParent() != null) {
			partFigure = partFigure.getParent();
		}
		Color color = ColorConstants.red;
		setForegroundColor(color);
		setBackgroundColor(color);
		setLineWidth(2);
//		setAlpha(255);
	}
}
