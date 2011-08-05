package org.eclipse.mylyn.modeling.gmf;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoration;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecorator;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;

public class MylynDecorator implements IDecorator {

	private final MylynDecoratorProvider provider;

	boolean interesting;
	private final Object model;
	private IDecoration lastDecoration;

	boolean dirty;
	boolean active;
	private IFigure decorationFigure;

	private final IDecoratorTarget target;

	public MylynDecorator(MylynDecoratorProvider provider,
			IDecoratorTarget target, Object model) {
		this.provider = provider;
		this.target = target;
		this.model = model;
		dirty = true;
	}

	@Override
	public void activate() {
		active = true;
		refresh();
	}

	@Override
	public void deactivate() {
		active = false;
		getDecoratorTarget().removeDecoration(lastDecoration);
	}

	@Override
	public void refresh() {
		if (active && dirty) {
			if (lastDecoration != null) {
				getDecoratorTarget().removeDecoration(lastDecoration);
			}
			IGraphicalEditPart part = (IGraphicalEditPart) getDecoratorTarget()
					.getAdapter(IGraphicalEditPart.class);
			decorationFigure = null;
			final IFigure decorated = part.getFigure();
			if (!interesting) {
				decorationFigure = new MaskingFigure(part);
			} else {
				decorationFigure = new InterestingFigure(part);
			}
			lastDecoration = getDecoratorTarget().addDecoration(decorationFigure,
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

	IDecoratorTarget getDecoratorTarget() {
		return target;
	}
	
//	IGraphicalEditPart getEditPart() {
//		return (IGraphicalEditPart) getDecoratorTarget()
//				.getAdapter(IGraphicalEditPart.class);
//	}

	public Object getModel() {
		return model;
	}
}
