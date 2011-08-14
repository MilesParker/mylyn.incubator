package org.eclipse.mylyn.modeling.gmf.figures;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.Shape;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * Allows us to handle all of the various combinations of figures in a consistent way.
 * 
 * @author milesparker
 * 
 */
public class FigureManagerHelper {

	public static FigureManagerHelper INSTANCE = new FigureManagerHelper();

    protected Map<RGB, Color> colorCache = new HashMap<RGB, Color>();

    
	public FigureManagerHelper() {
	}

	/**
	 * Note: Remeber taht we are working with a masking figure. Therefore "revealed" is when this mask is fully transparent, i.e. alpha is 225.
	 * @param figure
	 * @param nearness bounded in [0,1]
	 */
	public void reveal(IFigure figure, double nearness) {
		int alpha = (int) (255 - (nearness * 255.0));
		if (figure instanceof Shape) {
			((Shape) figure).setAlpha(alpha);
		}
		if (figure instanceof PolylineConnection) {
			((PolylineConnection) figure).setAlpha(alpha);
		} 
		if (figure instanceof EdgeMaskingFigure) {
			((EdgeMaskingFigure) figure).reveal(nearness);
		}
	}

	/**
	 * Note: Remeber taht we are working with a masking figure. Therefore "revealed" is when this mask is fully transparent, i.e. alpha is 225.
	 * @param figure
	 * @param nearness bounded in [0,1]
	 */
	public void reveal(IFigure figure, Color color1, Color color2, double nearness) {
		figure.setForegroundColor(getColor(color1, color2, (float) nearness));
	}
	
	/**
	 * 
	 * @param figure
	 * @param nearness bounded in [0,1]
	 */
	public void unreveal(IFigure figure) {
		if (figure instanceof Shape) {
			((Shape) figure).setAlpha(255);
		}
		if (figure instanceof PolylineConnection) {
			((PolylineConnection) figure).setAlpha(255);
		} 
		if (figure instanceof EdgeMaskingFigure) {
			((EdgeMaskingFigure) figure).unreveal();
		}
	}

    public final Color create(RGB rgb) {
        Color color = colorCache.get(rgb);
        if (color == null) {
            color = new Color(org.eclipse.swt.widgets.Display.getCurrent(), rgb.red, rgb.green, rgb.blue);
            colorCache.put(rgb, color);
        }
        return color;
    }
    
    public Color getColor(Color color1, Color color2, float distance) {
    	RGB rgb1 = new RGB(color1.getRed(), color1.getGreen(), color1.getBlue());
    	float[] hsb1 = rgb1.getHSB();
    	RGB rgb2 = new RGB(color2.getRed(), color2.getGreen(), color2.getBlue());
    	float[] hsb2 = rgb2.getHSB();

    	float[] hsb = new float[3];
    	
    	for (int i = 0; i<hsb.length; i++) {
    		hsb[i] = hsb1[i] + (((hsb2[i] - hsb1[i]) * distance) / 2.0f);
		}
    	return create(new RGB(hsb[0], hsb[1], hsb[2]));
     }
}