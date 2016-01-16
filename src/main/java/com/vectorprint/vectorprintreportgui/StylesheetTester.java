package com.vectorprint.vectorprintreportgui;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.Settings;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.data.ReportDataHolder;
import com.vectorprint.report.data.ReportDataHolderImpl;
import com.vectorprint.report.itext.BaseReportGenerator;
import com.vectorprint.report.itext.DefaultElementProducer;
import com.vectorprint.report.itext.EventHelper;
import com.vectorprint.report.itext.jaxb.Datamappingstype;
import com.vectorprint.report.running.ReportRunner;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Deque;

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
/**
 * When you finished creating your stylesheet this class lets you see the effects of it in a pdf.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class StylesheetTester extends BaseReportGenerator<ReportDataHolderImpl>{

   private final Controller controller;

   public StylesheetTester(Controller controller) throws VectorPrintException {
      super(new EventHelper<ReportDataHolderImpl>(), new DefaultElementProducer());
      this.controller = controller;
   }

   public StylesheetTester() throws VectorPrintException {
      super(new EventHelper<ReportDataHolderImpl>(), new DefaultElementProducer());
      controller = null;
   }

   public void testStyleSheet(String stylesheet) throws Exception {

      PipedInputStream in = new PipedInputStream();
      PipedOutputStream out = new PipedOutputStream(in);

      new Thread(new Runnable() {
         @Override
         public void run() {
            controller.openPdf(in, "test for stylesheet");
         }
      }).start();
      
      CachingProperties cachingProperties = new CachingProperties(new Settings());
      cachingProperties.put(ReportConstants.DATACLASS, StylesheetDataCollector.class.getName());
      cachingProperties.put(ReportConstants.REPORTCLASS, getClass().getName());
      cachingProperties.put("stylesheet", stylesheet);
      new ReportRunner(cachingProperties).buildReport(null, out);

   }

   @Override
   protected void processDataObject(ReportDataHolder.IdData dw, Deque containers, Datamappingstype dmt) throws VectorPrintException, DocumentException {
      try {
         createAndAddElement(dw.getData(), Paragraph.class, null);
      } catch (InstantiationException ex) {
         throw new VectorPrintException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintException(ex);
      }
   }
   
   

}
