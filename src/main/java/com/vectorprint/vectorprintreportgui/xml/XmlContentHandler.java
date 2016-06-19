/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.vectorprintreportgui.xml;

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
public class XmlContentHandler extends DefaultHandler {

   private final List<Text> texts = new ArrayList<>(100);

   @Override
   public void characters(char[] ch, int start, int length) throws SAXException {
      super.characters(ch, start, length);
      Text t = new Text(new String(ch));
      texts.add(t);
   }

   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException {
      super.endElement(uri, localName, qName);
      Text t = new Text('<' + qName + '>');
      t.getStyleClass().add("xmlelement");
      texts.add(t);
   }

   @Override
   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      super.startElement(uri, localName, qName, attributes);
      Text t = new Text("</" + qName + '>');
      t.getStyleClass().add("xmlelement");
      texts.add(t);
   }

   public List<Text> getTexts() {
      return texts;
   }

}
