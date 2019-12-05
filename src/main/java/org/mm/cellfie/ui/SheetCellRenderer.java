package org.mm.cellfie.ui;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 * Represents the renderer used by the {@code SheetTable} to draw each cell in
 * the target sheet.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class SheetCellRenderer extends JTextArea implements TableCellRenderer {

   private static final long serialVersionUID = 1L;

   @Override
   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
         boolean hasFocus, final int row, int column) {
      setFont(table.getFont());
      if (hasFocus) {
         setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
      } else {
         setBorder(new EmptyBorder(1, 2, 1, 2));
      }
      setText((value == null) ? "" : value.toString());
      return this;
   }
} 