/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.vectorprintreportgui;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.steadystate.css.parser.SACParser;
import com.vectorprint.IOHelper;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.VectorPrintProperties;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.configuration.parameters.ParameterHelper;
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.itext.EventHelper;
import com.vectorprint.report.itext.Help;
import com.vectorprint.report.itext.jaxb.Datamappingstype;
import com.vectorprint.report.itext.mappingconfig.DatamappingHelper;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.DefaultStylerFactory;
import com.vectorprint.report.itext.style.DocumentStyler;
import com.vectorprint.report.itext.style.StylerFactory;
import com.vectorprint.report.itext.style.StylerFactoryHelper;
import com.vectorprint.report.itext.style.StylingCondition;
import com.vectorprint.report.itext.style.conditions.AbstractCondition;
import com.vectorprint.report.itext.style.css.CssTransformer;
import com.vectorprint.report.itext.style.stylers.AbstractStyler;
import com.vectorprint.report.itext.style.stylers.Advanced;
import com.vectorprint.report.itext.style.stylers.DocumentSettings;
import com.vectorprint.report.itext.style.stylers.NewLine;
import com.vectorprint.report.itext.style.stylers.NewPage;
import com.vectorprint.report.itext.style.stylers.Padding;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.GroupBuilder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TooltipBuilder;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooserBuilder;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javax.xml.bind.UnmarshalException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Controller implements Initializable {

   private final ObservableList<String> styleClasses = FXCollections.observableArrayList(new ArrayList<String>(25));
   private final Map<String, List<BaseStyler>> stylingConfig = new TreeMap<String, List<BaseStyler>>();
   private final Map<String, List<String>> commentsBefore = new HashMap<String, List<String>>();
   private final List<String> commentsAfter = new ArrayList<String>(3);
   private final Map<String, List<StylingCondition>> conditionConfig = new TreeMap<String, List<StylingCondition>>();
   private final Map<String, Set<ParameterProps>> defaults = new TreeMap<String, Set<ParameterProps>>();
   private final Map<String, String> extraSettings = new TreeMap<String, String>();
   private static final Set<Class<? extends Parameterizable>> duplicatesAllowed = new HashSet();
   private final ObservableList<BaseStyler> stylersForClass = FXCollections.observableArrayList(new ArrayList<BaseStyler>(3));
   private final ObservableList<ParameterProps> parameters = FXCollections.observableArrayList(new ArrayList<ParameterProps>(50));

   @FXML
   private ComboBox<Parameterizable> stylerCombo;
   @FXML
   private ComboBox<String> stylerKeys;
   @FXML
   private ComboBox<String> stylerKeysCopy;
   @FXML
   private TextField configString;
   @FXML
   private TextField xmlconfig;
   @FXML
   private TextArea datamappingxml;
   @FXML
   private CheckBox toc;
   @FXML
   private CheckBox footer;
   @FXML
   private CheckBox cssvalidate;
   @FXML
   private CheckBox pdf1a;
   @FXML
   private CheckBox debug;
   @FXML
   private TextArea stylesheet;
   @FXML
   private TextArea help;
   @FXML
   private Button parHelp;
   @FXML
   private TextArea error;
   @FXML
   private Label stylerHelp;
   @FXML
   private Tab styleTab;
   @FXML
   private Tab build;
   @FXML
   private Tab errorTab;
   @FXML
   private Tab helpTab;
   @FXML
   private Tooltip stylerTooltip;
   @FXML
   private TableView<ParameterProps> parameterTable;
   @FXML
   private TableColumn<ParameterProps, String> pKey;
   @FXML
   private TableColumn pValue;
   @FXML
   private TableColumn<ParameterProps, String> pType;
   @FXML
   private TableColumn<ParameterProps, ParameterProps> pDefault;
   @FXML
   private TableColumn<ParameterProps, String> pHelp;
   @FXML
   private TableView<BaseStyler> stylerTable;
   @FXML
   private TableColumn sUpDown;
   @FXML
   private TableColumn rm;
   @FXML
   private TableColumn sCreates;
   @FXML
   private TableColumn sHelp;
   @FXML
   private Button pre;
   @FXML
   private Button post;
   @FXML
   private Button page;

   @FXML
   private void chooseStandardStyle(ActionEvent event) {
      if (pre.equals(event.getSource())) {
         chooseOrAdd(DefaultStylerFactory.DEFAULTSTYLERSFIRST);
      } else if (post.equals(event.getSource())) {
         chooseOrAdd(DefaultStylerFactory.DEFAULTSTYLERSLAST);
      } else if (page.equals(event.getSource())) {
         chooseOrAdd(DefaultStylerFactory.PAGESTYLERS);
      }
   }

   private void chooseOrAdd(String styleClass) {
      if (!stylerKeys.getItems().contains(styleClass)) {
         stylerKeys.getItems().add(styleClass);
      }
      stylerKeys.getSelectionModel().select(styleClass);
   }

   @FXML
   private void showStyleOrCondition(ActionEvent event) {
      if (stylingConfig.isEmpty()||!stylingConfig.containsKey(stylerKeys.getValue())) {
         return;
      }
      try {
         pickStylerToConfigure(stylingConfig.get(stylerKeys.getValue()));
      } catch (Exception ex) {
         toError(ex);
      }
   }

   @FXML
   private void chooseStyleOrCondition(ActionEvent event) {
      if (null == stylerCombo.getValue()) {
         stylerCombo.requestFocus();
         return;
      }
      bs = (stylerCombo.getValue() instanceof DocumentStyler) ? stylerCombo.getValue() : stylerCombo.getValue().clone();
      if (bs instanceof DocumentSettings) {
         chooseOrAdd(ReportConstants.DOCUMENTSETTINGS);
      }
      parameters.clear();
      try {
         Parameterizable _st = stylerCombo.getValue();
         stylerHelp.setText((_st instanceof BaseStyler) ? ((BaseStyler) _st).getHelp() : "condition to determine when to style or not");
         for (Parameter p : _st.getParameters().values()) {
            parameters.add(new ParameterProps(p));
         }
      } catch (Exception ex) {
         toError(ex);
      }
      showStylerHelp(event);
   }
   private Parameterizable bs;

   @FXML
   private void clear(ActionEvent event) {
      parameters.clear();
      stylersForClass.clear();
      styleClasses.clear();
      stylingConfig.clear();
      conditionConfig.clear();
      defaults.clear();
      extraSettings.clear();
      processed.clear();
      commentsAfter.clear();
      commentsAfter.clear();
   }

   @FXML
   private void showStylers(Event event) {
      stylersForClass.clear();
      if (stylerKeysCopy.getValue() != null && stylingConfig.containsKey(stylerKeysCopy.getValue())) {
         for (BaseStyler bs : stylingConfig.get(stylerKeysCopy.getValue())) {
            stylersForClass.add(bs);
         }
      }
   }

   @FXML
   private void showConfig(ActionEvent event) {
      if ("".equals(stylerKeys.getValue()) || null == stylerKeys.getValue()) {
         stylerKeys.requestFocus();
         return;
      }
      try {
         for (ParameterProps pp : parameters) {
            if (pp.getValue() == null || "".equals(pp.getValue())) {
               continue;
            }
            Parameter p = bs.getParameters().get(pp.getKey());
            p.setValue(p.convert(pp.getValue()));
         }
         configString.setText(ParameterHelper.toConfig(bs, true));
      } catch (Exception ex) {
         toError(ex);
      }
   }

   private void toError(Throwable ex) {
      StringWriter sw = new StringWriter(1024);
      ex.printStackTrace(new PrintWriter(sw));
      error.clear();
      error.setText(sw.toString());
      notify("ok (errors tab for details)", "error", ex.getMessage());
   }

   private void prepareAdd(Parameterizable p, List current) {
      if (!duplicatesAllowed.contains(p.getClass())) {
         for (Iterator it = current.iterator(); it.hasNext();) {
            Object pz = it.next();
            if (p.getClass().equals(pz.getClass())) {
               it.remove();
               break;
            }
         }
      }
   }

   private static class StylerComparator implements Comparator<Parameterizable> {

      @Override
      public int compare(Parameterizable o1, Parameterizable o2) {
         if (o1 instanceof BaseStyler && o2 instanceof StylingCondition) {
            return 1;
         } else if (o1 instanceof StylingCondition && o2 instanceof BaseStyler) {
            return -1;
         } else {
            return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
         }
      }
   }
   private static final StylerComparator STYLER_COMPARATOR = new StylerComparator();

   private boolean add(Parameterizable p) {
      if ("".equals(stylerKeys.getValue())) {
         stylerKeys.requestFocus();
         return false;
      }
      String styleClass = stylerKeys.getValue();
      if (p instanceof BaseStyler) {
         if (conditionConfig.containsKey(styleClass)) {
            notify("ok",
                toConfigString(styleClass, conditionConfig.get(styleClass)),String.format("style class %s in use, choose another", styleClass));
            return false;
         }
         if (ReportConstants.DOCUMENTSETTINGS.equals(styleClass) && !(p instanceof DocumentStyler)) {
            notify("ok",
                styleClass,String.format("style class %s reserved for document settings, choose another",ReportConstants.DOCUMENTSETTINGS));
            return false;
         }
         if (!ReportConstants.DOCUMENTSETTINGS.equals(styleClass) && p instanceof DocumentStyler) {
            notify("ok",
                styleClass,String.format("style class should be %s for document settings",ReportConstants.DOCUMENTSETTINGS));
            return false;
         }
         if (!stylingConfig.containsKey(styleClass) && !"".equals(styleClass)) {
            stylingConfig.put(styleClass, new ArrayList<BaseStyler>());
         } else if (((BaseStyler) p).creates() && !stylingConfig.get(styleClass).isEmpty()) {
            notify("ok", "must be first",
                String.format("styler %s creates a report element, should be the first styler for a style class, you should probably reorder your stylers", p.getClass().getSimpleName()));
         }
         if (!configString.getText().startsWith(bs.getClass().getSimpleName() + ".")) {
            prepareAdd(p, stylingConfig.get(styleClass));
            stylingConfig.get(styleClass).add((BaseStyler) p);
            if (!"".equals(p.getValue(AbstractStyler.CONDITONS, String.class))) {
               String cnd = p.getValue(AbstractStyler.CONDITONS, String.class);
               if (null != cnd && !conditionConfig.containsKey(cnd)) {
                  chooseOrAdd(p.getValue(AbstractStyler.CONDITONS, String.class));
                  notify("add " + p.getValue(AbstractStyler.CONDITONS, String.class), "warning", String.format("condition %s is missing", p.getValue(AbstractStyler.CONDITONS, String.class)));
               }
            }
         }
      } else {
         if (stylingConfig.containsKey(styleClass)) {
            notify("ok",
                toConfigString(styleClass, stylingConfig.get(styleClass)),String.format("style class %s in use, choose another", styleClass));
            return false;
         }
         if (ReportConstants.DOCUMENTSETTINGS.equals(styleClass)) {
            notify("ok",
                "",String.format("style class %s reserved for document settings, choose another", styleClass));
            return false;
         }
         if (!conditionConfig.containsKey(styleClass) && !"".equals(styleClass)) {
            conditionConfig.put(styleClass, new ArrayList<StylingCondition>());
         }
         if (!configString.getText().startsWith(bs.getClass().getSimpleName() + ".")) {
            prepareAdd(p, conditionConfig.get(styleClass));
            conditionConfig.get(styleClass).add((StylingCondition) p);
         }
      }
      return true;
   }
   private Button b = new Button("add condition");
   private Label l = new Label("detail message");
   private Stage st = new Stage(StageStyle.UTILITY);

   {
      st.initOwner(StylesheetBuilder.topWindow);
      st.initModality(Modality.NONE);
      b.setOnAction(new EventHandler<ActionEvent>() {
         @Override
         public void handle(ActionEvent t) {
            st.hide();
         }
      });
   }

   private String addNewLines(String orig, int linelength) {
      if (orig == null || orig.length() <= linelength) {
         return orig;
      }
      StringBuilder sb = new StringBuilder(orig.length());
      int offset = 0;
      while (offset < orig.length()) {
         int end = (offset + linelength >= orig.length()) ? orig.length() : offset + linelength;
         sb.append(orig.substring(offset, end)).append(System.getProperty("line.separator"));
         offset = end;
      }
      return sb.toString();
   }

   private void notify(String buttonText, String title, String details) {
      l.setText(addNewLines(details, 120));
      b.setText(buttonText);
      HBox hb = new HBox(10d);
      hb.setAlignment(Pos.CENTER);
      hb.setPadding(new Insets(10));
      hb.getChildren().addAll(b, l);
      st.setScene(new Scene(hb));
      st.setTitle(title);
      st.show();
   }

   private void pickStylerToConfigure(final List<BaseStyler> stylers) {
      ToggleGroup tg = new ToggleGroup();
      VBox vb = new VBox(10d);
      Scene sc = new Scene(vb);
      vb.setPadding(new Insets(20d));
      int i = -1;
      for (BaseStyler s : stylers) {
         final BaseStyler kopie = s;
         RadioButton rb = new RadioButton(s.getClass().getSimpleName());
         rb.setTooltip(tip(s.toConfig()));
         rb.setToggleGroup(tg);
         vb.getChildren().add(rb);
         rb.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
               parameters.clear();
               bs = kopie;
               stylerHelp.setText((bs instanceof BaseStyler) ? ((BaseStyler) bs).getHelp() : "condition to determine when to style or not");
               for (Parameter p : kopie.getParameters().values()) {
                  parameters.add(new ParameterProps(p));
               }
            }
         });
      }
      st.setScene(sc);
      st.setTitle("choose styler");
      st.show();
   }

   private String toConfigString(String clazz, List<? extends Parameterizable> sp) {
      StringBuilder sb = new StringBuilder(clazz);
      sb.append("=");
      for (Parameterizable p : sp) {
         sb.append(ParameterHelper.toConfig(p, true)).append(";");
      }
      return sb.toString().substring(0, sb.length() - 1);
   }

   private void printComment(String key) {
      if (key != null && commentsBefore.containsKey(key)) {
         for (String s : commentsBefore.get(key)) {
            stylesheet.appendText(s);
         }
      } else {
         for (String s : commentsAfter) {
            stylesheet.appendText(s);
         }
      }
   }

   @FXML
   private void buildStylesheet(ActionEvent event) {
      try {
         stylesheet.clear();

         stylesheet.appendText("# default values for styler parameters");
         stylesheet.appendText(System.getProperty("line.separator"));

         for (Map.Entry<String, Set<ParameterProps>> def : defaults.entrySet()) {
            for (ParameterProps pp : def.getValue()) {
               printComment(def.getKey() + "." + pp.getKey());
               stylesheet.appendText(def.getKey());
               stylesheet.appendText(".");
               stylesheet.appendText(pp.getKey());
               stylesheet.appendText("=");
               stylesheet.appendText(pp.getValue());
               stylesheet.appendText(System.getProperty("line.separator"));
            }
         }

         stylesheet.appendText(System.getProperty("line.separator"));
         stylesheet.appendText("# style classes that define conditions for styling");
         stylesheet.appendText(System.getProperty("line.separator"));

         for (Map.Entry<String, List<StylingCondition>> e : conditionConfig.entrySet()) {
            printComment(e.getKey());
            stylesheet.appendText(toConfigString(e.getKey(), e.getValue()));
            stylesheet.appendText(System.getProperty("line.separator"));
         }

         stylesheet.appendText(System.getProperty("line.separator"));
         stylesheet.appendText("# style classes that define styling");
         stylesheet.appendText(System.getProperty("line.separator"));

         for (Map.Entry<String, List<BaseStyler>> e : stylingConfig.entrySet()) {
            printComment(e.getKey());
            stylesheet.appendText(toConfigString(e.getKey(), e.getValue()));
            stylesheet.appendText(System.getProperty("line.separator"));
         }

         stylesheet.appendText(System.getProperty("line.separator"));
         stylesheet.appendText("# several extra settings");
         stylesheet.appendText(System.getProperty("line.separator"));

         for (Map.Entry<String, String> e : extraSettings.entrySet()) {
            printComment(e.getKey());
            stylesheet.appendText(e.getKey() + '=' + e.getValue());
            stylesheet.appendText(System.getProperty("line.separator"));
         }
         printComment(null);

         styleTab.getTabPane().getSelectionModel().select(styleTab);
      } catch (Exception ex) {
         toError(ex);
      }

   }

   @FXML
   private void toConfig(ActionEvent event) {
      try {
         if (bs == null) {
            throw new VectorPrintRuntimeException("first choose a style or condition using configure");
         }
         showConfig(event);
         add(bs);
      } catch (Exception ex) {
         toError(ex);
      }
   }

   private Tooltip tip(String text) {
      return TooltipBuilder.create().maxWidth(400).autoHide(false).autoFix(true).wrapText(true).text(text).build();
   }

   @FXML
   private void showStylerHelp(Event event) {
      stylerHelp.setText(help(stylerCombo.getValue()));
      stylerHelp.setTooltip(tip(help(stylerCombo.getValue())));
   }

   private String help(Parameterizable p) {
      return p != null ? (p instanceof BaseStyler)
          ? ((BaseStyler) p).getHelp()
          : ((StylingCondition) p).getHelp() : "";
   }
// records relative x and y co-ordinates.

   private static class Delta {

      double x, y;
   }

   final Delta dragDelta = new Delta();
   final Stage parent = StylesheetBuilder.topWindow;

   public void dragStart(MouseEvent mouseEvent) {
      // record a delta distance for the drag and drop operation.
      dragDelta.x = parent.getX() - mouseEvent.getScreenX();
      dragDelta.y = parent.getY() - mouseEvent.getScreenY();
   }

   public void dragged(MouseEvent mouseEvent) {
      parent.setX(mouseEvent.getScreenX() + dragDelta.x);
      parent.setY(mouseEvent.getScreenY() + dragDelta.y);
   }

   @Override
   public void initialize(URL url, ResourceBundle rb) {
      try {
         List<Parameterizable> sorted = new ArrayList<Parameterizable>(Help.getStylersAndConditions());
         Collections.sort(sorted, STYLER_COMPARATOR);
         synchronized (duplicatesAllowed) {
            if (duplicatesAllowed.isEmpty()) {
               duplicatesAllowed.add(Padding.class);
               duplicatesAllowed.add(NewLine.class);
               duplicatesAllowed.add(NewPage.class);
               for (Parameterizable pz : sorted) {
                  if (pz instanceof Advanced) {
                     duplicatesAllowed.add(pz.getClass());
                  }
               }
            }
         }
         stylerCombo.setCellFactory(new Callback<ListView<Parameterizable>, ListCell<Parameterizable>>() {
            @Override
            public ListCell<Parameterizable> call(ListView<Parameterizable> p) {
               return new ListCell<Parameterizable>() {
                  @Override
                  protected void updateItem(Parameterizable t, boolean bln) {
                     super.updateItem(t, bln);
                     setText(t == null ? "" : t.getClass().getSimpleName());
                     setTooltip(t != null ? tip(t.getClass().getName() + ": " + help(t)) : null);
                  }
               };
            }
         });
         stylerCombo.setConverter(new StringConverter<Parameterizable>() {
            private Parameterizable p = null;

            @Override
            public String toString(Parameterizable t) {
               this.p = t;
               return t.getClass().getSimpleName();
            }

            @Override
            public Parameterizable fromString(String string) {
               return p;
            }
         });
         stylerCombo.setItems(FXCollections.observableArrayList(sorted));

         stylerKeys.setPromptText("required!");
         parameterTable.setItems(parameters);
         stylerTable.setItems(stylersForClass);

         pHelp.setCellValueFactory(new PropertyValueFactory<ParameterProps, String>("help"));
         pHelp.setCellFactory(new Callback<TableColumn<ParameterProps, String>, TableCell<ParameterProps, String>>() {
            @Override
            public TableCell<ParameterProps, String> call(TableColumn<ParameterProps, String> p) {
               return new TableCell<ParameterProps, String>() {
                  @Override
                  protected void updateItem(String t, boolean bln) {
                     super.updateItem(t, bln);
                     setText(t);
                     if (t != null) {
                        setTooltip(tip(t));
                     }
                  }
               };
            }
         });

         pType.setCellValueFactory(new PropertyValueFactory<ParameterProps, String>("type"));
         pType.setCellFactory(new Callback<TableColumn<ParameterProps, String>, TableCell<ParameterProps, String>>() {
            @Override
            public TableCell<ParameterProps, String> call(TableColumn<ParameterProps, String> p) {
               return new TableCell<ParameterProps, String>() {
                  @Override
                  protected void updateItem(final String t, boolean bln) {
                     super.updateItem(t, bln);
                     setText(t);
                     if (t == null) {
                        return;
                     }
                     setTooltip(tip(t));
                  }
               };
            }
         });
         pKey.setCellValueFactory(new PropertyValueFactory<ParameterProps, String>("key"));
         pKey.setCellFactory(new Callback<TableColumn<ParameterProps, String>, TableCell<ParameterProps, String>>() {

            @Override
            public TableCell<ParameterProps, String> call(TableColumn<ParameterProps, String> param) {
               return new TableCell<ParameterProps, String>() {

                  @Override
                  protected void updateItem(final String item, boolean empty) {
                     setText(item);
                     if (item == null) {
                        return;
                     }
                     setTooltip(tip(item + " (click for help)"));
                     setOnMouseClicked(new EventHandler<MouseEvent>() {

                        @Override
                        public void handle(MouseEvent event) {
                           Parameterizable p = stylerCombo.getValue();
                           searchArea(help, p.getClass().getSimpleName() + ": ");
                           searchArea(help, "key: " + item);
                           helpTab.getTabPane().getSelectionModel().select(helpTab);
                           help.requestFocus();
                        }
                     });
                  }

               };
            }

         });
         pValue.setCellValueFactory(new PropertyValueFactory<ParameterProps, String>("value"));
         pValue.setCellFactory(TextFieldTableCell.forTableColumn());
         pValue.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ParameterProps, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<ParameterProps, String> t) {
               try {
                  t.getRowValue().setValue(t.getNewValue());
               } catch (Exception ex) {
                  IllegalArgumentException illegalArgumentException = new IllegalArgumentException("wrong value (" + ex.getMessage() + ")", ex);
                  toError(illegalArgumentException);
                  throw illegalArgumentException;
               }
            }
         });
         pDefault.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ParameterProps, ParameterProps>, ObservableValue<ParameterProps>>() {
            @Override
            public ObservableValue<ParameterProps> call(TableColumn.CellDataFeatures<ParameterProps, ParameterProps> p) {
               return new ReadOnlyObjectWrapper<ParameterProps>(p.getValue());
            }
         });
         pDefault.setCellFactory(new Callback<TableColumn<ParameterProps, ParameterProps>, TableCell<ParameterProps, ParameterProps>>() {
            @Override
            public TableCell<ParameterProps, ParameterProps> call(TableColumn<ParameterProps, ParameterProps> p) {
               return new TableCell<ParameterProps, ParameterProps>() {
                  @Override
                  protected void updateItem(final ParameterProps t, boolean bln) {
                     super.updateItem(t, bln);
                     setGraphic(null);
                     if (t == null) {
                        return;
                     }
                     Button b = new Button("D");
                     {
                        b.setTooltip(tip(t == null ? "" : String.format("use value as default for %s in %s", t.getKey(), bs.getClass().getSimpleName())));
                     }
                     setGraphic(b);
                     b.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                           if (!defaults.containsKey(bs.getClass().getSimpleName())) {
                              defaults.put(bs.getClass().getSimpleName(), new TreeSet<ParameterProps>());
                           }
                           if (defaults.get(bs.getClass().getSimpleName()).contains(t)) {
                              defaults.get(bs.getClass().getSimpleName()).remove(t);
                           }
                           defaults.get(bs.getClass().getSimpleName()).add(t);
                           configString.clear();
                           configString.appendText(bs.getClass().getSimpleName());
                           configString.appendText(".");
                           configString.appendText(t.getKey());
                           configString.appendText("=");
                           configString.appendText(t.getValue());
                        }
                     });
                  }
               };

            }
         });

         sHelp.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<BaseStyler, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(CellDataFeatures<BaseStyler, String> p) {
               return new ReadOnlyObjectWrapper(p.getValue().getClass().getSimpleName() + ": " + p.getValue().getHelp());
            }
         });
         sHelp.setCellFactory(new Callback<TableColumn<BaseStyler, String>, TableCell<BaseStyler, String>>() {
            @Override
            public TableCell<BaseStyler, String> call(TableColumn<BaseStyler, String> p) {
               return new TableCell<BaseStyler, String>() {
                  @Override
                  protected void updateItem(String t, boolean bln) {
                     super.updateItem(t, bln);
                     setText(t);
                     if (t != null) {
                        setTooltip(tip(t));
                     }
                  }
               };
            }
         });
         sCreates.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<BaseStyler, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<BaseStyler, String> p) {
               return new ReadOnlyObjectWrapper<String>(String.valueOf(p.getValue().creates()));
            }
         });
         sUpDown.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<BaseStyler, BaseStyler>, ObservableValue<BaseStyler>>() {

            @Override
            public ObservableValue<BaseStyler> call(TableColumn.CellDataFeatures<BaseStyler, BaseStyler> p) {
               return new ReadOnlyObjectWrapper<BaseStyler>(p.getValue());
            }
         });
         sUpDown.setCellFactory(new Callback<TableColumn<BaseStyler, BaseStyler>, TableCell<BaseStyler, BaseStyler>>() {
            @Override
            public TableCell<BaseStyler, BaseStyler> call(TableColumn<BaseStyler, BaseStyler> p) {
               return new TableCell<BaseStyler, BaseStyler>() {
                  @Override
                  protected void updateItem(final BaseStyler bs, boolean bln) {
                     super.updateItem(bs, bln);
                     setGraphic(null);
                     if (bs == null || getTableRow() == null) {
                        return;
                     }
                     final Integer t = getTableRow().getIndex();
                     Button b = new Button("/\\");
                     b.setTooltip(tip("move styler up"));
                     b.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                           if (t == null || stylersForClass == null) {
                              return;
                           }
                           if (t > 0) {
                              BaseStyler toMove = stylersForClass.get(t);
                              BaseStyler sp = stylersForClass.set(t - 1, toMove);
                              stylersForClass.set(t, sp);
                              stylingConfig.get(stylerKeysCopy.getValue()).clear();
                              stylingConfig.get(stylerKeysCopy.getValue()).addAll(stylersForClass);
                           }
                        }
                     });
                     Button bd = new Button("\\/");
                     bd.setLayoutX(35);
                     bd.setTooltip(tip("move styler down"));
                     bd.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                           if (t == null || stylersForClass == null) {
                              return;
                           }
                           if (t < stylersForClass.size() - 1) {
                              BaseStyler toMove = stylersForClass.get(t);
                              BaseStyler sp = stylersForClass.set(t + 1, toMove);
                              stylersForClass.set(t, sp);
                              stylingConfig.get(stylerKeysCopy.getValue()).clear();
                              stylingConfig.get(stylerKeysCopy.getValue()).addAll(stylersForClass);
                           }
                        }
                     });

                     setGraphic(GroupBuilder.create().children(b, bd).build());
                  }
               };

            }
         });
         rm.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<BaseStyler, BaseStyler>, ObservableValue<BaseStyler>>() {

            @Override
            public ObservableValue<BaseStyler> call(TableColumn.CellDataFeatures<BaseStyler, BaseStyler> p) {
               return new ReadOnlyObjectWrapper<BaseStyler>(p.getValue());
            }
         });
         rm.setCellFactory(new Callback<TableColumn<BaseStyler, BaseStyler>, TableCell<BaseStyler, BaseStyler>>() {
            @Override
            public TableCell<BaseStyler, BaseStyler> call(TableColumn<BaseStyler, BaseStyler> p) {
               return new TableCell<BaseStyler, BaseStyler>() {
                  @Override
                  protected void updateItem(final BaseStyler bs, boolean bln) {
                     super.updateItem(bs, bln);
                     setGraphic(null);
                     if (bs == null || getTableRow() == null) {
                        return;
                     }
                     final Integer t = getTableRow().getIndex();
                     Button b = new Button("X");
                     b.setTooltip(tip("remove styler"));
                     b.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                           if (t == null) {
                              return;
                           }
                           BaseStyler bs = stylersForClass.get(t);
                           System.out.println("removed styler: " + stylersForClass.remove(bs));
                           if (stylersForClass.isEmpty()) {
                              String clazz = stylerKeysCopy.getValue();
                              if (clazz != null) {
                                 System.out.println("removed style config: " + stylingConfig.remove(clazz));
                              }
                           }
                        }
                     });

                     setGraphic(GroupBuilder.create().children(b).build());
                  }
               };

            }
         });
         stylerKeys.setItems(styleClasses);
         stylerKeysCopy.setItems(styleClasses);
         stylerKeys.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> p) {
               return new ListCell<String>() {
                  @Override
                  protected void updateItem(String t, boolean bln) {
                     super.updateItem(t, bln);
                     if (t != null && stylingConfig.containsKey(t)) {
                        setText(t);
                        setTooltip(tip(toConfigString(t, stylingConfig.get(t))));
                     }
                  }

               };
            }

         });
         stylerKeysCopy.setCellFactory(stylerKeys.getCellFactory());
         stylerKeys.valueProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
               if (newValue != null && newValue != "" && !styleClasses.contains(newValue)) {
                  styleClasses.add(newValue);
               }
            }

         });

         ByteArrayOutputStream bo = new ByteArrayOutputStream(4096);
         Help.printHelp(new PrintStream(bo));
         help.setText(bo.toString());
         bo.reset();
         IOHelper.load(DatamappingHelper.class.getResourceAsStream(DatamappingHelper.XSD), bo);
         datamappingxml.setText(bo.toString());

      } catch (NoClassDefFoundError ex) {
         Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
         throw new VectorPrintRuntimeException(ex);
      } catch (Exception ex) {
         Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @FXML
   private void save(ActionEvent event) {
      try {
         File f = FileChooserBuilder.create().title("save stylesheet").build().showSaveDialog(StylesheetBuilder.topWindow);
         if (f != null) {
            Files.write(f.toPath(), stylesheet.getText().getBytes());
         }
      } catch (Exception ex) {
         toError(ex);
      }
   }

   @FXML
   private void saveXML(ActionEvent event) {
      try {
         Datamappingstype fromXML = DatamappingHelper.fromXML(new StringReader(datamappingxml.getText()));
         if (fromXML != null) {
            File f = FileChooserBuilder.create().title("save data mapping xml").build().showSaveDialog(StylesheetBuilder.topWindow);
            if (f != null) {
               Files.write(f.toPath(), datamappingxml.getText().getBytes());
            }
         }
      } catch (UnmarshalException ex) {
         Throwable linked = ((UnmarshalException) ex).getLinkedException();
         if (linked instanceof SAXParseException) {
            SAXParseException se = (SAXParseException) linked;
         }
         toError((linked) != null ? linked : ex);
      } catch (Exception ex) {
         toError(ex);
      }
   }

   @FXML
   private void validateDataMappingXml(ActionEvent event) {
      try {
         try {
            DatamappingHelper.validateXml(new URL(xmlconfig.getText()));
         } catch (MalformedURLException malformedURLException) {
            System.out.println("wrong url, trying xml directly");
            malformedURLException.printStackTrace();
            DatamappingHelper.validateXml(xmlconfig.getText());
         }
      } catch (SAXException ex) {
         toError(ex);
      } catch (IOException ex) {
         toError(ex);
      }
   }

   @FXML
   private void checkUrl(ActionEvent event) {
      try {
         new URL(xmlconfig.getText());
         extraSettings.put(ReportConstants.DATAMAPPINGXML, xmlconfig.getText());
      } catch (MalformedURLException ex) {
         toError(ex);
      }
   }

   private void importStyle(EnhancedMap settings) throws DocumentException, VectorPrintException {
      settings.put(DefaultStylerFactory.DOFIRSTLAST, Boolean.FALSE.toString());
      StylerFactory sf = new DefaultStylerFactory();
      StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(sf, settings);
      Document d = new Document();
      PdfWriter w = PdfWriter.getInstance(d, new ByteArrayOutputStream(0));
      w.setPageEvent(new EventHelper());
      sf.setDocument(d, w);

      for (Map.Entry<String, String> e : settings.entrySet()) {
         String v = e.getValue();
         if (v.endsWith(";")) {
            e.setValue(v.substring(0, v.length() - 1));
         }
         commentsBefore.put(e.getKey(), settings.getCommentBeforeKey(e.getKey()));
         if (ReportConstants.DOCUMENTSETTINGS.equals(e.getKey())) {
            stylingConfig.put(e.getKey(), new ArrayList<BaseStyler>(1));
            stylingConfig.get(e.getKey()).add(sf.getDocumentStyler());
            styleClasses.add(e.getKey());
         } else if (isStyler(e.getKey(), settings)) {
            // assume styler
            stylingConfig.put(e.getKey(), new ArrayList<BaseStyler>(3));
            try {
               List<BaseStyler> l = sf.getStylers(e.getKey());
               stylingConfig.get(e.getKey()).addAll(l);
               getConditions(l);
               getDefaults(l, settings);
            } catch (VectorPrintException ex) {
               toError(ex);
            }
            styleClasses.add(e.getKey());
         } else if (!isCondition(e.getKey(), settings)) {
            extraSettings.put(e.getKey(), e.getValue());
         }
      }
      for (Iterator it = extraSettings.entrySet().iterator(); it.hasNext();) {
         Map.Entry<String, String> e = (Map.Entry<String, String>) it.next();
         if (processed.contains(e.getKey())) {
            it.remove();
         }
      }
      commentsAfter.addAll(settings.getTrailingComment());
      notify("ok", "import complete", "you can now adapt and (re)build your stylesheet");
   }

   @FXML
   private void importStyle(ActionEvent event) {
      try {
         clear(event);
         File f = FileChooserBuilder.create().title("import stylesheet").build().showOpenDialog(StylesheetBuilder.topWindow);
         if (f != null && f.canRead()) {
            importStyle(new VectorPrintProperties(f.getPath()));
         }
      } catch (Exception ex) {
         toError(ex);
      }
   }

   @FXML
   private void importCss(ActionEvent event) {
      try {
         clear(event);
         File f = FileChooserBuilder.create().title("import css").build().showOpenDialog(StylesheetBuilder.topWindow);
         if (f != null && f.canRead()) {
            System.setProperty("org.w3c.css.sac.parser", SACParser.class.getName());
            ByteArrayOutputStream bo = new ByteArrayOutputStream(2048);
            CssTransformer.transform(new FileInputStream(f), bo, cssvalidate.isSelected());
            importStyle(new VectorPrintProperties(new ByteArrayInputStream(bo.toByteArray()), f.getName(), null));
         }
      } catch (Exception ex) {
         toError(ex);
      }
   }

   private boolean isCondition(String key, EnhancedMap settings) {
      String[] classes = settings.getStringProperties(key, null);
      if (classes == null) {
         return false;
      }
      for (String s : classes) {
         try {
            Class.forName(AbstractCondition.class.getPackage().getName() + "." + s.split("\\(")[0]);
         } catch (ClassNotFoundException ex) {
            return false;
         }
      }
      return true;
   }

   private boolean isStyler(String key, EnhancedMap settings) {
      String[] classes = settings.getStringProperties(key, null);
      if (classes == null) {
         return false;
      }
      for (String s : classes) {
         try {
            Class.forName(AbstractStyler.class.getPackage().getName() + "." + s.split("\\(")[0]);
         } catch (ClassNotFoundException ex) {
            return false;
         }
      }
      return true;
   }

   private final Set<String> processed = new HashSet<String>(10);

   private void getConditions(Collection<? extends BaseStyler> l) {
      for (BaseStyler bs : l) {
         String scKey = bs.getValue(AbstractStyler.CONDITONS, String.class);
         if (bs.getConditions() != null && !bs.getConditions().isEmpty() && !processed.contains(scKey)) {
            processed.add(scKey);
            for (StylingCondition sc : (List<StylingCondition>) bs.getConditions()) {
               if (!AbstractStyler.NOT_FROM_CONFIGURATION.equals(sc.getConfigKey())) {
                  if (conditionConfig.get(sc.getConfigKey()) == null) {
                     conditionConfig.put(sc.getConfigKey(), new ArrayList<StylingCondition>(bs.getConditions().size()));
                  }
                  conditionConfig.get(sc.getConfigKey()).add(sc);
               }
            }
            getDefaults(bs.getConditions(), ((AbstractStyler) bs).getSettings());
         }
      }
   }

   private void getDefaults(Collection<? extends Parameterizable> l, EnhancedMap settings) {
      for (Parameterizable pz : l) {
         for (Parameter p : pz.getParameters().values()) {
            if (ParameterHelper.findDefaultKey(p.getKey(), pz.getClass(), settings) != null) {
               if (!defaults.containsKey(pz.getClass().getSimpleName())) {
                  defaults.put(pz.getClass().getSimpleName(), new TreeSet<ParameterProps>());
               }
               defaults.get(pz.getClass().getSimpleName()).add(new ParameterProps(p));
               processed.add(pz.getClass().getSimpleName() + "." + p.getKey());
            }
         }
      }
   }

   @FXML
   private void quit(ActionEvent event) {
      Platform.exit();
   }

   private Parameterizable findDocStyler() {
      List<Parameterizable> its = stylerCombo.getItems();
      for (Parameterizable p : its) {
         if (p instanceof DocumentStyler) {
            return p;
         }
      }
      return null;
   }

   @FXML
   private void toggleToc(ActionEvent event) {
      Parameterizable ds = findDocStyler();
      ds.setValue(DocumentSettings.TOC, toc.isSelected());
      if (ds.equals(stylerCombo.getValue())) {
         chooseStyleOrCondition(event);
      } else {
         stylerCombo.getSelectionModel().select(ds);
      }
   }

   @FXML
   private void toggleFooter(ActionEvent event) {
      extraSettings.put(ReportConstants.PRINTFOOTER, String.valueOf(footer.isSelected()));
      stylerHelp.setText(ReportConstants.PRINTFOOTER + "=" + String.valueOf(footer.isSelected()));
   }

   @FXML
   private void toggleDebug(ActionEvent event) {
      extraSettings.put(ReportConstants.DEBUG, String.valueOf(debug.isSelected()));
      stylerHelp.setText(ReportConstants.DEBUG + "=" + String.valueOf(debug.isSelected()));
   }

   @FXML
   private void togglePdf1a(ActionEvent event) {
      Parameterizable ds = findDocStyler();
      ds.setValue(DocumentSettings.PDFA, pdf1a.isSelected());
      if (ds.equals(stylerCombo.getValue())) {
         chooseStyleOrCondition(event);
      } else {
         stylerCombo.getSelectionModel().select(ds);
      }
   }

   @FXML
   private Label search;

   private TextArea area;

   @FXML
   private void searchHelp(Event event) {
      area = help;
      area.requestFocus();
   }

   @FXML
   private void searchError(Event event) {
      area = error;
      area.requestFocus();
   }

   @FXML
   private void searchMapping(Event event) {
      area = datamappingxml;
      area.requestFocus();
   }

   private boolean scroll;

   @FXML
   private void searchTxt(KeyEvent event) {
      KeyCode kc = event.getCode();

      scroll = false;
      search.setTextFill(Color.BLACK);
      String s = search.getText();

      if (kc.isLetterKey() || kc.isDigitKey() || kc.isWhitespaceKey()) {
         s += event.getText();
      } else {
         switch (kc) {
            case BACK_SPACE:
               if (s.length() > 0) {
                  s = s.substring(0, s.length() - 1);
               }
            case F3:
               // search again
               break;
            default:
               // ignore
               return;
         }
      }
      search.setText(s);
      if (s.length() == 0) {
         return;
      }

      search.setTextFill(searchArea(area, s) ? Color.GREEN : Color.RED);
   }

   private boolean searchArea(TextArea area, String s) {
      String contents = area.getText();
      int pos = area.getCaretPosition();
      if (contents.indexOf(s, pos) != -1) {
         area.selectRange(contents.indexOf(s, pos), contents.indexOf(s, pos) + s.length());
         scroll = true;
      } else if (contents.indexOf(s) != -1) {
         // wrap
         area.selectRange(contents.indexOf(s), contents.indexOf(s) + s.length());
      } else {
         return false;
      }
      return true;
   }

   @FXML
   private void scroll(KeyEvent event) {
      if (scroll) {
         area.setScrollTop(area.getScrollTop() + 20);
      }
   }

   @FXML
   private void showParHelp(Event event) {
      if (event instanceof KeyEvent) {
         KeyCode kc = ((KeyEvent) event).getCode();
         if (!KeyCode.ENTER.equals(kc)) {
            return;
         }
      }
      if (stylerCombo.getValue() != null) {
         searchArea(help, stylerCombo.getValue().getClass().getSimpleName() + ": ");
         helpTab.getTabPane().getSelectionModel().select(helpTab);
         help.requestFocus();
      } else {
         stylerCombo.requestFocus();
         return;
      }
   }

}
