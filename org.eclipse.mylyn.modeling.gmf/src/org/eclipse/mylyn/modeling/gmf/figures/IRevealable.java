package org.eclipse.mylyn.modeling.gmf.figures;

import org.eclipse.draw2d.IFigure;

/**
 * We have to mirror edit part functionality for Figure because we don't have access to the edit parts. Sigh.
 * 
 * @author milesparker
 * 
 */
public interface IRevealable {

	void reveal(double nearness);

	/**
	 * Returns the figure to it's original pre-revealed state, which will typically be "not shown" for a masked figure.
	 */
	void unreveal();

	/**
	 * Restores any elements -- such as connectors and text -- that could not be made alpha to their prior state before
	 * we started managing them. Note that this is different from unrevealing them. In this case, we want to reveal them
	 * completely as the result of their parent figures being restored. This is awkward but seems necessary
	 * because of decoration design.
	 */
	void restore();
}
