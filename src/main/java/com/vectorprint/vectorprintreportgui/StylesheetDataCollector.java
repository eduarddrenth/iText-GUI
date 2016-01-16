/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vectorprint.vectorprintreportgui;

import com.vectorprint.configuration.annotation.Setting;
import com.vectorprint.report.data.DataCollectorImpl;
import com.vectorprint.report.data.ReportDataHolderImpl;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class StylesheetDataCollector extends DataCollectorImpl {
   
   @Setting(keys = "stylesheet")
   private String stylesheet;

   @Override
   public ReportDataHolderImpl collect() {
      add(stylesheet,null);
      return getDataHolder();
   }

}
