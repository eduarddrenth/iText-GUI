/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.vectorprintreportgui;

import com.vectorprint.configuration.binding.parameters.ParameterHelper;
import java.util.Objects;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
class DefaultValue implements Comparable<DefaultValue> {

   final String clazz;
   final String key;
   final String value;
   final ParameterHelper.SUFFIX suffix;

   public DefaultValue(String clazz, String key, String value, ParameterHelper.SUFFIX suffix) {
      this.clazz = clazz;
      this.key = key;
      this.value = value;
      this.suffix = suffix;
   }

   @Override
   public int compareTo(DefaultValue o) {
      return (clazz + key).compareTo(o.clazz + o.key);
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 37 * hash + Objects.hashCode(this.clazz);
      hash = 37 * hash + Objects.hashCode(this.key);
      hash = 37 * hash + Objects.hashCode(this.suffix);
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final DefaultValue other = (DefaultValue) obj;
      if (!Objects.equals(this.clazz, other.clazz)) {
         return false;
      }
      if (!Objects.equals(this.key, other.key)) {
         return false;
      }
      if (this.suffix != other.suffix) {
         return false;
      }
      return true;
   }

}
