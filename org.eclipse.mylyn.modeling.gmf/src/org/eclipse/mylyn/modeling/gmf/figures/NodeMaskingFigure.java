package org.eclipse.mylyn.modeling.gmf.figures;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.swt.graphics.Color;

public class NodeMaskingFigure extends RectangleFigure implements IRevealable {
	private final IGraphicalEditPart part;

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
	public NodeMaskingFigure(IGraphicalEditPart part) {
		this.part = part;
		setLayoutManager(new XYLayout());
		setOpaque(true);
		setFill(true);
		setOutline(false);
		IFigure partFigure = part.getFigure();
		if (partFigure.getParent() != null) {
			partFigure = partFigure.getParent();
		}
		Color backgroundColor = partFigure.getBackgroundColor();
		setBackgroundColor(backgroundColor);
		setAlpha(255);
	}

	@Override
	public void reveal() {
		setAlpha(150);
	}

	@Override
	public void refresh() {
//		part.refresh();
	}
}
