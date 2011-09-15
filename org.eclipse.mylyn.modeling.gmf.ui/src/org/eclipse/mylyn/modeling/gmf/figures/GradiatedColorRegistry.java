/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.modeling.gmf.figures;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.modeling.gmf.GmfUiBridgePlugin;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Allows us to handle all of the various combinations of figures in a consistent way.
 * 
 * @author Miles Parker
 */
public class GradiatedColorRegistry {

	/**
	 * If the number of colors ever exceeds this threshold, we'll generate warning
	 */
	public static Integer COLOR_REPORT_THRESHOLD = 1000;

	private static boolean thresholdExceeded;

	public static GradiatedColorRegistry INSTANCE = new GradiatedColorRegistry();

	// TODO We need to revisit to make sure that we're not creating a bazillion colors here. We can do an indexed
	// version
	// if that is the case. Also, we need to be sure to dispose these properly.
	private final Map<RGB, Color> colorCache = new HashMap<RGB, Color>();

	/**
	 * Private to ensure that we can only access through singleton.
	 */
	private GradiatedColorRegistry() {
	}

	public final Color getColor(RGB rgb) {
		Color color = colorCache.get(rgb);
		if (color == null) {
			color = new Color(org.eclipse.swt.widgets.Display.getCurrent(), rgb.red, rgb.green, rgb.blue);
			colorCache.put(rgb, color);
			if (colorCache.size() > COLOR_REPORT_THRESHOLD && !thresholdExceeded) {
				thresholdExceeded = true;
				Status status = new Status(
						IStatus.WARNING,
						GmfUiBridgePlugin.PLUGIN_ID,
						"Color cache limit of " //$NON-NLS-1$
								+ COLOR_REPORT_THRESHOLD
								+ " exceeded. Please report to Mylyn Context project bugzilla. (This message will appear only once.)"); //$NON-NLS-1$
				StatusManager.getManager().handle(status, StatusManager.LOG);
			}
		}
		return color;
	}

	public Color getColor(Color color1, Color color2, float distance) {
		RGB rgb1 = new RGB(color1.getRed(), color1.getGreen(), color1.getBlue());
		float[] hsb1 = rgb1.getHSB();
		RGB rgb2 = new RGB(color2.getRed(), color2.getGreen(), color2.getBlue());
		float[] hsb2 = rgb2.getHSB();

		float[] hsb = new float[3];

		for (int i = 0; i < hsb.length; i++) {
			hsb[i] = hsb1[i] + (((hsb2[i] - hsb1[i]) * distance));
		}
		return getColor(new RGB(hsb[0], hsb[1], hsb[2]));
	}

}
