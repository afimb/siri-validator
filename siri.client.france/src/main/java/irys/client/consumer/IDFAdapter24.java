package irys.client.consumer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Element;

import uk.org.siri.wsdl.siri.IDFGeneralMessageRequestFilterStructure;
import uk.org.siri.wsdl.siri.IDFGeneralMessageStructure;


public class IDFAdapter24
{
   private JAXBContext jaxbContext;
   
   public IDFAdapter24() throws Exception
   {
      jaxbContext = JAXBContext.newInstance(IDFGeneralMessageRequestFilterStructure.class,IDFGeneralMessageStructure.class);
   }

   public IDFGeneralMessageStructure unmarshal(Element element) throws Exception
   {
      if (null == element) {
         return null;
     }
      Class<IDFGeneralMessageStructure> type = IDFGeneralMessageStructure.class;

      // 2. Unmarshal the element based on the value's type.
      DOMSource source = new DOMSource(element);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      JAXBElement<IDFGeneralMessageStructure> jaxbElement = unmarshaller.unmarshal(source, type);
      return jaxbElement.getValue();
   }
   

}
