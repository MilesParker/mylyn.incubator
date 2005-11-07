/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.monitor.reports;

import java.util.Comparator;

import org.eclipse.mylar.core.InteractionEvent;


/**
 * Comparator of InteractionEvents
 * @author Gail Murphy
 *
 */
public class InteractionEventComparator implements Comparator<InteractionEvent> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(T, T)
	 */
	public int compare(InteractionEvent arg0, InteractionEvent arg1) {
		if ( arg0.equals( arg1 ) ) {
			return 0;
		}
		if ( arg0.getDate().before( arg1.getDate() )) {
			return -1;
		}
		return 1;
	}

}