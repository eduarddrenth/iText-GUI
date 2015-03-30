/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vectorprint.vectorprintreportgui;

import com.vectorprint.ClassHelper;
import com.vectorprint.configuration.parameters.Parameter;
import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParameterProps implements Comparable<ParameterProps>{
   
   private StringProperty key = new SimpleStringProperty();
   private StringProperty type = new SimpleStringProperty();
   private StringProperty value = new SimpleStringProperty();
   private StringProperty help = new SimpleStringProperty();
   private StringProperty declaringClass = new SimpleStringProperty();
   private Parameter p;

   public ParameterProps(Parameter p) {
      this.p = p;
      key.set(p.getKey());
      help.set(p.getHelp());
      type.set(ClassHelper.findParameterClass(0, p.getClass(), Parameter.class).getName());
      declaringClass.set(p.getDeclaringClass().getName());
      value.set(p.serializeValue(p.getValue()));
   }
   
   

   public String getKey() {
      return key.get();
   }
   
   public String getType() {
      return type.get();
   }

   public String getValue() {
      return value.get();
   }

   public void setValue(String value) {
      p.setValue(p.convert(value)); // validates value
      this.value.set(value==null?"":value);
   }

   public String getHelp() {
      return help.get();
   }

   public String getDeclaringClass() {
      return declaringClass.get();
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 97 * hash + Objects.hashCode(this.key);
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final ParameterProps other = (ParameterProps) obj;
      if (!Objects.equals(this.key, other.key)) {
         return false;
      }
      return true;
   }

   @Override
   public int compareTo(ParameterProps o) {
      return getKey().compareTo(o.getKey());
   }

}
