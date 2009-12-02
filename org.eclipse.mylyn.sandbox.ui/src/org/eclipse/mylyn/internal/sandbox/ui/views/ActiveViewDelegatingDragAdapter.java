/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * Initially copied from JDT
 */
@SuppressWarnings("unchecked")
public class ActiveViewDelegatingDragAdapter implements DragSourceListener {

	private TransferDragSourceListener[] fPossibleListeners;

	private List<TransferDragSourceListener> fActiveListeners;

	private TransferDragSourceListener fFinishListener;

	public ActiveViewDelegatingDragAdapter(TransferDragSourceListener[] listeners) {
		setPossibleListeners(listeners);
	}

	protected void setPossibleListeners(TransferDragSourceListener[] listeners) {
		fPossibleListeners = listeners;
	}

	public void dragStart(DragSourceEvent event) {
		fFinishListener = null;
		boolean saveDoit = event.doit;
		Object saveData = event.data;
		boolean doIt = false;
		List<Transfer> transfers = new ArrayList<Transfer>(fPossibleListeners.length);
		fActiveListeners = new ArrayList<TransferDragSourceListener>(fPossibleListeners.length);

		for (TransferDragSourceListener listener : fPossibleListeners) {
			event.doit = saveDoit;
			listener.dragStart(event);
			if (event.doit) {
				transfers.add(listener.getTransfer());
				fActiveListeners.add(listener);
			}
			doIt = doIt || event.doit;
		}
		if (doIt) {
			((DragSource) event.widget).setTransfer(transfers.toArray(new Transfer[transfers.size()]));
		}
		event.data = saveData;
		event.doit = doIt;
	}

	public void dragSetData(DragSourceEvent event) {
		fFinishListener = getListener(event.dataType);
		if (fFinishListener != null) {
			fFinishListener.dragSetData(event);
		}
	}

	public void dragFinished(DragSourceEvent event) {
		try {
			if (fFinishListener != null) {
				fFinishListener.dragFinished(event);
			} else {
				// If the user presses Escape then we get a dragFinished without
				// getting a dragSetData before.
				fFinishListener = getListener(event.dataType);
				if (fFinishListener != null) {
					fFinishListener.dragFinished(event);
				}
			}
		} finally {
			fFinishListener = null;
			fActiveListeners = null;
		}
	}

	private TransferDragSourceListener getListener(TransferData type) {
		if (type == null) {
			return null;
		}

		for (Object element : fActiveListeners) {
			TransferDragSourceListener listener = (TransferDragSourceListener) element;
			if (listener.getTransfer().isSupportedType(type)) {
				return listener;
			}
		}
		return null;
	}
}
