package org.eclipse.mylyn.modeling.papyrus;

import javax.management.relation.Relation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.gef.EditPart;
import org.eclipse.mylyn.modeling.context.IModelStructureProvider;
import org.eclipse.mylyn.modeling.ui.IModelUIProvider;
import org.eclipse.papyrus.diagram.clazz.edit.parts.ClassEditPart;
import org.eclipse.papyrus.editor.PapyrusMultiDiagramEditor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;

public class UML2DomainBridge implements IModelStructureProvider, IModelUIProvider {

	private static UML2DomainBridge INSTANCE = new UML2DomainBridge();
	
	public static final String UML2_CONTENT_TYPE = "uml2"; //$NON-NLS-1$

	@Override
	public boolean acceptsPart(IWorkbenchPart part) {
		return part instanceof PapyrusMultiDiagramEditor;
	}

	@Override
	public String getContentType() {
		return UML2_CONTENT_TYPE;
	}
	
	@Override
	public String getLabel(Object object) {
		if (object instanceof NamedElement) {
			return ((NamedElement) object).getName();
		}
		return null;
	}
	
	@Override
	public Class<?> getDomainBaseNodeClass() {
		return Element.class;
	}

	@Override
	public Class<?>[] getDomainNodeClasses() {
		return new Class[]{Classifier.class};
	}

	public Class<?> getDomainBaseEdgeClass() {
		return Relation.class;
	}

	@Override
	public Class<?>[] getDomainEdgeClasses() {
		return new Class[]{Relation.class};
	}

	public static UML2DomainBridge getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new UML2DomainBridge();
		}
		return INSTANCE;
	}

	@Override
	public boolean acceptsEditPart(EObject domainObject, EditPart part) {
		if (domainObject instanceof Classifier) {
			return part instanceof ClassEditPart;
		}
		return false;
	}
}
