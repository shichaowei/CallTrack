/****************************************************************************
 * This demo file is part of yFiles for Java 2.12.0.1.
 * Copyright (c) 2000-2016 by yWorks GmbH, Vor dem Kreuzberg 28,
 * 72070 Tuebingen, Germany. All rights reserved.
 * 
 * yFiles demo files exhibit yFiles for Java functionalities. Any redistribution
 * of demo files in source code or binary form, with or without
 * modification, is not permitted.
 * 
 * Owners of a valid software license for a yFiles for Java version that this
 * demo is shipped with are allowed to use the demo source code as basis
 * for their own yFiles for Java powered applications. Use of such programs is
 * governed by the rights and conditions as set out in the yFiles for Java
 * license agreement.
 * 
 * THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL yWorks BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ***************************************************************************/
package demo.view.uml;

import y.geom.YDimension;
import y.view.GenericNodeRealizer;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.SmartNodeLabelModel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Helper class to synchronize the labels of the uml class realizer with the {@link UmlClassModel}. It supports
 * <ul>
 *   <li>creating labels from the model</li>
 *   <li>remove attribute or operation from model by removing its appropriate label</li>
 *   <li>add attribute or operation to model by adding an appropriate label</li>
 * </ul>
 * The mapping between labels and model item:
 * <ul>
 *   <li>label 0: name</li>
 *   <li>label 1: attribute heading</li>
 *   <li>label 2: operation heading</li>
 *   <li>label 3 to n: attributes</li>
 *   <li>label n+1 to m: operations</li>
 * </ul>
 */
class UmlClassLabelSupport {

  /** Gap between the last item of one section and the cation of the next section. */
  public static final double SECTION_GAP = 5;

  private UmlClassLabelSupport() {
  }

  /**
   * Removes and creates all labels of the given node realizer.
   */
  public static void updateAllLabels(final NodeRealizer context) {
    removeAllLabels(context);
    createAllLabels(context);
  }

  /**
   * Removes all labels of the given node realizer.
   */
  private static void removeAllLabels(final NodeRealizer context) {
    for (int i = context.labelCount() - 1; i >= 0; i--) {
      context.removeLabel(i);
    }
  }

  /**
   * Creates all labels of the given node realizer.
   */
  private static void createAllLabels(final NodeRealizer context) {
    final UmlClassModel model = getModel(context);
    double yOffset = 0d;

    // Create and add label 0: name label.
    final NodeLabel nameLabel = createNameLabel(context);
    configureLabelModel(nameLabel, false, 20d, yOffset);
    context.addLabel(nameLabel);
    yOffset += nameLabel.getHeight() + 5;

    if (model.areSectionsVisible()) {
      // Create and add label 1: attribute heading label.
      final NodeLabel attributeHeadingLabel = createCaptionLabel("Attributes");
      configureLabelModel(attributeHeadingLabel, false, 20d, yOffset);
      context.addLabel(attributeHeadingLabel);
      yOffset += attributeHeadingLabel.getHeight();

      // Create and add label 2: operation heading label. It is below all attribute labels!
      final NodeLabel operationHeadingLabel = createCaptionLabel("Operations");
      context.addLabel(operationHeadingLabel);

      // Create and add label 3 to n: attribute labels.
      if (model.areAttributesVisible()) {
        final List attributes = model.getAttributes();
        for (java.util.Iterator it = attributes.iterator(); it.hasNext(); ) {
          final String text = (String) it.next();
          final NodeLabel attributeLabel = createLabelByText(text);
          configureLabelModel(attributeLabel, false, 30d, yOffset);
          context.addLabel(attributeLabel);
          yOffset += attributeLabel.getHeight();
        }
      }

      // Configure label 2; now we know its position.
      yOffset += SECTION_GAP;
      configureLabelModel(operationHeadingLabel, false, 20d, yOffset);
      yOffset += operationHeadingLabel.getHeight();

      // Create and add label n+1 to m: operation labels.
      if (model.areOperationsVisible()) {
        final List operations = model.getOperations();
        for (java.util.Iterator it = operations.iterator(); it.hasNext(); ) {
          final String text = (String) it.next();
          final NodeLabel operationLabel = createLabelByText(text);
          configureLabelModel(operationLabel, false, 30d, yOffset);
          context.addLabel(operationLabel);
          yOffset += operationLabel.getHeight();
        }
      }
    }
  }

  /**
   * Returns the {@link UmlClassModel model} stored and visualized by the given node realizer.
   */
  static UmlClassModel getModel(final NodeRealizer context) {
    final GenericNodeRealizer gnr = (GenericNodeRealizer) context;
    return (UmlClassModel) gnr.getUserData();
  }

  /**
   * Creates a name label for the given node realizer.
   */
  private static NodeLabel createNameLabel(final NodeRealizer context) {
    final NodeLabel label = new NodeLabel();
    final UmlClassModel model = getModel(context);
    label.setText(model.getClassName());
    label.setConfiguration(UmlRealizerFactory.LABEL_CONFIG_NAME);
    label.setFontSize(16);
    label.setFontStyle(Font.BOLD);
    label.setTextColor(Color.WHITE);
    label.setInsets(new Insets(10, 0, 15, 0));
    return label;
  }

  /**
   * Creates a caption label for the given node realizer.
   */
  private static NodeLabel createCaptionLabel(final String text) {
    final NodeLabel label = new NodeLabel();
    label.setConfiguration(UmlRealizerFactory.LABEL_CONFIG_NAME);
    label.setText(text);
    label.setFontSize(14);
    label.setTextColor(Color.WHITE);
    return label;
  }

  private static NodeLabel createLabelByText(final String text) {
    final NodeLabel label = new NodeLabel();
    label.setConfiguration(UmlRealizerFactory.LABEL_CONFIG_NAME);
    label.setText(text);
    label.setFontSize(14);
    label.setTextColor(Color.DARK_GRAY);
    return label;
  }

  /**
   * Configures the model for the given label.
   */
  private static void configureLabelModel(
      final NodeLabel label,
      final boolean isCenter,
      final double xOffset,
      final double yOffset
  ) {
    final SmartNodeLabelModel model = new SmartNodeLabelModel();
    if (isCenter) {
      label.setLabelModel(model, model.createSpecificModelParameter(0, -0.5, 0, -0.5, xOffset, yOffset, 0, -1));
    } else {
      label.setLabelModel(model, model.createSpecificModelParameter(-0.5, -0.5, -0.5, -0.5, xOffset, yOffset, 0, -1));
    }
  }

  /**
   * Removes the label and its corresponding list item from the model.
   */
  public static void removeSelectedLabel(final NodeRealizer context) {
    final NodeLabel label = getSelectedLabel(context);
    if (label != null) {
      int labelIndex = indexOfLabel(context, label);
      updateSelection(context, labelIndex);
      removeLabelFromModel(context, labelIndex);
      updateAllLabels(context);
      updateRealizerSize(context);
    }
  }

  /**
   * Updates the selection when removing a label with the given index.
   */
  private static void updateSelection(final NodeRealizer context, final int labelIndexToRemove) {
    // Removing the last item -> select the second last item
    // Removing the sole item -> select none
    // Removing any other -> keep selected index
    final int modelListIndex = modelListIndexOf(context, labelIndexToRemove);
    final List modelList = getModelList(context, labelIndexToRemove);
    if (modelListIndex == modelList.size() - 1) {
      getModel(context).setSelectedListIndex(modelList.size() - 2);
    } else if (modelList.size() == 1) {
      getModel(context).setSelectedListIndex(UmlClassModel.LIST_INDEX_NONE);
    }
  }

  /**
   * Removes an attribute or operation corresponding to the given label index from the model.
   */
  private static void removeLabelFromModel(final NodeRealizer context, final int labelIndex) {
    final List modelList = getModelList(context, labelIndex);
    final int modelListIndex = modelListIndexOf(context, labelIndex);
    modelList.remove(modelListIndex);
  }

  /**
   * Returns the list of attributes or operations depending to which one the given label index belongs to.
   */
  private static List getModelList(final NodeRealizer context, final int labelIndex) {
    final UmlClassModel model = getModel(context);
    if (getModel(context).areAttributesVisible()) {
      final int attributeCount = model.getAttributes().size();
      if (labelIndex < attributeCount + 3) {
        // starts with index 3 -> labels for name, attribute and operation caption
        return model.getAttributes();
      }
    }
    return model.getOperations();
  }

  /**
   * Returns the list index of an attribute or operation depending to which one the given label index belongs to.
   */
  private static int modelListIndexOf(final NodeRealizer context, final int labelIndex) {
    final UmlClassModel model = getModel(context);
    if (getModel(context).areAttributesVisible()) {
      final int attributeCount = model.getAttributes().size();
      // starts with index 3 -> labels for name, attribute and operation caption
      if (labelIndex >= attributeCount + 3) {
        return labelIndex - 3 - attributeCount;
      }
    }
    return labelIndex - 3;
  }

  /**
   * Returns the index of the given label that belongs to the given node realizer.
   */
  private static int indexOfLabel(final NodeRealizer context, final NodeLabel label) {
    int labelIndex = -1;
    for (int i = 0; i < context.labelCount(); i++) {
      if (label == context.getLabel(i)) {
        labelIndex = i;
        break;
      }
    }
    return labelIndex;
  }

  /**
   * Adds a label and a list item to the model for a new attribute.
   */
  public static NodeLabel addAttribute(final NodeRealizer context) {
    addAttributeToModel(context, "attribute");
    updateAllLabels(context);
    updateRealizerSize(context);
    final int index = indexOfLastAttributeLabel(context);
    selectLabel(context, index);
    return context.getLabel(index);
  }

  /**
   * Adds a list item to the model for a new attribute.
   */
  private static void addAttributeToModel(final NodeRealizer context, final String text) {
    final UmlClassModel model = getModel(context);
    model.getAttributes().add(text);
  }

  /**
   * Returns the index of the labels of the last attribute.
   */
  private static int indexOfLastAttributeLabel(final NodeRealizer context) {
    final UmlClassModel model = getModel(context);
    return model.getAttributes().size() + 2;
  }

  /**
   * Adds a label and a list item to the model for a new operation.
   */
  public static NodeLabel addOperation(final NodeRealizer context) {
    addOperationToModel(context, "operation");
    updateAllLabels(context);
    updateRealizerSize(context);
    final int index = indexOfLastOperationLabel(context);
    selectLabel(context, index);
    return context.getLabel(index);
  }

  /**
   * Adds a list item to the model for a new operation.
   */
  private static void addOperationToModel(final NodeRealizer context, final String text) {
    final UmlClassModel model = getModel(context);
    model.getOperations().add(text);
  }

  /**
   * Returns the index of the labels of the last operation.
   */
  private static int indexOfLastOperationLabel(final NodeRealizer context) {
    final UmlClassModel model = getModel(context);
    if (getModel(context).areAttributesVisible()) {
      return model.getAttributes().size() + model.getOperations().size() + 2;
    } else
      return model.getOperations().size() + 2;
  }

  /**
   * Updates the attribute or operation corresponding to the given label with its text.
   */
  public static void updateLabelText(final NodeRealizer context, final NodeLabel label) {
    final String text = label.getText();
    final int labelIndex = indexOfLabel(context, label);
    updateLabelTextInModel(context, labelIndex, text);
  }

  /**
   * Updates the attribute or operation corresponding to the given label index with the given text.
   */
  private static void updateLabelTextInModel(final NodeRealizer context, final int labelIndex, final String text) {
    if (labelIndex == 0) {
      final UmlClassModel model = getModel(context);
      model.setClassName(text);
    } else {
      final List modelList = getModelList(context, labelIndex);
      final int modelListIndex = modelListIndexOf(context, labelIndex);
      modelList.set(modelListIndex, text);
    }
  }

  /**
   * Recalculates the size of the given node realizer.
   */
  public static void updateRealizerSize(final NodeRealizer context) {
    final double x = context.getX();
    final double y = context.getY();
    final YDimension minimumSize = context.getSizeConstraintProvider().getMinimumSize();
    final double width = Math.max(context.getWidth(), minimumSize.getWidth());
    context.setSize(width, minimumSize.getHeight());
    context.setLocation(x, y);
  }

  /**
   * Selects the list item of the label that contains the given point.
   */
  public static boolean selectListItemAt(final NodeRealizer context, final double x, final double y) {
    if (!UmlClassLabelSupport.getModel(context).areSectionsVisible()) {
      return false;
    }

    final int labelIndex = indexOfLabelAt(context, x, y);
    if (labelIndex > 2) {
      selectLabel(context, labelIndex);
      return true;
    }
    return false;
  }

  /**
   * Returns the index of the label that contains the given point.
   */
  private static int indexOfLabelAt(final NodeRealizer context, final double x, final double y) {
    for (int i = 3; i < context.labelCount(); i++) {
      final NodeLabel label = context.getLabel(i);
      if (labelContains(context, label, x, y)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Selects the list item corresponding to the given label index.
   */
  private static void selectLabel(final NodeRealizer context, final int labelIndex) {
    final UmlClassModel model = getModel(context);
    if (model.getAttributes() == getModelList(context, labelIndex)) {
      getModel(context).setSelectedList(UmlClassModel.LIST_ATTRIBUTES);
    } else {
      getModel(context).setSelectedList(UmlClassModel.LIST_OPERATIONS);
    }
    getModel(context).setSelectedListIndex(modelListIndexOf(context, labelIndex));
  }

  /**
   * Selects the list item corresponding to the given label.
   */
  public static void selectLabel(final NodeRealizer context, final NodeLabel label) {
    final int labelIndex = indexOfLabel(context, label);
    selectLabel(context, labelIndex);
  }

  /**
   * Checks whether or not the given label contains the given point.
   */
  private static boolean labelContains(final NodeRealizer context, final NodeLabel label, final double x, final double y) {
   final double lx = context.getX();
   final double ly = label.getLocation().getY();
   final double lw = context.getWidth();
   final double lh = label.getHeight();
    return x >= lx && x <= lx + lw &&
           y >= ly && y <= ly + lh;
  }

  /**
   * Returns the area of the given label.
   */
  public static void getLabelArea(final NodeRealizer context, final NodeLabel label, final Rectangle2D rect) {
    rect.setFrame(
        context.getX(),
        label.getLocation().getY(),
        context.getWidth(),
        label.getHeight());
  }

  /**
   * Returns the label that is selected.
   */
  public static NodeLabel getSelectedLabel(final NodeRealizer context) {
    final int listIndex = getModel(context).getSelectedListIndex();
    if (listIndex < 0 || !isSelectedSectionVisible(context)) {
      return null;
    }

    final int list = getModel(context).getSelectedList();
    final int labelIndex = indexOfLabel(context, list, listIndex);
    return context.getLabel(labelIndex);
  }

  /**
   * Returns the index of the label corresponding to the item with the given index in the given list.
   */
  private static int indexOfLabel(final NodeRealizer context, final int list, final int index) {
    if ((list == UmlClassModel.LIST_ATTRIBUTES) || !getModel(context).areAttributesVisible()) {
      return index + 3;
    } else {
      final UmlClassModel model = getModel(context);
      return model.getAttributes().size() + index + 3;
    }
  }

  /**
   * Checks whether or not the section with the selection is visible.
   */
  private static boolean isSelectedSectionVisible(final NodeRealizer context) {
    final int list = getModel(context).getSelectedList();
    return UmlClassLabelSupport.getModel(context).areSectionsVisible() &&
           (getModel(context).areAttributesVisible() && (list == UmlClassModel.LIST_ATTRIBUTES)) ||
           (getModel(context).areOperationsVisible() && (list == UmlClassModel.LIST_OPERATIONS));
  }

  /**
   * Checks whether or not an attribute item is selected.
   */
  public static boolean isAttributeSelected(final NodeRealizer context) {
    return (getModel(context).getSelectedList() == UmlClassModel.LIST_ATTRIBUTES) &&
           getModel(context).getSelectedListIndex() >= 0;

  }

  /**
   * Checks whether or not an operation item is selected.
   */
  public static boolean isOperationSelected(final NodeRealizer context) {
    return (getModel(context).getSelectedList() == UmlClassModel.LIST_OPERATIONS) &&
           getModel(context).getSelectedListIndex() >= 0;

  }

  /**
   * Returns the label of the class name.
   */
  static NodeLabel getNameLabel(final NodeRealizer context) {
    return context.getLabel(0);
  }

  /**
   * Returns the label of the caption of the attribute section.
   */
  static NodeLabel getAttributeCaptionLabel(final NodeRealizer context) {
    return context.getLabel(1);
  }

  /**
   * Returns the label of the caption of the operation section.
   */
  static NodeLabel getOperationCaptionLabel(final NodeRealizer context) {
    return context.getLabel(2);
  }

  /**
   * Checks whether or not the given label is a caption label e.g. shows "Attributes" or "Operations".
   */
  static boolean isCaptionLabel(final NodeLabel label) {
    final int labelIndex = indexOfLabel(label.getRealizer(), label);
    return (labelIndex == 1) || (labelIndex == 2);
  }
}
