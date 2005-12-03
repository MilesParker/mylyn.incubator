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
package org.eclipse.mylar.monitor.reports.internal;

import org.eclipse.mylar.core.InteractionEvent;
import org.eclipse.mylar.core.InteractionEvent.Kind;
import org.eclipse.mylar.monitor.reports.ReportGenerator;

/**
 * Test whether an InteractionEvent meets particular criteria
 * @author Gail Murphy and Mik Kersten
 */
public class InteractionEventClassifier {
	
	//TODO: Move this class into mylar reporting framework
	
	/**
	 * isEdit currently classifies selections in editor as edits. May need to split off a different version
	 */
	public static boolean isEdit(InteractionEvent event) {
		return event.getKind().equals(InteractionEvent.Kind.EDIT)
			|| (event.getKind().equals(InteractionEvent.Kind.SELECTION) && isSelectionInEditor(event));
	}

	public static boolean isSelection(InteractionEvent event) {
		return event.getKind().equals(InteractionEvent.Kind.SELECTION)
			&& !isSelectionInEditor(event);
	}
	
	public static boolean isCommand(InteractionEvent event) {
		return event.getKind().equals(InteractionEvent.Kind.COMMAND);
	}
	
	public static boolean isJavaEdit(InteractionEvent event) {
		return
			event.getKind().equals(InteractionEvent.Kind.EDIT) &&
			(event.getOriginId().contains("java") || event.getOriginId().contains("jdt.ui"));
	}
	
	public static boolean isJDTEvent(InteractionEvent event) {
		return (isEdit(event) || isSelection(event) || isCommand(event)) && getCleanOriginId(event).contains("jdt");
	}
	
	private static boolean isSelectionInEditor(InteractionEvent event) {
		return event.getOriginId().contains("Editor")
		   || event.getOriginId().contains("editor")
		   || event.getOriginId().contains("source");
	}

	public static String getCleanOriginId(InteractionEvent event) {
		String cleanOriginId = "";
		String originId = event.getOriginId();

		if (event.getKind().equals(InteractionEvent.Kind.COMMAND)) {
			for (int i = 0; i < originId.length(); i++) {
				char curChar = originId.charAt(i);
				if (!(curChar == '&')) {
					if (Character.getType(curChar) == Character.CONTROL) {
						cleanOriginId = cleanOriginId.concat(" ");
					} else {
						cleanOriginId = cleanOriginId.concat(String.valueOf(curChar));
					}
				}
			}
			return cleanOriginId;
		} else {
			return originId;
		}
	}

}
