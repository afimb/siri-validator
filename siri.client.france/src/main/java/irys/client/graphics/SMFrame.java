package irys.client.graphics;

import irys.client.consumer.StopMonitoringConsumer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import uk.org.siri.siri.AnnotatedLineStructure;
import uk.org.siri.siri.AnnotatedStopPointStructure;
import uk.org.siri.siri.ArrivalBoardingActivityEnumeration;
import uk.org.siri.siri.CallStatusEnumeration;
import uk.org.siri.siri.DepartureBoardingActivityEnumeration;
import uk.org.siri.siri.MonitoredCallStructure;
import uk.org.siri.siri.MonitoredStopVisitCancellationStructure;
import uk.org.siri.siri.MonitoredStopVisitStructure;
import uk.org.siri.siri.NaturalLanguageStringStructure;
import uk.org.siri.siri.StopMonitoringDeliveryStructure;

public class SMFrame extends JFrame implements StopMonitoringConsumer
{
   private static int xPos = 10;
   private static int yPos = 10;
   private static final long serialVersionUID = 1L;
   private JPanel contentPane;
   private GraphicControler controler;
   private String subscriptionRef;
   private String monitoredRef;

   private JLabel[] lineLabels = new JLabel[3];
   private JLabel[] directionLabels = new JLabel[3];
   private JLabel[] quayLabels = new JLabel[3];
   private JLabel[] destinationLabels = new JLabel[3];
   private JLabel[] arrivalTimeLabels = new JLabel[3];
   private JLabel[] arrivalStatusLabels = new JLabel[3];
   private JLabel[] arrivalActivityLabels = new JLabel[3];
   private JLabel[] departureTimeLabels = new JLabel[3];
   private JLabel[] departureStatusLabels = new JLabel[3];
   private JLabel[] departureActivityLabels = new JLabel[3];
   private JLabel[] vehicleJourneyLabels = new JLabel[3];
   private JLabel[] callNoteLabels = new JLabel[3];

   private String[] itemIdentifiers = new String[5];
   private Object[] datas = new Object[5];
   private AnnotatedStopPointStructure point;
   private AnnotatedLineStructure line;
   private String direction;

   /**
    * Create the frame.
    */
   public SMFrame(GraphicControler controler, String subscriptionRef, AnnotatedStopPointStructure point,
                  AnnotatedLineStructure line, String direction)
   {
      this.controler = controler;
      this.subscriptionRef = subscriptionRef;
      this.point = point;
      this.line = line;
      this.direction = direction;
      setTitle(subscriptionRef);
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      setBounds(xPos, yPos, 630, 230);
      xPos += 30;
      yPos += 30;
      if (xPos > 500)
         xPos = 10;
      if (yPos > 500)
         yPos = 10;
      contentPane = new JPanel();
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      contentPane.setLayout(new BorderLayout(0, 0));
      setContentPane(contentPane);

      JPanel panelGrid = new JPanel();
      panelGrid.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      contentPane.add(panelGrid, BorderLayout.CENTER);
      GridBagLayout gbl_panelGrid = new GridBagLayout();
      gbl_panelGrid.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
      gbl_panelGrid.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
      gbl_panelGrid.columnWeights = new double[] { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0, 1 };
      gbl_panelGrid.rowWeights = new double[] { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1 };
      panelGrid.setLayout(gbl_panelGrid);

      JLabel lblLine = new JLabel("Line");
      lblLine.setBorder(getNewLabelBorder());
      lblLine.setHorizontalAlignment(JLabel.CENTER);
      GridBagConstraints gbc_lblLine = new GridBagConstraints();
      gbc_lblLine.insets = new Insets(0, 0, -1, -1);
      gbc_lblLine.gridx = 0;
      gbc_lblLine.gridy = 0;
      gbc_lblLine.fill = GridBagConstraints.BOTH;
      panelGrid.add(lblLine, gbc_lblLine);

      JLabel lblDirection = new JLabel("Direction");
      lblDirection.setBorder(getNewLabelBorder());
      lblDirection.setHorizontalAlignment(JLabel.CENTER);
      GridBagConstraints gbc_lblDirection = new GridBagConstraints();
      gbc_lblDirection.insets = new Insets(0, 0, -1, -1);
      gbc_lblDirection.gridx = 1;
      gbc_lblDirection.gridy = 0;
      gbc_lblDirection.fill = GridBagConstraints.BOTH;
      panelGrid.add(lblDirection, gbc_lblDirection);

      JLabel lblDestination = new JLabel("Destination");
      lblDestination.setBorder(getNewLabelBorder());
      lblDestination.setHorizontalAlignment(JLabel.CENTER);
      GridBagConstraints gbc_lblDestination = new GridBagConstraints();
      gbc_lblDestination.insets = new Insets(0, 0, -1, -1);
      gbc_lblDestination.gridx = 2;
      gbc_lblDestination.gridy = 0;
      gbc_lblDestination.fill = GridBagConstraints.BOTH;
      panelGrid.add(lblDestination, gbc_lblDestination);

      JLabel lblQuay = new JLabel("Quay");
      lblQuay.setBorder(getNewLabelBorder());
      lblQuay.setHorizontalAlignment(JLabel.CENTER);
      GridBagConstraints gbc_lblQuay = new GridBagConstraints();
      gbc_lblQuay.insets = new Insets(0, 0, -1, -1);
      gbc_lblQuay.gridx = 3;
      gbc_lblQuay.gridy = 0;
      gbc_lblQuay.fill = GridBagConstraints.BOTH;
      panelGrid.add(lblQuay, gbc_lblQuay);

      JLabel lblArrival = new JLabel("Arrival");
      lblArrival.setHorizontalAlignment(JLabel.CENTER);
      lblArrival.setBorder(getNewLabelBorder());
      GridBagConstraints gbc_lblArrival = new GridBagConstraints();
      gbc_lblArrival.gridwidth = 3;
      gbc_lblArrival.insets = new Insets(0, 0, -1, -1);
      gbc_lblArrival.gridx = 4;
      gbc_lblArrival.gridy = 0;
      gbc_lblArrival.fill = GridBagConstraints.BOTH;
      panelGrid.add(lblArrival, gbc_lblArrival);

      JLabel lblDeparture = new JLabel("Departure");
      lblDeparture.setHorizontalAlignment(JLabel.CENTER);
      lblDeparture.setBorder(getNewLabelBorder());
      GridBagConstraints gbc_lblDeparture = new GridBagConstraints();
      gbc_lblDeparture.gridwidth = 3;
      gbc_lblDeparture.insets = new Insets(0, 0, -1, -1);
      gbc_lblDeparture.gridx = 7;
      gbc_lblDeparture.gridy = 0;
      gbc_lblDeparture.fill = GridBagConstraints.BOTH;
      panelGrid.add(lblDeparture, gbc_lblDeparture);

      JLabel lblJourney = new JLabel("Journey");
      lblJourney.setHorizontalAlignment(JLabel.CENTER);
      lblJourney.setBorder(getNewLabelBorder());
      GridBagConstraints gbc_lblJourney = new GridBagConstraints();
      gbc_lblJourney.insets = new Insets(0, 0, -1, -1);
      gbc_lblJourney.gridx = 10;
      gbc_lblJourney.gridy = 0;
      gbc_lblJourney.fill = GridBagConstraints.BOTH;
      panelGrid.add(lblJourney, gbc_lblJourney);

      addLineInfo(panelGrid, 0);
      addLineInfo(panelGrid, 1);
      addLineInfo(panelGrid, 2);

      populateForm();

      JPanel panelTitle = new JPanel();
      contentPane.add(panelTitle, BorderLayout.NORTH);
      panelTitle.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

      String textParams = point.getStopPointRef().getValue() + " (" + point.getStopName().get(0).getValue() + ")";
      JLabel lblParams = new JLabel(textParams);
      panelTitle.add(lblParams);

      JPanel panelButton = new JPanel();
      contentPane.add(panelButton, BorderLayout.SOUTH);
      panelButton.setLayout(new BorderLayout(0, 0));
      JButton btnClose = new JButton("Close");

      panelButton.add(btnClose, BorderLayout.EAST);

      btnClose.addActionListener(new ActionListener()
      {

         @Override
         public void actionPerformed(ActionEvent event)
         {
            dispose();

         }
      });

   }

   private Border getNewLabelBorder()
   {
      return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY), new EmptyBorder(2, 2,
            2, 2));
   }

   private void addLineInfo(JPanel panel, int i)
   {
      int x = 0;
      int y = 2 * i + 1;
      lineLabels[i] = new JLabel("no data");
      lineLabels[i].setBorder(getNewLabelBorder());
      lineLabels[i].setHorizontalAlignment(JLabel.LEFT);
      GridBagConstraints gbc_lblL = new GridBagConstraints();
      gbc_lblL.insets = new Insets(0, 0, -1, -1);
      gbc_lblL.gridx = x++;
      gbc_lblL.gridy = y;
      gbc_lblL.fill = GridBagConstraints.BOTH;
      panel.add(lineLabels[i], gbc_lblL);

      directionLabels[i] = new JLabel("n.a.");
      directionLabels[i].setBorder(getNewLabelBorder());
      directionLabels[i].setHorizontalAlignment(JLabel.CENTER);
      GridBagConstraints gbc_lblDi = new GridBagConstraints();
      gbc_lblDi.insets = new Insets(0, 0, -1, -1);
      gbc_lblDi.gridx = x++;
      gbc_lblDi.gridy = y;
      gbc_lblDi.fill = GridBagConstraints.BOTH;
      panel.add(directionLabels[i], gbc_lblDi);

      destinationLabels[i] = new JLabel("no data");
      destinationLabels[i].setBorder(getNewLabelBorder());
      destinationLabels[i].setHorizontalAlignment(JLabel.CENTER);
      GridBagConstraints gbc_lblDe = new GridBagConstraints();
      gbc_lblDe.insets = new Insets(0, 0, -1, -1);
      gbc_lblDe.gridx = x++;
      gbc_lblDe.gridy = y;
      gbc_lblDe.fill = GridBagConstraints.BOTH;
      panel.add(destinationLabels[i], gbc_lblDe);

      quayLabels[i] = new JLabel("n.a.");
      quayLabels[i].setBorder(getNewLabelBorder());
      GridBagConstraints gbc_lblQy = new GridBagConstraints();
      gbc_lblQy.insets = new Insets(0, 0, -1, -1);
      gbc_lblQy.gridx = x++;
      gbc_lblQy.gridy = y;
      gbc_lblQy.fill = GridBagConstraints.BOTH;
      panel.add(quayLabels[i], gbc_lblQy);

      arrivalTimeLabels[i] = new JLabel("hh:mm:ss");
      arrivalTimeLabels[i].setBorder(getNewLabelBorder());
      GridBagConstraints gbc_lblAt = new GridBagConstraints();
      gbc_lblAt.insets = new Insets(0, 0, -1, -1);
      gbc_lblAt.gridx = x++;
      gbc_lblAt.gridy = y;
      gbc_lblAt.fill = GridBagConstraints.BOTH;
      panel.add(arrivalTimeLabels[i], gbc_lblAt);

      arrivalStatusLabels[i] = new JLabel("n.a.");
      arrivalStatusLabels[i].setBorder(getNewLabelBorder());
      GridBagConstraints gbc_lblAs = new GridBagConstraints();
      gbc_lblAs.insets = new Insets(0, 0, -1, -1);
      gbc_lblAs.gridx = x++;
      gbc_lblAs.gridy = y;
      gbc_lblAs.fill = GridBagConstraints.BOTH;
      panel.add(arrivalStatusLabels[i], gbc_lblAs);

      arrivalActivityLabels[i] = new JLabel("n.a.");
      arrivalActivityLabels[i].setBorder(getNewLabelBorder());
      GridBagConstraints gbc_lblAa = new GridBagConstraints();
      gbc_lblAa.insets = new Insets(0, 0, -1, -1);
      gbc_lblAa.gridx = x++;
      gbc_lblAa.gridy = y;
      gbc_lblAa.fill = GridBagConstraints.BOTH;
      panel.add(arrivalActivityLabels[i], gbc_lblAa);

      departureTimeLabels[i] = new JLabel("hh:mm:ss");
      departureTimeLabels[i].setBorder(getNewLabelBorder());
      GridBagConstraints gbc_lblDt = new GridBagConstraints();
      gbc_lblDt.insets = new Insets(0, 0, -1, -1);
      gbc_lblDt.gridx = x++;
      gbc_lblDt.gridy = y;
      gbc_lblDt.fill = GridBagConstraints.BOTH;
      panel.add(departureTimeLabels[i], gbc_lblDt);

      departureStatusLabels[i] = new JLabel("n.a.");
      departureStatusLabels[i].setBorder(getNewLabelBorder());
      GridBagConstraints gbc_lblDs = new GridBagConstraints();
      gbc_lblDs.insets = new Insets(0, 0, -1, -1);
      gbc_lblDs.gridx = x++;
      gbc_lblDs.gridy = y;
      gbc_lblDs.fill = GridBagConstraints.BOTH;
      panel.add(departureStatusLabels[i], gbc_lblDs);

      departureActivityLabels[i] = new JLabel("n.a.");
      departureActivityLabels[i].setBorder(getNewLabelBorder());
      GridBagConstraints gbc_lblDa = new GridBagConstraints();
      gbc_lblDa.insets = new Insets(0, 0, -1, -1);
      gbc_lblDa.gridx = x++;
      gbc_lblDa.gridy = y;
      gbc_lblDa.fill = GridBagConstraints.BOTH;
      panel.add(departureActivityLabels[i], gbc_lblDa);

      vehicleJourneyLabels[i] = new JLabel("n.a.");
      vehicleJourneyLabels[i].setBorder(getNewLabelBorder());
      vehicleJourneyLabels[i].setHorizontalAlignment(JLabel.RIGHT);
      GridBagConstraints gbc_lblVj = new GridBagConstraints();
      gbc_lblVj.insets = new Insets(0, 0, -1, -1);
      gbc_lblVj.gridx = x++;
      gbc_lblVj.gridy = y;
      gbc_lblVj.fill = GridBagConstraints.BOTH;
      panel.add(vehicleJourneyLabels[i], gbc_lblVj);

      callNoteLabels[i] = new JLabel("--");
      Font font = callNoteLabels[i].getFont();
      callNoteLabels[i].setFont(font.deriveFont((float) font.getSize() / 2));
      callNoteLabels[i].setBorder(getNewLabelBorder());
      GridBagConstraints gbc_lblCnj = new GridBagConstraints();
      gbc_lblCnj.insets = new Insets(0, 0, -1, -1);
      gbc_lblCnj.gridx = 0;
      gbc_lblCnj.gridy = y + 1;
      gbc_lblCnj.gridwidth = 11;
      gbc_lblCnj.fill = GridBagConstraints.BOTH;
      panel.add(callNoteLabels[i], gbc_lblCnj);

   }

   public void dispose()
   {
      super.dispose();
      // unsubscribe
      controler.unsubscribe(this, subscriptionRef);
   }

   @Override
   public void consume(StopMonitoringDeliveryStructure notification)
   {
      // clean cancelled and departed
      {
         int rank = 0;
         while (rank < 5 && datas[rank] != null)
         {
            if (datas[rank] instanceof MonitoredStopVisitCancellationStructure)
            {
               removeRank(rank);
            }
            else
            {
               MonitoredStopVisitStructure msv = (MonitoredStopVisitStructure) datas[rank];
               MonitoredCallStructure call = msv.getMonitoredVehicleJourney().getMonitoredCall();
               CallStatusEnumeration status = msv.getMonitoredVehicleJourney().getMonitoredCall().getDepartureStatus();
               if (status.equals(CallStatusEnumeration.DEPARTED) || status.equals(CallStatusEnumeration.MISSED)
                     || status.equals(CallStatusEnumeration.CANCELLED))
               {
                  System.out.println("call ended normally : journey = "
                        + token(msv.getMonitoredVehicleJourney().getFramedVehicleJourneyRef()
                              .getDatedVehicleJourneyRef(), 3) + " status = " + status.name());
                  removeRank(rank);
               }
               else if (call.isSetActualDepartureTime())
               {
                  System.out.println("call ended without expected status : journey = "
                        + token(msv.getMonitoredVehicleJourney().getFramedVehicleJourneyRef()
                              .getDatedVehicleJourneyRef(), 3) + " status = " + status.name());
                  removeRank(rank);
               }
               else
               {
                  rank++;
               }
            }
         }
      }

      for (MonitoredStopVisitStructure monitoredStopVisit : notification.getMonitoredStopVisit())
      {
         if (monitoredRef == null)
         {
            monitoredRef = monitoredStopVisit.getMonitoringRef().getValue();
            if (direction != null)
            {
               int order = monitoredStopVisit.getMonitoredVehicleJourney().getMonitoredCall().getOrder().intValue();
               setTitle(subscriptionRef + " on " + monitoredRef + "(" + order + ")");
            }
            else
            {
               setTitle(subscriptionRef + " on " + monitoredRef);
            }
         }
         String ref = monitoredStopVisit.getItemIdentifier();
         int rank = 0;
         while (rank < 5 && itemIdentifiers[rank] != null && !ref.equals(itemIdentifiers[rank]))
         {
            rank++;
         }
         if (rank == 5)
         {
            System.out.println("problem : no more place to follow stop, ref = " + ref);
         }
         else
         {
            if (itemIdentifiers[rank] == null)
            {
               itemIdentifiers[rank] = ref;
            }
            datas[rank] = monitoredStopVisit;
         }
      }
      for (MonitoredStopVisitCancellationStructure monitoredStopVisitCancelled : notification
            .getMonitoredStopVisitCancellation())
      {
         String ref = monitoredStopVisitCancelled.getItemRef().getValue();
         int rank = 0;
         while (rank < 5 && itemIdentifiers[rank] != null && !ref.equals(itemIdentifiers[rank]))
         {
            rank++;
         }
         if (rank == 5)
         {
            System.out.println("problem : no more place to follow stop");
         }
         else if (itemIdentifiers[rank] == null)
         {
            System.out.println("cancel on unknown itemidentifier " + ref);
         }
         else
         {
            datas[rank] = monitoredStopVisitCancelled;
         }
      }
      populateForm();

      validate();
   }

   private void populateForm()
   {
      for (int i = 0; i < 3; i++)
      {
         if (datas[i] == null)
         {
            nullify(i);
         }
         else if (datas[i] instanceof MonitoredStopVisitCancellationStructure)
         {
            cancel(i);
         }
         else
         {
            fill(i);
         }
      }

   }

   private void fill(int i)
   {
      SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
      MonitoredStopVisitStructure msv = (MonitoredStopVisitStructure) datas[i];
      MonitoredCallStructure call = msv.getMonitoredVehicleJourney().getMonitoredCall();
      lineLabels[i].setText(msv.getMonitoredVehicleJourney().getLineRef().getValue());
      directionLabels[i].setText(msv.getMonitoredVehicleJourney().getDirectionRef().getValue());
      String dest = msv.getMonitoredVehicleJourney().getDestinationRef().getValue();
      String[] tokens = dest.split(":");
      destinationLabels[i].setText(tokens[tokens.length - 1]);
      String quayId = "undef";
      String aimedQuayId = "undef";
      if (call.getDepartureStopAssignment().getExpectedQuayRef() != null)
         quayId = call.getDepartureStopAssignment().getExpectedQuayRef().getValue();
      else if (call.getDepartureStopAssignment().getActualQuayRef() != null)
         quayId = call.getDepartureStopAssignment().getActualQuayRef().getValue();
      if (call.getDepartureStopAssignment().getAimedQuayRef() != null)
         aimedQuayId = call.getDepartureStopAssignment().getAimedQuayRef().getValue();
      quayLabels[i].setText(token(quayId, 4));
      if (quayId.equals(aimedQuayId))
      {
         quayLabels[i].setForeground(Color.BLACK);
      }
      else
      {
         quayLabels[i].setForeground(Color.RED);
      }
      Date d = call.getExpectedArrivalTime() != null ? call.getExpectedArrivalTime().toGregorianCalendar().getTime() :
            call.getActualArrivalTime().toGregorianCalendar().getTime();
      arrivalTimeLabels[i].setText(format.format(d));
      arrivalStatusLabels[i].setText(convertStatus(call.getArrivalStatus()));
      arrivalStatusLabels[i].setForeground(colorStatus(call.getArrivalStatus()));
      arrivalActivityLabels[i].setText(convertActivity(call.getArrivalBoardingActivity()));
      arrivalActivityLabels[i].setForeground(colorActivity(call.getArrivalBoardingActivity()));
      d = call.getExpectedDepartureTime() != null ? call.getExpectedDepartureTime().toGregorianCalendar().getTime() :
            call.getActualDepartureTime().toGregorianCalendar().getTime();
      departureTimeLabels[i].setText(format.format(d));
      departureStatusLabels[i].setText(convertStatus(call.getDepartureStatus()));
      departureStatusLabels[i].setForeground(colorStatus(call.getDepartureStatus()));
      departureActivityLabels[i].setText(convertActivity(call.getDepartureBoardingActivity()));
      departureActivityLabels[i].setForeground(colorActivity(call.getDepartureBoardingActivity()));
      vehicleJourneyLabels[i].setText(token(msv.getMonitoredVehicleJourney().getFramedVehicleJourneyRef()
            .getDatedVehicleJourneyRef(), 3));
      vehicleJourneyLabels[i].setForeground(colorMonitored(msv.getMonitoredVehicleJourney().isMonitored()));
      if (call.isSetCallNote())
      {
         String text = "";
         for (NaturalLanguageStringStructure note : call.getCallNote())
         {
            text += note.getValue() + " ";
         }
         callNoteLabels[i].setText(text);
      }
      else
      {
         callNoteLabels[i].setText("--");
      }
   }

   private String convertStatus(CallStatusEnumeration status)
   {
      if (status == null)
         return "nul";
      switch (status)
      {
      case ARRIVED:
         return "Arr";
      case CANCELLED:
         return "Can";
      case DELAYED:
         return "Del";
      case DEPARTED:
         return "Dep";
      case EARLY:
         return "Ear";
      case MISSED:
         return "Skp";
      case NO_REPORT:
         return "NoR";
      case NOT_EXPECTED:
         return "NoE";
      case ON_TIME:
         return "OnT";
      }
      return "Und";
   }

   private Color colorStatus(CallStatusEnumeration status)
   {
      if (status == null)
         return Color.BLACK;
      switch (status)
      {
      case ARRIVED:
         return Color.GREEN;
      case CANCELLED:
         return Color.RED;
      case DELAYED:
         return Color.ORANGE;
      case DEPARTED:
         return Color.GREEN;
      case EARLY:
         return Color.CYAN;
      case MISSED:
         return Color.RED;
      case NO_REPORT:
         return Color.BLACK;
      case NOT_EXPECTED:
         return Color.MAGENTA;
      case ON_TIME:
         return Color.BLACK;
      }
      return Color.BLACK;
   }

   private String convertActivity(ArrivalBoardingActivityEnumeration activity)
   {
      if (activity == null)
         return "---";
      switch (activity)
      {
      case ALIGHTING:
         return "ALT";
      case NO_ALIGHTING:
         return "NoA";
      case PASS_THRU:
         return "PTH";
      }
      return "Und";
   }

   private Color colorActivity(ArrivalBoardingActivityEnumeration activity)
   {
      if (activity == null)
         return Color.BLACK;
      switch (activity)
      {
      case ALIGHTING:
         return Color.BLACK;
      case NO_ALIGHTING:
         return Color.ORANGE;
      case PASS_THRU:
         return Color.RED;
      }
      return Color.BLACK;
   }
   
   private Color colorMonitored(Boolean monitored)
   {
      if (monitored == null || !monitored)
         return Color.ORANGE;
      return Color.BLACK;
   }


   private String convertActivity(DepartureBoardingActivityEnumeration activity)
   {
      if (activity == null)
         return "---";
      switch (activity)
      {
      case BOARDING:
         return "BRD";
      case NO_BOARDING:
         return "NoB";
      case PASS_THRU:
         return "PTH";
      }
      return "Und";
   }

   private Color colorActivity(DepartureBoardingActivityEnumeration activity)
   {
      if (activity == null)
         return Color.BLACK;
      switch (activity)
      {
      case BOARDING:
         return Color.BLACK;
      case NO_BOARDING:
         return Color.ORANGE;
      case PASS_THRU:
         return Color.RED;
      }
      return Color.BLACK;
   }

   private void cancel(int i)
   {
      MonitoredStopVisitCancellationStructure msvc = (MonitoredStopVisitCancellationStructure) datas[i];
      lineLabels[i].setText(msvc.getLineRef().getValue());
      directionLabels[i].setText(msvc.getDirectionRef().getValue());
      destinationLabels[i].setText(msvc.getDirectionName().get(0).getValue());
      quayLabels[i].setText("--");
      quayLabels[i].setForeground(Color.BLACK);
      arrivalTimeLabels[i].setText("--:--:--");
      arrivalStatusLabels[i].setText("Can");
      arrivalStatusLabels[i].setForeground(Color.RED);
      arrivalActivityLabels[i].setText("--");
      arrivalActivityLabels[i].setForeground(Color.BLACK);
      departureTimeLabels[i].setText("--:--:--");
      departureStatusLabels[i].setText("Can");
      departureStatusLabels[i].setForeground(Color.RED);
      departureActivityLabels[i].setText("--");
      departureActivityLabels[i].setForeground(Color.BLACK);
      vehicleJourneyLabels[i].setText(token(msvc.getVehicleJourneyRef().getDatedVehicleJourneyRef(), 3));
      if (msvc.isSetReason())
      {
         callNoteLabels[i].setText(msvc.getReason().get(0).getValue());
      }
      else
      {
         callNoteLabels[i].setText("-");
      }
   }

   private String token(String value, int size)
   {
      if (value == null)
         return "nul";
      String[] tokens = value.split(":");
      if (tokens.length != size)
         return value;
      return tokens[size - 1];
   }

   private void nullify(int i)
   {
      lineLabels[i].setText("----");
      directionLabels[i].setText("----");
      destinationLabels[i].setText("----");
      quayLabels[i].setText("----");
      quayLabels[i].setForeground(Color.BLACK);
      arrivalTimeLabels[i].setText("--:--:--");
      arrivalStatusLabels[i].setText("-");
      arrivalStatusLabels[i].setForeground(Color.BLACK);
      arrivalActivityLabels[i].setText("-");
      arrivalActivityLabels[i].setForeground(Color.BLACK);
      departureTimeLabels[i].setText("--:--:--");
      departureStatusLabels[i].setText("-");
      departureStatusLabels[i].setForeground(Color.BLACK);
      departureActivityLabels[i].setText("-");
      departureActivityLabels[i].setForeground(Color.BLACK);
      vehicleJourneyLabels[i].setText("--");
      vehicleJourneyLabels[i].setForeground(Color.BLACK);
      callNoteLabels[i].setText("--");
   }

   private void removeRank(int rank)
   {
      for (int i = rank; i < 4; i++)
      {
         datas[i] = datas[i + 1];
         itemIdentifiers[i] = itemIdentifiers[i + 1];
      }
      datas[4] = null;
      itemIdentifiers[4] = null;
   }

}
