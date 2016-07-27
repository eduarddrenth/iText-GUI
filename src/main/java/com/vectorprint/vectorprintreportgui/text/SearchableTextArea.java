package com.vectorprint.vectorprintreportgui.text;

/*
 * #%L
 * VectorPrintReportGUI
 * %%
 * Copyright (C) 2015 - 2016 VectorPrint
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

public class SearchableTextArea extends AnchorPane {

   @FXML
   private Label searchText;
   @FXML
   private TextArea text;

   @FXML
   private ToggleButton findMode;

   public SearchableTextArea() {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(SearchableTextArea.class.getSimpleName() + ".fxml"));
      fxmlLoader.setRoot(this);
      fxmlLoader.setController(this);

      try {
         fxmlLoader.load();
      } catch (IOException exception) {
         throw new RuntimeException(exception);
      }
   }

   public Label getSearchText() {
      return searchText;
   }

   public TextArea getText() {
      return text;
   }

   public void setEditable(boolean editable) {
      text.editableProperty().set(editable);
   }

   public boolean isEditable() {
      return text.editableProperty().get();
   }

   private boolean scroll;

   private static boolean supportedInSearch(KeyCode keyCode) {
      return KeyCode.SEMICOLON == keyCode || KeyCode.COLON == keyCode || KeyCode.COMMA == keyCode
          || KeyCode.EXCLAMATION_MARK == keyCode || KeyCode.EQUALS == keyCode || KeyCode.UNDERSCORE == keyCode
          || KeyCode.UNDERSCORE == keyCode || KeyCode.PERIOD == keyCode || KeyCode.MINUS == keyCode
          || KeyCode.BRACELEFT == keyCode || KeyCode.BRACERIGHT == keyCode || KeyCode.OPEN_BRACKET == keyCode
          || KeyCode.CLOSE_BRACKET == keyCode || KeyCode.ASTERISK == keyCode || KeyCode.AMPERSAND == keyCode
          || KeyCode.LEFT_PARENTHESIS == keyCode || KeyCode.RIGHT_PARENTHESIS == keyCode || KeyCode.PLUS == keyCode
          || KeyCode.QUOTE == keyCode || KeyCode.QUOTEDBL == keyCode || KeyCode.BACK_QUOTE == keyCode
          || KeyCode.BACK_SLASH == keyCode || KeyCode.SLASH == keyCode || KeyCode.DOLLAR == keyCode
          || KeyCode.EURO_SIGN == keyCode || KeyCode.POUND == keyCode || KeyCode.GREATER == keyCode
          || KeyCode.LESS == keyCode || KeyCode.STAR == keyCode;
   }

   private void searchTxt(KeyEvent event) {
      KeyCode kc = event.getCode();

      scroll = false;
      searchText.setTextFill(Color.BLACK);
      String s = searchText.getText();
      boolean again = false;

      if (kc.isLetterKey() || kc.isDigitKey() || kc.isWhitespaceKey() || supportedInSearch(kc)) {
         s += event.getText();
      } else {
         switch (kc) {
            case BACK_SPACE:
               if (s.length() > 0) {
                  s = s.substring(0, s.length() - 1);
               }
            case F3:
               // search again
               again = true;
               break;
            default:
               // ignore
               return;
         }
      }
      searchText.setText(s);
      if (s.length() == 0) {
         return;
      }

      searchText.setTextFill(searchArea(s, again) ? Color.GREEN : Color.RED);
   }

   public final boolean searchArea(String s, boolean again) {
      String contents = text.getText();
      int pos = again ? text.getCaretPosition() : text.getCaretPosition() - s.length() - 1;
      boolean noBackspace = text.getSelection().getLength() < s.length();
      if (contents.indexOf(s, pos) != -1) {
         text.selectRange(contents.indexOf(s, pos), contents.indexOf(s, pos) + s.length());
         if (noBackspace && text.getCaretPosition() != pos + s.length() + 2) {
            scroll = true;
         }
      } else if (contents.contains(s)) {
         // wrap
         text.selectRange(contents.indexOf(s), contents.indexOf(s) + s.length());
      } else {
         return false;
      }
      return true;
   }

   @FXML
   private void scroll(KeyEvent event) {
      if (scroll) {
         text.setScrollTop(text.getScrollTop() + 20);
      }
   }

   @FXML
   private void ignore(KeyEvent event) {
      if (findMode.isSelected()) {
         event.consume();
      }
   }

   @FXML
   private void searchOrEdit(KeyEvent event) {
      if (findMode.isSelected()) {
         event.consume();
      }
      if (event.isControlDown() && KeyCode.F == event.getCode()) {
         setFindMode(!isFindMode());
      } else if (KeyCode.ESCAPE == event.getCode()) {
         // stop searching
         setFindMode(false);
      } else if (isFindMode()) {
         searchTxt(event);
      }
   }

   @FXML
   private void toggleMode() {
      searchStyle();
      text.requestFocus();
   }

   private void searchStyle() {
      if (findMode.isSelected()) {
         text.getStyleClass().add("searching");
      } else {
         text.getStyleClass().remove("searching");
      }
   }

   public boolean isFindMode() {
      return !text.isEditable() || findMode.isSelected();
   }

   public void setFindMode(boolean findMode) {
      this.findMode.setSelected(findMode);
      if (!findMode) {
         searchText.setText("");
      }
      searchStyle();
   }

}
