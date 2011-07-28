package org.eclipse.mylyn.emf.context;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;

public class EcoreDiagramBridge implements IDiagramContextBridge {

	public static final String ECORE_CONTENT_TYPE = "ecore";

	@Override
	public String getContentType() {
		return ECORE_CONTENT_TYPE;
	}

	@Override
	public Class<?> getDomainBaseClass() {
		return EObject.class;
	}

	@Override
	public Class<?>[] getDomainNodeClasses() {
		return new Class[]{EClassifier.class};
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof ENamedElement) {
			return ((ENamedElement) object).getName();
		}
		return null;
	}
}
