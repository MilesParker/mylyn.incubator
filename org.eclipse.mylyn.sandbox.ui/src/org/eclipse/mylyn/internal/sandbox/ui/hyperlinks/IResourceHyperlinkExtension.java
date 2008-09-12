/*******************************************************************************
* Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jingwen Ou - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.ui.hyperlinks;

import org.eclipse.jface.text.hyperlink.IHyperlink;

/**
 * @author Jingwen Ou
 */
public interface IResourceHyperlinkExtension {

	/**
	 * Returns specific hyperlinks in text.
	 */
	IHyperlink[] findHyperlink(String text, int lineOffset, int regionOffset);

}
