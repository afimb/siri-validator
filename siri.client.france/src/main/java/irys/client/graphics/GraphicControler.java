package irys.client.graphics;

import irys.client.command.SubscribeCommand;
import irys.client.consumer.SiriConsumer;
import irys.client.services.CheckStatusClient;
import irys.client.services.DiscoveryClient;
import irys.client.services.SiriException;
import irys.client.services.SubscriptionClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.org.siri.siri.AnnotatedLineStructure;
import uk.org.siri.siri.AnnotatedStopPointStructure;
import uk.org.siri.siri.CheckStatusRequestStructure;
import uk.org.siri.siri.CheckStatusResponseBodyStructure;
import uk.org.siri.siri.LineRefStructure;
import uk.org.siri.siri.LinesDeliveryStructure;
import uk.org.siri.siri.LinesDiscoveryRequestStructure;
import uk.org.siri.siri.ProducerResponseEndpointStructure;
import uk.org.siri.siri.RequestStructure;
import uk.org.siri.siri.ResponseEndpointStructure;
import uk.org.siri.siri.SiriSubscriptionRequestStructure;
import uk.org.siri.siri.StopPointsDeliveryStructure;
import uk.org.siri.siri.StopPointsDiscoveryRequestStructure;
import uk.org.siri.siri.SubscriptionResponseBodyStructure;
import uk.org.siri.siri.TerminateSubscriptionRequestBodyStructure;
import uk.org.siri.siri.TerminateSubscriptionResponseStructure;
import uk.org.siri.wsdl.CheckStatusError;
import uk.org.siri.wsdl.LinesDiscoveryError;
import uk.org.siri.wsdl.StopPointsDiscoveryError;
import uk.org.siri.wsdl.WsSubscriptionRequestInfoStructure;

import com.sun.xml.ws.client.ClientTransportException;

public class GraphicControler
{
   private DiscoveryClient discovery;
   private SubscriptionClient subscription;
   private SubscribeCommand subCommand;
   private CheckStatusClient chechStatus;

   private SiriConsumer consumer;

   private Endpoint endPoint;
   private String notifyAddress;

   private boolean subscriptionsStarted = false;

   private List<JFrame> frames = new ArrayList<JFrame>();

   // private ClassPathXmlApplicationContext applicationContext;
   MainJFrame mainFrame;

   public GraphicControler(ClassPathXmlApplicationContext applicationContext)
   {
      // this.applicationContext = applicationContext;
      ConfigurableBeanFactory factory = applicationContext.getBeanFactory();
      discovery = (DiscoveryClient) factory.getBean("discoveryClient");
      chechStatus = (CheckStatusClient) factory.getBean("checkStatusClient");
      subscription = (SubscriptionClient) factory.getBean("subscriptionClient");
      subCommand = (SubscribeCommand) factory.getBean("Subscribe");
      SubscribeCommand subscriptionClient = (SubscribeCommand) factory.getBean("Subscribe");
      notifyAddress = subscriptionClient.getNotifyAddress();
      consumer = new SiriConsumer();
      endPoint = Endpoint.create(consumer);
      SOAPBinding binding = (SOAPBinding) endPoint.getBinding();
      binding.setMTOMEnabled(true);
      endPoint.publish(notifyAddress);

      Runtime.getRuntime().addShutdownHook(new Thread()
      {
         @Override
         public void run()
         {
            System.out.println("program exit");
            clean();
         }
      });
   }

   public void execute()
   {
      waitForServer();

      mainFrame = new MainJFrame(this);
      mainFrame.addStopList(getStops());
      mainFrame.addLineList(getLines());
      Vector<String> directions = new Vector<String>();
      directions.add("-- none --");
      directions.add("Left");
      directions.add("Right");
      mainFrame.addDirectionList(directions);
      mainFrame.setLocationRelativeTo(null);
      mainFrame.setVisible(true);
   }

   public boolean checkStatus()
   {
      CheckStatusRequestStructure request = chechStatus.buildRequest();
      Holder<ProducerResponseEndpointStructure> checkStatusAnswerInfo = new Holder<ProducerResponseEndpointStructure>();
      Holder<CheckStatusResponseBodyStructure> answer = new Holder<CheckStatusResponseBodyStructure>();
      try
      {
         chechStatus.checkStatus(request, checkStatusAnswerInfo, answer);
         return true;
      }
      catch (CheckStatusError e)
      {
         System.out.println(e);
         return false;

      }
      catch (ClientTransportException e)
      {
         System.out.println("no server responding");
         return false;
      }
   }

   private void waitForServer()
   {
      int count = 1;

      String[] buttons = { "Retry", "Cancel" };
      int ret = 0;
      do
      {
         if (checkStatus())
            return;
         ret = JOptionPane.showOptionDialog(null, "Server not responding (" + count + " tests)",
               "SiriClient", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, buttons, buttons[0]);
         count++;
      }
      while (ret == 0);
      System.exit(0);
   }

   public void terminate()
   {
      System.out.println("terminate program");
      clean();
      System.exit(0);
   }

   public synchronized void clean()
   {
      System.out.println("clean");
      if (subscriptionsStarted && consumer.hasConsumers())
      {
         try
         {
            System.out.println("terminate subscriptions");
            RequestStructure deleteSubscriptionInfo = subscription.buildDeleteSubscriptionInfo();
            TerminateSubscriptionRequestBodyStructure request = subscription.buildTerminateSubcriptionRequest("all");
            Holder<ResponseEndpointStructure> deleteSubscriptionAnswerInfo = new Holder<ResponseEndpointStructure>();
            Holder<TerminateSubscriptionResponseStructure> answer = new Holder<TerminateSubscriptionResponseStructure>();
            subscription.deleteSubscription(deleteSubscriptionInfo, request, deleteSubscriptionAnswerInfo, answer);
         }
         catch (Exception ex)
         {
            ex.printStackTrace();
         }
         subscriptionsStarted = false;
      }
      for (JFrame frame : frames)
      {
         frame.dispose();
      }
      frames.clear();
      if (endPoint != null)
      {
         endPoint.stop();
         endPoint = null;
      }

   }

   Vector<AnnotatedStopPointStructure> stops = null;

   private Vector<AnnotatedStopPointStructure> getStops()
   {
      if (stops == null)
      {
         stops = new Vector<AnnotatedStopPointStructure>();
         StopPointsDiscoveryRequestStructure request = discovery.buildStopRequest(null);
         Holder<StopPointsDeliveryStructure> answer = new Holder<StopPointsDeliveryStructure>();
         try
         {
            discovery.stopPointsDiscovery(request, answer);
            for (AnnotatedStopPointStructure sp : answer.value.getAnnotatedStopPointRef())
            {
               stops.add(sp);
            }
         }
         catch (StopPointsDiscoveryError e)
         {
            e.printStackTrace();
         }
         Collections.sort(stops, new StopSorter());
         stops.add(0, null);
      }
      return stops;
   }

   private Vector<AnnotatedLineStructure> lines = null;

   private Vector<AnnotatedLineStructure> getLines()
   {
      if (lines == null)
      {
         lines = new Vector<AnnotatedLineStructure>();
         LinesDiscoveryRequestStructure request = discovery.buildLineRequest(null);
         Holder<LinesDeliveryStructure> answer = new Holder<LinesDeliveryStructure>();
         try
         {
            discovery.linesDiscovery(request, answer);
            for (AnnotatedLineStructure li : answer.value.getAnnotatedLineRef())
            {
               lines.add(li);
            }
         }
         catch (LinesDiscoveryError e)
         {
            e.printStackTrace();
         }
         Collections.sort(lines, new LineSorter());
         lines.add(0, null);
      }
      return lines;
   }

   private class StopSorter implements Comparator<AnnotatedStopPointStructure>
   {
      @Override
      public int compare(AnnotatedStopPointStructure o1, AnnotatedStopPointStructure o2)
      {

         return o1.getStopPointRef().getValue().compareTo(o2.getStopPointRef().getValue());
      }
   }

   private class LineSorter implements Comparator<AnnotatedLineStructure>
   {
      @Override
      public int compare(AnnotatedLineStructure o1, AnnotatedLineStructure o2)
      {

         return o1.getLineRef().getValue().compareTo(o2.getLineRef().getValue());
      }
   }

   public void subscribeSM(AnnotatedStopPointStructure stop, AnnotatedLineStructure line, String direction)
   {

      if (stop == null)
      {
         if (line == null)
         {
            System.out.println("StopMonitoring subscription : stop or line must be set");
            return;
         }
         String lineRef = line.getLineRef().getValue();
         System.out.println("StopMonitoring subscription : launch on each stop of line " + lineRef);
         for (AnnotatedStopPointStructure stopPoint : getStops())
         {
            if (stopPoint != null &&
                  stopPoint.getStopPointRef().getValue().contains(":SP:"))
            {

               boolean found = false;
               for (Serializable item : stopPoint.getLines().getLineRefOrLineDirection())
               {
                  if (item instanceof LineRefStructure)
                  {
                     LineRefStructure lineItem = (LineRefStructure) item;
                     if (lineItem.getValue().equals(lineRef))
                     {
                        found = true;
                        break;
                     }
                  }
               }
               if (!found)
                  continue;

               subscribeSM(stopPoint, line, direction);
            }
         }
      }
      else
      {
         subscriptionsStarted = true;
         String lineRef = line != null ? line.getLineRef().getValue() : null;
         System.out.println("subscribe SM on stop " + stop.getStopPointRef().getValue() + ", line " + lineRef
               + ", direction " + direction);
         List<String> args = new ArrayList<String>();
         args.add("subscribe");
         args.add("-service");
         args.add("SMClient");
         args.add("-stopId");
         args.add(stop.getStopPointRef().getValue());
         if (line != null)
         {
            args.add("-lineId");
            args.add(line.getLineRef().getValue());
         }
         if (direction != "-- none --")
         {
            args.add("-directionId");
            args.add(direction);
         }
         else
         {
            direction = null;
         }
         args.add("-maxStop");
         args.add("4");

         try
         {
            WsSubscriptionRequestInfoStructure subscriptionRequestInfo = subscription
                  .buildWsSubscriptionRequestInfoStructure(notifyAddress);
            SiriSubscriptionRequestStructure request = subCommand.getSubscriptionRequest(args.toArray(new String[0]));
            String subscriptionRef = request.getStopMonitoringSubscriptionRequest().get(0).getSubscriptionIdentifier()
                  .getValue();
            SMFrame frame = new SMFrame(this, subscriptionRef, stop, line, direction);
            consumer.addStopMonitoringConsumer(subscriptionRef, frame);
            Holder<ResponseEndpointStructure> subscriptionAnswerInfo = new Holder<ResponseEndpointStructure>();
            Holder<SubscriptionResponseBodyStructure> answer = new Holder<SubscriptionResponseBodyStructure>();
            subscription.subscribe(subscriptionRequestInfo, request, subscriptionAnswerInfo, answer);
            if (!answer.value.getResponseStatus().get(0).isStatus())
            {
               // failure
               System.out.println("subscription failed");
               consumer.removeConsumer(subscriptionRef);
            }
            else
            {
               frame.setVisible(true);
               frames.add(frame);
            }
         }
         catch (SiriException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }

   public void unsubscribe(JFrame frame, String subscriptionRef)
   {
      if (subscriptionsStarted)
      {
         try
         {
            System.out.println("terminate subscription " + subscriptionRef);
            RequestStructure deleteSubscriptionInfo = subscription.buildDeleteSubscriptionInfo();
            TerminateSubscriptionRequestBodyStructure request = subscription
                  .buildTerminateSubcriptionRequest(subscriptionRef);
            Holder<ResponseEndpointStructure> deleteSubscriptionAnswerInfo = new Holder<ResponseEndpointStructure>();
            Holder<TerminateSubscriptionResponseStructure> answer = new Holder<TerminateSubscriptionResponseStructure>();
            subscription.deleteSubscription(deleteSubscriptionInfo, request, deleteSubscriptionAnswerInfo, answer);
         }
         catch (Exception ex)
         {
            ex.printStackTrace();
         }
         consumer.removeConsumer(subscriptionRef);
         frames.remove(frame);
      }
   }

}
