/*******************************************************************************
 * Copyright (c) 2015-2020 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.windhoverlabs.display.representation;

import static org.csstudio.display.builder.representation.ToolkitRepresentation.logger;

import com.windhoverlabs.display.model.widgets.CommanderCommandActionButtonWidget;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.model.properties.ActionInfos;
import org.csstudio.display.builder.model.properties.OpenDisplayActionInfo;
import org.csstudio.display.builder.model.properties.RotationStep;
import org.csstudio.display.builder.model.properties.StringWidgetProperty;
import org.csstudio.display.builder.model.properties.WritePVActionInfo;
import org.csstudio.display.builder.model.widgets.ActionButtonWidget;
import org.csstudio.display.builder.representation.javafx.Cursors;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.display.builder.representation.javafx.Messages;
import org.csstudio.display.builder.representation.javafx.widgets.RegionBaseRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.TooltipSupport;
import org.phoebus.framework.macros.MacroHandler;
import org.phoebus.framework.macros.MacroValueProvider;
import org.phoebus.ui.javafx.Styles;
import org.phoebus.ui.javafx.TextUtils;

/**
 * Creates JavaFX item for model widget
 *
 * @author Megan Grodowitz
 * @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class CommanderActionButtonRepresentation
    extends RegionBaseRepresentation<Pane, CommanderCommandActionButtonWidget> {
  // Uses a Button if there is only one action,
  // otherwise a MenuButton so that user can select the specific action.
  //
  // These two types were chosen because they share the same ButtonBase base class.
  // ChoiceBox is not derived from ButtonBase, plus it has currently selected 'value',
  // and with action buttons it wouldn't make sense to select one of the actions.
  //
  // The 'base' button is wrapped in a 'pane'
  // to allow replacing the button as actions change from single actions (or zero)
  // to multiple actions.

  private final DirtyFlag dirty_representation = new DirtyFlag();
  private final DirtyFlag dirty_enablement = new DirtyFlag();
  private final DirtyFlag dirty_actionls = new DirtyFlag();

  private volatile ButtonBase base;
  private volatile String background;
  private volatile Color foreground;
  private volatile String button_text;
  private volatile boolean enabled = true;
  private volatile boolean writable = true;

  // Had to do this because the ones in GroupRepresentation are scoped to the package
  static final BorderWidths EDIT_NONE_BORDER =
      new BorderWidths(0.5, 0.5, 0.5, 0.5, false, false, false, false);
  static final BorderStrokeStyle EDIT_NONE_DASHED =
      new BorderStrokeStyle(
          StrokeType.INSIDE,
          StrokeLineJoin.MITER,
          StrokeLineCap.BUTT,
          10,
          0,
          List.of(
              Double.valueOf(11.11),
              Double.valueOf(7.7),
              Double.valueOf(3.3),
              Double.valueOf(7.7)));

  /**
   * Was there ever any transformation applied to the jfx_node?
   *
   * <p>Used to optimize: If there never was a rotation, don't even _clear()_ it to keep the Node's
   * nodeTransformation == null
   */
  private boolean was_ever_transformed = false;

  /**
   * Is it a 'Write PV' action?
   *
   * <p>If not, we don't have to disable the button if the PV is readonly and/or disconnected
   */
  private volatile boolean is_writePV = false;

  /** Optional modifier of the open display 'target */
  private Optional<OpenDisplayActionInfo.Target> target_modifier = Optional.empty();

  private Pane pane;

  private final UntypedWidgetPropertyListener buttonChangedListener = this::buttonChanged;
  private final UntypedWidgetPropertyListener representationChangedListener =
      this::representationChanged;
  private final WidgetPropertyListener<Boolean> enablementChangedListener = this::enablementChanged;
  private final UntypedWidgetPropertyListener pvsListener = this::pvsChanged;

  @Override
  protected boolean isFilteringEditModeClicks() {
    return true;
  }

  @Override
  public Pane createJFXNode() throws Exception {
    updateColors();
    base = makeBaseButton();

    pane = new Pane();
    pane.getChildren().add(base);

    return pane;
  }

  /** @param event Mouse event to check for target modifier keys */
  private void checkModifiers(final MouseEvent event) {
    if (!enabled) {
      // Do not let the user click a disabled button
      event.consume();
      base.disarm();
      return;
    }

    // 'control' ('command' on Mac OS X)
    if (event.isShortcutDown()) target_modifier = Optional.of(OpenDisplayActionInfo.Target.TAB);
    else if (event.isShiftDown())
      target_modifier = Optional.of(OpenDisplayActionInfo.Target.WINDOW);
    else target_modifier = Optional.empty();

    // At least on Linux, a Control-click or Shift-click
    // will not 'arm' the button, so the click is basically ignored.
    // Force the 'arm', so user can Control-click or Shift-click to
    // invoke the button
    if (target_modifier.isPresent()) {
      logger.log(
          Level.FINE, "{0} modifier: {1}", new Object[] {model_widget, target_modifier.get()});
      base.arm();
    }
  }

  //    private int calls = 0;

  /**
   * Create <code>base</code>, either single-action button or menu for selecting one out of N
   * actions
   */
  private ButtonBase makeBaseButton() {
    final ActionInfos actions = model_widget.propActions().getValue();
    final ButtonBase result;
    boolean has_non_writePVAction = false;

    for (final ActionInfo action : actions.getActions()) {
      if (action instanceof WritePVActionInfo) is_writePV = true;
      else has_non_writePVAction = true;
    }

    if (actions.isExecutedAsOne() || actions.getActions().size() < 2) {
      final Button button = new Button();
      button.setGraphic(new ImageView("/icons/send.png"));
      button.setOnAction(event -> sendCommand());
      result = button;
    } else {
      // If there is at least one non-WritePVActionInfo then is_writePV should be false
      is_writePV = !has_non_writePVAction;

      final MenuButton button = new MenuButton();
      // Experimenting with ways to force update of popup location,
      // #226
      button
          .showingProperty()
          .addListener(
              (prop, old, showing) -> {
                if (showing) {
                  // System.out.println("Showing " + model_widget + " menu: " + showing);
                  //                    if (++calls > 2)
                  //                    {
                  //                        System.out.println("Hack!");
                  //                        if (button.getPopupSide() == Side.BOTTOM)
                  //                            button.setPopupSide(Side.LEFT);
                  //                        else
                  //                            button.setPopupSide(Side.BOTTOM);
                  //                        // button.layout();
                  //                    }
                }
              });
      for (final ActionInfo action : actions.getActions()) {
        final MenuItem item =
            new MenuItem(
                makeActionText(action),
                new ImageView(new Image(action.getType().getIconURL().toExternalForm())));
        item.getStyleClass().add("action_button_item");
        item.setOnAction(event -> confirm(() -> handleAction(action)));
        button.getItems().add(item);
      }
      result = button;
    }

    result.setStyle(background);

    // In edit mode, show dashed border for transparent/invisible widget
    if (toolkit.isEditMode() && model_widget.propTransparent().getValue())
      result.setBorder(
          new Border(
              new BorderStroke(
                  Color.BLACK, EDIT_NONE_DASHED, CornerRadii.EMPTY, EDIT_NONE_BORDER)));
    result.getStyleClass().add("action_button");
    result.setMnemonicParsing(false);

    // Model has width/height, but JFX widget has min, pref, max size.
    // updateChanges() will set the 'pref' size, so make min use that as well.
    result.setMinSize(ButtonBase.USE_PREF_SIZE, ButtonBase.USE_PREF_SIZE);

    // Monitor keys that modify the OpenDisplayActionInfo.Target.
    // Use filter to capture event that's otherwise already handled.
    if (!toolkit.isEditMode())
      result.addEventFilter(MouseEvent.MOUSE_PRESSED, this::checkModifiers);

    // Need to attach TT to the specific button, not the common jfx_node Pane
    TooltipSupport.attach(result, model_widget.propTooltip());

    return result;
  }

  /** Called by ContextMenuSupport when an action menu is selected */
  public void handleContextMenuAction(ActionInfo action) {
    if (action instanceof WritePVActionInfo && !writable) {
      logger.log(Level.FINE, "{0} ignoring WritePVActionInfo because of readonly PV", model_widget);
      return;
    }

    confirm(() -> toolkit.fireAction(model_widget, action));
  }

  private void confirm(final Runnable action) {
    System.out.println("confirm trigger");
    Platform.runLater(
        () -> {
          // If confirmation is requested..
          if (model_widget.propConfirmDialog().getValue()) {
            final String message = model_widget.propConfirmMessage().getValue();
            final String password = model_widget.propPassword().getValue();
            // .. check either with password or generic Ok/Cancel prompt
            if (password.length() > 0) {
              if (toolkit.showPasswordDialog(model_widget, message, password) == null) return;
            } else if (!toolkit.showConfirmationDialog(model_widget, message)) return;
          }

          action.run();
        });
  }

  /**
   * Triggered by button click. It will gather command arguments(if any) and send the specified
   * command to commander.
   */
  private void sendCommand() {
    System.out.println("sendCommand");
    System.out.println("command name:" + model_widget.propCommand().getValue());
    System.out.println("pv name :" + model_widget.propPvs().getValue().get(0).pv().getValue());
    System.out.println("pv value:" + model_widget.propPvs().getValue().get(0).value().getValue());
  }

  /** @return Should 'label' show the PV's current value? */
  private boolean isLabelValue() {
    final StringWidgetProperty text_prop = (StringWidgetProperty) model_widget.propText();
    return ActionButtonWidget.VALUE_LABEL.equals(text_prop.getSpecification());
  }

  private String makeButtonText() {
    // If text is "$(actions)", evaluate the actions ourself because
    // a) That way we can format it beyond just "[ action1, action2, ..]"
    // b) Macro won't be re-evaluated as actions change,
    //    while this code will always use current actions
    final StringWidgetProperty text_prop = (StringWidgetProperty) model_widget.propText();
    if (isLabelValue())
      //            return FormatOptionHandler.format(model_widget.runtimePropValue().getValue(),
      // FormatOption.DEFAULT, -1, true);
      return "dummy_pv";
    else if ("$(actions)".equals(text_prop.getSpecification())) {
      final List<ActionInfo> actions = model_widget.propActions().getValue().getActions();
      if (actions.size() < 1) return Messages.ActionButton_NoActions;
      if (actions.size() > 1) {
        if (model_widget.propActions().getValue().isExecutedAsOne())
          return MessageFormat.format(Messages.ActionButton_N_ActionsAsOneFmt, actions.size());

        return MessageFormat.format(Messages.ActionButton_N_ActionsFmt, actions.size());
      }
      return makeActionText(actions.get(0));
    } else return text_prop.getValue();
  }

  private String makeActionText(final ActionInfo action) {
    String action_str = action.getDescription();
    if (action_str.isEmpty()) action_str = action.toString();
    String expanded;
    try {
      final MacroValueProvider macros = model_widget.getMacrosOrProperties();
      expanded = MacroHandler.replace(macros, action_str);
    } catch (final Exception ex) {
      logger.log(
          Level.WARNING,
          model_widget + " action " + action + " cannot expand macros for " + action_str,
          ex);
      expanded = action_str;
    }
    return expanded;
  }

  /** @param actions Actions that the user invoked */
  private void handleActions(final List<ActionInfo> actions) {
    for (ActionInfo action : actions) handleAction(action);
  }

  /**
   * @param action Action that the user invoked * In the context of commander, this means sending a
   *     command to the server, which in turn sends it to the vehicle.
   */
  private void handleAction(ActionInfo action) {
    // Keyboard presses are not supressed so check if the widget is enabled
    System.out.println("$$$$handleAction$$$");
    // send command to yamcs
    model_widget.getPropertyValue("");

    if (!enabled) return;

    logger.log(Level.FINE, "{0} pressed", model_widget);

    if (action instanceof WritePVActionInfo && !writable) {
      logger.log(Level.FINE, "{0} ignoring WritePVActionInfo because of readonly PV", model_widget);
      return;
    }

    if (action instanceof OpenDisplayActionInfo && target_modifier.isPresent()) {
      final OpenDisplayActionInfo orig = (OpenDisplayActionInfo) action;
      action =
          new OpenDisplayActionInfo(
              orig.getDescription(),
              orig.getFile(),
              orig.getMacros(),
              target_modifier.get(),
              orig.getPane());
    }
    toolkit.fireAction(model_widget, action);
  }

  @Override
  protected void registerListeners() {
    updateColors();
    super.registerListeners();

    model_widget.propWidth().addUntypedPropertyListener(representationChangedListener);
    model_widget.propHeight().addUntypedPropertyListener(representationChangedListener);
    model_widget.propText().addUntypedPropertyListener(representationChangedListener);
    model_widget.propFont().addUntypedPropertyListener(representationChangedListener);
    model_widget.propRotationStep().addUntypedPropertyListener(representationChangedListener);

    model_widget.propEnabled().addPropertyListener(enablementChangedListener);
    model_widget.runtimePropPVWritable().addPropertyListener(enablementChangedListener);

    model_widget.propBackgroundColor().addUntypedPropertyListener(buttonChangedListener);
    model_widget.propForegroundColor().addUntypedPropertyListener(buttonChangedListener);
    model_widget.propTransparent().addUntypedPropertyListener(buttonChangedListener);
    model_widget.propActions().addUntypedPropertyListener(buttonChangedListener);
    //        model_widget.propPvs().getValue().get(0).addUntypedPropertyListener(pvsListener);

    //        if (! toolkit.isEditMode()  &&  isLabelValue())
    //
    // model_widget.runtimePropValue().addUntypedPropertyListener(representationChangedListener);

    enablementChanged(null, null, null);
  }

  @Override
  protected void unregisterListeners() {
    //        if (! toolkit.isEditMode()  &&  isLabelValue())
    //
    // model_widget.runtimePropValue().removePropertyListener(representationChangedListener);
    model_widget.propWidth().removePropertyListener(representationChangedListener);
    model_widget.propHeight().removePropertyListener(representationChangedListener);
    model_widget.propText().removePropertyListener(representationChangedListener);
    model_widget.propFont().removePropertyListener(representationChangedListener);
    model_widget.propRotationStep().removePropertyListener(representationChangedListener);
    model_widget.propEnabled().removePropertyListener(enablementChangedListener);
    model_widget.runtimePropPVWritable().removePropertyListener(enablementChangedListener);
    model_widget.propBackgroundColor().removePropertyListener(buttonChangedListener);
    model_widget.propForegroundColor().removePropertyListener(buttonChangedListener);
    model_widget.propTransparent().removePropertyListener(buttonChangedListener);
    model_widget.propActions().removePropertyListener(buttonChangedListener);
    super.unregisterListeners();
  }

  @Override
  protected void attachTooltip() {
    // Cannot attach tool tip to the jfx_node (Pane).
    // Needs to be attached to actual button, which
    // is done in makeBaseButton()
  }

  /** Complete button needs to be updated */
  private void buttonChanged(
      final WidgetProperty<?> property, final Object old_value, final Object new_value) {
    dirty_actionls.mark();
    representationChanged(property, old_value, new_value);
  }

  /** Only details of the existing button need to be updated */
  private void representationChanged(
      final WidgetProperty<?> property, final Object old_value, final Object new_value) {
    updateColors();
    dirty_representation.mark();
    toolkit.scheduleUpdate(this);
  }

  /** Only details of the existing button need to be updated */
  private void pvsChanged(
      final WidgetProperty<?> property, final Object old_value, final Object new_value) {
    System.out.println("pvsChanged");
    //        updateColors();
    //        dirty_representation.mark();
    //        toolkit.scheduleUpdate(this);
  }

  /** enabled or pv_writable changed */
  private void enablementChanged(
      final WidgetProperty<Boolean> property, final Boolean old_value, final Boolean new_value) {
    enabled = model_widget.propEnabled().getValue();
    writable = model_widget.runtimePropPVWritable().getValue();
    // If clicking on the button would result in a PV write then enabled has to be false if PV is
    // not writable
    if (is_writePV) enabled &= writable;
    dirty_enablement.mark();
    toolkit.scheduleUpdate(this);
  }

  private void updateColors() {
    foreground = JFXUtil.convert(model_widget.propForegroundColor().getValue());
    if (model_widget.propTransparent().getValue())
      // Set most colors to transparent, including the 'arrow' used by MenuButton
      background =
          "-fx-background: transparent; -fx-color: transparent; -fx-focus-color: rgba(3,158,211,0.1); -fx-mark-color: transparent; -fx-background-color: transparent;";
    else background = JFXUtil.shadedStyle(model_widget.propBackgroundColor().getValue());
  }

  @Override
  public void updateChanges() {
    super.updateChanges();
    if (dirty_actionls.checkAndClear()) {
      base = makeBaseButton();
      jfx_node.getChildren().setAll(base);
    }
    if (dirty_representation.checkAndClear()) {
      button_text = makeButtonText();
      base.setText(button_text);
      base.setTextFill(foreground);
      base.setFont(JFXUtil.convert(model_widget.propFont().getValue()));

      // If widget is not wide enough to show the label, hide menu button 'arrow'.
      if (base instanceof MenuButton) {
        // Assume that desired gap and arrow occupy similar space as "__VV_".
        // Check if the text exceeds the width.
        final Dimension2D size = TextUtils.computeTextSize(base.getFont(), button_text + "__VV_");
        final boolean hide = size.getWidth() >= model_widget.propWidth().getValue();
        Styles.update(base, "hide_arrow", hide);
      }

      final RotationStep rotation = model_widget.propRotationStep().getValue();
      final int width = model_widget.propWidth().getValue(),
          height = model_widget.propHeight().getValue();
      // Button 'base' is inside 'jfx_node' Pane.
      // Rotation needs to be applied to the Pane,
      // which then auto-sizes to the 'base' Button dimensions.
      // If transforming the Button instead of the Pane,
      // it will still remain sensitive to mouse clicks in the
      // original, un-transformed rectangle. Unclear why.
      // Applying the transformation to the Pane does not exhibit this problem.
      switch (rotation) {
        case NONE:
          base.setPrefSize(width, height);
          if (was_ever_transformed) jfx_node.getTransforms().clear();
          break;
        case NINETY:
          base.setPrefSize(height, width);
          jfx_node
              .getTransforms()
              .setAll(new Rotate(-rotation.getAngle()), new Translate(-height, 0));
          was_ever_transformed = true;
          break;
        case ONEEIGHTY:
          base.setPrefSize(width, height);
          jfx_node
              .getTransforms()
              .setAll(new Rotate(-rotation.getAngle()), new Translate(-width, -height));
          was_ever_transformed = true;
          break;
        case MINUS_NINETY:
          base.setPrefSize(height, width);
          jfx_node
              .getTransforms()
              .setAll(new Rotate(-rotation.getAngle()), new Translate(0, -width));
          was_ever_transformed = true;
          break;
      }
    }
    if (dirty_enablement.checkAndClear()) {
      // Don't disable the widget, because that would also remove the
      // tooltip
      // Just apply a style that matches the disabled look.
      Styles.update(base, Styles.NOT_ENABLED, !enabled);
      // Apply the cursor to the pane and not to the button
      jfx_node.setCursor(enabled ? Cursor.HAND : Cursors.NO_WRITE);
    }
  }
}
