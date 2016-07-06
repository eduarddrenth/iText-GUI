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
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.steadystate.css.parser.SACParser;
import com.vectorprint.ArrayHelper;
import com.vectorprint.ClassHelper;
import com.vectorprint.IOHelper;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.Settings;
import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.binding.parameters.ParamBindingService;
import com.vectorprint.configuration.binding.parameters.ParameterHelper;
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactory;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactory;
import com.vectorprint.configuration.binding.settings.SettingsBindingService;
import com.vectorprint.configuration.binding.settings.SpecificClassValidator;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.decoration.SortedProperties;
import com.vectorprint.configuration.jaxb.SettingsXMLHelper;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessor;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.itext.EventHelper;
import com.vectorprint.report.itext.Help;
import com.vectorprint.report.itext.jaxb.Datamappingstype;
import com.vectorprint.report.itext.mappingconfig.DatamappingHelper;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.DefaultStylerFactory;
import com.vectorprint.report.itext.style.DocumentStyler;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javax.swing.JPanel;
import javax.xml.bind.UnmarshalException;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Controller implements Initializable {


   /* State of the stylesheet */
   private final ObservableMap<String, List<Parameterizable>> stylingConfig = FXCollections.observableMap(new TreeMap<>());
   private final Map<String, List<String>> commentsBefore = new HashMap<>();
   private final List<String> commentsAfter = new ArrayList<>(3);
   private final Set<DefaultValue> defaults = new TreeSet<>();
   private final Map<String, String> extraSettings = new TreeMap<>();

   private final ObservableList<String> styleClasses = FXCollections.observableArrayList();

   {
      stylingConfig.addListener(new MapChangeListener<String, List<Parameterizable>>() {
         @Override
         public void onChanged(MapChangeListener.Change<? extends String, ? extends List<Parameterizable>> change) {
            if (change.wasAdded()) {
               if (!styleClasses.contains(change.getKey())) {
                  styleClasses.add(change.getKey());
                  Collections.sort(styleClasses);
               }
            } else if (change.wasRemoved()) {
               styleClasses.remove(change.getKey());
            }
         }

      });
   }
   private final ObservableList<Parameterizable> parameterizableForClass = FXCollections.observableArrayList(new ArrayList<Parameterizable>(3));
   private final ObservableList<ParameterProps> parameters = FXCollections.observableArrayList(new ArrayList<ParameterProps>(25));

   /* parameterizables in this set can be used more then once for a styleClass */
   private static final Set<Class<? extends Parameterizable>> duplicatesAllowed = new HashSet();

   @FXML
   private ComboBox<Parameterizable> parameterizableCombo;
   @FXML
   private ComboBox<String> stylerKeys;
   @FXML
   private ComboBox<String> stylerKeysCopy;
   @FXML
   private TextField configString;
   @FXML
   private TextField xmlconfig;
   @FXML
   private TextArea datamappingxsd;
   @FXML
   private TextField xmlsettings;
   @FXML
   private TextArea settingsxsd;
   @FXML
   private ComboBox<Class<? extends EnhancedMapBindingFactory>> settingsfactory;
   @FXML
   private ComboBox<Class<? extends ParameterizableBindingFactory>> paramfactory;
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
   private CheckBox prepost;
   @FXML
   private TextArea stylesheet;
   @FXML
   private Tab pdftab;
   @FXML
   private SwingNode pdfpane;
   private SwingController controller;
   @FXML
   private TextArea help;
   @FXML
   private TextArea error;
   @FXML
   private Label stylerHelp;
   @FXML
   private Tab styleTab;
   @FXML
   private Tab helpTab;
   @FXML
   private TableView<ParameterProps> parameterTable;
   @FXML
   private TableColumn<ParameterProps, String> pKey;
   @FXML
   private TableColumn<ParameterProps, ParameterProps> pValue;
   @FXML
   private TableColumn<ParameterProps, String> pType;
   @FXML
   private TableColumn<ParameterProps, ParameterProps> pDefault;
   @FXML
   private TableColumn<ParameterProps, String> pDeclaringClass;
   @FXML
   private TableView<Parameterizable> parameterizableTable;
   @FXML
   private TableColumn<Parameterizable, Parameterizable> sUpDown;
   @FXML
   private TableColumn<Parameterizable, Parameterizable> rm;
   @FXML
   private TableColumn<Parameterizable, String> sHelp;
   @FXML
   private Button pre;
   @FXML
   private Button post;
   @FXML
   private Button page;

   @FXML
   private void chooseStandardStyle(ActionEvent event) {
      if (pre.equals(event.getSource())) {
         chooseOrAdd(DefaultStylerFactory.PRESTYLERS);
      } else if (post.equals(event.getSource())) {
         chooseOrAdd(DefaultStylerFactory.POSTSTYLERS);
      } else if (page.equals(event.getSource())) {
         chooseOrAdd(DefaultStylerFactory.PAGESTYLERS);
      }
   }

   @FXML
   private void removeFromStylesheet(ActionEvent event) {
      String clazz = stylerKeys.getValue();
      if (null == clazz || !stylingConfig.containsKey(clazz)) {
         stylerKeys.requestFocus();
         return;
      }
      for (Iterator<Parameterizable> it = parameterizableForClass.iterator(); it.hasNext();) {
         Parameterizable pz = it.next();
         if (pz instanceof BaseStyler && clazz.equals(((BaseStyler) pz).getStyleClass())) {
            it.remove();
         } else if (pz instanceof StylingCondition && clazz.equals(((StylingCondition) pz).getConfigKey())) {
            it.remove();
         }
      }
      stylingConfig.remove(clazz);
      styleClasses.remove(clazz);
      commentsBefore.remove(clazz);
      configString.clear();
      parameters.clear();
      parameterizableForClass.clear();
   }

   private void chooseOrAdd(String styleClass) {
      if (!stylerKeys.getItems().contains(styleClass)) {
         stylerKeys.getItems().add(styleClass);
      }
      stylerKeys.getSelectionModel().select(styleClass);
   }

   @FXML
   private void showStyleOrCondition(ActionEvent event) {
      if (stylingConfig.isEmpty() || stylerKeys.getValue() == null
          || !stylingConfig.containsKey(stylerKeys.getValue())) {
         return;
      }
      st.hide();
      try {
         pickStylerToConfigure(stylingConfig.get(stylerKeys.getValue()));
      } catch (Exception ex) {
         toError(ex);
      }
   }

   @FXML
   private void chooseStyleOrCondition(ActionEvent event) {
      if (null == parameterizableCombo.getValue()) {
         parameterizableCombo.requestFocus();
         return;
      }

      try {
         currentParameterizable = (parameterizableCombo.getValue() instanceof DocumentStyler) ? parameterizableCombo.getValue() : parameterizableCombo.getValue().clone();
      } catch (NullPointerException e) {
         e.printStackTrace();
      }
      if (currentParameterizable instanceof DocumentSettings) {
         chooseOrAdd(ReportConstants.DOCUMENTSETTINGS);
      }
      parameters.clear();
      try {
         Parameterizable _st = parameterizableCombo.getValue();
         stylerHelp.setText((_st instanceof BaseStyler) ? ((BaseStyler) _st).getHelp() : "condition to determine when to style or not");
         _st.getParameters().values().stream().forEach((p) -> {
            parameters.add(new ParameterProps(p));
         });
         FXCollections.sort(parameters);
      } catch (Exception ex) {
         toError(ex);
      }
      showStylerHelp(event);
   }
   /*
   the currently item to be configured
    */
   private Parameterizable currentParameterizable;

   @FXML
   private void clear(ActionEvent event) {
      parameters.clear();
      parameterizableForClass.clear();
      stylingConfig.clear();
      defaults.clear();
      extraSettings.clear();
      processed.clear();
      commentsAfter.clear();
      commentsBefore.clear();
   }

   @FXML
   private void showStylers(Event event) {
      if (stylerKeysCopy.getValue() != null && stylingConfig.containsKey(stylerKeysCopy.getValue())) {
         parameterizableForClass.clear();
         stylingConfig.get(stylerKeysCopy.getValue()).stream().forEach((bs) -> {
            parameterizableForClass.add(bs);
         });

      }
   }

   @FXML
   private void showConfig(ActionEvent event) {
      if ("".equals(stylerKeys.getValue()) || null == stylerKeys.getValue()) {
         stylerKeys.requestFocus();
         return;
      }
      try {
         parameters.stream().filter((pp) -> !(pp.getValue() == null || "".equals(pp.getValue()))).forEach((pp) -> {
            Parameter p = currentParameterizable.getParameters().get(pp.getKey());
            p.setValue(pp.getP().getValue());
         });
         StringWriter sw = new StringWriter();
         ParamBindingService.getInstance().getFactory().getSerializer().serialize(currentParameterizable, sw);
         configString.setText(sw.toString());
      } catch (Exception ex) {
         toError(ex);
      }
   }

   private void writeStackTrace(Throwable ex) {
      StringWriter sw = new StringWriter(1024);
      ex.printStackTrace(new PrintWriter(sw));
      error.clear();
      error.setText(sw.toString());
   }

   private void toError(Throwable ex) {
      writeStackTrace(ex);
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

   private static class ParameterizableComparator implements Comparator<Parameterizable> {

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
   private static final ParameterizableComparator PARAMETERIZABLE_COMPARATOR = new ParameterizableComparator();

   private boolean add(Parameterizable p) throws IOException {
      if ("".equals(stylerKeys.getValue())) {
         stylerKeys.requestFocus();
         return false;
      }
      String styleClass = stylerKeys.getValue();
      if (p instanceof BaseStyler) {
         if (ReportConstants.DOCUMENTSETTINGS.equals(styleClass) && !(p instanceof DocumentStyler)) {
            notify("ok",
                styleClass, String.format("style class %s reserved for document settings, choose another", ReportConstants.DOCUMENTSETTINGS));
            return false;
         }
         if (!ReportConstants.DOCUMENTSETTINGS.equals(styleClass) && p instanceof DocumentStyler) {
            notify("ok",
                styleClass, String.format("style class should be %s for document settings", ReportConstants.DOCUMENTSETTINGS));
            return false;
         }
         if (!stylingConfig.containsKey(styleClass) && !"".equals(styleClass)) {
            stylingConfig.put(styleClass, new ArrayList<>());
         } else if (((BaseStyler) p).creates() && !stylingConfig.get(styleClass).isEmpty()) {
            notify("ok", "must be first",
                String.format("styler %s creates a report element, should be the first styler for a style class, you should probably reorder your stylers", p.getClass().getSimpleName()));
         }
         if (!configString.getText().startsWith(currentParameterizable.getClass().getSimpleName() + ".")) {
            prepareAdd(p, stylingConfig.get(styleClass));
            stylingConfig.get(styleClass).add((BaseStyler) p);
            if (!"".equals(p.getValue(AbstractStyler.CONDITONS, String.class))) {
               String cnd = p.getValue(AbstractStyler.CONDITONS, String.class);
               if (null != cnd && !stylingConfig.containsKey(cnd)) {
                  chooseOrAdd(p.getValue(AbstractStyler.CONDITONS, String.class));
                  parameterizableCombo.getSelectionModel().clearSelection();
                  notify("add " + p.getValue(AbstractStyler.CONDITONS, String.class), "warning", String.format("condition %s is missing", p.getValue(AbstractStyler.CONDITONS, String.class)));
               }
            }
         }
      } else {
         if (stylingConfig.containsKey(styleClass)) {
            notify("ok",
                String.format("style class %s in use for stylers, choose another", styleClass), stylingConfig.get(styleClass).toString());
            return false;
         }
         if (ReportConstants.DOCUMENTSETTINGS.equals(styleClass)) {
            notify("ok",
                "", String.format("style class %s reserved for document settings, choose another", styleClass));
            return false;
         }
         if (!stylingConfig.containsKey(styleClass) && !"".equals(styleClass)) {
            stylingConfig.put(styleClass, new ArrayList<>());
         }
         if (!configString.getText().startsWith(currentParameterizable.getClass().getSimpleName() + ".")) {
            prepareAdd(p, stylingConfig.get(styleClass));
            stylingConfig.get(styleClass).add((StylingCondition) p);
         }
         // place dummy condition in all stylers so a condition parameter can be added
         stylingConfig.entrySet().stream().forEach((stylers) -> {
            stylers.getValue().stream().forEach((bs) -> {
               bs.getSettings().put(styleClass, p.getClass().getSimpleName());
            });
         });
      }
      return true;
   }
   private final Button b = new Button("add condition");
   private final Label l = new Label("detail message");
   private final Stage st = new Stage(StageStyle.UTILITY);

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

   private void selectInCombo(Parameterizable par) {
      for (int j = 0; j < parameterizableCombo.getItems().size(); j++) {
         if (parameterizableCombo.getItems().get(j).getClass().equals(par.getClass())) {
            parameterizableCombo.getSelectionModel().select(j);
            break;
         }
      }
      parameters.clear();
      currentParameterizable = par;
      stylerHelp.setText((currentParameterizable instanceof BaseStyler) ? ((BaseStyler) currentParameterizable).getHelp() : "condition to determine when to style or not");
      for (Parameter p : currentParameterizable.getParameters().values()) {
         parameters.add(new ParameterProps(p));
      }
      showConfig(null);
   }

   private void pickStylerToConfigure(final List<? extends Parameterizable> stylers) {
      if (stylers.size() == 1) {
         selectInCombo(stylers.get(0));
         return;
      }
      ToggleGroup tg = new ToggleGroup();
      VBox vb = new VBox(10d);
      Scene sc = new Scene(vb);
      vb.setPadding(new Insets(20d));
      int i = -1;
      for (Parameterizable s : stylers) {
         RadioButton rb = new RadioButton(s.getClass().getSimpleName());
         if (s instanceof BaseStyler) {
            rb.setTooltip(tip(((BaseStyler) s).getHelp()));
         } else {
            rb.setTooltip(tip(((StylingCondition) s).getHelp()));
         }
         rb.setToggleGroup(tg);
         vb.getChildren().add(rb);
         rb.setOnAction((ActionEvent event) -> {
            selectInCombo(s);
         });
      }
      st.setScene(sc);
      st.setTitle("choose styler");
      st.show();
   }

   private String toConfigString(String clazz, List<? extends Parameterizable> sp) throws IOException {
      ParsingProperties p = new ParsingProperties(new Settings());
      toConfigString(clazz, sp, p);
      StringWriter sw = new StringWriter(p.size() * 30);
      SettingsBindingService.getInstance().getFactory().getSerializer().serialize(p, sw);
      return sw.toString();
   }

   private void stripPreAndPost(String clazz, List<? extends Parameterizable> sp) {
      if (!DefaultStylerFactory.PRESTYLERS.equals(clazz) && !DefaultStylerFactory.POSTSTYLERS.equals(clazz)) {
         if (stylingConfig.containsKey(DefaultStylerFactory.PRESTYLERS)) {
            // strip defaults
            stylingConfig.get(DefaultStylerFactory.PRESTYLERS).stream().forEach((p) -> {
               sp.remove(p);
            });
         }
         if (stylingConfig.containsKey(DefaultStylerFactory.POSTSTYLERS)) {
            stylingConfig.get(DefaultStylerFactory.POSTSTYLERS).stream().forEach((p) -> {
               sp.remove(p);
            });
         }
      }

   }

   private void toConfigString(String clazz, List<? extends Parameterizable> sp, ParsingProperties eh) throws IOException {
      stripPreAndPost(clazz, sp);
      List<String> pp = new ArrayList<>(sp.size());
      for (Parameterizable p : sp) {
         StringWriter sw = new StringWriter();
         ParamBindingService.getInstance().getFactory().getSerializer().serialize(p, sw);
         pp.add(sw.toString());
      }
      String[] toArray = ArrayHelper.toArray(pp);
      eh.put(clazz, toArray == null ? EMPTY : toArray);
   }

   private static final String[] EMPTY = new String[0];

   private void printComment(String key, ParsingProperties eh) {
      if (key != null && commentsBefore.containsKey(key)) {
         commentsBefore.get(key).stream().forEach((s) -> {
            eh.addCommentBeforeKey(key, s);
         });
      } else {
         commentsAfter.stream().forEach((s) -> {
            eh.addTrailingComment(s);
         });
      }
   }

   private EnhancedMap buildSettings() throws IOException {
      ParsingProperties eh = new ParsingProperties(new SortedProperties(new Settings()));
      stylesheet.clear();

      defaults.stream().forEach((DefaultValue def) -> {
         printComment(def.clazz + "." + def.key + '.' + def.suffix.name(), eh);
         eh.put(def.clazz + "." + def.key + '.' + def.suffix.name(), def.value);
      });

      for (Map.Entry<String, List<Parameterizable>> e : stylingConfig.entrySet()) {
         printComment(e.getKey(), eh);
         toConfigString(e.getKey(), e.getValue(), eh);
      }

      extraSettings.entrySet().stream().map((e) -> {
         printComment(e.getKey(), eh);
         return e;
      }).forEach((e) -> {
         eh.put(e.getKey(), e.getValue());
      });
      return eh;
   }

   @FXML
   private void buildStylesheet(ActionEvent event) {
      try {
         EnhancedMap eh = buildSettings();
         StringWriter sw = new StringWriter(eh.size() * 30);
         SettingsBindingService.getInstance().getFactory().getSerializer().serialize(eh, sw);
         stylesheet.appendText(sw.toString());

         styleTab.getTabPane().getSelectionModel().select(styleTab);
      } catch (Exception ex) {
         toError(ex);
      }

   }

   @FXML
   private void toConfig(ActionEvent event) {
      try {
         if (currentParameterizable == null) {
            throw new VectorPrintRuntimeException("first choose a style or condition using configure");
         }
         showConfig(event);
         add(currentParameterizable);
      } catch (Exception ex) {
         toError(ex);
      }
   }

   private Tooltip tip(String text) {
      Tooltip t = new Tooltip(text);
      t.setMaxWidth(400);
      t.setAutoHide(false);
      t.setAutoFix(true);
      t.setWrapText(true);
      return t;
   }

   @FXML
   private void showStylerHelp(Event event) {
      stylerHelp.setText(help(parameterizableCombo.getValue()));
      stylerHelp.setTooltip(tip(help(parameterizableCombo.getValue())));
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

   private final Delta dragDelta = new Delta();
   private final Stage parent = StylesheetBuilder.topWindow;

   public void dragStart(MouseEvent mouseEvent) {
      // record a delta distance for the drag and drop operation.
      dragDelta.x = parent.getX() - mouseEvent.getScreenX();
      dragDelta.y = parent.getY() - mouseEvent.getScreenY();
   }

   public void dragged(MouseEvent mouseEvent) {
      parent.setX(mouseEvent.getScreenX() + dragDelta.x);
      parent.setY(mouseEvent.getScreenY() + dragDelta.y);
   }

   private static String helpFor(Parameterizable p) {
      return p instanceof BaseStyler ? (((BaseStyler) p).creates() ? "creates iText element " : "") + ((BaseStyler) p).getHelp() : ((StylingCondition) p).getHelp();
   }

   @FXML
   private void addFromClassPath() {
      ClassLoader orig = Thread.currentThread().getContextClassLoader();
      try {
         FileChooser fc = new FileChooser();
         fc.setTitle("add jar");
         FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Jar files (*.jar)", "*.jar", "*.JAR", "*.Jar");
         fc.getExtensionFilters().add(extensionFilter);
         List<File> l = fc.showOpenMultipleDialog(StylesheetBuilder.topWindow);
         if (l != null) {
            URL[] u = new URL[l.size()];
            int i = -1;
            for (File f : l) {
               u[++i] = f.toURI().toURL();
            }
            URLClassLoader urlClassLoader = new URLClassLoader(u, orig);
            Thread.currentThread().setContextClassLoader(urlClassLoader);
            List<Parameterizable> sorted = new ArrayList<>();
            for (Class<?> c : ClassHelper.fromPackage(AbstractStyler.class.getPackage(), urlClassLoader)) {
               if (AbstractStyler.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
                  sorted.add((Parameterizable) c.newInstance());
               }
            }
            for (Class<?> c : ClassHelper.fromPackage(AbstractCondition.class.getPackage(), urlClassLoader)) {
               if (AbstractCondition.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
                  sorted.add((Parameterizable) c.newInstance());
               }
            }
            Collections.sort(sorted, PARAMETERIZABLE_COMPARATOR);
            parameterizableCombo.setItems(FXCollections.observableArrayList(sorted));

         }
      } catch (NoClassDefFoundError error) {
         Thread.currentThread().setContextClassLoader(orig);
         toError(error);
      } catch (Exception exception) {
         Thread.currentThread().setContextClassLoader(orig);
         toError(exception);
      }
   }

   @Override
   public void initialize(URL url, ResourceBundle rb) {
      try {
         initFactories();

         // make all stylers and conditions available in the dropdown
         List<Parameterizable> sorted = new ArrayList<>(Help.getStylersAndConditions());
         for (Parameterizable parameterizable : sorted) {
            ParamAnnotationProcessor.PAP.initParameters(parameterizable);
         }
         Collections.sort(sorted, PARAMETERIZABLE_COMPARATOR);
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
         parameterizableCombo.setCellFactory((ListView<Parameterizable> p) -> {
            return new ListCell<Parameterizable>() {
               @Override
               protected void updateItem(Parameterizable t, boolean bln) {
                  super.updateItem(t, bln);
                  setText(t == null ? "" : t.getClass().getSimpleName());
                  setTooltip(t != null ? tip(help(t)) : null);
               }
            };
         });
         parameterizableCombo.setConverter(new StringConverter<Parameterizable>() {
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
         parameterizableCombo.setItems(FXCollections.observableArrayList(sorted));

         stylerKeys.setPromptText("required!");
         parameterTable.setItems(parameters);
         parameterizableTable.setItems(parameterizableForClass);

         pDeclaringClass.setCellValueFactory(new PropertyValueFactory<>("declaringClass"));
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
                  Parameterizable p = parameterizableCombo.getValue();
                  searchArea(help, p.getClass().getSimpleName() + ": ", false);
                  searchArea(help, "key=" + item, false);
                  helpTab.getTabPane().getSelectionModel().select(helpTab);
                  help.requestFocus();
               });
            }

         });
         pValue.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ParameterProps, ParameterProps>, ObservableValue<ParameterProps>>() {
            @Override
            public ObservableValue<ParameterProps> call(TableColumn.CellDataFeatures<ParameterProps, ParameterProps> p) {
               return new ReadOnlyObjectWrapper<ParameterProps>(p.getValue());
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
                  Class valueClass = item.getP().getValueClass();
                  if (Boolean.class.equals(valueClass) || boolean.class.equals(valueClass)) {
                     final CheckBox checkBox = new CheckBox();
                     if (item.getKey().equals(DocumentSettings.PDFA)) {
                        bindToCheckbox(checkBox, item, pdf1a);
                     } else if (item.getKey().equals(DocumentSettings.TOC)) {
                        bindToCheckbox(checkBox, item, toc);
                     }
                     checkBox.setSelected(Boolean.parseBoolean(item.getValue()));
                     setGraphic(checkBox);
                  } else if (valueClass.isEnum()) {
                     final ComboBox<String> comboBox = new ComboBox();
                     ObservableList<String> ol = FXCollections.observableArrayList();
                     for (Object o : valueClass.getEnumConstants()) {
                        ol.add(String.valueOf(o));
                     }
                     comboBox.setItems(ol);
                     comboBox.getSelectionModel().select(item.getValue());
                     comboBox.valueProperty().addListener(item);
                     setGraphic(comboBox);
                  } else if (java.awt.Color.class.equals(valueClass)) {
                     java.awt.Color col = (java.awt.Color) item.getP().getValue();
                     final ColorPicker cp = col == null ? new ColorPicker() : new ColorPicker(new Color(col.getRed() / 255, col.getGreen() / 255, col.getBlue() / 255, col.getAlpha() / 255));
                     cp.valueProperty().addListener(item);
                     setGraphic(cp);
                  } else {
                     final TextField textField = new TextField(item.getValue());
                     textField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                        textField.setStyle(null);
                        try {
                           item.setValue(newValue);
                        } catch (Exception e) {
                           textField.setStyle("-fx-border-color: red");
                           writeStackTrace(e);
                        }
                     });
                     setGraphic(textField);
                  }
               }

               private void bindToCheckbox(final CheckBox child, final ParameterProps item, CheckBox master) {
                  child.selectedProperty().bindBidirectional(master.selectedProperty());
                  child.selectedProperty().addListener(item);
               }

            };
         }
         );
         pDefault.setCellValueFactory((TableColumn.CellDataFeatures<ParameterProps, ParameterProps> p) -> {
            return new ReadOnlyObjectWrapper<>(p.getValue());
         });
         pDefault.setCellFactory((TableColumn<ParameterProps, ParameterProps> p) -> new TableCell<ParameterProps, ParameterProps>() {
            @Override
            protected void updateItem(final ParameterProps pp, boolean bln) {
               super.updateItem(pp, bln);
               setGraphic(null);
               if (pp == null) {
                  return;
               }
               CheckBox checkbox = new CheckBox();
               DefaultValue defaultValue = new DefaultValue(currentParameterizable.getClass().getSimpleName(), pp.getKey(), pp.getValue(), ParameterHelper.SUFFIX.set_default);
               checkbox.setSelected(defaults.contains(defaultValue));
               checkbox.setTooltip(tip(String.format("use value as default for %s in %s", pp.getKey(), currentParameterizable.getClass().getSimpleName())));
               setGraphic(checkbox);
               checkbox.setOnAction((ActionEvent e) -> {
                  defaults.remove(defaultValue);
                  if (checkbox.isSelected()) {
                     defaults.add(defaultValue);
                     configString.clear();
                     configString.appendText(currentParameterizable.getClass().getSimpleName());
                     configString.appendText(".");
                     configString.appendText(pp.getKey());
                     configString.appendText(".");
                     configString.appendText(ParameterHelper.SUFFIX.set_default.name());
                     configString.appendText("=");
                     configString.appendText(pp.getValue());
                  } else {
                     configString.clear();
                  }
               });
            }
         });

         sHelp.setCellValueFactory((CellDataFeatures<Parameterizable, String> p)
             -> new ReadOnlyObjectWrapper(p.getValue().getClass().getSimpleName() + ": " + helpFor(p.getValue())));
         sHelp.setCellFactory((TableColumn<Parameterizable, String> p) -> new TableCell<Parameterizable, String>() {
            @Override
            protected void updateItem(String t, boolean bln) {
               super.updateItem(t, bln);
               setText(t);
               if (t != null) {
                  setTooltip(tip(t));
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
               b.setTooltip(tip("move styler up"));
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
               bd.setTooltip(tip("move styler down"));
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
               b.setTooltip(tip("remove styler"));
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
         stylerKeys.setItems(styleClasses);
         stylerKeysCopy.setItems(styleClasses);
         stylerKeys.setCellFactory((ListView<String> p) -> new ListCell<String>() {
            @Override
            protected void updateItem(final String t, boolean bln) {
               super.updateItem(t, bln);
               if (t != null) {
                  setText(t);
                  final Tooltip tip = tip("config....");
                  if (stylingConfig.containsKey(t)) {
                     tip.addEventHandler(WindowEvent.WINDOW_SHOWING, (WindowEvent event) -> {
                        try {
                           tip.setText(toConfigString(t, stylingConfig.get(t)));
                        } catch (IOException ex) {
                           Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                        }
                     });
                  }
                  setTooltip(tip);
               }
            }

         });
         stylerKeysCopy.setCellFactory(stylerKeys.getCellFactory());
         stylerKeys.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue != null && !"".equals(newValue) && !stylingConfig.containsKey(newValue)) {
               stylingConfig.put(newValue, new ArrayList<>());
            }
         });

         ByteArrayOutputStream bo = new ByteArrayOutputStream(4096);
         Help.printHelp(new PrintStream(bo));
         help.setText(bo.toString());
         bo.reset();

         IOHelper.load(DatamappingHelper.class.getResourceAsStream(DatamappingHelper.XSD), bo);
         datamappingxsd.setText(bo.toString());
         bo.reset();

         IOHelper.load(DatamappingHelper.class.getResourceAsStream(SettingsXMLHelper.XSD), bo);
         settingsxsd.setText(bo.toString());

         // build a controller
         controller = new SwingController();

         // Build a SwingViewFactory configured with the controller
         SwingViewBuilder factory = new SwingViewBuilder(controller);

         // Use the factory to build a JPanel that is pre-configured
         //with a complete, active Viewer UI.
         JPanel viewerComponentPanel = factory.buildViewerPanel();

         controller.getDocumentViewController().setAnnotationCallback(
             new org.icepdf.ri.common.MyAnnotationCallback(
                 controller.getDocumentViewController()));

         pdfpane.setContent(viewerComponentPanel);

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
         FileChooser fc = new FileChooser();
         fc.setTitle("save stylesheet");
         File f = fc.showSaveDialog(StylesheetBuilder.topWindow);
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
         Datamappingstype fromXML = DatamappingHelper.fromXML(new StringReader(datamappingxsd.getText()));
         if (fromXML != null) {
            FileChooser fc = new FileChooser();
            fc.setTitle("save data mapping xml");
            File f = fc.showSaveDialog(StylesheetBuilder.topWindow);
            if (f != null) {
               Files.write(f.toPath(), datamappingxsd.getText().getBytes());
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
   private void testStylesheet(ActionEvent event) {
      try {
         StylesheetTester stylesheetTester = new StylesheetTester(this);
         stylesheetTester.testStyleSheet(stylesheet.getText());
      } catch (Exception ex) {
         toError(ex);
      }
   }

   @FXML
   private void changeSyntax(ActionEvent event) {
      SpecificClassValidator.setClazz(settingsfactory.getValue());
      com.vectorprint.configuration.binding.parameters.SpecificClassValidator.setClazz(paramfactory.getValue());
   }

   @FXML
   private void validateDataMappingXml(ActionEvent event) {
      try {
         try {
            DatamappingHelper.validateXml(BindingHelper.URL_PARSER.convert(xmlconfig.getText()));
            notify("ok", "valid xml", "valid xml");
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
   private void validateSettingsXml(ActionEvent event) {
      try {
         try {
            SettingsXMLHelper.validateXml(BindingHelper.URL_PARSER.convert(xmlsettings.getText()));
            notify("ok", "valid xml", "valid xml");
         } catch (MalformedURLException malformedURLException) {
            System.out.println("wrong url, trying xml directly");
            malformedURLException.printStackTrace();
            SettingsXMLHelper.validateXml(xmlsettings.getText());
         }
      } catch (SAXException ex) {
         toError(ex);
      } catch (IOException ex) {
         toError(ex);
      }
   }

   @FXML
   private void checkUrlConfig(ActionEvent event) {
      try {
         BindingHelper.URL_PARSER.convert(xmlconfig.getText());
         extraSettings.put(ReportConstants.DATAMAPPINGXML, xmlconfig.getText());
      } catch (VectorPrintRuntimeException ex) {
         toError(ex);
      }
   }

   @FXML
   private void checkUrlSettings(ActionEvent event) {
      try {
         BindingHelper.URL_PARSER.convert(xmlsettings.getText());
      } catch (VectorPrintRuntimeException ex) {
         toError(ex);
      }
   }

   private void importStyle(ParsingProperties settings) throws DocumentException, VectorPrintException {
      clear(null);
      Boolean preAndPost = settings.getBooleanProperty(true, DefaultStylerFactory.PREANDPOSTSTYLE);
      // set to false when importing to prevent all pre and post stylers to be added to regulerar style classes
      settings.put(DefaultStylerFactory.PREANDPOSTSTYLE, Boolean.FALSE.toString());
      DefaultStylerFactory sf = new DefaultStylerFactory();
      StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(sf, settings);
      Document d = new Document();
      PdfWriter w = PdfWriter.getInstance(d, new ByteArrayOutputStream(0));
      w.setPageEvent(new EventHelper());
      sf.setDocument(d, w);

      for (Map.Entry<String, String[]> e : settings.entrySet()) {
         commentsBefore.put(e.getKey(), settings.getCommentBeforeKey(e.getKey()));
         if (ReportConstants.DOCUMENTSETTINGS.equals(e.getKey())) {
            stylingConfig.put(e.getKey(), new ArrayList<>(1));
            stylingConfig.get(e.getKey()).add(sf.getDocumentStyler());
            pdf1a.setSelected(sf.getDocumentStyler().getValue(DocumentSettings.PDFA, Boolean.class));
            toc.setSelected(sf.getDocumentStyler().getValue(DocumentSettings.TOC, Boolean.class));
         } else if (isStyler(e.getKey(), settings)) {
            stylingConfig.put(e.getKey(), new ArrayList<>(3));
            try {
               List<BaseStyler> l = sf.getStylers(e.getKey());
               stylingConfig.get(e.getKey()).addAll(l);
               getConditions(l);
               getDefaults(l, settings);
            } catch (VectorPrintException ex) {
               toError(ex);
            }
         } else if (!isCondition(e.getKey(), settings)) {
            if (DefaultStylerFactory.PREANDPOSTSTYLE.equals(e.getKey())) {
               prepost.setSelected(preAndPost);
               extraSettings.put(e.getKey(), preAndPost.toString());
            } else {
               if (ReportConstants.DEBUG.equals(e.getKey())) {
                  debug.setSelected(Boolean.valueOf(e.getValue()[0]));
               } else if (ReportConstants.PRINTFOOTER.equals(e.getKey())) {
                  footer.setSelected(Boolean.valueOf(e.getValue()[0]));
               }
               extraSettings.put(e.getKey(), e.getValue()[0]);
            }
         }
      }
      for (Iterator it = extraSettings.entrySet().iterator(); it.hasNext();) {
         Map.Entry<String, String> e = (Map.Entry<String, String>) it.next();
         if (processed.contains(e.getKey())) {
            it.remove();
         }
      }
      // check conditions not referenced
      settings.entrySet().stream().forEach((e) -> {
         if (isCondition(e.getKey(), settings)) {
            Logger.getLogger(Controller.class.getName()).warning(String.format("unreferenced conditions for key: %s", e.getKey()));
            List<StylingCondition> conditions;
            try {
               conditions = sf.getConditions(e.getKey());
            } catch (VectorPrintException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
            stylingConfig.put(e.getKey(), new ArrayList<>(conditions.size()));
            stylingConfig.get(e.getKey()).addAll(conditions);
         }
      });
      commentsAfter.addAll(settings.getTrailingComment());
      notify("ok", "import complete", "you can now adapt and (re)build your stylesheet");
   }

   @FXML
   private void importStyle(ActionEvent event) {
      try {
         FileChooser fc = new FileChooser();
         fc.setTitle("import stylesheet");
         File f = fc.showOpenDialog(StylesheetBuilder.topWindow);
         if (f != null && f.canRead()) {
            importStyle(new ParsingProperties(new SortedProperties(new Settings()), f.getPath()));
         }
      } catch (Exception ex) {
         toError(ex);
      }
   }

   @FXML
   private void importCss(ActionEvent event) {
      try {
         FileChooser fc = new FileChooser();
         fc.setTitle("import css");
         FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Css files (*.css)", "*.css", "*.CSS", "*.Css");
         fc.getExtensionFilters().add(extensionFilter);
         File f = fc.showOpenDialog(StylesheetBuilder.topWindow);
         if (f != null && f.canRead()) {
            System.setProperty("org.w3c.css.sac.parser", SACParser.class.getName());
            ByteArrayOutputStream bo = new ByteArrayOutputStream(2048);
            CssTransformer.transform(new FileInputStream(f), bo, cssvalidate.isSelected());
            importStyle(new ParsingProperties(new SortedProperties(new Settings()), new StringReader(bo.toString())));
         }
      } catch (Exception ex) {
         toError(ex);
      }
   }

   @FXML
   private void showPdf(ActionEvent event) {
      try {
         FileChooser fc = new FileChooser();
         fc.setTitle("open pdf");
         FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Pdf files (*.pdf)", "*.pdf", "*.PDF", "*.Pdf");
         fc.getExtensionFilters().add(extensionFilter);
         File f = fc.showOpenDialog(StylesheetBuilder.topWindow);
         if (f != null && f.canRead()) {
            openPdf(new FileInputStream(f), f.getPath());
         }
      } catch (Exception ex) {
         toError(ex);
      }
   }

   void openPdf(InputStream in, String description) {
      controller.openDocument(in, description, null);
      pdftab.getTabPane().getSelectionModel().select(pdftab);
   }

   static boolean isCondition(String key, EnhancedMap settings) {
      String[] classes = null;
      try {
         classes = settings.getStringProperties(null, key);
      } catch (VectorPrintRuntimeException e) {
         if (!e.getMessage().contains("this does not match requested class")) {
            throw e;
         }
      }
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

   static boolean isStyler(String key, EnhancedMap settings) {
      String[] classes = null;
      try {
         classes = settings.getStringProperties(null, key);
      } catch (VectorPrintRuntimeException e) {
         if (!e.getMessage().contains("this does not match requested class")) {
            throw e;
         }
      }
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

   private final Set<String> processed = new HashSet<>(10);

   /**
    *
    * @param l
    */
   private void getConditions(Collection<? extends BaseStyler> l) {
      l.stream().forEach((bs) -> {
         String scKey = bs.getValue(AbstractStyler.CONDITONS, String.class);
         if (bs.getConditions() != null && !bs.getConditions().isEmpty() && !processed.contains(scKey)) {
            for (StylingCondition sc : bs.getConditions()) {
               if (!AbstractStyler.NOT_FROM_CONFIGURATION.equals(sc.getConfigKey()) && scKey.equals(sc.getConfigKey())) {
                  if (stylingConfig.get(sc.getConfigKey()) == null) {
                     stylingConfig.put(sc.getConfigKey(), new ArrayList<>(bs.getConditions().size()));
                  }
                  stylingConfig.get(sc.getConfigKey()).add(sc);
               }
            }
            getDefaults(bs.getConditions(), ((AbstractStyler) bs).getSettings());
            processed.add(scKey);
         }
      });
   }

   private void getDefaults(Collection<? extends Parameterizable> l, EnhancedMap settings) {
      l.stream().forEach((pz) -> {
         pz.getParameters().values().stream().forEach((p) -> {
            String key = ParameterHelper.findKey(p.getKey(), pz.getClass(), settings, ParameterHelper.SUFFIX.set_default);
            if (key != null) {
               DefaultValue defaultValue = new DefaultValue(pz.getClass().getSimpleName(), p.getKey(), settings.get(key)[0], ParameterHelper.SUFFIX.set_default);
               defaults.remove(defaultValue);
               defaults.add(defaultValue);
               processed.add(pz.getClass().getSimpleName() + "." + p.getKey() + '.' + ParameterHelper.SUFFIX.set_default);
            }
         });
      });
   }

   @FXML
   private void quit(ActionEvent event) {
      Platform.exit();
   }

   private Parameterizable findDocStyler() {
      List<Parameterizable> its = parameterizableCombo.getItems();
      for (Parameterizable p : its) {
         if (p instanceof DocumentStyler) {
            return p;
         }
      }
      return null;
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
   private void togglePrePost(ActionEvent event) {
      extraSettings.put(DefaultStylerFactory.PREANDPOSTSTYLE, String.valueOf(prepost.isSelected()));
      stylerHelp.setText(DefaultStylerFactory.PREANDPOSTSTYLE + "=" + String.valueOf(prepost.isSelected()));
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
   private void searchSettings(Event event) {
      area = settingsxsd;
      area.requestFocus();
   }

   @FXML
   private void searchMapping(Event event) {
      area = datamappingxsd;
      area.requestFocus();
   }

   private boolean scroll;

   @FXML
   private void searchTxt(KeyEvent event) {
      KeyCode kc = event.getCode();

      scroll = false;
      search.setTextFill(Color.BLACK);
      String s = search.getText();
      boolean again = false;

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
               again = true;
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

      search.setTextFill(searchArea(area, s, again) ? Color.GREEN : Color.RED);
   }

   private boolean searchArea(TextArea area, String s, boolean again) {
      String contents = area.getText();
      int pos = again ? area.getCaretPosition() : area.getCaretPosition() - s.length() - 1;
      boolean noBackspace = area.getSelection().getLength() < s.length();
      if (contents.indexOf(s, pos) != -1) {
         area.selectRange(contents.indexOf(s, pos), contents.indexOf(s, pos) + s.length());
         if (noBackspace && area.getCaretPosition() != pos + s.length() + 2) {
            scroll = true;
         }
      } else if (contents.contains(s)) {
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
   private void toggleSearch(KeyEvent event) {
      if (event.isControlDown() && KeyCode.F == event.getCode()) {
         // start search in stylesheet
         if (stylesheet.equals(area) && !area.isEditable()) {
            // stop searching
            area.setEditable(true);
            area.getStyleClass().remove("grayed");
            search.setText("");
         } else {
            // start searching
            area = stylesheet;
            area.getStyleClass().add("grayed");
            area.setEditable(false);
         }
      } else if (KeyCode.ESCAPE == event.getCode()) {
         // stop searching
         area.setEditable(true);
         area.getStyleClass().remove("grayed");
         search.setText("");
      } else if (stylesheet.equals(area) && !area.isEditable()) {
         searchTxt(event);
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
      if (parameterizableCombo.getValue() != null) {
         searchArea(help, parameterizableCombo.getValue().getClass().getSimpleName() + ": ", false);
         helpTab.getTabPane().getSelectionModel().select(helpTab);
         help.requestFocus();
      } else {
         parameterizableCombo.requestFocus();
      }
   }

   /**
    * @throws InstantiationException
    * @throws IllegalAccessException
    */
   private void initFactories() throws InstantiationException, IllegalAccessException {
      settingsfactory.setItems(FXCollections.observableArrayList(SettingsBindingService.getInstance().getValidFactories()));
      paramfactory.setItems(FXCollections.observableArrayList(ParamBindingService.getInstance().getValidFactories()));

      Class<? extends EnhancedMapBindingFactory> aClass = SettingsBindingService.getInstance().getFactory().getClass();
      settingsfactory.getSelectionModel().select(aClass);
      Class<? extends ParameterizableBindingFactory> aClass1 = ParamBindingService.getInstance().getFactory().getClass();
      paramfactory.getSelectionModel().select(aClass1);
   }

   public static String toHex(Color color) {
      int red = (int) (color.getRed() * 255);
      int green = (int) (color.getGreen() * 255);
      int blue = (int) (color.getBlue() * 255);
      return "#" + Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue);
   }
}
