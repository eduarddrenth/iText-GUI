package com.vectorprint.vectorprintreportgui.xml;

import com.vectorprint.vectorprintreportgui.text.SearchableTextArea;
import java.io.IOException;
import java.io.InputStream;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextFlow;
import org.xml.sax.SAXException;

public class XmlArea extends AnchorPane {

   @FXML
   private SearchableTextArea searchText;

   @FXML
   private TextFlow xmlhighlight;

   public XmlArea() {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(XmlArea.class.getSimpleName() + ".fxml"));
      fxmlLoader.setRoot(this);
      fxmlLoader.setController(this);

      try {
         fxmlLoader.load();
      } catch (IOException exception) {
         throw new RuntimeException(exception);
      }
   }

   public TextFlow getXmlhighlight() {
      return xmlhighlight;
   }

   public void loadXml(InputStream xml) throws SAXException, IOException {
      XmlContentHandler xmlContentHandler = new XmlContentHandler();
      new SaxParser().parse(xml, xmlContentHandler);
      xmlhighlight.getChildren().addAll(xmlContentHandler.getTexts());
      searchText.getText().setText(xmlContentHandler.getText());
   }

   public void setEditable(boolean editable) {
      searchText.setEditable(editable);
   }

   public boolean isEditable() {
      return searchText.isEditable();
   }
}
