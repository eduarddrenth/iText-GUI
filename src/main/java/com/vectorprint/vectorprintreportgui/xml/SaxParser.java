/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.vectorprintreportgui.xml;

import java.util.List;
import javafx.scene.text.Text;
import javax.xml.parsers.SAXParser;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class SaxParser extends SAXParser {

   private final XMLReader reader;

   public SaxParser() throws SAXException {
      reader = XMLReaderFactory.createXMLReader();
   }

   @Override
   public Parser getParser() throws SAXException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public XMLReader getXMLReader() throws SAXException {
      return reader;
   }

   @Override
   public boolean isNamespaceAware() {
      return false;
   }

   @Override
   public boolean isValidating() {
      return false;
   }

   @Override
   public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public List<Text> getTexts() {
      return ((XmlContentHandler) reader.getContentHandler()).getTexts();
   }
}
