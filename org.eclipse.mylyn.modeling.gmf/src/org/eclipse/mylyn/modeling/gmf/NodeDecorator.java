package org.eclipse.mylyn.modeling.gmf;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.mylyn.modeling.gmf.figures.NodeLocator;
import org.eclipse.mylyn.modeling.gmf.figures.NodeMaskingFigure;

public class NodeDecorator extends ContextDecorator {

	private final EObject domainObject;

	public NodeDecorator(MylynDecoratorProvider provider, IDecoratorTarget target, EObject domainObject) {
		super(provider, target);
		this.domainObject = domainObject;
	}

	@Override
	protected void createDecoration(boolean interesting) {
		setDecorationFigure(null);
		IGraphicalEditPart part = (IGraphicalEditPart) getTarget().getAdapter(IGraphicalEditPart.class);
		final IFigure decorated = part.getFigure();
		if (!interesting) {
				setDecorationFigure(new NodeMaskingFigure(part));
				setLastDecoration(getDecoratorTarget().addDecoration(getDecorationFigure(),
						new NodeLocator(decorated), true));
		} else {
			// decorationFigure = new InterestingNodeFigure(part);
			// lastDecoration = getDecoratorTarget().addDecoration(
			// decorationFigure, new InterestingNodeLocator(decorated), true);
		}
	}

	@Override
	public boolean isInteresting() {
		return getProvider().isInteresting(domainObject);
	}
}