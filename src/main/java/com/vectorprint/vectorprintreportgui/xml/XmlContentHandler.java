/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

import java.util.ArrayList;
import java.util.List;
import javafx.scene.text.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
class XmlContentHandler extends DefaultHandler {

   private final List<Text> texts = new ArrayList<>(100);
   private final StringBuilder text = new StringBuilder();

   @Override
   public void characters(char[] ch, int start, int length) throws SAXException {
      Text t = new Text(new String(ch).substring(start, start + length));
      texts.add(t);
      text.append(t.getText());
   }

   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException {
      Text t = new Text("</" + qName + '>');
      t.getStyleClass().add("xmlelement");
      texts.add(t);
      text.append(t.getText());
   }

   @Override
   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

      Text t = new Text("<" + qName);
      t.getStyleClass().add("xmlelement");
      texts.add(t);
      text.append(t.getText());
      for (int i = 0; i < attributes.getLength(); i++) {
         t = new Text(" " + attributes.getQName(i) + "=\"");
         t.getStyleClass().add("xmlattribute");
         texts.add(t);
         text.append(t.getText());
         t = new Text(attributes.getValue(i));
         texts.add(t);
         text.append(t.getText());
         t = new Text("\"");
         t.getStyleClass().add("xmlattribute");
         texts.add(t);
         text.append(t.getText());

      }
      t = new Text(">");
      t.getStyleClass().add("xmlelement");
      texts.add(t);
      text.append(t.getText());
   }

   public List<Text> getTexts() {
      return texts;
   }

   public String getText() {
      return text.toString();
   }

}
