/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.modeling.gmf;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.Decoration;
import org.eclipse.mylyn.modeling.gmf.figures.IRevealableFigure;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;

/**
 * @author Miles Parker
 */
public class RevealMouseListener implements MouseMoveListener {

	private static final int REVEAL_DISTANCE = 180;

	// private final RootEditPart root;

	private Collection<IRevealableFigure> lastDecorations = new HashSet<IRevealableFigure>();

	private final IFigure layer;

	RevealMouseListener(IFigure layer) {
		this.layer = layer;
	}

	private Collection<IRevealableFigure> getTargetFigures(Point mousePoint) {
		Rectangle revealBounds = new Rectangle(mousePoint, new Dimension(REVEAL_DISTANCE * 2, REVEAL_DISTANCE * 2));
		revealBounds.translate(-REVEAL_DISTANCE, -REVEAL_DISTANCE);
		HashSet<IRevealableFigure> found = new HashSet<IRevealableFigure>();
		findChildFigure(layer, revealBounds, found);
		return found;
	}

	private void findChildFigure(IFigure parent, Rectangle revealBounds, HashSet<IRevealableFigure> found) {
		for (Object object : parent.getChildren()) {
			IFigure child = (IFigure) object;
			if (revealBounds.intersects(child.getClientArea())) {
				// only reveal outer-most
				IRevealableFigure figure = getRevealableMember(child);
				if (figure != null) {
					found.add(figure);
				}
				findChildFigure(child, revealBounds, found);
			}
		}
	}

	public IRevealableFigure getRevealableMember(IFigure candFigure) {
		if (candFigure instanceof IRevealableFigure) {
			return (IRevealableFigure) candFigure;
		} else if (candFigure instanceof Decoration) {
			for (Object object : ((Decoration) candFigure).getChildren()) {
				//there should only be one for each decoration?
				if (object instanceof IRevealableFigure) {
					return (IRevealableFigure) object;
				}
			}
		} else if (candFigure.getParent() != null) {
			return getRevealableMember(candFigure.getParent());
		}
		return null;
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
		double n = 1.0 - (d / REVEAL_DISTANCE);
		return n;
	}

	public void mouseMove(MouseEvent e) {
		Point mousePoint = new Point(e.x, e.y);
		layer.translateFromParent(mousePoint);
		Collection<IRevealableFigure> newDecorations = getTargetFigures(mousePoint);

		if (!newDecorations.equals(lastDecorations)) {
			Collection<IRevealableFigure> removedFigures = new HashSet<IRevealableFigure>(lastDecorations);
			removedFigures.removeAll(newDecorations);
			for (IRevealableFigure removedFigure : removedFigures) {
				if (removedFigure.getParent() != null & removedFigure.getParent().getParent() != null) {
					removedFigure.unreveal();
				}
			}
		}
		for (IRevealableFigure figure : newDecorations) {
			double n = nearness(figure, mousePoint);
			figure.reveal(n);
		}
		lastDecorations = newDecorations;
	}

	public void removeDecoration(IFigure decoration) {
		lastDecorations.remove(decoration);
	}

}