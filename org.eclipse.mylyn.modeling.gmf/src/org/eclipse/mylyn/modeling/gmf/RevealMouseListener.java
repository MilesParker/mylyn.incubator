package org.eclipse.mylyn.modeling.gmf;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.mylyn.modeling.gmf.figures.FigureManagerHelper;
import org.eclipse.mylyn.modeling.gmf.figures.IRevealable;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;

final class RevealMouseListener implements MouseMoveListener {

	private final RootEditPart root;

	private IFigure lastDecoration;

	RevealMouseListener(RootEditPart root) {
		this.root = root;
	}

	protected IFigure getTargetFigure(MouseEvent event, IFigure layer) {
		Point eventLocation = new Point(event.x, event.y);
		return getTargetFigure(eventLocation, layer);
	}

	private IFigure getTargetFigure(Point eventLocation, IFigure layer) {
		IFigure candFigure = layer.findFigureAt(eventLocation);
		if (isRevealableMember(candFigure)) {
			return candFigure;
		}
		return null;
	}

	private boolean isRevealableMember(IFigure candFigure) {
		if (candFigure instanceof IRevealable) {
			return true;
		} else if (candFigure.getParent() != null) {
			return isRevealableMember(candFigure.getParent());
		}
		return false;
	}

	public void mouseMove(MouseEvent e) {
		IFigure targetDecoration = getTargetFigure(e,
				((AbstractGraphicalEditPart) root.getViewer().getRootEditPart()).getFigure());
		if (targetDecoration != lastDecoration) {
			if (lastDecoration != null) {
				FigureManagerHelper.INSTANCE.refresh(lastDecoration);
			}
			if (targetDecoration != null) {
				FigureManagerHelper.INSTANCE.reveal(targetDecoration);
			}
		}
		lastDecoration = targetDecoration;
	}
}