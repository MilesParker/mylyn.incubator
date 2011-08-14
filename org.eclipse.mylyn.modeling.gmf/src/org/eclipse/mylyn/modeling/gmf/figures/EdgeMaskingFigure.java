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
import org.eclipse.ui.ISizeProvider;

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
		setOutline(false);
		setFill(false)
		;		// if (partFigure.getParent() != null) {
		// partFigure = partFigure.getParent();
		// }
		setLineWidth(4);
	}

	@Override
	public void addNotify() {
		// TODO Auto-generated method stub
		super.addNotify();
		IFigure partFigure = part.getFigure();
		if (partFigure.getParent() != null) {
			partFigure = partFigure.getParent();
		}
//		setLineWidth(4);
		partFigure = part.getFigure();
//		if (partFigure.getParent() != null) {
//			partFigure = partFigure.getParent();
//		}
		maskColor = partFigure.getBackgroundColor();
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
		priorForegroundForFigure.put(this, getForegroundColor());
		priorBackgroundForFigure.put(this, getBackgroundColor());
		priorForegroundForFigure.put(partFigure, partFigure.getForegroundColor());
		priorBackgroundForFigure.put(partFigure, partFigure.getBackgroundColor());
		unreveal();
	}

	
	@Override
	public void removeNotify() {
		revealChildren(0.0);
		super.removeNotify();
	}

	public void unreveal() {
		setForegroundColor(maskColor);
		part.getFigure().setForegroundColor(maskColor);
		unrevealChildren();
		// FigureManagerHelper.INSTANCE.refresh(this);
		// refreshChildren();
		// if (part.isActive()) {
		// part.refresh();
		// }
		// FigureManagerHelper.INSTANCE.refresh(this);
	}
	
	public void unrevealChildren() {
		for (Object child : part.getChildren()) {
			if (child instanceof IGraphicalEditPart) {
				GraphicalEditPart childPart = (GraphicalEditPart) child;
				IFigure figure = childPart.getFigure();
				IFigure partFigure = figure.getParent();
				FigureManagerHelper.INSTANCE.unreveal(figure);
				// figure.setBackgroundColor(maskColor);
				figure.setForegroundColor(maskColor);
				// partFigure.setBackgroundColor(maskColor);
				partFigure.setForegroundColor(maskColor);
			}
		}
		// part.getFigure().setBackgroundColor(maskColor);
		part.getFigure().setForegroundColor(maskColor);
	}


	@Override
	public void reveal(double nearness) {
		FigureManagerHelper.INSTANCE.reveal(this, maskColor, priorForegroundForFigure.get(part.getFigure()), nearness);
		FigureManagerHelper.INSTANCE.reveal(part.getFigure(), maskColor, priorForegroundForFigure.get(part.getFigure()), nearness);
//		part.getFigure().setForegroundColor(priorForegroundForFigure.get(part.getFigure()));
		revealChildren(nearness);
	}
	
	public void revealChildren(double nearness) {
		for (Object child : part.getChildren()) {
			if (child instanceof IGraphicalEditPart) {
				GraphicalEditPart childPart = (GraphicalEditPart) child;
				IFigure childFigure = childPart.getFigure();
				IFigure partFigure = childFigure.getParent();
				Color childColor = priorForegroundForFigure.get(childFigure);
				Color parentColor = priorForegroundForFigure.get(childFigure);
				FigureManagerHelper.INSTANCE.reveal(childFigure, maskColor, childColor, nearness);
				FigureManagerHelper.INSTANCE.reveal(partFigure, maskColor, parentColor, nearness);
			}
		}
	}

}