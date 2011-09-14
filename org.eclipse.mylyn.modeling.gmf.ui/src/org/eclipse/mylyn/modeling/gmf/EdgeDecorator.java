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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.Decoration;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.mylyn.modeling.gmf.figures.FigureManagerHelper;
import org.eclipse.mylyn.modeling.gmf.figures.IRevealableFigure;
import org.eclipse.swt.graphics.Color;

/**
 * @author Miles Parker
 */
public class EdgeDecorator extends ContextDecorator {

	private Map<IFigure, Color> priorForegroundForFigure;

	private final EObject connectionSource;

	private final EObject connectionTarget;

	private final List<EdgeMaskingFigure> edgeMaskHandles = new ArrayList<EdgeMaskingFigure>();

	/**
	 * These really aren't masks per se, they're just invisible figures that we can use as handle to get to the figure
	 * parts we're actually interested in.
	 * 
	 * @author Miles Parker
	 */
	class EdgeMaskingFigure extends RectangleFigure implements IRevealableFigure {
		double nearness;

		private final IFigure decorated;

		private final boolean head;

		/**
		 * Constructor.
		 * 
		 * @param part
		 * @param color
		 *            the highlight color
		 * @param size
		 *            the size of the border
		 */
		public EdgeMaskingFigure(IFigure decorated, boolean head) {
			this.decorated = decorated;
			this.head = head;
			setOutline(false);
			setFill(true);
			unreveal();
		}

		public void relocate(IFigure target) {
			if (target instanceof Decoration && decorated instanceof PolylineConnection) {
				PolylineConnection connection = (PolylineConnection) decorated;
				Point point;
				if (head) {
					point = connection.getPoints().getFirstPoint();
				} else {
					point = connection.getPoints().getLastPoint();

				}
				target.setBounds(new Rectangle(point, new Dimension(1, 1)));
			}
		}

		public void reveal(double nearness) {
			this.nearness = nearness;
			EdgeDecorator.this.reveal();
		}

		/**
		 * Edges must also return their non-alpha capable child figures to their orginal state.
		 */
		public void restore() {
			EdgeDecorator.this.restore();
		}

		public void unreveal() {
			EdgeDecorator.this.unreveal();
		}
	}

	public EdgeDecorator(ContextDecoratorProvider provider, IDecoratorTarget target, EObject connectionSource,
			EObject connectionTarget) {
		super(provider, target);
		this.connectionSource = connectionSource;
		this.connectionTarget = connectionTarget;
	}

	@Override
	protected void createDecoration() {
		removeDecorations();

		if (!isInteresting()) {
			priorForegroundForFigure = new HashMap<IFigure, Color>();

			for (IFigure figure : getManagedFigures()) {
				priorForegroundForFigure.put(figure, figure.getForegroundColor());
			}
			EdgeMaskingFigure edgeHandle = new EdgeMaskingFigure(getDecoratedFigure(), true);
			edgeMaskHandles.add(edgeHandle);
			addDecoration(edgeHandle);
			EdgeMaskingFigure edgeHandle2 = new EdgeMaskingFigure(getDecoratedFigure(), false);
			edgeMaskHandles.add(edgeHandle2);
			addDecoration(edgeHandle2);
			unreveal();
		}
	}

	public void reveal() {
		double nearness = 0.0;
		for (EdgeMaskingFigure mask : edgeMaskHandles) {
			if (mask.nearness > nearness) {
				nearness = mask.nearness;
			}
		}
		for (IFigure figure : getManagedFigures()) {
			Color figureColor = priorForegroundForFigure.get(figure);
			FigureManagerHelper.INSTANCE.reveal(figure, getMaskingColor(), figureColor, nearness);
		}
	}

	/**
	 * Edges must also return their non-alpha capable child figures to their orginal state.
	 */
	public void restore() {
		for (IFigure figure : getManagedFigures()) {
			Color priorColor = priorForegroundForFigure.get(figure);
			figure.setForegroundColor(priorColor);
		}
	}

	public void unreveal() {
		for (IFigure figure : getManagedFigures()) {
			FigureManagerHelper.INSTANCE.unreveal(figure, getMaskingColor());
		}
	}

	public Collection<IFigure> getManagedFigures() {
		Collection<IFigure> figures = new ArrayList<IFigure>();
		figures.add(getDecoratedFigure());
		for (Object child : getEditPart().getChildren()) {
			if (child instanceof IGraphicalEditPart) {
				GraphicalEditPart childPart = (GraphicalEditPart) child;
				IFigure childFigure = childPart.getFigure();
				figures.add(childFigure);
				figures.add(childFigure.getParent());
			}
		}
		return figures;
	}

	public Color getMaskingColor() {
		IFigure backgroundFigure = getDecoratedFigure();
		if (backgroundFigure.getParent() != null) {
			backgroundFigure = backgroundFigure.getParent();
		}
		return backgroundFigure.getBackgroundColor();
	}

	@Override
	public boolean isInteresting() {
		return getProvider().isInteresting(connectionSource) && getProvider().isInteresting(connectionTarget);
	}

	@Override
	public boolean isLandmark() {
		// Edges can never be landmarked
		return false;
	}

}