/*******************************************************************************
 * Copyright (c) 2003 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.internal.bugzilla.core;

import java.io.Serializable;

import org.eclipse.mylyn.tasks.core.RepositoryTaskData;


/**
 * This class is used to store data about the new bug that is being created
 * while the wizard is being used
 * 
 * @author Eric Booth
 * @author Rob Elves
 */
public class NewBugzillaTaskData extends RepositoryTaskData implements Serializable { 

	/** Automatically generated serialVersionUID */
	private static final long serialVersionUID = 3977859587934335283L;

	/** Whether the attributes have been parsed yet or not */
	protected boolean hasParsedAttributes = false;

	/** Whether the products have been parsed yet or not */
	protected boolean hasParsedProducts = false;

	/** The product that the bug is for */
	protected String product;

	/** The summary for the bug */
	protected String summary = "";

	/**
	 * Flag to indicate status of connection to Bugzilla server to identify
	 * whether ProductConfiguration should be used instead
	 */
	protected boolean connected = true;

	/** Whether or not this bug report is saved offline. */
	protected boolean savedOffline = false;

	/**
	 * Creates a new <code>NewBugModel</code>. The id chosen for this bug is
	 * based on the id of the last <code>NewBugModel</code> that was created.
	 */
	public NewBugzillaTaskData(String repositoryURL, String offlineId) {
		super(new BugzillaAttributeFactory(), BugzillaCorePlugin.REPOSITORY_KIND, repositoryURL, offlineId);
	}

	@Override
	public String getLabel() {
		return "<unsubmitted> "+ this.getRepositoryUrl();
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String newDescription) {
		description = newDescription;
	}

	@Override
	public String getSummary() {
		return summary;
	}

	@Override
	public void setSummary(String newSummary) {
		summary = newSummary;
	}

	/**
	 * @return The product that the bug is for.
	 */
	@Override
	public String getProduct() {
		return product;
	}

	/**
	 * Sets the product that the bug is for.
	 * 
	 * @param product
	 *            The product.
	 */
	public void setProduct(String product) {
		this.product = product;
	}

	/**
	 * @return Flag to indicate status of connection to Bugzilla server (to
	 *         identify whether ProductConfiguration should be used instead)
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Sets the value of the flag to indicate status of connection to Bugzilla
	 * server (to identify whether ProductConfiguration should be used instead)
	 * 
	 * @param newConnectionStatus
	 *            <code>true</code> if the bug is connected.
	 */
	public void setConnected(boolean newConnectionStatus) {
		connected = newConnectionStatus;
	}

	/**
	 * @return Returns whether the attributes have been parsed yet or not.
	 */
	public boolean hasParsedAttributes() {
		return hasParsedAttributes;
	}

	/**
	 * Sets whether the attributes have been parsed yet or not.
	 * 
	 * @param hasParsedAttributes
	 *            <code>true</code> if the attributes have been parsed.
	 */
	public void setParsedAttributesStatus(boolean hasParsedAttributes) {
		this.hasParsedAttributes = hasParsedAttributes;
	}

	/**
	 * @return Returns whether the products have been parsed yet or not.
	 */
	public boolean hasParsedProducts() {
		return hasParsedProducts;
	}

	/**
	 * Sets whether the products have been parsed yet or not.
	 * 
	 * @param hasParsedProducts
	 *            <code>true</code> if the products have been parsed.
	 */
	public void setParsedProductsStatus(boolean hasParsedProducts) {
		this.hasParsedProducts = hasParsedProducts;
	}

	public boolean isSavedOffline() {
		return savedOffline;
	}

	@Override
	public boolean isNew() {
		return true;
	}

//	public void setOfflineState(boolean newOfflineState) {
//		savedOffline = newOfflineState;
//	}

	@Override
	public boolean hasLocalChanges() {
		return true;
	}

	/** returns null */
	@Override
	public String getCreated() {
		return null;
	}

	@Override
	public String getLastModified() {
		return null;
	}
}
