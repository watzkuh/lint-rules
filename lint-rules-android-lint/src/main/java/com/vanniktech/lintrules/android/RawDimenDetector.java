package com.vanniktech.lintrules.android;

import com.android.annotations.NonNull;
import com.android.resources.ResourceFolderType;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.ResourceXmlDetector;
import com.android.tools.lint.detector.api.XmlContext;
import java.util.Collection;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import static com.android.SdkConstants.ATTR_LAYOUT_HEIGHT;
import static com.android.SdkConstants.ATTR_LAYOUT_WIDTH;
import static com.android.resources.ResourceFolderType.DRAWABLE;
import static com.android.resources.ResourceFolderType.LAYOUT;
import static com.android.tools.lint.detector.api.Category.CORRECTNESS;
import static com.android.tools.lint.detector.api.Scope.RESOURCE_FILE_SCOPE;
import static com.android.tools.lint.detector.api.Severity.WARNING;

public final class RawDimenDetector extends ResourceXmlDetector {
  static final Issue ISSUE_RAW_DIMEN = Issue.create("RawDimen", "This value should be defined as a dimen.",
      "This value should be defined as a dimen.", CORRECTNESS, 8, WARNING,
      new Implementation(RawDimenDetector.class, RESOURCE_FILE_SCOPE));

  @Override public boolean appliesTo(@NonNull final ResourceFolderType folderType) {
    return folderType == LAYOUT || folderType == DRAWABLE;
  }

  @Override public Collection<String> getApplicableElements() {
    return ALL;
  }

  @Override public void visitElement(final XmlContext context, final Element element) {
    final NamedNodeMap attributes = element.getAttributes();

    final boolean hasLayoutWeight = attributes.getNamedItem("android:layout_weight") != null;
    final Node parentNode = element.getParentNode();
    final String parentName = parentNode != null ? parentNode.getLocalName() : null;
    final boolean isParentConstraintLayout = "android.support.constraint.ConstraintLayout".equals(parentName);
    final boolean isVectorGraphic = "vector".equals(element.getLocalName()) || "path".equals(element.getLocalName());

    for (int i = 0; i < attributes.getLength(); i++) {
      final Node item = attributes.item(i);
      final String value = item.getNodeValue();

      final boolean isToolsAttribute = "http://schemas.android.com/tools".equalsIgnoreCase(item.getNamespaceURI());
      final boolean isSuppressed = context.getDriver().isSuppressed(context, ISSUE_RAW_DIMEN, item);
      final boolean is0Dp = value.charAt(0) == '0';
      final boolean is0DpLayoutWithWeight = (hasLayoutWeight || isParentConstraintLayout) && is0Dp && (ATTR_LAYOUT_WIDTH.equals(item.getLocalName()) || ATTR_LAYOUT_HEIGHT.equals(item.getLocalName()));

      if (!isToolsAttribute && !isSuppressed && !is0DpLayoutWithWeight && !isVectorGraphic && value.matches("[\\d.]+(sp|dp|dip)")) {
        context.report(ISSUE_RAW_DIMEN, context.getValueLocation((Attr) item), "Should be using dimen instead.");
      }
    }
  }
}
