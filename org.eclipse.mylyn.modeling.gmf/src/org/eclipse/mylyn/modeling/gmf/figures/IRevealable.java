package org.eclipse.mylyn.modeling.gmf.figures;

import org.eclipse.draw2d.IFigure;

/**
 * We have to mirror edit part functionality for Figure because we don't have access to the edit parts. Sigh.
 * @author milesparker
 *
 */
public interface IRevealable extends IFigure {

	void reveal();
//
//	void revealChildren();

	void refresh();
//
//	void refreshChildren();
}
