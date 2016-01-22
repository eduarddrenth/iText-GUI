package com.vectorprint.vectorprintreportgui;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Section;
import com.itextpdf.text.TextElementArray;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
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
import com.vectorprint.report.itext.ItextHelper;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.DefaultStylerFactory;
import com.vectorprint.report.itext.style.stylers.ImageAlign;
import com.vectorprint.report.itext.style.stylers.SimpleColumns;
import com.vectorprint.report.itext.style.stylers.Table;
import com.vectorprint.report.running.ReportRunner;
import static com.vectorprint.vectorprintreportgui.Controller.isStyler;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

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
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      new ReportRunner(settings).buildReport(null, out);
      controller.openPdf(new ByteArrayInputStream(out.toByteArray()), "test stylesheet in pdf");
   }

   @Override
   protected void createReportBody(Document document, ReportDataHolderImpl data, PdfWriter writer) throws DocumentException, VectorPrintException {
      document.add(new Paragraph("In this pdf you can see the effect of the stylesheet you built. If you want to see debugging info in this pdf, just put"
          + "\"debug=true\" in the stylesheet."));
      newLine(3);
      
      for (Map.Entry<String, String[]> e : getSettings().entrySet()) {
         if (DefaultStylerFactory.PRESTYLERS.equals(e.getKey()) || DefaultStylerFactory.POSTSTYLERS.equals(e.getKey())|| ReportConstants.DOCUMENTSETTINGS.equals(e.getKey())) {
            continue;
         }
         String[] v = e.getValue();
         if (isStyler(e.getKey(), getSettings())) {
            List<BaseStyler> stylers = getStylers(e.getKey());
            BaseStyler first = stylers.get(0);
            try {
               if (!first.creates()) {
                  document.add(new Paragraph(String.format("Effect of %s on %s:", Arrays.toString(v),first.getSupportedClasses())));
                  newLine(1);
                  
                  for (Class<? extends Element> clazz : first.getSupportedClasses()) {
                     if (!Modifier.isAbstract(clazz.getModifiers())) {
                        if (Rectangle.class.equals(clazz)) {
                           document.add(getStyleHelper().style(new Rectangle(ItextHelper.mmToPts(30), ItextHelper.mmToPts(30)), null, stylers));
                        } else if (first instanceof ImageAlign) {
                           BufferedImage read = ImageIO.read(getClass().getResourceAsStream("/testingrecources/pointer.png"));
                           Image img = com.itextpdf.text.Image.getInstance(read, null);
                           PdfPTable pt = new PdfPTable(1);
                           PdfPCell cell = new PdfPCell();
                           cell.addElement(img);
                           pt.addCell(getStyleHelper().style(cell, img, stylers));
                           document.add(pt);
                        } else if (Section.class.isAssignableFrom(clazz)) {
                           document.add(getIndex("style example of " + clazz.getSimpleName() + ": " + Arrays.toString(v), 1, stylers));
                        } else {
                           Element createAndAddElement = createAndAddElement("style example of " + clazz.getSimpleName() + ": " + Arrays.toString(v), stylers, clazz);
                        }
                        newLine(5);
                     } else if (clazz.equals(TextElementArray.class)) {
                        createAndAddElement("style example of " + Paragraph.class.getSimpleName() + ": " + Arrays.toString(v), stylers, Paragraph.class);
                        newLine(5);
                     }
                  }
               } else if (first instanceof Table) {
                  PdfPTable table = createElement(null, PdfPTable.class, stylers);
                  Table t = (Table) first;
                  int cells = t.getColumns() * 25;
                  for (int i = 0; i < cells; i++) {
                     table.addCell("cell" + i);
                  }
                  document.add(table);
               } else if (first instanceof SimpleColumns) {
                  SimpleColumns cols = createColumns(stylers);
                  int texts = cols.getNumColumns() * 100;
                  for (int i = 0; i < texts; i++) {
                     cols.addContent(new Chunk("text" + i), null);
                  }
                  cols.write();
               }
            } catch (IOException ex) {
               throw new VectorPrintException(ex);
            } catch (InstantiationException ex) {
               throw new VectorPrintException(ex);
            } catch (IllegalAccessException ex) {
               throw new VectorPrintException(ex);
            }
         }
      }
   }
   
   private static List<BaseStyler> filter(List<BaseStyler> stylers, Object element, Object data) {
      List<BaseStyler> l = new ArrayList<>(stylers.size());
      stylers.stream().filter((bs) -> (bs.canStyle(element)&&bs.shouldStyle(data, element))).forEach((bs) -> {
         l.add(bs);
      });
      return l;
   }

}
