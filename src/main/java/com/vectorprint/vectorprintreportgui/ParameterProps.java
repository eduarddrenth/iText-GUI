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
import com.vectorprint.ClassHelper;
import com.vectorprint.configuration.binding.parameters.ParamBindingHelper;
import com.vectorprint.configuration.binding.parameters.ParamBindingService;
import com.vectorprint.configuration.parameters.Parameter;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParameterProps implements Comparable<ParameterProps>, Observer, ChangeListener {

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
      p.addObserver(this);
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
      if (value != null && !value.isEmpty()) {
         Serializable parseAsParameterValue = ParamBindingService.getInstance().getFactory().getParser(new StringReader("")).parseAsParameterValue(value, p);
         helper.setValueOrDefault(p, parseAsParameterValue, false);
      } else {
         helper.setValueOrDefault(p, null, false);
      }
      this.value.set(value == null ? "" : value);
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

   @Override
   public void update(Observable o, Object arg) {
      ParamBindingHelper helper = ParamBindingService.getInstance().getFactory().getBindingHelper();
      value.set(helper.serializeValue(helper.getValueToSerialize(p, false)));
   }

   @Override
   public void changed(ObservableValue observable, Object oldValue, Object newValue) {
      // a screen component has changed, update the parameter value
      if (newValue instanceof Boolean) {
         setValue(newValue.toString());
         System.err.println("now: " + newValue);
      }
   }

}
