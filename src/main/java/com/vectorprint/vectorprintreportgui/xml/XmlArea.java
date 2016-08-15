package com.vectorprint.vectorprintreportgui.xml;

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

import com.vectorprint.vectorprintreportgui.text.SearchableTextArea;
import java.io.IOException;
import java.io.InputStream;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextFlow;
import org.xml.sax.SAXException;

public class XmlArea extends AnchorPane {

   @FXML
   private SearchableTextArea searchText;

   @FXML
   private TextFlow xmlhighlight;

   public XmlArea() {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(XmlArea.class.getSimpleName() + ".fxml"));
      fxmlLoader.setRoot(this);
      fxmlLoader.setController(this);

      try {
         fxmlLoader.load();
      } catch (IOException exception) {
         throw new RuntimeException(exception);
      }
   }

   public TextFlow getXmlhighlight() {
      return xmlhighlight;
   }

   public void loadXml(InputStream xml) throws SAXException, IOException {
      XmlContentHandler xmlContentHandler = new XmlContentHandler();
      new SaxParser().parse(xml, xmlContentHandler);
      xmlhighlight.getChildren().addAll(xmlContentHandler.getTexts());
      searchText.getText().setText(xmlContentHandler.getText());
   }

   public void setEditable(boolean editable) {
      searchText.setEditable(editable);
   }

   public boolean isEditable() {
      return searchText.isEditable();
   }
}
