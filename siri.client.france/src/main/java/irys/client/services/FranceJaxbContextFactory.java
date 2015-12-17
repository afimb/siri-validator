package irys.client.services;

import java.util.List;

import javax.xml.bind.JAXBException;

import uk.org.siri.siri.IDFGeneralMessageRequestFilterStructure;
import uk.org.siri.siri.IDFGeneralMessageStructure;

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.developer.JAXBContextFactory;


public class FranceJaxbContextFactory implements JAXBContextFactory 
{
   @SuppressWarnings("rawtypes")
   @Override
   @NotNull
   public JAXBRIContext createJAXBContext(@NotNull SEIModel seim, @NotNull List<Class> classes,
         @NotNull List<TypeReference> references) throws JAXBException
   {
      if (!classes.contains(IDFGeneralMessageRequestFilterStructure.class))
         classes.add(IDFGeneralMessageRequestFilterStructure.class);
      if (!classes.contains(IDFGeneralMessageStructure.class))
         classes.add(IDFGeneralMessageStructure.class);
      return JAXBContextFactory.DEFAULT.createJAXBContext(seim, classes, references);
   }
}
