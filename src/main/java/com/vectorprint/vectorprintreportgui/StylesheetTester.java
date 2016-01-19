package com.vectorprint.vectorprintreportgui;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.Settings;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.data.ReportDataHolderImpl;
import com.vectorprint.report.itext.BaseReportGenerator;
import com.vectorprint.report.itext.DefaultElementProducer;
import com.vectorprint.report.itext.EventHelper;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.DefaultStylerFactory;
import com.vectorprint.report.running.ReportRunner;
import static com.vectorprint.vectorprintreportgui.Controller.isStyler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

      EnhancedMap settings = new CachingProperties(new ParsingProperties(new Settings(), new StringReader(stylesheet)));
      settings.put(ReportConstants.REPORTCLASS, getClass().getName());
      settings.put(ReportConstants.DEBUG, "true");
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      new ReportRunner(settings).buildReport(null, out);
      controller.openPdf(new ByteArrayInputStream(out.toByteArray()), "test stylesheet in pdf");
   }

   @Override
   protected void createReportBody(Document document, ReportDataHolderImpl data, PdfWriter writer) throws DocumentException, VectorPrintException {
      for (Map.Entry<String, String[]> e : getSettings().entrySet()) {
         if (DefaultStylerFactory.PRESTYLERS.equals(e.getKey())||DefaultStylerFactory.POSTSTYLERS.equals(e.getKey())) {
            continue;
         }
         String[] v = e.getValue();
         if (isStyler(e.getKey(), getSettings())) {
            List<BaseStyler> stylers = getStylers(e.getKey());
            if (!stylers.get(0).creates()) {
               try {
                  // assume styling text
                  createAndAddElement(Arrays.toString(v), Paragraph.class, e.getKey());
                  newLine(5);
               } catch (InstantiationException ex) {
                  throw new VectorPrintException(ex);
               } catch (IllegalAccessException ex) {
                  throw new VectorPrintException(ex);
               }
            }
         }
      }
   }

}
