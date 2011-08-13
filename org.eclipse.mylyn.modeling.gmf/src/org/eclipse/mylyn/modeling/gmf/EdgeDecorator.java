package org.eclipse.mylyn.modeling.gmf;

import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.mylyn.modeling.gmf.figures.EdgeLocator;
import org.eclipse.mylyn.modeling.gmf.figures.EdgeMaskingFigure;

public class EdgeDecorator extends ContextDecorator {

	private final EObject connectionSource;
	private final EObject connectionTarget;

	public EdgeDecorator(MylynDecoratorProvider provider, IDecoratorTarget target, EObject connectionSource,
			EObject connectionTarget) {
		super(provider, target);
		this.connectionSource = connectionSource;
		this.connectionTarget = connectionTarget;
	}

	@Override
	protected void createDecoration(boolean interesting) {
		IGraphicalEditPart part = (IGraphicalEditPart) getTarget().getAdapter(IGraphicalEditPart.class);
		setDecorationFigure(null);
		final IFigure decorated = part.getFigure();
		if (!interesting) {
			setDecorationFigure(new EdgeMaskingFigure(part));
			setLastDecoration(getDecoratorTarget().addDecoration(getDecorationFigure(), new EdgeLocator(decorated),
					true));

		}
	}

	@Override
	public boolean isInteresting() {
		return getProvider().isInteresting(connectionSource) && getProvider().isInteresting(connectionTarget);
	}
}