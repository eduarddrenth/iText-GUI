package com.vectorprint.vectorprintreportgui;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.Settings;
import com.vectorprint.configuration.annotation.Setting;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.data.ReportDataHolderImpl;
import com.vectorprint.report.itext.BaseReportGenerator;
import com.vectorprint.report.itext.DefaultElementProducer;
import com.vectorprint.report.itext.EventHelper;
import com.vectorprint.report.running.ReportRunner;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import javafx.application.Platform;

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
public class StylesheetTester extends BaseReportGenerator<ReportDataHolderImpl> {

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

      Platform.runLater(new Runnable() {
         @Override
         public void run() {
            controller.openPdf(in, "test for stylesheet");
         }
      });

      CachingProperties cachingProperties = new CachingProperties(new Settings());
      cachingProperties.put(ReportConstants.REPORTCLASS, getClass().getName());
      cachingProperties.put("stylesheet", stylesheet);
      new ReportRunner(cachingProperties).buildReport(null, out);

   }

   @Setting(keys = "stylesheet")
   private String stylesheet;

   @Override
   protected void createReportBody(Document document, ReportDataHolderImpl data, PdfWriter writer) throws DocumentException, VectorPrintException {
      try {
         createAndAddElement(stylesheet, Paragraph.class, null);
      } catch (InstantiationException ex) {
         throw new VectorPrintException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintException(ex);
      }
   }

}
