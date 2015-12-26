/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vectorprint.vectorprintreportgui;

import com.vectorprint.ClassHelper;
import com.vectorprint.configuration.binding.parameters.ParamBindingHelper;
import com.vectorprint.configuration.binding.parameters.ParamBindingService;
import com.vectorprint.configuration.parameters.Parameter;
import java.io.Serializable;
import java.io.StringReader;
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
      ParamBindingHelper helper = ParamBindingService.getInstance().getFactory().getBindingHelper();
      value.set(helper.serializeValue(helper.getValueToSerialize(p, false)));
   }

   public Parameter getP() {
      return p;
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
      ParamBindingHelper helper = ParamBindingService.getInstance().getFactory().getBindingHelper();
      Serializable parseAsParameterValue = ParamBindingService.getInstance().getFactory().getParser(new StringReader("")).parseAsParameterValue(value, p);
      helper.setValueOrDefault(p, parseAsParameterValue, false);
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
