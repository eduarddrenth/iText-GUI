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
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.report.itext.style.BaseStyler;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class StyleClassTableController implements Initializable {

   private final ObservableList<Parameterizable> parameterizableForClass = FXCollections.observableArrayList(new ArrayList<Parameterizable>(3));

   private ObservableMap<String, List<Parameterizable>> stylingConfig;

   @FXML
   private ComboBox<String> stylerKeysCopy;
   @FXML
   private TableView<Parameterizable> parameterizableTable;
   @FXML
   private TableColumn<Parameterizable, Parameterizable> sUpDown;
   @FXML
   private TableColumn<Parameterizable, Parameterizable> rm;
   @FXML
   private TableColumn<Parameterizable, String> sHelp;

   @FXML
   private void showStylers(Event event) {
      if (stylerKeysCopy.getValue() != null && stylingConfig.containsKey(stylerKeysCopy.getValue())) {
         parameterizableForClass.clear();
         stylingConfig.get(stylerKeysCopy.getValue()).stream().forEach((bs) -> {
            parameterizableForClass.add(bs);
         });

      }
   }

   @Override
   public void initialize(URL url, ResourceBundle rb) {
      try {
         parameterizableTable.setItems(parameterizableForClass);

         sHelp.setCellValueFactory((CellDataFeatures<Parameterizable, String> p)
             -> new ReadOnlyObjectWrapper(p.getValue().getClass().getSimpleName() + ": " + ViewHelper.helpFor(p.getValue())));
         sHelp.setCellFactory((TableColumn<Parameterizable, String> p) -> new TableCell<Parameterizable, String>() {
            @Override
            protected void updateItem(String t, boolean bln) {
               super.updateItem(t, bln);
               setText(t);
               if (t != null) {
                  setTooltip(ViewHelper.tip(t));
               }
            }
         });
         sUpDown.setCellValueFactory((TableColumn.CellDataFeatures<Parameterizable, Parameterizable> p) -> new ReadOnlyObjectWrapper<>(p.getValue()));
         sUpDown.setCellFactory((TableColumn<Parameterizable, Parameterizable> p) -> new TableCell<Parameterizable, Parameterizable>() {
            @Override
            protected void updateItem(final Parameterizable bs, boolean bln) {
               super.updateItem(bs, bln);
               setGraphic(null);
               if (bs == null || getTableRow() == null) {
                  return;
               }
               final Integer t = getTableRow().getIndex();
               Button b = new Button("/\\");
               b.setTooltip(ViewHelper.tip("move styler up"));
               b.setOnAction((ActionEvent e) -> {
                  if (t == null || parameterizableForClass == null) {
                     return;
                  }
                  if (t > 0) {
                     Parameterizable toMove = parameterizableForClass.get(t);
                     Parameterizable sp = parameterizableForClass.set(t - 1, toMove);
                     parameterizableForClass.set(t, sp);
                     stylingConfig.get(stylerKeysCopy.getValue()).clear();
                     parameterizableForClass.forEach((p) -> {
                        stylingConfig.get(stylerKeysCopy.getValue()).add((BaseStyler) p);
                     });
                  }
               });
               Button bd = new Button("\\/");
               bd.setLayoutX(35);
               bd.setTooltip(ViewHelper.tip("move styler down"));
               bd.setOnAction((ActionEvent e) -> {
                  if (t == null || parameterizableForClass == null) {
                     return;
                  }
                  if (t < parameterizableForClass.size() - 1) {
                     Parameterizable toMove = parameterizableForClass.get(t);
                     Parameterizable sp = parameterizableForClass.set(t + 1, toMove);
                     parameterizableForClass.set(t, sp);
                     stylingConfig.get(stylerKeysCopy.getValue()).clear();
                     stylingConfig.get(stylerKeysCopy.getValue()).addAll(parameterizableForClass);
                  }
               });

               setGraphic(new Group(b, bd));
            }
         });
         rm.setCellValueFactory((TableColumn.CellDataFeatures<Parameterizable, Parameterizable> p) -> new ReadOnlyObjectWrapper<>(p.getValue()));
         rm.setCellFactory((TableColumn<Parameterizable, Parameterizable> p) -> new TableCell<Parameterizable, Parameterizable>() {
            @Override
            protected void updateItem(final Parameterizable bs, boolean bln) {
               super.updateItem(bs, bln);
               setGraphic(null);
               if (bs == null || getTableRow() == null) {
                  return;
               }
               final Integer t = getTableRow().getIndex();
               Button b = new Button("X");
               b.setTooltip(ViewHelper.tip("remove styler"));
               b.setOnAction((ActionEvent e) -> {
                  if (t == null) {
                     return;
                  }
                  Parameterizable bs1 = parameterizableForClass.get(t);
                  boolean removed = parameterizableForClass.remove(bs1);
                  if (removed) {
                     // remove from config
                     stylingConfig.get(stylerKeysCopy.getValue()).clear();
                     stylingConfig.get(stylerKeysCopy.getValue()).addAll(parameterizableForClass);
                  }
                  if (parameterizableForClass.isEmpty()) {
                     String clazz = stylerKeysCopy.getValue();
                     if (clazz != null) {
                        System.out.println("removed config: " + stylingConfig.remove(clazz));
                     }
                  }
               });

               setGraphic(b);
            }
         });
      } catch (NoClassDefFoundError ex) {
         Logger.getLogger(StyleClassTableController.class.getName()).log(Level.SEVERE, null, ex);
         throw new VectorPrintRuntimeException(ex);
      } catch (Exception ex) {
         Logger.getLogger(StyleClassTableController.class.getName()).log(Level.SEVERE, null, ex);
         throw new VectorPrintRuntimeException(ex);
      }
   }

   void setStylingConfig(ObservableMap<String, List<Parameterizable>> stylingConfig) {
      this.stylingConfig = stylingConfig;
   }

   void setStylerKeyItems(ObservableList<String> items) {
      stylerKeysCopy.setItems(items);
   }

   void select(String key) {
      stylerKeysCopy.getSelectionModel().select(key);
   }

   void setCStylerKeysFactory(Callback<ListView<String>, ListCell<String>> factory) {
      stylerKeysCopy.setCellFactory(factory);
   }

}
