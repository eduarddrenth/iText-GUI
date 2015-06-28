/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.vectorprintreportgui;

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

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class StylesheetBuilder extends Application {

   static Stage topWindow;

   @Override
   public void start(final Stage stage) throws Exception {
      topWindow = stage;
      Parent root = FXMLLoader.load(getClass().getResource("/fxml/StylesheetBuilder.fxml"));

      Scene scene = new Scene(root);
      scene.getStylesheets().add("/styles/Styles.css");
      stage.setScene(scene);
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
   public static final String LOGGINGPROPERTIES = System.getProperty("user.dir")+File.separator+"VectorPrintGUILogging.properties";
}
