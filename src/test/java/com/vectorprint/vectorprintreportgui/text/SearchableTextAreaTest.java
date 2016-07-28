/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.vectorprintreportgui.text;

import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class SearchableTextAreaTest {

   private SearchableTextArea searchableTextArea;

   @Before
   public void init() {
      new JFXPanel(); // initialize toolkit
      searchableTextArea = new SearchableTextArea();
   }

   public SearchableTextAreaTest() {
   }

   @Test
   public void testControllerInit() throws Exception {
      Parent root = null;
      try {
         root = FXMLLoader.load(getClass().getResource("/fxml/StylesheetBuilder.fxml"));
      } catch (Exception ex) {
         ex.printStackTrace();
         throw ex;
      }
   }

   @Test
   public void testGetSearchText() {
   }

   @Test
   public void testGetText() {
   }

   @Test
   public void testSetEditable() {
      Assert.assertTrue(searchableTextArea.isEditable());
      searchableTextArea.setEditable(false);
      Assert.assertFalse(searchableTextArea.isEditable());
   }

   @Test
   public void testIsEditable() {
   }

   @Test
   public void testSearchArea() {
   }

   @Test
   public void testIsFindMode() {
   }

   @Test
   public void testSetFindMode() {
   }

}
