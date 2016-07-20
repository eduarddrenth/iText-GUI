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
class SaxParser extends SAXParser {

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

}
