package org.eclipse.mylyn.modeling.gmf.figures;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionNodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.swt.graphics.Color;

public class EdgeMaskingFigure extends PolylineConnection implements IRevealable {

	private IFigure partFigure;
	private final ConnectionNodeEditPart part;
	private Map<IFigure, Color> priorForegroundForFigure;
	private Map<IFigure, Color> priorBackgroundForFigure;
	private Color maskColor;

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
	public EdgeMaskingFigure(IGraphicalEditPart part) {
		this.part = (ConnectionNodeEditPart) part;
		IFigure partFigure = part.getFigure();
		if (partFigure.getParent() != null) {
			partFigure = partFigure.getParent();
		}
		setLineWidth(4);
		partFigure = part.getFigure();
		if (partFigure.getParent() != null) {
			partFigure = partFigure.getParent();
		}
		maskColor = partFigure.getBackgroundColor();
		setBackgroundColor(maskColor);
		setForegroundColor(maskColor);
		priorForegroundForFigure = new HashMap<IFigure, Color>();
		priorBackgroundForFigure = new HashMap<IFigure, Color>();
		setAlpha(255);
		for (Object child : part.getChildren()) {
			if (child instanceof IGraphicalEditPart) {
				GraphicalEditPart childPart = (GraphicalEditPart) child;
				IFigure childFigure = childPart.getFigure();
				priorForegroundForFigure.put(childFigure, childFigure.getForegroundColor());
				priorBackgroundForFigure.put(childFigure, childFigure.getBackgroundColor());
			}
		}
		refreshChildren();
	}

	public void refresh() {
		if (part.isActive()) {
			part.refresh();
		}
		// FigureManagerHelper.INSTANCE.refresh(this);
	}

	@Override
	public void reveal() {
		setAlpha(0);
		// FigureManagerHelper.INSTANCE.reveal(this);
	}

	public void refreshChildren() {
		for (Object child : part.getChildren()) {
			if (child instanceof IGraphicalEditPart) {
				GraphicalEditPart childPart = (GraphicalEditPart) child;
				IFigure figure = childPart.getFigure();
				IFigure partFigure = figure.getParent();
				figure.setBackgroundColor(maskColor);
				figure.setForegroundColor(maskColor);
				partFigure.setBackgroundColor(maskColor);
				partFigure.setForegroundColor(maskColor);
			}
		}
		part.getFigure().setBackgroundColor(maskColor);
		part.getFigure().setForegroundColor(maskColor);

	}

	public void revealChildren() {
		for (Object child : part.getChildren()) {
			if (child instanceof IGraphicalEditPart) {
				GraphicalEditPart childPart = (GraphicalEditPart) child;
				IFigure figure = childPart.getFigure();
				FigureManagerHelper.INSTANCE.reveal(figure);
				IFigure partFigure = figure.getParent();
				partFigure.setBackgroundColor(priorBackgroundForFigure.get(figure));
				partFigure.setForegroundColor(priorForegroundForFigure.get(figure));
				figure.setBackgroundColor(priorBackgroundForFigure.get(figure));
				figure.setForegroundColor(priorForegroundForFigure.get(figure));
			}
		}
		part.getFigure().setBackgroundColor(ColorConstants.black);
		part.getFigure().setForegroundColor(ColorConstants.black);
	}

}
