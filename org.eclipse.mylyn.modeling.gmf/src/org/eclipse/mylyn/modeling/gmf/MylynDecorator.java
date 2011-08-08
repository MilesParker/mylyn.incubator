package org.eclipse.mylyn.modeling.gmf;

import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoration;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecorator;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;

public class MylynDecorator implements IDecorator {

	private final MylynDecoratorProvider provider;

	boolean wasInteresting;
	boolean wasFocussed;
	private final EObject model;
	private IDecoration lastDecoration;

	boolean initialized;
	private IFigure decorationFigure;

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
		if (focussed != wasFocussed || interesting != wasInteresting || !initialized) {
			removeDecoration();
			if (focussed) {
				IGraphicalEditPart part = (IGraphicalEditPart) getDecoratorTarget()
						.getAdapter(IGraphicalEditPart.class);
				decorationFigure = null;
				final IFigure decorated = part.getFigure();
				if (!interesting) {
					decorationFigure = new MaskingFigure(part);
				} else {
					decorationFigure = new InterestingFigure(part);
				}
				lastDecoration = getDecoratorTarget().addDecoration(
						decorationFigure, new NodeLocator(decorated), false);
			}
		}
		wasInteresting = interesting;
		wasFocussed = focussed;
		initialized = true;
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
