package com.vectorprint.vectorprintreportgui;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Section;
import com.itextpdf.text.TextElementArray;
import com.itextpdf.text.pdf.ColumnText;
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
import com.vectorprint.report.itext.style.stylers.Barcode;
import com.vectorprint.report.itext.style.stylers.ImageAlign;
import com.vectorprint.report.itext.style.stylers.SVG;
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
      document.add(new Paragraph("In this pdf you can see how your document will look like using stylesheet you built. If you want to see debugging info in this pdf, just put"
          + "\"debug=true\" in the stylesheet."));
      if (getSettings().containsKey(DefaultStylerFactory.PRESTYLERS)) {
         String[] setting = getSettings().getStringProperties(null, DefaultStylerFactory.PRESTYLERS);
         printExplanation(String.format("styles applied before configured styles: %s", Arrays.toString(setting)));
      }
      if (getSettings().containsKey(DefaultStylerFactory.POSTSTYLERS)) {
         String[] setting = getSettings().getStringProperties(null, DefaultStylerFactory.POSTSTYLERS);
         printExplanation(String.format("styles applied after configured styles: %s", Arrays.toString(setting)));
      }
      if (getSettings().containsKey(DefaultStylerFactory.PAGESTYLERS)) {
         String[] setting = getSettings().getStringProperties(null, DefaultStylerFactory.PAGESTYLERS);
         printExplanation(String.format("styles applied on every page: %s", Arrays.toString(setting)));
      }
      for (Map.Entry<String, String[]> e : getSettings().entrySet()) {
         if (Controller.isCondition(e.getKey(), getSettings())) {
            String[] setting = e.getValue();
            printExplanation(String.format("active conditions for %s: %s", e.getKey(), Arrays.toString(setting)));
         }
      }
      newLine(3);

      for (Map.Entry<String, String[]> e : getSettings().entrySet()) {
         if (DefaultStylerFactory.PRESTYLERS.equals(e.getKey()) || DefaultStylerFactory.POSTSTYLERS.equals(e.getKey()) || ReportConstants.DOCUMENTSETTINGS.equals(e.getKey())) {
            continue;
         }
         if (isStyler(e.getKey(), getSettings())) {
            String[] setting = e.getValue();
            List<BaseStyler> stylers = getStylers(e.getKey());
            BaseStyler first = stylers.get(0);
            try {
               if (!first.creates()) {
                  printExplanation(String.format(TRYING_TO_SHOW_THE_EFFECT_OF_S_ON_S_, e.getKey(), Arrays.toString(setting), first.getSupportedClasses()));

                  for (Class<? extends Element> clazz : first.getSupportedClasses()) {
                     if (!Modifier.isAbstract(clazz.getModifiers())) {
                        if (Rectangle.class.equals(clazz)) {
                           Rectangle rectangle = new Rectangle(ItextHelper.mmToPts(30), ItextHelper.mmToPts(30));
                           List<BaseStyler> filter = filter(stylers, rectangle, null);
                           if (!filter.isEmpty()) {
                              printExplanation(String.format(SHOWING_EFFECT_OF_S_ON_S_, print(filter), Rectangle.class.getSimpleName()));

                              document.add(getStyleHelper().style(rectangle, null, stylers));

                              printExplanation(END_EFFECT_OF_S_ON_S_);

                           }
                        } else if (first instanceof ImageAlign) {
                           BufferedImage read = ImageIO.read(getClass().getResourceAsStream("/testingrecources/pointer.png"));
                           Image img = com.itextpdf.text.Image.getInstance(read, null);
                           PdfPTable pt = new PdfPTable(1);
                           PdfPCell cell = new PdfPCell();
                           cell.addElement(img);
                           pt.addCell(getStyleHelper().style(cell, img, stylers));

                           List<BaseStyler> filter = filter(stylers, PdfPCell.class, img);

                           printExplanation(String.format(SHOWING_EFFECT_OF_S_ON_S_, print(filter), PdfPCell.class.getSimpleName()));

                           document.add(pt);

                           printExplanation(END_EFFECT_OF_S_ON_S_);
                        } else if (Section.class.isAssignableFrom(clazz)) {
                           List<BaseStyler> filter = filter(stylers, Section.class, EXAMPLE_STYLING_OF_ + clazz.getSimpleName());

                           if (!filter.isEmpty()) {
                              printExplanation(String.format(SHOWING_EFFECT_OF_S_ON_S_, print(filter), Section.class.getSimpleName()));

                              document.add(getIndex(EXAMPLE_STYLING_OF_ + clazz.getSimpleName(), 1, stylers));

                              printExplanation(END_EFFECT_OF_S_ON_S_);
                           }
                        } else {
                           Element created = createElement(EXAMPLE_STYLING_OF_ + clazz.getSimpleName(), clazz, stylers);
                           List<BaseStyler> filter = filter(stylers, created, EXAMPLE_STYLING_OF_ + clazz.getSimpleName());
                           if (!filter.isEmpty()) {
                              printExplanation(String.format(SHOWING_EFFECT_OF_S_ON_S_, print(filter), created.getClass().getSimpleName()));

                              document.add(created);

                              printExplanation(END_EFFECT_OF_S_ON_S_);
                           }
                        }
                     } else if (clazz.equals(TextElementArray.class)) {
                        Element created = createElement(EXAMPLE_STYLING_OF_ + Paragraph.class.getSimpleName(), Paragraph.class, stylers);
                        List<BaseStyler> filter = filter(stylers, created, EXAMPLE_STYLING_OF_ + Paragraph.class.getSimpleName());
                        if (!filter.isEmpty()) {
                           printExplanation(String.format(SHOWING_EFFECT_OF_S_ON_S_, print(filter), created.getClass().getSimpleName()));

                           document.add(created);

                           printExplanation(END_EFFECT_OF_S_ON_S_);
                        }
                     }
                  }
               } else if (first instanceof Table) {
                  printExplanation(String.format(TRYING_TO_SHOW_THE_EFFECT_OF_S_ON_S_, e.getKey(), Arrays.toString(setting), first.getSupportedClasses()));
                  PdfPTable table = createElement(null, PdfPTable.class, stylers);
                  List<BaseStyler> filter = filter(stylers, table, EXAMPLE_STYLING_OF_ + PdfPTable.class.getSimpleName());
                  Table t = (Table) first;
                  int cells = t.getColumns() * 25;
                  for (int i = 0; i < cells; i++) {
                     table.addCell("cell" + i);
                  }
                  printExplanation(String.format(SHOWING_EFFECT_OF_S_ON_S_, print(filter), PdfPTable.class.getSimpleName()));
                  document.add(table);
                  printExplanation(END_EFFECT_OF_S_ON_S_);
               } else if (first instanceof SimpleColumns) {
                  printExplanation(String.format(TRYING_TO_SHOW_THE_EFFECT_OF_S_ON_S_, e.getKey(), Arrays.toString(setting), ColumnText.class));
                  SimpleColumns cols = createColumns(stylers);
                  ColumnText ct = new ColumnText(writer.getDirectContent());
                  List<BaseStyler> filter = filter(stylers, ct, EXAMPLE_STYLING_OF_ + ColumnText.class.getSimpleName());
                  int texts = cols.getNumColumns() * 100;
                  for (int i = 0; i < texts; i++) {
                     cols.addContent(new Chunk("text" + i), null);
                  }
                  printExplanation(String.format(SHOWING_EFFECT_OF_S_ON_S_, print(filter), ColumnText.class.getSimpleName()));
                  cols.write();
                  printExplanation(END_EFFECT_OF_S_ON_S_);
               } else if (first instanceof Barcode) {
                  Barcode bc = (Barcode) first;
                  if (bc.getData()==null) {
                     bc.setData("0123456789012");
                  }
                  printExplanation(String.format(TRYING_TO_SHOW_THE_EFFECT_OF_S_ON_S_, e.getKey(), Arrays.toString(setting), Image.class));
                  Image barcode = createElement(null, Image.class, stylers);
                  List<BaseStyler> filter = filter(stylers, barcode, EXAMPLE_STYLING_OF_ + Image.class.getSimpleName());
                  printExplanation(String.format(SHOWING_EFFECT_OF_S_ON_S_, print(filter), Image.class.getSimpleName()));
                  document.add(barcode);
                  printExplanation(END_EFFECT_OF_S_ON_S_);
               } else if (first instanceof SVG) {
                  SVG bc = (SVG) first;
                  if (bc.getData()==null) {
                     bc.setData("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"200\" version=\"1.1\"><defs><filter id=\"test\" filterUnits=\"objectBoundingBox\" x=\"0\" y=\"0\" width=\"1.5\" height=\"4\"><feOffset result=\"Off1\" dx=\"15\" dy=\"20\" /><feFlood style=\"flood-color:#ff0000;flood-opacity:0.8\" /><feComposite in2=\"Off1\" operator=\"in\" result=\"C1\" /><feOffset in=\"SourceGraphic\" result=\"Off2\" dx=\"30\" dy=\"40\" /><feFlood style=\"flood-color:#ff0000;flood-opacity:0.4\" /><feComposite in2=\"Off2\" operator=\"in\" result=\"C2\" /><feMerge><feMergeNode in=\"C2\" /><feMergeNode in=\"C1\" /><feMergeNode in=\"SourceGraphic\" /></feMerge></filter></defs><text x=\"30\" y=\"100\" style=\"font:36px verdana bold;fill:blue;filter:url(#test)\">This is some text!</text></svg>");
                  }
                  printExplanation(String.format(TRYING_TO_SHOW_THE_EFFECT_OF_S_ON_S_, e.getKey(), Arrays.toString(setting), Image.class));
                  Image svg = createElement(null, Image.class, stylers);
                  List<BaseStyler> filter = filter(stylers, svg, EXAMPLE_STYLING_OF_ + Image.class.getSimpleName());
                  printExplanation(String.format(SHOWING_EFFECT_OF_S_ON_S_, print(filter), Image.class.getSimpleName()));
                  document.add(svg);
                  printExplanation(END_EFFECT_OF_S_ON_S_);
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
   private static final String EXAMPLE_STYLING_OF_ = "example styling of ";
   private static final String TRYING_TO_SHOW_THE_EFFECT_OF_S_ON_S_ = "Trying to apply style %s (%s) to %s.";
   private static final String END_EFFECT_OF_S_ON_S_ = "End showing applied style.";
   private static final String SHOWING_EFFECT_OF_S_ON_S_ = "Showing style (%s) applied to %s:";

   private void printExplanation(String txt) throws DocumentException {
      Font f = FontFactory.getFont(FontFactory.COURIER, 10, BaseColor.MAGENTA);
      Paragraph paragraph = new Paragraph(txt, f);
      float padding = ItextHelper.mmToPts(2);
      for (Chunk c : paragraph.getChunks()) {
         c.setBackground(new BaseColor(230, 230, 230), padding, padding, padding, padding);
      }
      getDocument().add(paragraph);
      newLine();
   }

   private static List<BaseStyler> filter(List<BaseStyler> stylers, Object element, Object data) {
      List<BaseStyler> l = new ArrayList<>(stylers.size());
      stylers.stream().filter((bs) -> (bs.canStyle(element) && bs.shouldStyle(data, element))).forEach((bs) -> {
         l.add(bs);
      });
      return l;
   }

   private static String print(List<BaseStyler> stylers) {
      StringBuilder sb = new StringBuilder(stylers.size() * 20);
      for (BaseStyler styler : stylers) {
         sb.append(styler.getClass().getSimpleName()).append(", ");
      }
      return sb.length() > 0 ? sb.substring(0, sb.length() - 2).toString() : "";
   }

}
