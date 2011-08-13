package org.eclipse.mylyn.modeling.gmf;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoration;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecorator;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.mylyn.modeling.gmf.figures.IRevealable;

public abstract class ContextDecorator implements IDecorator {


	private final MylynDecoratorProvider provider;

	boolean wasInteresting;
	boolean wasFocussed;
	private IDecoration lastDecoration;

	boolean initialized;
	private IRevealable decorationFigure;

	private final IDecoratorTarget target;

	public ContextDecorator(MylynDecoratorProvider provider, IDecoratorTarget target) {
		this.provider = provider;
		this.target = target;
		target.installDecorator(MylynDecoratorProvider.MYLYN_DETAIL, this);
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
		boolean interesting = isInteresting();
		boolean focussed = provider.isFocussed();
		if (focussed != wasFocussed || interesting != wasInteresting || !initialized) {
			removeDecoration();
			if (focussed) {
				createDecoration(interesting);
			}
		}
		wasInteresting = interesting;
		wasFocussed = focussed;
		initialized = true;
	}

	protected abstract void createDecoration(boolean interesting);

	private void removeDecoration() {
		if (decorationFigure != null) {
			decorationFigure.refresh();
		}
		if (getLastDecoration() != null && ((IFigure) getLastDecoration()).getParent() != null) {
			getDecoratorTarget().removeDecoration(getLastDecoration());
			setLastDecoration(null);
		}
	}

	public MylynDecoratorProvider getProvider() {
		return provider;
	}

	IDecoratorTarget getDecoratorTarget() {
		return getTarget();
	}

	public abstract boolean isInteresting();

	public IRevealable getDecorationFigure() {
		return decorationFigure;
	}

	public void setDecorationFigure(IRevealable decorationFigure) {
//		if (decorationFigure == null && this.decorationFigure != null) {
//			((IRevealable) this.decorationFigure).restore();
//		}
		this.decorationFigure = decorationFigure;
	}

	public IDecoration getLastDecoration() {
		return lastDecoration;
	}

	public void setLastDecoration(IDecoration lastDecoration) {
		this.lastDecoration = lastDecoration;
	}

	public IDecoratorTarget getTarget() {
		return target;
	}

	protected void reveal() {
		getDecorationFigure().reveal();
	}
}
