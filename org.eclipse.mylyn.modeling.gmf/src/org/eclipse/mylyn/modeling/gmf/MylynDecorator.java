package org.eclipse.mylyn.modeling.gmf;

import org.eclipse.draw2d.Animation;
import org.eclipse.draw2d.Animator;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutAnimator;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.Shape;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.ui.services.parser.GetParserOperation;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoration;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecorator;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget.Direction;

public class MylynDecorator implements IDecorator {

	private final class RevealMouseHandler extends MouseMotionListener.Stub {
		public void mouseEntered(MouseEvent me) {
			Animation.markBegin();
			decorationFigure.setAlpha(150);
			decorationFigure.validate();
			// getDecoratorTarget().removeDecoration(lastDecoration);
			Animation.run(2000);
		}

		@Override
		public void mouseExited(MouseEvent me) {
			Animation.markBegin();
			decorationFigure.setAlpha(255);
			decorationFigure.validate();
			// getDecoratorTarget().removeDecoration(lastDecoration);
			Animation.run(2000);
		}
	}

	private final MylynDecoratorProvider provider;

	boolean wasInteresting;
	boolean wasFocussed;
	private final EObject model;
	private IDecoration lastDecoration;

	boolean initialized;
	private Shape decorationFigure;

	private final IDecoratorTarget target;

	public MylynDecorator(MylynDecoratorProvider provider,
			IDecoratorTarget target, EObject model) {
		this.provider = provider;
		this.target = target;
		this.model = model;
		getDecoratorTarget().installDecorator(
				MylynDecoratorProvider.MYLYN_DETAIL, this);
	}

	@Override
	public void activate() {
		refresh();
	}

	@Override
	public void deactivate() {
		initialized = false;
		removeDecoration();
	}

	@Override
	public void refresh() {
		boolean interesting = provider.isInteresting(model);
		boolean focussed = provider.isFocussed();
		if (focussed != wasFocussed || interesting != wasInteresting
				|| !initialized) {
			removeDecoration();
			if (focussed) {
				createDecoration(interesting);
			}
		}
		wasInteresting = interesting;
		wasFocussed = focussed;
		initialized = true;
	}

	private void createDecoration(boolean interesting) {
		final IGraphicalEditPart part = (IGraphicalEditPart) getDecoratorTarget()
				.getAdapter(IGraphicalEditPart.class);
		decorationFigure = null;
		final IFigure decorated = part.getFigure();
		if (!interesting) {
			if (decorated instanceof PolylineConnection) {
				decorationFigure = new MaskingEdgeFigure(part);
				lastDecoration = getDecoratorTarget().addDecoration(
						decorationFigure, new EdgeLocator(decorated), true);
			} else {
				decorationFigure = new MaskingNodeFigure(part);
				lastDecoration = getDecoratorTarget().addDecoration(
						decorationFigure, new NodeLocator(decorated), true);
			}
			// } else {
			// decorationFigure = new InterestingFigure(part);
		}
		if (decorationFigure != null) {
			decorationFigure.addMouseMotionListener(new RevealMouseHandler());
		}
	}

	private void removeDecoration() {
		if (lastDecoration != null
				&& ((IFigure) lastDecoration).getParent() != null) {
			getDecoratorTarget().removeDecoration(lastDecoration);
			lastDecoration = null;
		}
	}

	IDecoratorTarget getDecoratorTarget() {
		return target;
	}

	public Object getModel() {
		return model;
	}
}
