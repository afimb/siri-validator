package irys.client.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SiriClientUtil {
	
   private static SimpleDateFormat fileDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss-SSS");

   public static String nowFile()
   {
      return fileDateTimeFormat.format(new Date());
   }


}
