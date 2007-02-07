/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.monitor.usage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.core.net.HtmlStreamTokenizer;
import org.eclipse.mylar.core.net.HtmlStreamTokenizer.Token;
import org.eclipse.mylar.internal.context.core.MylarContextExternalizer;
import org.eclipse.mylar.internal.core.util.XmlStringConverter;
import org.eclipse.mylar.monitor.core.AbstractMonitorLog;
import org.eclipse.mylar.monitor.core.IInteractionEventListener;
import org.eclipse.mylar.monitor.core.InteractionEvent;
import org.eclipse.mylar.monitor.core.InteractionEvent.Kind;
import org.eclipse.mylar.monitor.usage.core.InteractionEventObfuscator;

/**
 * @author Mik Kersten
 * @author Ken Sueda (XML serialization)
 * 
 * TODO: use buffered output stream for better performance?
 */
public class InteractionEventLogger extends AbstractMonitorLog implements IInteractionEventListener {

	private int eventAccumulartor = 0;

	private List<InteractionEvent> queue = new ArrayList<InteractionEvent>();

	private InteractionEventObfuscator handleObfuscator = new InteractionEventObfuscator();

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z", Locale.ENGLISH);

	public InteractionEventLogger(File outputFile) {
		this.outputFile = outputFile;
	}

	public synchronized void interactionObserved(InteractionEvent event) {
		// System.err.println("> " + event);
		if (MylarUsageMonitorPlugin.getDefault().isObfuscationEnabled()) {
			String obfuscatedHandle = handleObfuscator.obfuscateHandle(event.getStructureKind(), event
					.getStructureHandle());
			event = new InteractionEvent(event.getKind(), event.getStructureKind(), obfuscatedHandle, event
					.getOriginId(), event.getNavigation(), event.getDelta(), event.getInterestContribution());
		}
		try {
			if (started) {
				String xml = getXmlForEvent(event);
				outputStream.write(xml.getBytes());
			} else {
				queue.add(event);
			}
			eventAccumulartor++;
		} catch (NullPointerException e) {
			MylarStatusHandler.log(e, "could not log interaction event");
		} catch (Throwable t) {
			MylarStatusHandler.log(t, "could not log interaction event");
		}
	}

	@Override
	public void startMonitoring() {
		super.startMonitoring();
		for (InteractionEvent queuedEvent : queue) {
			interactionObserved(queuedEvent);
		}
		queue.clear();
	}

	@Override
	public void stopMonitoring() {
		super.stopMonitoring();
		if (MylarUsageMonitorPlugin.getDefault() != null)
			MylarUsageMonitorPlugin.getDefault().incrementObservedEvents(eventAccumulartor);
		eventAccumulartor = 0;
	}

	private String getXmlForEvent(InteractionEvent event) {
		return writeLegacyEvent(event);
	}

	/**
	 * @return true if successfully cleared
	 */
	public synchronized void clearInteractionHistory() throws IOException {
		stopMonitoring();
		outputStream = new FileOutputStream(outputFile, false);
		outputStream.flush();
		outputStream.close();
		outputFile.delete();
		outputFile.createNewFile();
		startMonitoring();
	}

	public List<InteractionEvent> getHistoryFromFile(File file) {
		List<InteractionEvent> events = new ArrayList<InteractionEvent>();
		try {
			// The file may be a zip file...
			if (file.getName().endsWith(".zip")) {
				ZipFile zip = new ZipFile(file);
				if (zip.entries().hasMoreElements()) {
					ZipEntry entry = zip.entries().nextElement();
					getHistoryFromStream(zip.getInputStream(entry), events);
				}
			} else {
				InputStream reader = new FileInputStream(file);
				getHistoryFromStream(reader, events);
				reader.close();
			}

		} catch (Exception e) {
			MylarStatusHandler.log("could not read interaction history", this);
			e.printStackTrace();
		}
		return events;
	}

	/**
	 * @param events
	 * @param tag
	 * @param endl
	 * @param buf
	 */
	private void getHistoryFromStream(InputStream reader, List<InteractionEvent> events) throws IOException {
		String xml;
		int index;
		String buf = "";
		String tag = "</" + MylarContextExternalizer.ELMNT_INTERACTION_HISTORY_OLD + ">";
		String endl = "\r\n";
		byte[] buffer = new byte[1000];
		int bytesRead = 0;
		while ((bytesRead = reader.read(buffer)) != -1) {
			buf = buf + new String(buffer, 0, bytesRead);
			while ((index = buf.indexOf(tag)) != -1) {
				index += tag.length();
				xml = buf.substring(0, index);
				InteractionEvent event = readLegacyEvent(xml);
				if (event != null)
					events.add(event);

				if (index + endl.length() > buf.length()) {
					buf = "";
				} else {
					buf = buf.substring(index + endl.length(), buf.length());
				}
			}
			buffer = new byte[1000];
		}
	}

	private static final String OPEN = "<";

	private static final String CLOSE = ">";

	private static final String SLASH = "/";

	private static final String ENDL = "\n";

	private static final String TAB = "\t";

	@Deprecated
	public String writeLegacyEvent(InteractionEvent e) {
		StringBuffer res = new StringBuffer();
		String tag = "interactionEvent";
		res.append(OPEN);
		res.append(tag);
		res.append(CLOSE);
		res.append(ENDL);

		openElement(res, "kind");
		formatContent(res, e.getKind());
		closeElement(res, "kind");

		openElement(res, "date");
		formatContent(res, e.getDate());
		closeElement(res, "date");

		openElement(res, "endDate");
		formatContent(res, e.getEndDate());
		closeElement(res, "endDate");

		openElement(res, "originId");
		formatContent(res, e.getOriginId());
		closeElement(res, "originId");

		openElement(res, "structureKind");
		formatContent(res, e.getStructureKind());
		closeElement(res, "structureKind");

		openElement(res, "structureHandle");
		formatContent(res, e.getStructureHandle());
		closeElement(res, "structureHandle");

		openElement(res, "navigation");
		formatContent(res, e.getNavigation());
		closeElement(res, "navigation");

		openElement(res, "delta");
		formatContent(res, e.getDelta());
		closeElement(res, "delta");

		openElement(res, "interestContribution");
		formatContent(res, e.getInterestContribution());
		closeElement(res, "interestContribution");

		res.append(OPEN);
		res.append(SLASH);
		res.append(tag);
		res.append(CLOSE);
		res.append(ENDL);
		return res.toString();
	}

	private void formatContent(StringBuffer buffer, float interestContribution) {
		buffer.append(interestContribution);
	}

	private void formatContent(StringBuffer buffer, String content) {
		if (content != null && content.length() > 0) {
			String xmlContent;
			xmlContent = XmlStringConverter.convertToXmlString(content);
			xmlContent = xmlContent.replace("\n", "\n\t\t");
			buffer.append(xmlContent);
		}
	}

	private void formatContent(StringBuffer buffer, Kind kind) {
		buffer.append(kind.toString());
	}

	private void formatContent(StringBuffer buffer, Date date) {
		buffer.append(dateFormat.format(date));
	}

	private void openElement(StringBuffer buffer, String tag) {
		buffer.append(TAB);
		buffer.append(OPEN);
		buffer.append(tag);
		buffer.append(CLOSE);
	}

	private void closeElement(StringBuffer buffer, String tag) {
		buffer.append(OPEN);
		buffer.append(SLASH);
		buffer.append(tag);
		buffer.append(CLOSE);
		buffer.append(ENDL);
	}

	public InteractionEvent readLegacyEvent(String xml) {
		Reader reader = new StringReader(xml);
		HtmlStreamTokenizer tokenizer = new HtmlStreamTokenizer(reader, null);
		String kind = "";
		String startDate = "";
		String endDate = "";
		String originId = "";
		String structureKind = "";
		String structureHandle = "";
		String navigation = "";
		String delta = "";
		String interest = "";
		try {
			for (Token token = tokenizer.nextToken(); token.getType() != Token.EOF; token = tokenizer.nextToken()) {
				if (token.getValue().toString().equals("<kind>")) {
					kind = readStringContent(tokenizer, "</kind>");
					kind = kind.toLowerCase(Locale.ENGLISH);
				} else if (token.getValue().toString().equals("<date>")) {
					startDate = readStringContent(tokenizer, "</date>");
				} else if (token.getValue().toString().equals("<endDate>")) {
					endDate = readStringContent(tokenizer, "</endDate>");
				} else if (token.getValue().toString().equals("<originId>")) {
					originId = readStringContent(tokenizer, "</originId>");
				} else if (token.getValue().toString().equals("<structureKind>")) {
					structureKind = readStringContent(tokenizer, "</structureKind>");
				} else if (token.getValue().toString().equals("<structureHandle>")) {
					structureHandle = readStringContent(tokenizer, "</structureHandle>");
				} else if (token.getValue().toString().equals("<navigation>")) {
					navigation = readStringContent(tokenizer, "</navigation>");
				} else if (token.getValue().toString().equals("<delta>")) {
					delta = readStringContent(tokenizer, "</delta>");
				} else if (token.getValue().toString().equals("<interestContribution>")) {
					interest = readStringContent(tokenizer, "</interestContribution>");
				}
			}
			float interestFloatVal = 0;
			try {
				interestFloatVal = Float.parseFloat(interest);
			} catch (NumberFormatException nfe) {
				// ignore for empty interest values
			}
			InteractionEvent event = new InteractionEvent(Kind.fromString(kind), structureKind, structureHandle,
					originId, navigation, delta, interestFloatVal, dateFormat.parse(startDate), dateFormat
							.parse(endDate));
			return event;

		} catch (ParseException e) {
			System.err.println("readevent: " + xml);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("readevent: " + xml);
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("readevent: " + xml);
			e.printStackTrace();
		}

		return null;
	}

	private String readStringContent(HtmlStreamTokenizer tokenizer, String endTag) throws IOException, ParseException {
		StringBuffer content = new StringBuffer();
		Token token = tokenizer.nextToken();
		while (!token.getValue().toString().equals(endTag)) {
			if (content.length() > 0) {
				content.append(' ');
			}
			content.append(token.getValue().toString());
			token = tokenizer.nextToken();
		}
		return XmlStringConverter.convertXmlToString(content.toString()).trim();
	}
}
