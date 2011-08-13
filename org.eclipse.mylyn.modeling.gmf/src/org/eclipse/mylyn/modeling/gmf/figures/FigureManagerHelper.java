package org.eclipse.mylyn.modeling.gmf.figures;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.Shape;
import org.eclipse.swt.graphics.Color;

/**
 * Allows us to handle all of the various combinations of figures in a consistent way.
 * 
 * @author milesparker
 * 
 */
public class FigureManagerHelper {

	public static FigureManagerHelper INSTANCE = new FigureManagerHelper();

	public FigureManagerHelper() {
	}

	public void reveal(IFigure figure) {
		if (figure instanceof Shape) {
			((Shape) figure).setAlpha(0);
		}
		if (figure instanceof PolylineConnection) {
			((PolylineConnection) figure).setAlpha(0);
		} 
		if (figure instanceof EdgeMaskingFigure) {
			((EdgeMaskingFigure) figure).revealChildren();
		}

	}

	public void refresh(IFigure figure) {
		if (figure instanceof Shape) {
			((Shape) figure).setAlpha(255);
		}
		if (figure instanceof PolylineConnection) {
			((PolylineConnection) figure).setAlpha(255);
		} 
		if (figure instanceof EdgeMaskingFigure) {
			((EdgeMaskingFigure) figure).refreshChildren();
		}
	}

//	public void mask(IFigure figure) {
//		if (figure instanceof Shape) {
//			((Shape) figure).setAlpha(255);
//		}
//		if (figure instanceof PolylineConnection) {
//			((PolylineConnection) figure).setAlpha(255);
//		} 
//		if (figure instanceof EdgeMaskingFigure) {
//			((EdgeMaskingFigure) figure).refreshChildren();
//		}
//	}
}
