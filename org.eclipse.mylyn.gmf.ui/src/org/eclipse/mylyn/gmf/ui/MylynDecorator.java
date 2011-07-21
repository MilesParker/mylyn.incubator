package org.eclipse.mylyn.gmf.ui;

import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoration;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecorator;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;

public class MylynDecorator implements IDecorator {

	private final IDecoratorTarget target;
	private final MylynDecoratorProvider provider;

	boolean interesting;
	private final EObject model;
	private IDecoration lastDecoration;

	boolean dirty;

	public MylynDecorator(MylynDecoratorProvider provider,
			IDecoratorTarget target, EObject model) {
		this.provider = provider;
		this.target = target;
		this.model = model;
		dirty = true;
	}

	@Override
	public void activate() {
	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh() {
		if (dirty) {
			IGraphicalEditPart part = (IGraphicalEditPart) target
					.getAdapter(IGraphicalEditPart.class);
			IFigure decoration = null;
			final IFigure decorated = part.getFigure();
			if (!interesting) {
				decoration = new MaskingFigure(part);
			} else {
				decoration = new InterestingFigure(part);
			}
			if (lastDecoration != null) {
				target.removeDecoration(lastDecoration);
			}
			lastDecoration = target.addDecoration(decoration,
					new NodeLocator(decorated), false);
			dirty = false;
		}
	}

	public void setInteresting(boolean interesting) {
		if (this.interesting != interesting) {
			dirty = true;
		}
		this.interesting = interesting;
	}

	public IDecoratorTarget getTarget() {
		return target;
	}

	public EObject getModel() {
		return model;
	}
}
