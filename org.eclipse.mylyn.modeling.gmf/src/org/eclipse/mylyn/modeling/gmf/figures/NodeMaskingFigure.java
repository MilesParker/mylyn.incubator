package org.eclipse.mylyn.modeling.gmf.figures;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.Decoration;
import org.eclipse.swt.graphics.Color;

public class NodeMaskingFigure extends RectangleFigure implements IRevealableFigure {

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

		if (decorated.getParent() != null) {
			decorated = decorated.getParent();
		}
		Color backgroundColor = decorated.getBackgroundColor();
		setBackgroundColor(backgroundColor);
		setAlpha(255);
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
			target.setBounds(decorated.getBounds().getCopy());
			((IFigure) target.getChildren().get(0)).setBounds(decorated.getBounds().getCopy());
		}
	}
}
