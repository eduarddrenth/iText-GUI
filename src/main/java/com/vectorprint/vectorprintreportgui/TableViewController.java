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
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.binding.parameters.ParameterHelper;
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.report.itext.style.stylers.DocumentSettings;
import static com.vectorprint.vectorprintreportgui.ViewHelper.*;
import com.vectorprint.vectorprintreportgui.text.SearchableTextArea;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Callback;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class TableViewController implements Initializable {

   private final ObservableList<ParameterProps> parameters = FXCollections.observableArrayList(new ArrayList<ParameterProps>(25));

   @FXML
   private TextField configString;
   @FXML
   private TableView<ParameterProps> parameterTable;
   @FXML
   private TableColumn<ParameterProps, String> pKey;
   @FXML
   private TableColumn<ParameterProps, ParameterProps> pValue;
   @FXML
   private TableColumn<ParameterProps, String> pType;
   @FXML
   private TableColumn<ParameterProps, String> pDefault;
   @FXML
   private TableColumn<ParameterProps, String> pReset;
   @FXML
   private TableColumn<ParameterProps, String> pDeclaringClass;

   private SearchableTextArea help, error;
   private Tab helpTab;
   private CheckBox pdf1a, toc;
   private Set<DefaultValue> defaults;

   private final ObjectProperty<Parameterizable> currentParameterizable = new SimpleObjectProperty<>();

   @Override
   public void initialize(URL url, ResourceBundle rb) {
      try {
         parameterTable.setItems(parameters);

         // TODO make pDeclaringClass use the correct property from ParameterProps as its cell value
         pDeclaringClass.setCellFactory((TableColumn<ParameterProps, String> p) -> new TableCell<ParameterProps, String>() {
            @Override
            protected void updateItem(String t, boolean bln) {
               super.updateItem(t, bln);
               setText(t);
               if (t != null && getTableRow().getItem() != null) {
                  setTooltip(tip(((ParameterProps) getTableRow().getItem()).getHelp()));
               }
            }
         });

         pType.setCellValueFactory(new PropertyValueFactory<>("type"));
         pType.setCellFactory((TableColumn<ParameterProps, String> p) -> new TableCell<ParameterProps, String>() {
            @Override
            protected void updateItem(final String t, boolean bln) {
               super.updateItem(t, bln);
               setText(t);
               if (t == null) {
                  return;
               }
               setTooltip(tip(t));
            }
         });
         pKey.setCellValueFactory(new PropertyValueFactory<>("key"));
         pKey.setCellFactory((TableColumn<ParameterProps, String> param) -> new TableCell<ParameterProps, String>() {

            @Override
            protected void updateItem(final String item, boolean empty) {
               setText(item);
               if (item == null) {
                  return;
               }
               setTooltip(tip(item + " (click for help)"));
               setOnMouseClicked((MouseEvent event) -> {
                  Parameterizable p = currentParameterizable.get();
                  help.searchArea(p.getClass().getSimpleName() + ": ", false);
                  help.searchArea("key=" + item, false);
                  helpTab.getTabPane().getSelectionModel().select(helpTab);
                  help.requestFocus();
               });
            }

         });
         pValue.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ParameterProps, ParameterProps>, ObservableValue<ParameterProps>>() {
            @Override
            public ObservableValue<ParameterProps> call(TableColumn.CellDataFeatures<ParameterProps, ParameterProps> param) {
               return param.getValue();
            }
         });
         pValue.setCellFactory((TableColumn<ParameterProps, ParameterProps> param) -> {
            return new TableCell<ParameterProps, ParameterProps>() {

               @Override
               protected void updateItem(final ParameterProps item, boolean empty) {
                  setGraphic(null);
                  if (item == null) {
                     return;
                  }
                  Class valueClass = item.getValueClass();
                  if (Boolean.class.equals(valueClass) || boolean.class.equals(valueClass)) {
                     final CheckBox checkBox = new CheckBox();
                     if (item.getKey().equals(DocumentSettings.PDFA)) {
                        bindToCheckbox(checkBox, item, pdf1a);
                     } else if (item.getKey().equals(DocumentSettings.TOC)) {
                        bindToCheckbox(checkBox, item, toc);
                     }
                     checkBox.setSelected(Boolean.parseBoolean(item.getVal()));
                     setGraphic(checkBox);
                  } else if (valueClass.isEnum()) {
                     final ComboBox<String> comboBox = new ComboBox();
                     ObservableList<String> ol = FXCollections.observableArrayList();
                     for (Object o : valueClass.getEnumConstants()) {
                        ol.add(String.valueOf(o));
                     }
                     comboBox.setItems(ol);
                     comboBox.getSelectionModel().select(item.getVal());
                     comboBox.valueProperty().addListener(item);
                     setGraphic(comboBox);
                  } else if (java.awt.Color.class.equals(valueClass)) {
                     java.awt.Color col = (java.awt.Color) item.getRawValue();
                     final ColorPicker cp = col == null ? new ColorPicker()
                         : new ColorPicker(new Color(col.getRed() / 255, col.getGreen() / 255, col.getBlue() / 255, col.getAlpha() / 255));
                     cp.valueProperty().addListener(item);
                     setGraphic(cp);
                  } else {
                     final TextField textField = new TextField(item.getVal());
                     textField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                        textField.getStyleClass().remove("error");
                        try {
                           item.setValue(newValue);
                        } catch (Exception e) {
                           textField.getStyleClass().add("error");
                           ViewHelper.writeStackTrace(e, error);
                        }
                     });
                     setGraphic(textField);
                  }
               }

               private void bindToCheckbox(final CheckBox child, final ParameterProps item, CheckBox master) {
                  // TODO make sure that whenever child changes master changes as well and vise versa
                  // TODO make sure item is notified of changes in child
               }

            };
         }
         );
         pDefault.setCellValueFactory(new PropertyValueFactory<>("val"));
         pDefault.setCellFactory((TableColumn<ParameterProps, String> p) -> new TableCell<ParameterProps, String>() {
            @Override
            protected void updateItem(final String v, boolean bln) {
               setGraphic(null);
               if (v == null) {
                  return;
               }
               ParameterProps pp = getTableView().getItems().get(getTableRow().getIndex());
               CheckBox checkbox = new CheckBox();
               DefaultValue defaultValue = new DefaultValue(currentParameterizable.get().getClass().getSimpleName(), pp.getKey(), pp.getVal(), ParameterHelper.SUFFIX.set_default);
               checkbox.setSelected(defaults.contains(defaultValue));
               checkbox.setTooltip(tip(String.format("use value as default for %s in %s", pp.getKey(), currentParameterizable.get().getClass().getSimpleName())));
               setGraphic(checkbox);
               checkbox.setOnAction((ActionEvent e) -> {
                  defaults.remove(defaultValue);
                  if (checkbox.isSelected()) {
                     defaults.add(defaultValue);
                     configString.clear();
                     configString.appendText(currentParameterizable.get().getClass().getSimpleName());
                     configString.appendText(".");
                     configString.appendText(pp.getKey());
                     configString.appendText(".");
                     configString.appendText(ParameterHelper.SUFFIX.set_default.name());
                     configString.appendText("=");
                     configString.appendText(pp.getVal());
                  } else {
                     configString.clear();
                  }
               });
            }
         });
         pReset.setCellValueFactory(new PropertyValueFactory<>("val"));
         pReset.setCellFactory((TableColumn<ParameterProps, String> p) -> new TableCell<ParameterProps, String>() {
            @Override
            protected void updateItem(final String v, boolean bln) {
               setGraphic(null);
               if (v == null) {
                  return;
               }
               ParameterProps pp = getTableView().getItems().get(getTableRow().getIndex());

               Button button = new Button("reset");
               button.setTooltip(tip(String.format("click to reset to default value: %s", pp.getDefault())));
               setGraphic(button);
               button.setOnAction((ActionEvent e) -> {
                  pp.resetToDefault();
               });
            }
         });

      } catch (NoClassDefFoundError ex) {
         Logger.getLogger(TableViewController.class.getName()).log(Level.SEVERE, null, ex);
         throw new VectorPrintRuntimeException(ex);
      } catch (Exception ex) {
         Logger.getLogger(TableViewController.class.getName()).log(Level.SEVERE, null, ex);
         throw new VectorPrintRuntimeException(ex);
      }
   }

   public ObservableList<ParameterProps> getParameters() {
      return parameters;
   }

   public TextField getConfigString() {
      return configString;
   }

   public ObjectProperty<Parameterizable> getCurrentParameterizable() {
      return currentParameterizable;
   }

   public void setHelp(SearchableTextArea help) {
      this.help = help;
   }

   public void setError(SearchableTextArea error) {
      this.error = error;
   }

   public void setHelpTab(Tab helpTab) {
      this.helpTab = helpTab;
   }

   public void setPdf1a(CheckBox pdf1a) {
      this.pdf1a = pdf1a;
   }

   public void setToc(CheckBox toc) {
      this.toc = toc;
   }

   public void setDefaults(Set<DefaultValue> defaults) {
      this.defaults = defaults;
   }

}
