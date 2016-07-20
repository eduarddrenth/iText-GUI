/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.vectorprintreportgui;

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

import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.StylingCondition;
import com.vectorprint.report.itext.style.conditions.AbstractCondition;
import com.vectorprint.report.itext.style.stylers.AbstractStyler;
import com.vectorprint.vectorprintreportgui.text.SearchableTextArea;
import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ViewHelper {

   public static Tooltip tip(String text) {
      Tooltip t = new Tooltip(text);
      t.setMaxWidth(400);
      t.setAutoHide(false);
      t.setAutoFix(true);
      t.setWrapText(true);
      return t;
   }

   public static void notify(String buttonText, String title, String details) {
      Dialog<String> dialog = new Dialog<>();
      dialog.setContentText(details);
      dialog.setTitle(title);
      dialog.setResizable(true);
      ButtonType bt = new ButtonType(buttonText, ButtonBar.ButtonData.OK_DONE);
      dialog.getDialogPane().getButtonTypes().add(bt);
      dialog.showAndWait();
   }

   public static String helpFor(Parameterizable p) {
      return p instanceof BaseStyler ? (((BaseStyler) p).creates() ? "creates iText element " : "") + ((BaseStyler) p).getHelp() : ((StylingCondition) p).getHelp();
   }

   public static boolean isStyler(String key, EnhancedMap settings) {
      String[] classes = null;
      try {
         classes = settings.getStringProperties(null, key);
      } catch (VectorPrintRuntimeException e) {
         if (!e.getMessage().contains("this does not match requested class")) {
            throw e;
         }
      }
      if (classes == null) {
         return false;
      }
      for (String s : classes) {
         try {
            Class.forName(AbstractStyler.class.getPackage().getName() + "." + s.split("\\(")[0]);
         } catch (ClassNotFoundException ex) {
            return false;
         }
      }
      return true;
   }

   public static boolean isCondition(String key, EnhancedMap settings) {
      String[] classes = null;
      try {
         classes = settings.getStringProperties(null, key);
      } catch (VectorPrintRuntimeException e) {
         if (!e.getMessage().contains("this does not match requested class")) {
            throw e;
         }
      }
      if (classes == null) {
         return false;
      }
      for (String s : classes) {
         try {
            Class.forName(AbstractCondition.class.getPackage().getName() + "." + s.split("\\(")[0]);
         } catch (ClassNotFoundException ex) {
            return false;
         }
      }
      return true;
   }

   public static String toHex(Color color) {
      int red = (int) (color.getRed() * 255);
      int green = (int) (color.getGreen() * 255);
      int blue = (int) (color.getBlue() * 255);
      return "#" + Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue);
   }

   public static void writeStackTrace(Throwable ex, SearchableTextArea area) {
      StringWriter sw = new StringWriter(1024);
      ex.printStackTrace(new PrintWriter(sw));
      area.getText().clear();
      area.getText().setText(sw.toString());
   }

   public static void toError(Throwable ex, SearchableTextArea area) {
      ViewHelper.writeStackTrace(ex, area);
      ViewHelper.notify("ok (errors tab for details)", "error", ex.getMessage());
   }

   private ViewHelper() {
   }

}
