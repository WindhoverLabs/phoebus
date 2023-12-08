package com.windhoverlabs.yamcs.applications.parameterviewer;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

public class CopiableLabel extends Label {
  public CopiableLabel() {
    addCopyButton();
  }

  public CopiableLabel(String text) {
    super(text);
    addCopyButton();
  }

  public CopiableLabel(String text, Node graphic) {
    super(text, graphic);
  }

  private void addCopyButton() {
    Button button = new Button();
    button.visibleProperty().bind(textProperty().isEmpty().not());
    button.managedProperty().bind(textProperty().isEmpty().not());
    button.setFocusTraversable(false);
    button.setPadding(new Insets(0.0, 4.0, 0.0, 4.0));
    button.setOnAction(
        actionEvent -> {
          Clipboard clipboard = Clipboard.getSystemClipboard();
          ClipboardContent content = new ClipboardContent();
          content.putString(getText());
          clipboard.setContent(content);
        });
    GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    Glyph clipboardIcon = fontAwesome.create(FontAwesome.Glyph.CLIPBOARD);
    clipboardIcon.setFontSize(8.0);
    button.setGraphic(clipboardIcon);
    setGraphic(button);
    setContentDisplay(ContentDisplay.RIGHT);
  }
}
