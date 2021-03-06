/*
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javafx.scene.control;

import java.time.LocalDate;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.Scene;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.javafx.scene.control.infrastructure.ControlTestUtils;

import static org.junit.Assert.*;

public class DateCellTest {
    private DateCell cell;
    private Class type;
    private final LocalDate today = LocalDate.now();
    private final LocalDate tomorrow = today.plusDays(1);

    @Before public void setup() throws Exception {
        cell = new DateCell();
    }

    /*********************************************************************
     * Tests for the constructors                                        *
     ********************************************************************/

    @Test public void cellsShouldBeNonFocusableByDefault() {
        // Cells are non-focusable because we manually position the focus from
        // the ListView / TableView / TreeView skin, rather than making them
        // focus traversable and having directional focus work etc. We must
        // keep the focus on the actual table component UNLESS we are
        // editing, in which case it is set on the cell itself.
        assertFalse(cell.isFocusTraversable());
        assertFalse(cell.isFocused());
    }

    @Test public void styleClassShouldDefaultTo_cell_and_datecell() {
        ControlTestUtils.assertStyleClassContains(cell, "cell");
        ControlTestUtils.assertStyleClassContains(cell, "date-cell");
    }

    @Test public void pseudoClassStateShouldBe_empty_ByDefault() {
        ControlTestUtils.assertPseudoClassExists(cell, "empty");
        ControlTestUtils.assertPseudoClassDoesNotExist(cell, "filled");
        ControlTestUtils.assertPseudoClassDoesNotExist(cell, "selected");
        ControlTestUtils.assertPseudoClassDoesNotExist(cell, "focused");
    }

    /*********************************************************************
     * Tests for updating the item, selection, editable                  *
     ********************************************************************/

    @Test public void updatingItemAffectsBothItemAndEmpty() {
        cell.updateItem(today, false);
        assertEquals(today, cell.getItem());
        assertFalse(cell.isEmpty());
    }

    @Test public void updatingItemWithEmptyTrueAndItemNotNullIsWeirdButOK() {
        cell.updateItem(today, true);
        assertEquals(today, cell.getItem());
        assertTrue(cell.isEmpty());
    }

    @Test public void updatingItemWithEmptyFalseAndNullItemIsOK() {
        cell.updateItem(null, false);
        assertNull(cell.getItem());
        assertFalse(cell.isEmpty());
    }

    @Test public void selectingANonEmptyCellIsOK() {
        cell.updateItem(today, false);
        cell.updateSelected(true);
        assertTrue(cell.isSelected());
    }

    @Test public void unSelectingANonEmptyCellIsOK() {
        cell.updateItem(today, false);
        cell.updateSelected(true);
        cell.updateSelected(false);
        assertFalse(cell.isSelected());
    }

    public void selectingAnEmptyCellResultsInNoChange() {
        cell.updateItem(null, true);
        cell.updateSelected(true);
        assertFalse(cell.isSelected());
    }

    @Test public void updatingASelectedCellToBeEmptyClearsSelection() {
        cell.updateItem(today, false);
        cell.updateSelected(true);
        cell.updateItem(null, true);
        assertFalse(cell.isSelected());
    }

    @Test public void updatingItemWithEmptyTrueResultsIn_empty_pseudoClassAndNot_filled() {
        cell.updateItem(null, true);
        ControlTestUtils.assertPseudoClassExists(cell, "empty");
        ControlTestUtils.assertPseudoClassDoesNotExist(cell, "filled");
    }

    @Test public void updatingItemWithEmptyFalseResultsIn_filled_pseudoClassAndNot_empty() {
        cell.updateItem(null, false);
        ControlTestUtils.assertPseudoClassExists(cell, "filled");
        ControlTestUtils.assertPseudoClassDoesNotExist(cell, "empty");
    }

    @Test public void updatingSelectedToTrueResultsIn_selected_pseudoClass() {
        cell.updateItem(today, false);
        cell.updateSelected(true);
        ControlTestUtils.assertPseudoClassExists(cell, "selected");
    }

    @Test public void updatingSelectedToFalseResultsInNo_selected_pseudoClass() {
        cell.updateItem(today, false);
        cell.updateSelected(true);
        cell.updateSelected(false);
        ControlTestUtils.assertPseudoClassDoesNotExist(cell, "selected");
    }

    @Test public void editableIsTrueByDefault() {
        assertTrue(cell.isEditable());
        assertTrue(cell.editableProperty().get());
    }

    @Test public void editableCanBeSet() {
        cell.setEditable(false);
        assertFalse(cell.isEditable());
    }

    @Test public void editableSetToNonDefaultValueIsReflectedInModel() {
        cell.setEditable(false);
        assertFalse(cell.editableProperty().get());
    }

    @Test public void editableCanBeCleared() {
        cell.setEditable(false);
        cell.setEditable(true);
        assertTrue(cell.isEditable());
    }

    @Test public void editableCanBeBound() {
        BooleanProperty other = new SimpleBooleanProperty(false);
        cell.editableProperty().bind(other);
        assertFalse(cell.isEditable());
        other.set(true);
        assertTrue(cell.isEditable());
    }

    @Ignore("impl_cssSet API removed")
    @Test public void cannotSpecifyEditableViaCSS() {
//        cell.impl_cssSet("-fx-editable", false);
        assertTrue(cell.isEditable());
    }

    /*********************************************************************
     * Tests for editing                                                 *
     ********************************************************************/

    public void editingAnEmptyCellResultsInNoChange() {
        cell.startEdit();
        assertFalse(cell.isEditing());
    }

    public void editingAnEmptyCellResultsInNoChange2() {
        cell.updateItem(null, false);
        cell.updateItem(null, true);
        cell.startEdit();
        assertFalse(cell.isEditing());
    }

    @Test public void updatingACellBeingEditedDoesNotResultInACancelOfEdit() {
        cell.updateItem(today, false);
        cell.startEdit();
        cell.updateItem(tomorrow, false);
        assertTrue(cell.isEditing());
    }

    @Test public void updatingACellBeingEditedResultsInFirstACancelOfEdit2() {
        cell.updateItem(today, false);
        cell.startEdit();
        cell.updateItem(null, true);
        assertTrue(cell.isEditing());
    }

    @Test public void startEditWhenEditableIsTrue() {
        cell.updateItem(today, false);
        cell.startEdit();
        assertTrue(cell.isEditing());
    }

    @Test public void startEditWhenEditableIsFalse() {
        cell.updateItem(today, false);
        cell.setEditable(false);
        cell.startEdit();
        assertFalse(cell.isEditing());
    }

    @Test public void startEditWhileAlreadyEditingIsIgnored() {
        cell.updateItem(today, false);
        cell.startEdit();
        cell.startEdit();
        assertTrue(cell.isEditing());
    }

    @Test public void cancelEditWhenEditableIsTrue() {
        cell.updateItem(today, false);
        cell.startEdit();
        cell.cancelEdit();
        assertFalse(cell.isEditing());
    }

    @Test public void cancelEditWhenEditableIsFalse() {
        cell.updateItem(today, false);
        cell.setEditable(false);
        cell.startEdit();
        cell.cancelEdit();
        assertFalse(cell.isEditing());
    }

    @Test public void commitEditWhenEditableIsTrue() {
        cell.updateItem(today, false);
        cell.startEdit();
        cell.commitEdit(tomorrow);
        assertFalse(cell.isEditing());
    }

    @Test public void commitEditWhenEditableIsFalse() {
        cell.updateItem(today, false);
        cell.setEditable(false);
        cell.startEdit();
        cell.commitEdit(tomorrow);
        assertFalse(cell.isEditing());
    }

    @Test public void getBeanIsCorrectForItemProperty() {
        assertSame(cell, cell.itemProperty().getBean());
    }

    @Test public void getNameIsCorrectForItemProperty() {
        assertEquals("item", cell.itemProperty().getName());
    }

    @Test public void getBeanIsCorrectForEmptyProperty() {
        assertSame(cell, cell.emptyProperty().getBean());
    }

    @Test public void getNameIsCorrectForEmptyProperty() {
        assertEquals("empty", cell.emptyProperty().getName());
    }

    @Test public void getBeanIsCorrectForSelectedProperty() {
        assertSame(cell, cell.selectedProperty().getBean());
    }

    @Test public void getNameIsCorrectForSelectedProperty() {
        assertEquals("selected", cell.selectedProperty().getName());
    }

    @Test public void getBeanIsCorrectForEditingProperty() {
        assertSame(cell, cell.editingProperty().getBean());
    }

    @Test public void getNameIsCorrectForEditingProperty() {
        assertEquals("editing", cell.editingProperty().getName());
    }

    @Test public void getBeanIsCorrectForEditableProperty() {
        assertSame(cell, cell.editableProperty().getBean());
    }

    @Test public void getNameIsCorrectForEditableProperty() {
        assertEquals("editable", cell.editableProperty().getName());
    }

    // When the cell was focused, but is no longer focused, we should cancel editing
    // Check for focused pseudoClass state change?
    @Ignore(value = "I'm not sure how to test this, since I need a scene & such to move focus around")
    @Test public void loseFocusWhileEditing() {
        Button other = new Button();
        Group root = new Group(other, cell);
        Scene scene = new Scene(root);

        cell.updateItem(today, false);
        cell.startEdit();
        cell.requestFocus();

        other.requestFocus();

        assertFalse(cell.isEditing());
    }
}
