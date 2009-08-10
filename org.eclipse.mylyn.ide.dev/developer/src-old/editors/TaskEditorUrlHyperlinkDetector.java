/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.tasks.ui.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;

/**
 * @author Rob Elves
 */
public class TaskEditorUrlHyperlinkDetector implements IHyperlinkDetector {

	// URL BNF: http://www.foad.org/~abigail/Perl/url2.html
	// Source:
	// http://www.truerwords.net/articles/ut/urlactivation.html#expressions
	// Original pattern: (^|[
	// \\t\\r\\n])((ftp|http|https|gopher|mailto|news|nntp|telnet|wais|file|prospero|aim|webcal):(([A-Za-z0-9$_.+!*(),;/?:@&~=-])|%[A-Fa-f0-9]{2}){2,}(#([a-zA-Z0-9][a-zA-Z0-9$_.+!*(),;/?:@&~=%-]*))?([A-Za-z0-9$_+!*();/?:~-]))
	private static final Pattern urlPattern = Pattern
			.compile(
					"((ftp|http|https|gopher|mailto|news|nntp|telnet|wais|file|prospero|aim|webcal):(([A-Za-z0-9$_.+!*,;/?:@&~=-])|%[A-Fa-f0-9]{2}){2,}(#([a-zA-Z0-9][a-zA-Z0-9$_.+!*,;/?:@&~=%-]*))?([A-Za-z0-9$_+!*;/?:~-]))",
					Pattern.CASE_INSENSITIVE);

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {

		if (region == null || textViewer == null)
			return null;

		IDocument document = textViewer.getDocument();

		List<IHyperlink> hyperlinksFound = new ArrayList<IHyperlink>();

		int offset = region.getOffset();

		if (document == null)
			return null;

		IRegion lineInfo;
		String line;
		try {
			lineInfo = document.getLineInformationOfOffset(offset);
			line = document.get(lineInfo.getOffset(), lineInfo.getLength());
		} catch (BadLocationException ex) {
			return null;
		}

		int offsetInLine = offset - lineInfo.getOffset();

		Matcher m = urlPattern.matcher(line);

		while (m.find()) {
			if (offsetInLine >= m.start() && offsetInLine <= m.end()) {
				IHyperlink link = extractHyperlink(lineInfo.getOffset(), m);
				if (link != null)
					hyperlinksFound.add(link);
			}
		}

		if (hyperlinksFound.size() > 0) {
			return hyperlinksFound.toArray(new IHyperlink[hyperlinksFound.size()]);
		}

		return null;

	}

	private TaskEditorUrlHyperlink extractHyperlink(int lineOffset, Matcher m) {

		int start = m.start();
		int end = m.end();

		if (end == -1)
			end = m.group().length();

		start += lineOffset;
		end += lineOffset;

		IRegion sregion = new Region(start, end - start);
		return new TaskEditorUrlHyperlink(sregion, m.group());
	}

	static class TaskEditorUrlHyperlink extends URLHyperlink {

		public TaskEditorUrlHyperlink(IRegion region, String urlString) {
			super(region, urlString);
		}

		@Override
		public void open() {
			// TODO: if url is to a repository task, open task instead of url
			TasksUiUtil.openBrowser(getURLString());
		}

	}

}
