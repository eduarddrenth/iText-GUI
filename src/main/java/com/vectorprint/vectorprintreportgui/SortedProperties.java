package com.vectorprint.vectorprintreportgui;

import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * will sort entrySet and keySet
 * @deprecated will be removed when new config is released
 * @author Eduard Drenth at VectorPrint.nl
 */
@Deprecated
public class SortedProperties extends AbstractPropertiesDecorator {
   
   public SortedProperties(EnhancedMap settings) {
      super(settings);
   }
   
   private static final Comparator<Entry<String, String[]>> ECOMP = new Comparator<Entry<String, String[]>>() {
      @Override
      public int compare(Entry<String, String[]> o1, Entry<String, String[]> o2) {
         return o1.getKey().compareTo(o2.getKey());
      }
      
   };

   @Override
   public Set<Entry<String, String[]>> entrySet() {
      TreeSet<Entry<String, String[]>> treeSet = new TreeSet<Entry<String, String[]>>(ECOMP);
      treeSet.addAll(super.entrySet());
      return treeSet;
   }
   
   @Override
   public Set<String> keySet() {
      return new TreeSet<>(super.keySet());
   }
   
}
