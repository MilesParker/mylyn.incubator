package org.eclipse.mylyn.modeling.gmf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoration;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecorator;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.mylyn.modeling.gmf.figures.IRevealableFigure;

public abstract class ContextDecorator implements IDecorator {

	private final MylynDecoratorProvider provider;

	boolean wasInteresting;
	boolean wasLandmark;
	boolean wasFocussed;

	private IFigure decoratedFigure;
	boolean initialized;
	private List<IRevealableFigure> decorationFigures;
	private List<IDecoration> decorations;

	private final IDecoratorTarget target;

	public ContextDecorator(MylynDecoratorProvider provider, IDecoratorTarget target) {
		this.provider = provider;
		this.target = target;
		decorationFigures = new ArrayList<IRevealableFigure>();
		decorations = new ArrayList<IDecoration>();
		target.installDecorator(MylynDecoratorProvider.MYLYN_DETAIL, this);
	}

	@Override
	public void activate() {
		refresh();
	}

	@Override
	public void deactivate() {
		initialized = false;
		removeDecorations();
	}

	@Override
	public void refresh() {
		boolean interesting = isInteresting();
		boolean landmark = isLandmark();
		boolean focussed = provider.isFocussed();
		if (focussed != wasFocussed || interesting != wasInteresting || landmark != wasLandmark || !initialized) {
			removeDecorations();
			if (focussed) {
				createDecoration();
			}
		}
		wasInteresting = interesting;
		wasLandmark = landmark;
		wasFocussed = focussed;
		initialized = true;
	}

	protected abstract void createDecoration();

	IDecoration addDecoration(IRevealableFigure decorationFigure) {
		IDecoration decoration = getDecoratorTarget().addDecoration(decorationFigure, decorationFigure, true);
		decorationFigures.add(decorationFigure);
		decorations.add(decoration);
		return decoration;
	}

	protected void removeDecorations() {
		for (IRevealableFigure figure : decorationFigures) {
			figure.restore();
			IGraphicalEditPart part = (IGraphicalEditPart) getTarget().getAdapter(IGraphicalEditPart.class);
			RevealMouseListener listenerForRoot = provider.getListenerForRoot(part.getRoot());
			if (listenerForRoot != null) {
				listenerForRoot.removeDecoration(figure);
			}
		}
		decorationFigures.clear();
		for (IDecoration decoration : decorations) {
			getDecoratorTarget().removeDecoration(decoration);
		}
		decorations.clear();
	}

	public MylynDecoratorProvider getProvider() {
		return provider;
	}

	IDecoratorTarget getDecoratorTarget() {
		return getTarget();
	}

	public abstract boolean isInteresting();

	public abstract boolean isLandmark();
	
	public IDecoratorTarget getTarget() {
		return target;
	}

	IGraphicalEditPart getEditPart() {
		return (IGraphicalEditPart) getTarget().getAdapter(IGraphicalEditPart.class);
	}
	
	/**
	 * Assumes that part has already been created.
	 * @return
	 */
	public IFigure getDecoratedFigure() {
		if (decoratedFigure == null && getEditPart() != null) {
			return getEditPart().getFigure();
		}
		return decoratedFigure;
	}

	public List<IRevealableFigure> getDecorationFigures() {
		return decorationFigures;
	}
}
