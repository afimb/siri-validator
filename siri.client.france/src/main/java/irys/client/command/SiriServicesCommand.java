/**
 *   Siri Product - Produit SIRI
 *  
 *   a set of tools for easy application building with 
 *   respect of the France Siri Local Agreement
 *
 *   un ensemble d'outils facilitant la realisation d'applications
 *   respectant le profil France de la norme SIRI
 * 
 *   Copyright DRYADE 2009-2010
 */
package irys.client.command;

import irys.client.graphics.GraphicControler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jline.console.ConsoleReader;
import jline.console.UserInterruptException;
import jline.console.completer.StringsCompleter;
import jline.console.history.FileHistory;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class SiriServicesCommand
{

   private static ClassPathXmlApplicationContext applicationContext;

   private static boolean verbose = true;
   
   private static boolean graphicOn = false;
   
   /**
    * @param args
    */
   public static void main(String[] args)
   {
	  Locale.setDefault(Locale.ENGLISH);
      if (args.length == 0)
         printHelp(false);
      else
      {
         String[] context = null;

         PathMatchingResourcePatternResolver test = new PathMatchingResourcePatternResolver();
         try
         {
            List<String> newContext = new ArrayList<String>();
            Resource[] re = test.getResources("classpath*:/irysContext.xml");
            for (Resource resource : re)
            {
               System.out.println(resource.getURL().toString());
               newContext.add(resource.getURL().toString());
            }
            context = newContext.toArray(new String[0]);

         }
         catch (Exception e)
         {
            System.err.println("cannot parse contexts : " + e.getLocalizedMessage());
         }
         applicationContext = new ClassPathXmlApplicationContext(context);
         executeCommmand(args, false);
      }
      if (!graphicOn) System.exit(0);

   }

   private static void executeCommmand(String[] args, boolean consoleMode)
   {
      ConfigurableBeanFactory factory = applicationContext.getBeanFactory();

      String service = args[0];
      AbstractCommand client = null;
      if (service.equalsIgnoreCase("SMClient"))
      {
         client = (AbstractCommand) factory.getBean("SMClient");
      }
      else if (service.equalsIgnoreCase("GMClient"))
      {
         client = (AbstractCommand) factory.getBean("GMClient");
      }
      else if (service.equalsIgnoreCase("VMClient"))
      {
         client = (AbstractCommand) factory.getBean("VMClient");
      }
      else if (service.equalsIgnoreCase("ETClient"))
      {
         client = (AbstractCommand) factory.getBean("ETClient");
      }
      else if (service.equalsIgnoreCase("PTClient"))
      {
         client = (AbstractCommand) factory.getBean("PTClient");
      }
      else if (service.equalsIgnoreCase("DSClient"))
      {
         client = (AbstractCommand) factory.getBean("DSClient");
      }
      else if (service.equalsIgnoreCase("CSClient"))
      {
         client = (AbstractCommand) factory.getBean("CSClient");
      }
      else if (service.equalsIgnoreCase("Subscribe") || service.equalsIgnoreCase("Unsubscribe"))
      {
         if (!consoleMode)
         {
            System.out.println("Abonnements autorisés uniquement en mode console");
            return;
         }
         client = (AbstractCommand) factory.getBean("Subscribe");
      }
      else if (!consoleMode && service.equalsIgnoreCase("console"))
      {
         SubscribeCommand subscriptionClient = (SubscribeCommand) factory.getBean("Subscribe");
         if (args.length > 2 && args[1].equalsIgnoreCase("-saveNotification"))
         {
            Boolean b = Boolean.parseBoolean(args[2]);
            subscriptionClient.setNotifyLog(b.booleanValue());
         }
         console();
         if (subscriptionClient.getConsumer() != null)
         {
            subscriptionClient.getConsumer().stop();
            subscriptionClient.setConsumer(null);
         }
         return;
      }
      else if (!consoleMode && service.equalsIgnoreCase("graphic"))
      {
         graphic();
         return;
      }
      else
      {
         printHelp(consoleMode);
         return;
      }
      client.setConsoleMode(consoleMode);
      if (!consoleMode) client.setVerbose(verbose);
      client.call(args);
   }

   private static void graphic()
   {
      System.out.println("graphic mode on");
      graphicOn = true;
      GraphicControler frame = new GraphicControler(applicationContext);
      frame.execute();
   }

   private static File historyFile = new File("./console.hst");
   private static FileHistory history;

   private static void console()
   {
      // work in console mode
      System.out.println("console mode on");

      // BufferedReader in = new BufferedReader(new
      // InputStreamReader(System.in));
      try
      {
         ConsoleReader in = new ConsoleReader();
         history = new FileHistory(historyFile);
         in.setHistory(history);
         in.setHistoryEnabled(true);
         in.setHandleUserInterrupt(true);
                  
         in.addCompleter(new StringsCompleter("Subscribe", "Unsubscribe", "SMClient", "ETClient", "CSClient",
               "VMClient", "DSClient", "PTClient", "GMClient","subscribe", "unsubscribe", "smclient", "etclient", "csclient",
               "vmclient", "dsclient", "ptclient", "gmclient", "help", "quit", "exit", "execute"));
         in.setPrompt("command > ");

         String line = "";
         while (true)
         {
            try
            {
               // System.out.print("command > ");
               line = in.readLine();
               if (line == null)
                  break;
               line = line.trim();

            }
            catch (UserInterruptException e)
            {
               System.out.println("exit");
               break;
            }
            catch (Exception e)
            {
               System.err.println("cannot read input");
               break;
            }
            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("q"))
               break;
            String[] args = line.split("\\s");
            if (args.length > 0 && args[0].equalsIgnoreCase("execute"))
            {
               executeScript(args);
            }
            else
            {
               executeCommmand(args, true);
            }
         }
      }
      catch (IOException e)
      {
         System.err.println("cannot launch console " + e.getMessage());
      }
      try
      {
         history.flush();
      }
      catch (IOException e)
      {
         System.err.println("cannot save history " + e.getMessage());
      }

   }

   private static void executeScript(String[] args)
   {
      if (args.length != 2) 
      {
         System.err.println("invalid syntax");
         printHelp(true);
         return;
      }
      File file = new File(args[1]);
      if (!file.exists())
      {
         System.err.println("File not found : "+args[1]);
         return;
      }
      List<String> lines = null;
      try
      {
         lines = FileUtils.readLines(file);
      }
      catch (IOException e)
      {
         System.err.println("File error : "+e.getMessage());
         return;
      }
      for (String line : lines)
      {
         line = line.trim();
         System.out.println(line);
         if (line.isEmpty() || line.startsWith("#")) continue;
         String[] cmd = line.split("\\s");
         if (cmd.length > 0 && cmd[0].equalsIgnoreCase("execute"))
         {
            executeScript(cmd);
         }
         else if (cmd.length > 0 && cmd[0].equalsIgnoreCase("exit"))
         {
            return;
         }
         else
         {
            executeCommmand(cmd, true);
         }
         try
         {
            Thread.sleep(3000);
         }
         catch (InterruptedException e)
         {
            return;
         }
      }
   }

   private static void printHelp(boolean consoleMode)
   {
      if (consoleMode)
      {
         System.out.println("serviceName [serviceOption]+");
         System.out.println("                   -[help]");
         System.out.println(" serviceName : SMClient|GMClient|ETClient|PTClient|VMClient|DSClient|CSClient ");
         System.out.println("               Subscribe|Unsubscribe");
         System.out.println("");
         System.out.println("serviceName -help for serviceOptions help");
         System.out.println("");
         System.out.println("execute filename : execute all lines in filename ; 'execute' commands can be included in file");
         System.out.println("");
         System.out.println("exit or [q]uit to stop console mode");
      }
      else
      {
         System.out.println("client.sh serviceName [serviceOption]+");
         System.out.println("                   -[help]");
         System.out.println(" serviceName : SMClient|GMClient|ETClient|PTClient|VMClient|DSClient|CSClient ");
         System.out.println("               Subscribe|Unsubscribe");
         System.out.println("");
         System.out.println("client.sh console : for interactive mode");
         System.out.println("");
         System.out.println("call client.sh serviceName -help for serviceOptions help");
      }
   }

}
