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
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParameterProps<T extends Serializable> extends SimpleObjectProperty<ParameterProps<T>> implements Comparable<ParameterProps>, ChangeListener, Observer {

   private final Class type;
   private String value;
   private final Parameter<T> p;

   public ParameterProps(Parameter<T> p) {
      this.p = p;
      type = ClassHelper.findParameterClass(0, p.getClass(), Parameter.class);
      ParamBindingHelper helper = ParamBindingService.getInstance().getFactory().getBindingHelper();
      value = helper.serializeValue(helper.getValueToSerialize(p, false));
      fireValueChangedEvent();
      p.addObserver(this);
      set(this);
   }

   public String getKey() {
      return p.getKey();
   }

   public String getType() {
      return type.getName();
   }

   public Class getValueClass() {
      return type;
   }

   public String getVal() {
      return value;
   }

   public T getRawValue() {
      return p.getValue();
   }

   public T getDefault() {
      return p.getDefault();
   }

   public void setValue(String value) {
      ParamBindingHelper helper = ParamBindingService.getInstance().getFactory().getBindingHelper();
      if (value != null && !value.isEmpty()) {
         T parseAsParameterValue = (T) ParamBindingService.getInstance().getFactory().getParser(new StringReader("")).parseAsParameterValue(value, p);
         helper.setValueOrDefault(p, parseAsParameterValue, false);
      } else {
         helper.setValueOrDefault(p, null, false);
      }
   }

   public String getHelp() {
      return p.getHelp();
   }

   public String getDeclaringClass() {
      return p.getDeclaringClass().getName();
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 97 * hash + Objects.hashCode(p.getKey());
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
      if (!Objects.equals(p.getKey(), other.p.getKey())) {
         return false;
      }
      return true;
   }

   @Override
   public int compareTo(ParameterProps o) {
      return getKey().compareTo(o.getKey());
   }

   @Override
   public void changed(ObservableValue observable, Object oldValue, Object newValue) {
      // a screen component has changed, update the parameter value
      ParamBindingHelper helper = ParamBindingService.getInstance().getFactory().getBindingHelper();
      Object val = newValue;
      if (val instanceof Color) {
         Color c = (Color) val;
         val = new java.awt.Color((int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
      }
      if (p.getValueClass().isInstance(val)) {
         helper.setValueOrDefault(p, (T) val, false);
      } else {
         /* here we rely on serialization / parsing to try and get a correct value for the parameter
            - using current syntax
            - serialize
            - parse (in setValue method)
          */
         setValue(helper.serializeValue(val));
      }
   }

   public void resetToDefault() {
      p.setValue(p.getDefault());
   }

   @Override
   public void update(Observable o, Object arg) {
      Parameter<T> p = (Parameter<T>) o;
      ParamBindingHelper helper = ParamBindingService.getInstance().getFactory().getBindingHelper();
      value = helper.serializeValue(helper.getValueToSerialize(p, false));
      fireValueChangedEvent();
   }

}
