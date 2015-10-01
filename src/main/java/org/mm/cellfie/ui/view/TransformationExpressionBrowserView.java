package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.mm.cellfie.ui.exception.CellfieException;
import org.mm.core.MappingExpression;
import org.mm.core.MappingExpressionSetFactory;
import org.mm.ui.DialogManager;
import org.mm.ui.ModelView;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.core.ui.util.JOptionPaneEx;

public class TransformationExpressionBrowserView extends JPanel implements ModelView
{
   private static final long serialVersionUID = 1L;

   private ApplicationView container;

   private JPanel pnlContainer;

   private JButton cmdAdd;
   private JButton cmdEdit;
   private JButton cmdDelete;
   private JButton cmdSave;
   private JButton cmdSaveAs;
   private JButton cmdGenerateAxioms;

   private JTable tblMappingExpression;

   private MappingExpressionTableModel tableModel;

   public TransformationExpressionBrowserView(ApplicationView container)
   {
      this.container = container;

      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

      pnlContainer = new JPanel();
      pnlContainer.setLayout(new BorderLayout());
      add(pnlContainer, BorderLayout.CENTER);

      tblMappingExpression = new JTable();
      tblMappingExpression.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      tblMappingExpression.setGridColor(new Color(220, 220, 220));
      tblMappingExpression.setDefaultRenderer(String.class, new MultiLineCellRenderer());
      tblMappingExpression.addMouseListener(new MappingExpressionSelectionListener());

      JScrollPane scrMappingExpression = new JScrollPane(tblMappingExpression);

      JPanel pnlTop = new JPanel(new BorderLayout());
      pnlTop.setBorder(new EmptyBorder(2, 5, 7, 5));
      pnlContainer.add(pnlTop, BorderLayout.NORTH);

      JPanel pnlCommandButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      pnlTop.add(pnlCommandButton, BorderLayout.WEST);

      cmdAdd = new JButton("Add");
      cmdAdd.setPreferredSize(new Dimension(72, 22));
      cmdAdd.addActionListener(new AddButtonActionListener());
      pnlCommandButton.add(cmdAdd);

      cmdEdit = new JButton("Edit");
      cmdEdit.setPreferredSize(new Dimension(72, 22));
      cmdEdit.setEnabled(false);
      cmdEdit.addActionListener(new EditButtonActionListener());
      pnlCommandButton.add(cmdEdit);

      cmdDelete = new JButton("Delete");
      cmdDelete.setPreferredSize(new Dimension(72, 22));
      cmdDelete.setEnabled(false);
      cmdDelete.addActionListener(new DeleteButtonActionListener());
      pnlCommandButton.add(cmdDelete);

      JPanel pnlMappingOpenSave = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      pnlTop.add(pnlMappingOpenSave, BorderLayout.EAST);

      JButton cmdLoad = new JButton("Load Expressions");
      cmdLoad.setPreferredSize(new Dimension(152, 22));
      cmdLoad.addActionListener(new OpenMappingAction());
      pnlMappingOpenSave.add(cmdLoad);

      cmdSave = new JButton("Save Expressions");
      cmdSave.setPreferredSize(new Dimension(152, 22));
      cmdSave.addActionListener(new SaveMappingAction());
      cmdSave.setEnabled(false);
      pnlMappingOpenSave.add(cmdSave);

      cmdSaveAs = new JButton("Save As...");
      cmdSaveAs.setPreferredSize(new Dimension(152, 22));
      cmdSaveAs.addActionListener(new SaveAsMappingAction());
      cmdSaveAs.setEnabled(false);
      pnlMappingOpenSave.add(cmdSaveAs);

      JPanel pnlCenter = new JPanel(new BorderLayout());
      pnlContainer.add(pnlCenter, BorderLayout.CENTER);

      pnlCenter.add(scrMappingExpression, BorderLayout.CENTER);

      JPanel pnlGenerateAxioms = new JPanel();
      pnlContainer.add(pnlGenerateAxioms, BorderLayout.SOUTH);

      cmdGenerateAxioms = new JButton("Generate Axioms");
      cmdGenerateAxioms.setPreferredSize(new Dimension(152, 22));
      cmdGenerateAxioms.addActionListener(new GenerateAxiomsAction(container));
      pnlGenerateAxioms.add(cmdGenerateAxioms);

      update();
      validate();
   }

   @Override
   public void update()
   {
      tableModel = new MappingExpressionTableModel(container.getActiveMappingExpressions());
      tblMappingExpression.setModel(tableModel);
      setTableHeaderAlignment(SwingConstants.CENTER);
      setPreferredColumnWidth();
      setPreferredColumnHeight();
      updateBorderUI();
   }

   private void setTableHeaderAlignment(int alignment)
   {
      ((DefaultTableCellRenderer) tblMappingExpression.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(alignment);
   }

   private void updateBorderUI()
   {
      pnlContainer.setBorder(ComponentFactory.createTitledBorder(getTitle()));
   }

   private String getTitle()
   {
      String filename = container.getMappingFileLocation();
      if (filename == null || filename.isEmpty()) {
         return String.format("Transformation Expressions");
      }
      return String.format("Transformation Expressions (%s)", filename);
   }

   private void updateTableModel(int selectedRow, String sheetName, String startColumn, String endColumn,
         String startRow, String endRow, String expression, String comment)
   {
      Vector<String> row = new Vector<>();
      row.add(0, sheetName);
      row.add(1, startColumn);
      row.add(2, endColumn);
      row.add(3, startRow);
      row.add(4, endRow);
      row.add(5, expression);
      row.add(6, comment);

      if (selectedRow != -1) { // user selected a row
         tableModel.removeRow(selectedRow);
      }
      tableModel.addRow(row);
   }

   private void setPreferredColumnWidth()
   {
      final TableColumnModel columnModel = tblMappingExpression.getColumnModel();
      columnModel.getColumn(0).setPreferredWidth(100);
      columnModel.getColumn(1).setPreferredWidth(100);
      columnModel.getColumn(2).setPreferredWidth(100);
      columnModel.getColumn(3).setPreferredWidth(100);
      columnModel.getColumn(4).setPreferredWidth(100);
      columnModel.getColumn(5).setPreferredWidth(360);
      columnModel.getColumn(6).setPreferredWidth(180);
   }

   private void setPreferredColumnHeight()
   {
      int columnIndex = 5; // only for expression column
      for (int row = 0; row < tblMappingExpression.getRowCount(); row++) {
         int height = 0; // min height;
         Object value = tblMappingExpression.getModel().getValueAt(row, columnIndex);
         TableCellRenderer renderer = tblMappingExpression.getDefaultRenderer(String.class);
         Component comp = renderer.getTableCellRendererComponent(tblMappingExpression, value, false, false, row, columnIndex);
         height = Math.max(comp.getPreferredSize().height, height);
         tblMappingExpression.setRowHeight(row, height);
      }
   }

   public List<MappingExpression> getMappingExpressions()
   {
      return tableModel.getMappingExpressions();
   }

   private DialogManager getApplicationDialogManager()
   {
      return container.getApplicationDialogManager();
   }

   class MappingExpressionTableModel extends DefaultTableModel
   {
      private static final long serialVersionUID = 1L;

      private final String[] COLUMN_NAMES = { "Sheet Name", "Start Column", "End Column", "Start Row", "End Row",
            "Transformation Expression", "Comment" };

      public MappingExpressionTableModel(final List<MappingExpression> mappings)
      {
         super();
         for (MappingExpression mapping : mappings) {
            Vector<Object> row = new Vector<Object>();
            row.add(mapping.getSheetName());
            row.add(mapping.getStartColumn());
            row.add(mapping.getEndColumn());
            row.add(mapping.getStartRow());
            row.add(mapping.getEndRow());
            row.add(mapping.getExpressionString());
            row.add(mapping.getComment());
            addRow(row);
         }
      }

      @Override
      public String getColumnName(int column) // 0-based
      {
         return COLUMN_NAMES[column];
      }

      @Override
      public int getColumnCount()
      {
         return COLUMN_NAMES.length;
      }

      @Override
      public Class<?> getColumnClass(int columnIndex)
      {
         return String.class;
      }

      @Override
      public boolean isCellEditable(int rowIndex, int columnIndex)
      {
         return false;
      }

      public List<MappingExpression> getMappingExpressions()
      {
         List<MappingExpression> mappings = new ArrayList<>();
         for (int row = 0; row < getRowCount(); row++) {
            @SuppressWarnings("unchecked")
            Vector<String> rowVector = (Vector<String>) getDataVector().elementAt(row);
            String sheetName = rowVector.get(0);
            String startColumn = rowVector.get(1);
            String endColumn = rowVector.get(2);
            String startRow = rowVector.get(3);
            String endRow = rowVector.get(4);
            String expression = rowVector.get(5);
            String comment = rowVector.get(6);
            mappings.add(new MappingExpression(sheetName, startColumn, endColumn, startRow, endRow, comment, expression));
         }
         return mappings;
      }
   }

   /**
    * To allow cells in the mapping browser table to have multi-lines.
    */
   class MultiLineCellRenderer extends JTextArea implements TableCellRenderer
   {
      private static final long serialVersionUID = 1L;

      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column)
      {
         if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
         } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
         }
         setFont(table.getFont());
         if (hasFocus) {
            setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            if (table.isCellEditable(row, column)) {
               setForeground(UIManager.getColor("Table.focusCellForeground"));
               setBackground(UIManager.getColor("Table.focusCellBackground"));
            }
         } else {
            setBorder(new EmptyBorder(1, 2, 1, 2));
         }
         setText((value == null) ? "" : value.toString());
         return this;
      }
   }

   /**
    * To allow user editing immediately the mapping expressions by double-clicking the expression row.
    */
   class MappingExpressionSelectionListener extends MouseAdapter
   {
      private int lastSelectedRow = -1;

      @Override
      public void mouseClicked(MouseEvent e)
      {
         int selectedRow = tblMappingExpression.getSelectedRow();
         if (e.getClickCount() == 1) { // single click
            if (selectedRow != lastSelectedRow) {
               cmdEdit.setEnabled(true);
               cmdDelete.setEnabled(true);
               lastSelectedRow = selectedRow;
            } else {
               tblMappingExpression.clearSelection();
               cmdEdit.setEnabled(false);
               cmdDelete.setEnabled(false);
               lastSelectedRow = -1; // reset
            }
         } else if (e.getClickCount() == 2) { // double-click
            TransformationExpressionEditorPanel editorPanel = new TransformationExpressionEditorPanel();
            editorPanel.setSheetNames(container.getActiveWorkbook().getSheetNames());
            editorPanel.fillFormFields(getValueAt(selectedRow, 0), getValueAt(selectedRow, 1),
                  getValueAt(selectedRow, 2), getValueAt(selectedRow, 3), getValueAt(selectedRow, 4),
                  getValueAt(selectedRow, 5), getValueAt(selectedRow, 6));
            showMappingEditorDialog(editorPanel, selectedRow);
         }
      }

      private String getValueAt(int row, int column)
      {
         return (String) tableModel.getValueAt(row, column);
      }
   }

   /*
    * Action listener implementations for command buttons in
    * MappingExpressionView panel
    */

   class AddButtonActionListener implements ActionListener
   {
      @Override
      public void actionPerformed(ActionEvent e)
      {
         TransformationExpressionEditorPanel editorPanel = new TransformationExpressionEditorPanel();
         editorPanel.setSheetNames(container.getActiveWorkbook().getSheetNames());
         showMappingEditorDialog(editorPanel, -1);
      }
   }

   class EditButtonActionListener implements ActionListener
   {
      @Override
      public void actionPerformed(ActionEvent e)
      {
         int selectedRow = tblMappingExpression.getSelectedRow();
         try {
            validateSelection(selectedRow);
            TransformationExpressionEditorPanel editorPanel = new TransformationExpressionEditorPanel();
            editorPanel.setSheetNames(container.getActiveWorkbook().getSheetNames());
            editorPanel.fillFormFields(getValueAt(selectedRow, 0), getValueAt(selectedRow, 1),
                  getValueAt(selectedRow, 2), getValueAt(selectedRow, 3), getValueAt(selectedRow, 4),
                  getValueAt(selectedRow, 5), getValueAt(selectedRow, 6));
            showMappingEditorDialog(editorPanel, selectedRow);
         } catch (CellfieException ex) {
            getApplicationDialogManager().showMessageDialog(container, ex.getMessage());
         }
      }

      private String getValueAt(int row, int column)
      {
         return (String) tableModel.getValueAt(row, column);
      }
   }

   private void showMappingEditorDialog(TransformationExpressionEditorPanel editorPanel, int selectedRow)
   {
      int answer = JOptionPaneEx.showConfirmDialog(
            container, "Transformation Expression Editor", editorPanel, JOptionPane.PLAIN_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION, null);
      switch (answer) {
         case JOptionPane.OK_OPTION :
            MappingExpression userInput = editorPanel.getUserInput();
            updateTableModel(selectedRow, userInput.getSheetName(), userInput.getStartColumn(),
                  userInput.getEndColumn(), userInput.getStartRow(), userInput.getEndRow(),
                  userInput.getExpressionString(), userInput.getComment());
            cmdSaveAs.setEnabled(true);
            setPreferredColumnHeight();
            break;
      }
   }

   class DeleteButtonActionListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         int selectedRow = tblMappingExpression.getSelectedRow();
         try {
            validateSelection(selectedRow);
            int answer = getApplicationDialogManager().showConfirmDialog(
                  container, "Delete", "Do you really want to delete the selected expression?");
            switch (answer) {
               case JOptionPane.YES_OPTION:
                  tableModel.removeRow(selectedRow);
                  cmdEdit.setEnabled(false);
                  cmdDelete.setEnabled(false);
                  break;
            }
         } catch (CellfieException ex) {
            getApplicationDialogManager().showMessageDialog(container, ex.getMessage());
         }
      }
   }

   class OpenMappingAction implements ActionListener
   {
      @Override
      public void actionPerformed(ActionEvent e)
      {
         try {
            File file = getApplicationDialogManager().showOpenFileChooser(
                  container, "Open", "json", "Transformation Expression File (.json)");
            if (file != null) {
               String filePath = file.getAbsolutePath();
               container.loadMappingDocument(filePath);
               cmdSave.setEnabled(true);
               cmdSaveAs.setEnabled(true);
            }
         } catch (Exception ex) {
            getApplicationDialogManager().showErrorMessageDialog(container, "Error opening file: " + ex.getMessage());
            ex.printStackTrace();
         }
      }
   }

   private void validateSelection(int selectedRow) throws CellfieException
   {
      if (selectedRow == -1) {
         throw new CellfieException("No transformation expression was selected");
      }
   }

   class SaveMappingAction implements ActionListener
   {
      @Override
      public void actionPerformed(ActionEvent e)
      {
         try {
            MappingExpressionSetFactory.saveMappingExpressionSetToDocument(container.getMappingFileLocation(),
                  tableModel.getMappingExpressions());
            container.updateMappingExpressionModel(tableModel.getMappingExpressions());
         } catch (IOException ex) {
            getApplicationDialogManager().showErrorMessageDialog(container, "Error saving file: " + ex.getMessage());
         }
      }
   }

   class SaveAsMappingAction implements ActionListener
   {
      @Override
      public void actionPerformed(ActionEvent e)
      {
         try {
            File file = getApplicationDialogManager().showSaveFileChooser(
                  container, "Save As", "json", "Transformationg Expression File (.json)", true);
            if (file != null) {
               String filePath = file.getAbsolutePath();
               String ext = ".json";
               if (!filePath.endsWith(ext)) {
                  filePath = filePath + ext;
               }
               MappingExpressionSetFactory.saveMappingExpressionSetToDocument(filePath,
                     tableModel.getMappingExpressions());
               container.updateMappingExpressionModel(tableModel.getMappingExpressions());
               cmdSave.setEnabled(true);
               updateBorderUI();
            }
         } catch (Exception ex) {
            getApplicationDialogManager().showErrorMessageDialog(container, "Error saving file: " + ex.getMessage());
         }
      }
   }

   /**
    * A helper class for creating mapping editor command buttons.
    */
   class SaveOption implements Comparable<SaveOption>
   {
      private int option;
      private String title;

      public SaveOption(int option, String title)
      {
         this.option = option;
         this.title = title;
      }

      public int get()
      {
         return option;
      }

      @Override
      public String toString()
      {
         return title;
      }

      @Override
      public int compareTo(SaveOption o)
      {
         return option - o.option;
      }
   }
}