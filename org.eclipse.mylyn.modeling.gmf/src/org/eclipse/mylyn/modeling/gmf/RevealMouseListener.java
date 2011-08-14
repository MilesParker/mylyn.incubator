package org.eclipse.mylyn.modeling.gmf;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.mylyn.modeling.gmf.figures.FigureManagerHelper;
import org.eclipse.mylyn.modeling.gmf.figures.IRevealable;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;

final class RevealMouseListener implements MouseMoveListener {

	private static final int REVEAL_DISTANCE = 180;

	// private final RootEditPart root;

	private Collection<IFigure> lastDecorations = new HashSet<IFigure>();

	private final IFigure layer;

	RevealMouseListener(IFigure layer) {
		this.layer = layer;
	}

	private Collection<IFigure> getTargetFigures(Point mousePoint) {
		Rectangle revealBounds = new Rectangle(mousePoint, new Dimension(REVEAL_DISTANCE * 2, REVEAL_DISTANCE * 2));
		revealBounds.translate(-REVEAL_DISTANCE, -REVEAL_DISTANCE);
		HashSet<IFigure> found = new HashSet<IFigure>();
		findChildFigure(layer, revealBounds, found);
		return found;
	}

	private void findChildFigure(IFigure parent, Rectangle revealBounds, HashSet<IFigure> found) {
		for (Object object : parent.getChildren()) {
			IFigure child = (IFigure) object;
			if (revealBounds.intersects(child.getClientArea())) {
				// only reveal outer-most
				if (isRevealableMember(child)) {
					found.add(child);
				} else {
					findChildFigure(child, revealBounds, found);
				}
			}
		}
	}

	public boolean isRevealableMember(IFigure candFigure) {
		if (candFigure instanceof IRevealable) {
			return true;
		} else if (candFigure.getParent() != null) {
			return isRevealableMember(candFigure.getParent());
		}
		return false;
	}

	private int distance(Rectangle rectangle, Point point) {
		int dx = 0;
		if (point.x < rectangle.x) {
			dx = rectangle.x - point.x;
		} else if (point.x > rectangle.right()) {
			dx = point.x - rectangle.right();
		}
		int dy = 0;
		if (point.y < rectangle.y) {
			dy = rectangle.y - point.y;
		} else if (point.y > rectangle.bottom()) {
			dy = point.y - rectangle.bottom();
		}
		return (int) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
	}

	private double nearness(IFigure figure, Point point) {
		double d = distance(figure.getClientArea(), point);
		d = Math.min(d, REVEAL_DISTANCE);
		double n = 1.0 - (d / (double) REVEAL_DISTANCE);
		return n;
	}

	public void mouseMove(MouseEvent e) {
		Point mousePoint = new Point(e.x, e.y);
		layer.translateFromParent(mousePoint);
		Collection<IFigure> newDecorations = getTargetFigures(mousePoint);

		if (!newDecorations.equals(lastDecorations)) {
			Collection<IFigure> removedFigures = new HashSet<IFigure>(lastDecorations);
			removedFigures.removeAll(newDecorations);
			for (IFigure removedFigure : removedFigures) {
				FigureManagerHelper.INSTANCE.unreveal(removedFigure);
			}
		}
		for (IFigure figure : newDecorations) {
			double n = nearness(figure, mousePoint);
			FigureManagerHelper.INSTANCE.reveal(figure, n);
		}
		lastDecorations = newDecorations;
	}
}