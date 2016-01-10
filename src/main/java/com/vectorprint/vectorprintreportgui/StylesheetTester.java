package com.vectorprint.vectorprintreportgui;

import com.vectorprint.configuration.Settings;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.report.running.ReportRunner;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;

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
 * @author Eduard Drenth at VectorPrint.nl
 */
public class StylesheetTester {
   
   private final Controller controller;

   public StylesheetTester(Controller controller) {
      this.controller = controller;
   }
   
   public void testStyleSheet(Reader stylesheet) throws Exception {
      PipedInputStream in = new PipedInputStream();
      PipedOutputStream out = new PipedOutputStream(in);
      new Thread(new Runnable() {
         @Override
         public void run() {
            controller.openPdf(in, "test for stylesheet");
         }
      });
      // TODO datacollector, preparing data using stylesheet
      new ReportRunner(new CachingProperties(new Settings())).buildReport(null,out);
   }

}
