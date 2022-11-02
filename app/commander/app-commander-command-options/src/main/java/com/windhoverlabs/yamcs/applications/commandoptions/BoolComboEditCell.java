package com.windhoverlabs.yamcs.applications.commandoptions;

import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import org.phoebus.ui.javafx.EditCell;

public class BoolComboEditCell<S, T> extends EditCell<S, T> {

  private final TextField textField = new TextField();

  // Converter for converting the text in the text field to the user type, and vice-versa:
  private final StringConverter<T> converter;

  /**
   * Creates and initializes an edit cell object.
   *
   * @param converter the converter to convert from and to strings
   */
  public BoolComboEditCell(StringConverter<T> converter) {
    super(converter);
    this.converter = converter;

    itemProperty()
        .addListener(
            (o, b, n) -> {
              setText(n != null ? this.converter.toString(n) : null);
            });

    setGraphic(this.textField);
    setContentDisplay(ContentDisplay.TEXT_ONLY);
    setAlignment(Pos.CENTER_LEFT);

    this.textField.setOnAction(
        evt -> {
          commitEdit(this.converter.fromString(this.textField.getText()));
        });

    this.textField
        .focusedProperty()
        .addListener(
            (o, b, n) -> {
              if (!n) {
                commitEdit(this.converter.fromString(this.textField.getText()));
              }
            });

    this.addEventFilter(
        KeyEvent.KEY_PRESSED,
        event -> {
          if (event.getCode() == KeyCode.ESCAPE) {
            textField.setText(converter.toString(getItem()));
            cancelEdit();
            event.consume();
          }
        });
  }

  /** Convenience converter that does nothing (converts Strings to themselves and vice-versa...). */
  public static final StringConverter<String> IDENTITY_CONVERTER =
      new StringConverter<String>() {

        @Override
        public String toString(String object) {
          return object;
        }

        @Override
        public String fromString(String string) {
          return string;
        }
      };

  /**
   * Convenience method for creating an EditCell for a String value.
   *
   * @return the edit cell
   */
  public static <S> EditCell<S, String> createStringEditCell() {
    return new EditCell<S, String>(IDENTITY_CONVERTER);
  }

  // set the text of the text field and display the graphic
  @Override
  public void startEdit() {
    super.startEdit();
    this.textField.setText(this.converter.toString(getItem()));
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    this.textField.requestFocus();
  }

  // revert to text display
  @Override
  public void cancelEdit() {
    super.cancelEdit();
    setContentDisplay(ContentDisplay.TEXT_ONLY);
  }
}
