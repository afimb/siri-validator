package irys.client.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import uk.org.siri.siri.AnnotatedLineStructure;
import uk.org.siri.siri.AnnotatedStopPointStructure;
import uk.org.siri.siri.LineRefStructure;

public class MainJFrame extends JFrame
{
   private static final long serialVersionUID = 1L;
   private JPanel contentPane;
   private JComboBox<AnnotatedStopPointStructure> comboBoxStop;
   private JComboBox<AnnotatedLineStructure> comboBoxLine;
   private JComboBox<String> comboBoxDirection;
   private ComboBoxModel<AnnotatedStopPointStructure> stopModel;
   private ComboBoxModel<AnnotatedLineStructure> lineModel;
   private ComboBoxModel<String> directionModel;
   private GraphicControler controler;

   /**
    * Create the frame.
    */
   public MainJFrame(GraphicControler aControler)
   {
      this.controler = aControler;
      setTitle("Siri Client");
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      setBounds(100, 100, 530, 200);
      contentPane = new JPanel();
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      contentPane.setLayout(new BorderLayout(0, 0));
      setContentPane(contentPane);

      JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
      contentPane.add(tabbedPane, BorderLayout.CENTER);

      JPanel panel = new JPanel();
      tabbedPane.addTab("StopMonitoring", null, panel, null);
      GridBagLayout gbl_panel = new GridBagLayout();
      gbl_panel.columnWidths = new int[] { 0, 0, 0 };
      gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
      gbl_panel.columnWeights = new double[] { 0.1, 0.1, 1.0 };
      gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
      panel.setLayout(gbl_panel);

      JLabel lblMonitoredStop = new JLabel("Monitored Stop");
      lblMonitoredStop.setHorizontalAlignment(SwingConstants.LEFT);
      GridBagConstraints gbc_lblMonitoredStop = new GridBagConstraints();
      gbc_lblMonitoredStop.insets = new Insets(0, 0, 5, 5);
      gbc_lblMonitoredStop.anchor = GridBagConstraints.WEST;
      gbc_lblMonitoredStop.gridx = 0;
      gbc_lblMonitoredStop.gridy = 1;
      panel.add(lblMonitoredStop, gbc_lblMonitoredStop);

      comboBoxStop = new JComboBox<AnnotatedStopPointStructure>();
      comboBoxStop.setRenderer(new StopRender());
      GridBagConstraints gbc_comboBox = new GridBagConstraints();
      gbc_comboBox.insets = new Insets(0, 0, 5, 0);
      gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
      gbc_comboBox.gridx = 1;
      gbc_comboBox.gridy = 1;
      gbc_comboBox.gridwidth = 2;
      panel.add(comboBoxStop, gbc_comboBox);

      JLabel lblNewLlineAbel = new JLabel("Line");
      lblNewLlineAbel.setHorizontalAlignment(SwingConstants.LEFT);
      GridBagConstraints gbc_lblNewLlineAbel = new GridBagConstraints();
      gbc_lblNewLlineAbel.anchor = GridBagConstraints.WEST;
      gbc_lblNewLlineAbel.insets = new Insets(0, 0, 5, 5);
      gbc_lblNewLlineAbel.gridx = 0;
      gbc_lblNewLlineAbel.gridy = 2;
      panel.add(lblNewLlineAbel, gbc_lblNewLlineAbel);

      comboBoxLine = new JComboBox<AnnotatedLineStructure>();
      comboBoxLine.setRenderer(new LineRender());
      GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
      gbc_comboBox_1.insets = new Insets(0, 0, 5, 0);
      gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
      gbc_comboBox_1.gridx = 1;
      gbc_comboBox_1.gridy = 2;
      gbc_comboBox_1.gridwidth = 2;
      panel.add(comboBoxLine, gbc_comboBox_1);

      JLabel lblDirection = new JLabel("Direction");
      lblDirection.setHorizontalAlignment(SwingConstants.LEFT);
      GridBagConstraints gbc_lblDirection = new GridBagConstraints();
      gbc_lblDirection.anchor = GridBagConstraints.WEST;
      gbc_lblDirection.insets = new Insets(0, 0, 5, 5);
      gbc_lblDirection.gridx = 0;
      gbc_lblDirection.gridy = 3;
      panel.add(lblDirection, gbc_lblDirection);

      comboBoxDirection = new JComboBox<String>();
      GridBagConstraints gbc_comboBox_2 = new GridBagConstraints();
      gbc_comboBox_2.insets = new Insets(0, 0, 5, 0);
      gbc_comboBox_2.fill = GridBagConstraints.HORIZONTAL;
      gbc_comboBox_2.gridx = 1;
      gbc_comboBox_2.gridy = 3;
      gbc_comboBox_2.gridwidth = 2;
      panel.add(comboBoxDirection, gbc_comboBox_2);

      JButton btnSubscribeSM = new JButton("Subscribe");
      GridBagConstraints gbc_btnSubscribe = new GridBagConstraints();
      gbc_btnSubscribe.gridx = 0;
      gbc_btnSubscribe.gridy = 4;
      gbc_btnSubscribe.anchor = GridBagConstraints.EAST;
      gbc_btnSubscribe.gridwidth = 3;
      panel.add(btnSubscribeSM, gbc_btnSubscribe);
      btnSubscribeSM.addActionListener(new ActionListener()
      {

         @Override
         public void actionPerformed(ActionEvent event)
         {
            controler.subscribeSM((AnnotatedStopPointStructure) comboBoxStop.getSelectedItem(),
                  (AnnotatedLineStructure) comboBoxLine.getSelectedItem(), (String) comboBoxDirection.getSelectedItem());

         }
      });

   }

   public void dispose()
   {
      super.dispose();
      // unsubscribe all
      controler.terminate();
   }

   public void addStopList(Vector<AnnotatedStopPointStructure> stops)
   {
      stopModel = new DefaultComboBoxModel<AnnotatedStopPointStructure>(stops);
      comboBoxStop.setModel(stopModel);
      comboBoxStop.setSelectedIndex(0);
   }

   public void addLineList(Vector<AnnotatedLineStructure> lines)
   {
      lineModel = new DefaultComboBoxModel<AnnotatedLineStructure>(lines);
      comboBoxLine.setModel(lineModel);
   }

   public void addDirectionList(Vector<String> directions)
   {
      directionModel = new DefaultComboBoxModel<String>(directions);
      comboBoxDirection.setModel(directionModel);
   }

   private class StopRender implements ListCellRenderer<AnnotatedStopPointStructure>
   {
      protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

      @Override
      public Component getListCellRendererComponent(JList<? extends AnnotatedStopPointStructure> list,
            AnnotatedStopPointStructure value, int index, boolean isSelected, boolean cellHasFocus)
      {
         if (value == null)
         {
            return defaultRenderer.getListCellRendererComponent(list, "-- none --", index,
                  isSelected, cellHasFocus);

         }
         else
         {
            JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
                  isSelected, cellHasFocus);
            renderer.setText(value.getStopPointRef().getValue() + " (" + value.getStopName().get(0).getValue() + ")");

            return renderer;
         }
      }
   }

   private class LineRender implements ListCellRenderer<AnnotatedLineStructure>
   {
      protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

      @Override
      public Component getListCellRendererComponent(JList<? extends AnnotatedLineStructure> list,
            AnnotatedLineStructure value, int index, boolean isSelected, boolean cellHasFocus)
      {
         if (value == null)
         {
            return defaultRenderer.getListCellRendererComponent(list, "-- none --", index,
                  isSelected, cellHasFocus);

         }
         else
         {
            JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
                  isSelected, cellHasFocus);
            renderer.setText(value.getLineRef().getValue() + " (" + value.getLineName().get(0).getValue() + ")");

            AnnotatedStopPointStructure stop = (AnnotatedStopPointStructure) comboBoxStop.getSelectedItem();
            if (stop != null && stop.getLines() != null)
            {
               boolean found = false;

               for (Serializable item : stop.getLines().getLineRefOrLineDirection())
               {
                  if (item instanceof LineRefStructure)
                  {
                     LineRefStructure lineRef = (LineRefStructure) item;
                     if (lineRef.getValue().equals(value.getLineRef().getValue()))
                     {
                        found = true;
                        break;
                     }
                  }
               }
               if (!found)
                  renderer.setForeground(Color.RED);
            }
            return renderer;
         }
      }

   }

}
