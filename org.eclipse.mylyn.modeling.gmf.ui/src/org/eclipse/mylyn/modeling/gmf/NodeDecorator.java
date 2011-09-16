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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.Decoration;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.mylyn.modeling.gmf.figures.FigureManagerHelper;
import org.eclipse.mylyn.modeling.gmf.figures.IRevealableFigure;

/**
 * @author Miles Parker
 */
public class NodeDecorator extends ContextDecorator {

	class NodeMaskingFigure extends RectangleFigure implements IRevealableFigure {

		private final IFigure decorated;

		/**
		 * Constructor.
		 * 
		 * @param part
		 * @param color
		 *            the highlight color
		 * @param size
		 *            the size of the border
		 */
		public NodeMaskingFigure(IFigure decorated) {
			this.decorated = decorated;
			setLayoutManager(new XYLayout());
			setOpaque(true);
			setFill(true);
			setOutline(false);
			//Find the first parent that is managed and is also interesting, otherwise return the rootmost part.
			IGraphicalEditPart backgroundPart = getDecoratedInterestingParent(getEditPart());
			if (backgroundPart instanceof ShapeNodeEditPart) {
				setBackgroundColor(ColorConstants.white);
//				setBackgroundColor(((IFigure) backgroundPart.getFigure().getChildren().get(0)).getBackgroundColor());
//				setBackgroundColor(((IFigure) backgroundPart.getFigure().getChildren().get(0)).getBackgroundColor());
			} else {
				setBackgroundColor(backgroundPart.getFigure().getBackgroundColor());
			}
			setAlpha(255);

//			if (decorated.getParent() != null) {
//				decorated = decorated.getParent();
//			}
//			Color backgroundColor = decorated.getBackgroundColor();
//			setBackgroundColor(backgroundColor);
		}

		public void reveal(double nearness) {
			FigureManagerHelper.INSTANCE.reveal(this, nearness);
		}

		public void unreveal() {
			FigureManagerHelper.INSTANCE.unreveal(this);
		}

		public void restore() {
			//noop, nodes are handled normally.
		}

		public void relocate(IFigure target) {
			if (target instanceof Decoration) {
				Rectangle bounds = decorated.getBounds().getCopy();
				IGraphicalEditPart backgroundPart = getDecoratedParent(getEditPart());
				if (backgroundPart instanceof ShapeNodeEditPart) {
					IFigure donorFigure = (IFigure) backgroundPart.getFigure().getChildren().get(0);
					bounds.width = donorFigure.getBounds().width - 4;
					bounds.x = donorFigure.getBounds().x + 2;
				}
				target.setBounds(bounds);
				((IFigure) target.getChildren().get(0)).setBounds(bounds);
			}
		}
	}

	public class NodeLandmarkFigure extends RectangleFigure implements IRevealableFigure, Locator {

		private static final int BORDER_SIZE = 2;

		private final IFigure decorated;

		/**
		 * Constructor.
		 * 
		 * @param part
		 * @param color
		 *            the highlight color
		 * @param size
		 *            the size of the border
		 */
		public NodeLandmarkFigure(IFigure decorated) {
			this.decorated = decorated;
			setLayoutManager(new XYLayout());
			setOpaque(false);
			setFill(false);
			setOutline(true);
			setForegroundColor(ColorConstants.gray);
//			setSize(decorated.getSize().expand(BORDER_SIZE, 0));
			setLineWidth(BORDER_SIZE);
			setAlpha(255);
		}

		public void relocate(IFigure target) {
			if (target instanceof Decoration) {
				//bounds may be returned by reference
				Rectangle borderBounds = decorated.getBounds().getCopy();

				borderBounds = new Rectangle(borderBounds.x - BORDER_SIZE / 2, borderBounds.y - BORDER_SIZE,
						borderBounds.width, BORDER_SIZE);
				target.setBounds(borderBounds);
				((IFigure) target.getChildren().get(0)).setBounds(borderBounds);
			}
		}

		@Override
		public void paintFigure(Graphics graphics) {
			super.paintFigure(graphics);
		}

		public void reveal(double nearness) {
		}

		public void unreveal() {
			//noop, landmarks are never hidden
		}

		public void restore() {
			//noop, landmarks are never hidden
		}
	}

	private final EObject domainObject;

	public NodeDecorator(ContextDecoratorProvider provider, IDecoratorTarget target, EObject domainObject) {
		super(provider, target);
		this.domainObject = domainObject;
	}

	@Override
	protected void createDecoration() {
		removeDecorations();
		if (!isInteresting()) {
			final IFigure decorated = getEditPart().getFigure();
			NodeMaskingFigure decorationFigure = new NodeMaskingFigure(decorated);
			addDecoration(decorationFigure);
		} else if (isLandmark()) {
			final IFigure decorated = getEditPart().getFigure();
			addDecoration(new NodeLandmarkFigure(decorated));
		} //we don't do anything with "just interesting" case right now. See http://bugs.eclipse.org/bugs/show_bug.cgi?id=343218
	}

	private IGraphicalEditPart getDecoratedParent(IGraphicalEditPart part) {
		IGraphicalEditPart backgroundPart = (IGraphicalEditPart) part;
		while (backgroundPart.getParent() instanceof IGraphicalEditPart) {
			backgroundPart = (IGraphicalEditPart) backgroundPart.getParent();
			Object object = getProvider().getDomainObject(backgroundPart);
			if (getProvider().getDomainUIBridge().acceptsViewObject(object, backgroundPart)) {
				break;
			}
		}
		return backgroundPart;
	}

	private IGraphicalEditPart getDecoratedInterestingParent(IGraphicalEditPart part) {
		IGraphicalEditPart backgroundPart = (IGraphicalEditPart) part;
		while (backgroundPart.getParent() instanceof IGraphicalEditPart) {
			backgroundPart = (IGraphicalEditPart) backgroundPart.getParent();
			if (getProvider().isInteresting(backgroundPart)) {
				break;
			}
		}
		return backgroundPart;
	}

	@Override
	public boolean isInteresting() {
		return getProvider().isInteresting(domainObject) || getProvider().isLandmark(domainObject);
	}

	@Override
	public boolean isLandmark() {
		return getProvider().isLandmark(domainObject);
	}

}