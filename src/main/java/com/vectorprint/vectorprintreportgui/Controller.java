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
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.itext.EventHelper;
import com.vectorprint.report.itext.Help;
import com.vectorprint.report.itext.mappingconfig.DatamappingHelper;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.DefaultStylerFactory;
import com.vectorprint.report.itext.style.DocumentStyler;
import com.vectorprint.report.itext.style.StylerFactoryHelper;
import com.vectorprint.report.itext.style.StylingCondition;
import com.vectorprint.report.itext.style.css.CssTransformer;
import com.vectorprint.report.itext.style.stylers.AbstractStyler;
import com.vectorprint.report.itext.style.stylers.Advanced;
import com.vectorprint.report.itext.style.stylers.DocumentSettings;
import com.vectorprint.report.itext.style.stylers.NewLine;
import com.vectorprint.report.itext.style.stylers.NewPage;
import com.vectorprint.report.itext.style.stylers.Padding;
import com.vectorprint.vectorprintreportgui.text.SearchableTextArea;
import com.vectorprint.vectorprintreportgui.xml.XmlArea;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import javax.swing.JPanel;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.xml.sax.SAXException;

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

   /* parameterizables in this set can be used more then once for a styleClass */
   private static final Set<Class<? extends Parameterizable>> duplicatesAllowed = new HashSet();

   @FXML
   private TableViewController tableViewController;

   @FXML
   private ComboBox<Parameterizable> parameterizableCombo;
   @FXML
   private ComboBox<String> stylerKeys;
   @FXML
   private ComboBox<String> stylerKeysCopy;
   @FXML
   private TextField xmlconfig;
   @FXML
   private XmlArea datamappingxsd;
   @FXML
   private TextField xmlsettings;
   @FXML
   private XmlArea settingsxsd;
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
   private SearchableTextArea stylesheet;
   @FXML
   private Tab pdftab;
   @FXML
   private SwingNode pdfpane;
   private SwingController controller;
   @FXML
   private SearchableTextArea help;
   @FXML
   private SearchableTextArea error;
   @FXML
   private Label stylerHelp;
   @FXML
   private Tab styleTab;
   @FXML
   private Tab helpTab;
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
      tableViewController.getConfigString().clear();
      tableViewController.getParameters().clear();
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
      try {
         pickStylerToConfigure(stylingConfig.get(stylerKeys.getValue()));
      } catch (Exception ex) {
         ViewHelper.toError(ex, error);
      }
   }

   @FXML
   private void chooseStyleOrCondition(ActionEvent event) {
      if (null == parameterizableCombo.getValue()) {
         parameterizableCombo.requestFocus();
         return;
      }

      try {
         currentParameterizable.set((parameterizableCombo.getValue() instanceof DocumentStyler) ? parameterizableCombo.getValue() : parameterizableCombo.getValue().clone());
      } catch (NullPointerException e) {
         e.printStackTrace();
      }
      if (currentParameterizable.get() instanceof DocumentSettings) {
         chooseOrAdd(ReportConstants.DOCUMENTSETTINGS);
      }
      tableViewController.getParameters().clear();
      try {
         Parameterizable _st = parameterizableCombo.getValue();
         stylerHelp.setText((_st instanceof BaseStyler) ? ((BaseStyler) _st).getHelp() : "condition to determine when to style or not");
         _st.getParameters().values().stream().forEach((p) -> {
            tableViewController.getParameters().add(new ParameterProps(p));
         });
         FXCollections.sort(tableViewController.getParameters());
      } catch (Exception ex) {
         ViewHelper.toError(ex, error);
      }
      showStylerHelp(event);
   }
   /*
   the currently item to be configured
    */
   public final ObjectProperty<Parameterizable> currentParameterizable = new SimpleObjectProperty<>();

   @FXML
   private void clear(ActionEvent event) {
      tableViewController.getParameters().clear();
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
         tableViewController.getParameters().stream().filter((pp) -> !(pp.getValue() == null || "".equals(pp.getValue()))).forEach((pp) -> {
            Parameter p = currentParameterizable.get().getParameters().get(pp.getKey());
            p.setValue(pp.getP().getValue());
         });
         StringWriter sw = new StringWriter();
         ParamBindingService.getInstance().getFactory().getSerializer().serialize(currentParameterizable.get(), sw);
         tableViewController.getConfigString().setText(sw.toString());
      } catch (Exception ex) {
         ViewHelper.toError(ex, error);
      }
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
            ViewHelper.notify("ok",
                styleClass, String.format("style class %s reserved for document settings, choose another", ReportConstants.DOCUMENTSETTINGS));
            return false;
         }
         if (!ReportConstants.DOCUMENTSETTINGS.equals(styleClass) && p instanceof DocumentStyler) {
            ViewHelper.notify("ok",
                styleClass, String.format("style class should be %s for document settings", ReportConstants.DOCUMENTSETTINGS));
            return false;
         }
         if (!stylingConfig.containsKey(styleClass) && !"".equals(styleClass)) {
            stylingConfig.put(styleClass, new ArrayList<>());
         } else if (((BaseStyler) p).creates() && !stylingConfig.get(styleClass).isEmpty()) {
            ViewHelper.notify("ok", "must be first",
                String.format("styler %s creates a report element, should be the first styler for a style class, you should probably reorder your stylers", p.getClass().getSimpleName()));
         }
         if (!tableViewController.getConfigString().getText().startsWith(currentParameterizable.getClass().getSimpleName() + ".")) {
            prepareAdd(p, stylingConfig.get(styleClass));
            stylingConfig.get(styleClass).add((BaseStyler) p);
            if (!"".equals(p.getValue(AbstractStyler.CONDITONS, String.class))) {
               String cnd = p.getValue(AbstractStyler.CONDITONS, String.class);
               if (null != cnd && !stylingConfig.containsKey(cnd)) {
                  chooseOrAdd(p.getValue(AbstractStyler.CONDITONS, String.class));
                  parameterizableCombo.getSelectionModel().clearSelection();
                  ViewHelper.notify("add " + p.getValue(AbstractStyler.CONDITONS, String.class), "warning", String.format("condition %s is missing", p.getValue(AbstractStyler.CONDITONS, String.class)));
               }
            }
         }
      } else {
         if (stylingConfig.containsKey(styleClass)) {
            ViewHelper.notify("ok",
                String.format("style class %s in use for stylers, choose another", styleClass), stylingConfig.get(styleClass).toString());
            return false;
         }
         if (ReportConstants.DOCUMENTSETTINGS.equals(styleClass)) {
            ViewHelper.notify("ok",
                "", String.format("style class %s reserved for document settings, choose another", styleClass));
            return false;
         }
         if (!stylingConfig.containsKey(styleClass) && !"".equals(styleClass)) {
            stylingConfig.put(styleClass, new ArrayList<>());
         }
         if (!tableViewController.getConfigString().getText().startsWith(currentParameterizable.getClass().getSimpleName() + ".")) {
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

   private void selectInCombo(Parameterizable par) {
      for (int j = 0; j < parameterizableCombo.getItems().size(); j++) {
         if (parameterizableCombo.getItems().get(j).getClass().equals(par.getClass())) {
            parameterizableCombo.getSelectionModel().select(j);
            break;
         }
      }
      tableViewController.getParameters().clear();
      currentParameterizable.set(par);
      stylerHelp.setText((currentParameterizable instanceof BaseStyler) ? ((BaseStyler) currentParameterizable).getHelp() : "condition to determine when to style or not");
      for (Parameter p : currentParameterizable.get().getParameters().values()) {
         tableViewController.getParameters().add(new ParameterProps(p));
      }
      showConfig(null);
   }

   private static class ParameterizableWrapper {

      private final Parameterizable p;

      public ParameterizableWrapper(Parameterizable p) {
         this.p = p;
      }

      @Override
      public String toString() {
         return p.getClass().getSimpleName();
      }

   }

   private void pickStylerToConfigure(final List<Parameterizable> stylers) {
      if (stylers.size() == 1) {
         selectInCombo(stylers.get(0));
         return;
      }
      List<ParameterizableWrapper> pw = new ArrayList<>(stylers.size());
      stylers.stream().forEach((p) -> {
         pw.add(new ParameterizableWrapper(p));
      });
      Dialog<ParameterizableWrapper> dialog = new ChoiceDialog<>(pw.get(0), pw);
      ButtonType bt = new ButtonType("choose", ButtonBar.ButtonData.OK_DONE);
      dialog.getDialogPane().getButtonTypes().clear();
      dialog.getDialogPane().getButtonTypes().add(bt);
      dialog.setTitle("Please choose");
      dialog.setHeaderText(null);
      dialog.setResizable(true);
      dialog.setContentText("choose the styler or condition you want to configure");
      Optional<ParameterizableWrapper> choice = dialog.showAndWait();
      if (choice.isPresent()) {
         selectInCombo(choice.get().p);
      }
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
      stylesheet.getText().clear();

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
         stylesheet.getText().appendText(sw.toString());

         styleTab.getTabPane().getSelectionModel().select(styleTab);
      } catch (Exception ex) {
         ViewHelper.toError(ex, error);
      }

   }

   @FXML
   private void toConfig(ActionEvent event) {
      try {
         if (currentParameterizable == null) {
            throw new VectorPrintRuntimeException("first choose a style or condition using configure");
         }
         showConfig(event);
         add(currentParameterizable.get());
      } catch (Exception ex) {
         ViewHelper.toError(ex, error);
      }
   }

   @FXML
   private void showStylerHelp(Event event) {
      stylerHelp.setText(help(parameterizableCombo.getValue()));
      stylerHelp.setTooltip(ViewHelper.tip(help(parameterizableCombo.getValue())));
   }

   private String help(Parameterizable p) {
      return p != null ? (p instanceof BaseStyler)
          ? ((BaseStyler) p).getHelp()
          : ((StylingCondition) p).getHelp() : "";
   }

   @FXML
   private void addFromClassPath() {
      ClassLoader orig = Thread.currentThread().getContextClassLoader();
      try {
         FileChooser fc = new FileChooser();
         fc.setTitle("add jar");
         FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Jar files (*.jar)", "*.jar", "*.JAR", "*.Jar");
         fc.getExtensionFilters().add(extensionFilter);
         List<File> l = fc.showOpenMultipleDialog(null);
         if (l != null) {
            URL[] u = new URL[l.size()];
            int i = -1;
            for (File f : l) {
               u[++i] = f.toURI().toURL();
            }
            URLClassLoader urlClassLoader = new URLClassLoader(u, orig);
            Thread.currentThread().setContextClassLoader(urlClassLoader);
            List<Parameterizable> sorted = new ArrayList<>(Help.getStylersAndConditions());
            Collections.sort(sorted, PARAMETERIZABLE_COMPARATOR);
            parameterizableCombo.setItems(FXCollections.observableArrayList(sorted));

         }
      } catch (NoClassDefFoundError error) {
         Thread.currentThread().setContextClassLoader(orig);
         ViewHelper.toError(error, this.error);
      } catch (Exception exception) {
         Thread.currentThread().setContextClassLoader(orig);
         ViewHelper.toError(exception, this.error);
      }
   }

   @Override
   public void initialize(URL url, ResourceBundle rb) {
      try {
         tableViewController.getCurrentParameterizable().bind(currentParameterizable);
         tableViewController.setDefaults(defaults);
         tableViewController.setError(error);
         tableViewController.setHelp(help);
         tableViewController.setHelpTab(helpTab);
         tableViewController.setPdf1a(pdf1a);
         tableViewController.setToc(toc);

         initFactories();

         // make all stylers and conditions available in the dropdown
         List<Parameterizable> sorted = new ArrayList<>(Help.getStylersAndConditions());
         Collections.sort(sorted, PARAMETERIZABLE_COMPARATOR);
         synchronized (duplicatesAllowed) {
            if (duplicatesAllowed.isEmpty()) {
               duplicatesAllowed.add(Padding.class);
               duplicatesAllowed.add(NewLine.class);
               duplicatesAllowed.add(NewPage.class);
               sorted.stream().filter((pz) -> (pz instanceof Advanced)).forEach((pz) -> {
                  duplicatesAllowed.add(pz.getClass());
               });
            }
         }
         parameterizableCombo.setCellFactory((ListView<Parameterizable> p) -> {
            return new ListCell<Parameterizable>() {
               @Override
               protected void updateItem(Parameterizable t, boolean bln) {
                  super.updateItem(t, bln);
                  setText(t == null ? "" : t.getClass().getSimpleName());
                  setTooltip(t != null ? ViewHelper.tip(help(t)) : null);
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
         stylerKeys.setItems(styleClasses);
         stylerKeysCopy.setItems(styleClasses);
         stylerKeys.setCellFactory((ListView<String> p) -> new ListCell<String>() {
            @Override
            protected void updateItem(final String t, boolean bln) {
               super.updateItem(t, bln);
               if (t != null) {
                  setText(t);
                  final Tooltip tip = ViewHelper.tip("config....");
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
         help.getText().setText(bo.toString());
         bo.reset();

         datamappingxsd.loadXml(DatamappingHelper.class.getResourceAsStream(DatamappingHelper.XSD));

         settingsxsd.loadXml(DatamappingHelper.class.getResourceAsStream(SettingsXMLHelper.XSD));

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
         File f = fc.showSaveDialog(null);
         if (f != null) {
            Files.write(f.toPath(), stylesheet.getText().getText().getBytes());
         }
      } catch (Exception ex) {
         ViewHelper.toError(ex, error);
      }
   }

   @FXML
   private void testStylesheet(ActionEvent event) {
      try {
         StylesheetTester stylesheetTester = new StylesheetTester(this);
         stylesheetTester.testStyleSheet(stylesheet.getText().getText());
      } catch (Exception ex) {
         ViewHelper.toError(ex, error);
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
            ViewHelper.notify("ok", "valid xml", "valid xml");
         } catch (MalformedURLException malformedURLException) {
            System.out.println("wrong url, trying xml directly");
            malformedURLException.printStackTrace();
            DatamappingHelper.validateXml(xmlconfig.getText());
         }
      } catch (SAXException ex) {
         ViewHelper.toError(ex, error);
      } catch (IOException ex) {
         ViewHelper.toError(ex, error);
      }
   }

   @FXML
   private void validateSettingsXml(ActionEvent event) {
      try {
         try {
            SettingsXMLHelper.validateXml(BindingHelper.URL_PARSER.convert(xmlsettings.getText()));
            ViewHelper.notify("ok", "valid xml", "valid xml");
         } catch (MalformedURLException malformedURLException) {
            System.out.println("wrong url, trying xml directly");
            malformedURLException.printStackTrace();
            SettingsXMLHelper.validateXml(xmlsettings.getText());
         }
      } catch (SAXException ex) {
         ViewHelper.toError(ex, error);
      } catch (IOException ex) {
         ViewHelper.toError(ex, error);
      }
   }

   @FXML
   private void checkUrlConfig(ActionEvent event) {
      try {
         BindingHelper.URL_PARSER.convert(xmlconfig.getText());
         extraSettings.put(ReportConstants.DATAMAPPINGXML, xmlconfig.getText());
      } catch (VectorPrintRuntimeException ex) {
         ViewHelper.toError(ex, error);
      }
   }

   @FXML
   private void checkUrlSettings(ActionEvent event) {
      try {
         BindingHelper.URL_PARSER.convert(xmlsettings.getText());
      } catch (VectorPrintRuntimeException ex) {
         ViewHelper.toError(ex, error);
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
         } else if (ViewHelper.isStyler(e.getKey(), settings)) {
            stylingConfig.put(e.getKey(), new ArrayList<>(3));
            try {
               List<BaseStyler> l = sf.getStylers(e.getKey());
               stylingConfig.get(e.getKey()).addAll(l);
               getConditions(l);
               getDefaults(l, settings);
            } catch (VectorPrintException ex) {
               ViewHelper.toError(ex, error);
            }
         } else if (!ViewHelper.isCondition(e.getKey(), settings)) {
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
         if (ViewHelper.isCondition(e.getKey(), settings)) {
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
      ViewHelper.notify("ok", "import complete", "you can now adapt and (re)build your stylesheet");
   }

   @FXML
   private void importStyle(ActionEvent event) {
      try {
         FileChooser fc = new FileChooser();
         fc.setTitle("import stylesheet");
         File f = fc.showOpenDialog(null);
         if (f != null && f.canRead()) {
            importStyle(new ParsingProperties(new SortedProperties(new Settings()), f.getPath()));
         }
      } catch (Exception ex) {
         ViewHelper.toError(ex, error);
      }
   }

   @FXML
   private void importCss(ActionEvent event) {
      try {
         FileChooser fc = new FileChooser();
         fc.setTitle("import css");
         FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Css files (*.css)", "*.css", "*.CSS", "*.Css");
         fc.getExtensionFilters().add(extensionFilter);
         File f = fc.showOpenDialog(null);
         if (f != null && f.canRead()) {
            System.setProperty("org.w3c.css.sac.parser", SACParser.class.getName());
            ByteArrayOutputStream bo = new ByteArrayOutputStream(2048);
            CssTransformer.transform(new FileInputStream(f), bo, cssvalidate.isSelected());
            importStyle(new ParsingProperties(new SortedProperties(new Settings()), new StringReader(bo.toString())));
         }
      } catch (Exception ex) {
         ViewHelper.toError(ex, error);
      }
   }

   @FXML
   private void showPdf(ActionEvent event) {
      try {
         FileChooser fc = new FileChooser();
         fc.setTitle("open pdf");
         FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Pdf files (*.pdf)", "*.pdf", "*.PDF", "*.Pdf");
         fc.getExtensionFilters().add(extensionFilter);
         File f = fc.showOpenDialog(null);
         if (f != null && f.canRead()) {
            openPdf(new FileInputStream(f), f.getPath());
         }
      } catch (Exception ex) {
         ViewHelper.toError(ex, error);
      }
   }

   void openPdf(InputStream in, String description) {
      controller.openDocument(in, description, null);
      pdftab.getTabPane().getSelectionModel().select(pdftab);
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
   private void showParHelp(Event event) {
      if (event instanceof KeyEvent) {
         KeyCode kc = ((KeyEvent) event).getCode();
         if (!KeyCode.ENTER.equals(kc)) {
            return;
         }
      }
      if (parameterizableCombo.getValue() != null) {
         help.searchArea(parameterizableCombo.getValue().getClass().getSimpleName() + ": ", false);
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

}
