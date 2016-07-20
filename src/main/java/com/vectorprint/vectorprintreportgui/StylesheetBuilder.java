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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class StylesheetBuilder extends Application {

   @Override
   public void start(final Stage stage) throws Exception {
      Parent root = null;
      try {
         root = FXMLLoader.load(getClass().getResource("/fxml/StylesheetBuilder.fxml"));
      } catch (Exception ex) {
         ex.printStackTrace();
         throw ex;
      }
      Scene scene = new Scene(root);
      scene.getStylesheets().add("/styles/Styles.css");
      stage.setScene(scene);
      stage.initStyle(StageStyle.DECORATED);
      stage.setTitle("Build VectorPrint report configurations");
      stage.show();
   }

   @Override
   public void stop() throws Exception {
      System.exit(0);
   }

   public static void main(String[] args) throws IOException {
      if (new File(LOGGINGPROPERTIES).canRead()) {
         LogManager.getLogManager().readConfiguration(new FileInputStream(LOGGINGPROPERTIES));
      } else {
         Logger.getGlobal().warning(LOGGINGPROPERTIES + " not found, using built in configuration");
         LogManager.getLogManager().readConfiguration(StylesheetBuilder.class.getResourceAsStream("/logging.properties"));
      }
      launch(args);
   }
   public static final String LOGGINGPROPERTIES = System.getProperty("user.dir") + File.separator + "VectorPrintGUILogging.properties";
}
