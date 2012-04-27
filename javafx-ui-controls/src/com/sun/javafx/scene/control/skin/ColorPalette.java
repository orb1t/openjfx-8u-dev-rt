/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.javafx.scene.control.skin;

import com.sun.javafx.scene.control.*;
import com.sun.javafx.css.StyleManager;
import java.util.List;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Window;


public class ColorPalette extends Region {
    
    private static final int ARROW_SIZE = 10;
    private static final int RADIUS = 8;
    private static final int GAP = 15;
    private static final int SQUARE_SIZE = 15;
    private static final int NUM_OF_COLUMNS = 12;
    private static final int NUM_OF_ROWS = 10;
    
    private boolean customColorAdded = false;
    ColorPickerGrid cpg;
    Path path;
    ColorPicker colorPicker;
    GridPane customColorGrid = new GridPane();
//    Button addColorButton = new Button("Add Color...");
    Hyperlink customColorLink = new Hyperlink("Custom Color..");
    Separator separator = new Separator();
    Window owner;
    CustomColorDialog customColorDialog = null;
    int customColumnIndex = 0, customRowIndex = 0;
    private final List<ColorSquare> customSquares = FXCollections.observableArrayList();
 
    private double x;
    private double y;
    private PopupControl popupControl;
    private ColorSquare focusedSquare;
    
    public ColorPalette(Color initPaint, final ColorPicker colorPicker) {
        getStyleClass().add("color-panel");
        this.colorPicker = colorPicker;
        cpg = new ColorPickerGrid(initPaint);
        customColorDialog = new CustomColorDialog(owner, colorPicker.valueProperty());
        customColorLink.setPrefWidth(cpg.prefWidth(-1));
        customColorLink.setAlignment(Pos.CENTER);
        customColorLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (popupControl != null) popupControl.setAutoHide(false);
                customColorDialog.show(x, y);
                if (popupControl != null) popupControl.setAutoHide(true);
            }
        });
        customColorDialog.customColorProperty.addListener(new ChangeListener<Color>() {
            @Override public void changed(ObservableValue<? extends Color> ov, Color t, Color t1) {
                if (customColorDialog.saveCustomColor) {
                    if (!customColorAdded) {
                        customColorAdded = true;
                        customColorGrid.setGridLinesVisible(true);
                    }
                    ColorSquare cs = new ColorSquare(customColorDialog.customColorProperty.get());
                    customSquares.add(cs);
                    customColorGrid.add(cs, customColumnIndex, customRowIndex);
                    if (customColumnIndex == 0) { // new row - add empty squares
                        for (int i = 0; i < NUM_OF_COLUMNS - 1; i++) {
                            ColorSquare emptySquare = new ColorSquare(null);
                            customSquares.add(emptySquare);
                            customColorGrid.add(emptySquare, i+1, customRowIndex);
                        }
                    }
                    customColumnIndex++;
                    if (customColumnIndex == NUM_OF_COLUMNS) {
                        customColumnIndex = 0;
                        customRowIndex++;
                    } 
                    requestLayout();
                }
                colorPicker.setValue(customColorDialog.customColorProperty.get());
            }
        });
        
        
        // create popup path for main shape
        path = new Path();
//        path.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.web("#313131")), new Stop(0.5, Color.web("#5f5f5f")), new Stop(1, Color.web("#313131"))));
        path.setFill(Color.LIGHTGRAY);
        path.setStroke(null);
        path.setEffect(new DropShadow(15, 0, 1, Color.gray(0, 0.6)));
        path.setCache(true);
        initNavigation();
        getChildren().addAll(path, cpg, customColorGrid, separator, customColorLink);
    }

    private void initNavigation() {
        setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override public void handle(KeyEvent ke) {
                switch (ke.getCode()) {
                    case LEFT:
                        processLeftKey(ke);
                        break;
                    case RIGHT:
                        processRightKey(ke);
                        break;
                    case UP:
                        processUpKey(ke);
                        break;
                    case DOWN:
                        processDownKey(ke);
                }
            }
        });
    }
    
    private void processLeftKey(KeyEvent ke) {
        int index;
        for (index = (NUM_OF_ROWS*NUM_OF_COLUMNS)-1; index >= 0; index--)  {
            ColorSquare cs = cpg.getSquares().get(index);
            if (cs == focusedSquare) {
                cs.setHoverValue(false);
                ColorSquare prevSquare = cpg.getSquares().get((index != 0) ? 
                                    (index-1) : (NUM_OF_ROWS*NUM_OF_COLUMNS)-1);
                prevSquare.setHoverValue(true);
                focusedSquare = prevSquare;
                System.out.println("cs isFocused = "+prevSquare.isFocused());
                break;
            } 
        }
        if (index == -1) {
            ColorSquare cs = cpg.getSquares().get((NUM_OF_ROWS*NUM_OF_COLUMNS)-1);
            cs.setHoverValue(true);
            focusedSquare = cs;
        }
    }
    
     private void processUpKey(KeyEvent ke) {
        int index;
        for (index = (NUM_OF_ROWS*NUM_OF_COLUMNS)-1; index >= 0; index--)  {
            ColorSquare cs = cpg.getSquares().get(index);
            if (cs == focusedSquare) {
                cs.setHoverValue(false);
                ColorSquare prevSquare = cpg.getSquares().get((index-12 >= 0)? 
                        (index-12) : ((NUM_OF_ROWS-1)*NUM_OF_COLUMNS)+index);
                prevSquare.setHoverValue(true);
                focusedSquare = prevSquare;
                break;
            } 
        }
        if (index == -1) {
            ColorSquare cs = cpg.getSquares().get((NUM_OF_ROWS*NUM_OF_COLUMNS)-1);
            cs.setHoverValue(true);
            focusedSquare = cs;
        }
    }
     
    private void processRightKey(KeyEvent ke) {
        int index;
        for (index = 0; index < (NUM_OF_ROWS*NUM_OF_COLUMNS); index++)  {
            ColorSquare cs = cpg.getSquares().get(index);
            if (cs == focusedSquare) {
                cs.setHoverValue(false);
                ColorSquare prevSquare = cpg.getSquares().get(
                        (index != (NUM_OF_ROWS*NUM_OF_COLUMNS)-1) ? (index+1) : 0);
                prevSquare.setHoverValue(true);
                System.out.println("cs isFocused = "+prevSquare.isFocused());
                focusedSquare = prevSquare;
                break;
            } 
        }
        if (index == (NUM_OF_ROWS*NUM_OF_COLUMNS)) {
            ColorSquare cs = cpg.getSquares().get(0);
            cs.setHoverValue(true);
            focusedSquare = cs;
        }
    }
    
     private void processDownKey(KeyEvent ke) {
        int index;
        for (index = 0; index < (NUM_OF_ROWS*NUM_OF_COLUMNS); index++)  {
            ColorSquare cs = cpg.getSquares().get(index);
            if (cs == focusedSquare) {
                cs.setHoverValue(false);
                ColorSquare prevSquare = cpg.getSquares().get((index+12 < NUM_OF_ROWS*NUM_OF_COLUMNS)? 
                        (index+12) : index-((NUM_OF_ROWS-1)*NUM_OF_COLUMNS));
                prevSquare.setHoverValue(true);
                focusedSquare = prevSquare;
                break;
            } 
        }
        if (index == (NUM_OF_ROWS*NUM_OF_COLUMNS)) {
            ColorSquare cs = cpg.getSquares().get(0);
            cs.setHoverValue(true);
            focusedSquare = cs;
        }
    }
    
    public void setPopupControl(PopupControl pc) {
        this.popupControl = pc;
    }
    
    public void setDialogLocation(double xValue, double yValue) {
        x = xValue;
        y = yValue;
    }
    
    public ColorPickerGrid getColorGrid() {
        return cpg;
    }
    public void setOwner(ColorPicker colorPicker) {
        this.colorPicker = colorPicker;
        owner = colorPicker.getScene().getWindow();
        cpg.owner = colorPicker.getScene().getWindow();
    }
    
    @Override protected void layoutChildren() {
        double paddingX = getInsets().getLeft();
        double paddingY = getInsets().getTop();
//        double popupWidth = cpg.prefWidth(-1) + paddingX+getInsets().getRight();
//        double popupHeight = cpg.prefHeight(-1) + getInsets().getTop() + getInsets().getBottom();
        double popupWidth = getWidth();
        double popupHeight = getHeight();
        double arrowX = paddingX+RADIUS;
        path.getElements().addAll(
                new MoveTo(paddingX, getInsets().getTop() + ARROW_SIZE + RADIUS), 
                new ArcTo(RADIUS, RADIUS, 90, paddingX + RADIUS, paddingX + ARROW_SIZE, false, true), 
                new LineTo(paddingX + arrowX - (ARROW_SIZE * 0.8), paddingX + ARROW_SIZE), 
                new LineTo(paddingX + arrowX, paddingX), 
                new LineTo(paddingX + arrowX + (ARROW_SIZE * 0.8), paddingX + ARROW_SIZE), 
                new LineTo(paddingX + popupWidth - RADIUS, paddingX + ARROW_SIZE), 
                new ArcTo(RADIUS, RADIUS, 90, paddingX + popupWidth, paddingX + ARROW_SIZE + RADIUS, false, true), 
                new LineTo(paddingX + popupWidth, paddingX + ARROW_SIZE + popupHeight - RADIUS), 
                new ArcTo(RADIUS, RADIUS, 90, paddingX + popupWidth - RADIUS, paddingX + ARROW_SIZE + popupHeight, false, true), 
                new LineTo(paddingX + RADIUS, paddingX + ARROW_SIZE + popupHeight), 
                new ArcTo(RADIUS, RADIUS, 90, paddingX, paddingX + ARROW_SIZE + popupHeight - RADIUS, false, true), 
                new ClosePath());
        double x = paddingX*2;
        double y = 2*getInsets().getTop()+ARROW_SIZE;
        cpg.relocate(x, y);
        y = y+cpg.prefHeight(-1)+GAP;
        if (customColorAdded) {
            customColorGrid.relocate(x, y);
            y = y+customColorGrid.prefHeight(-1)+GAP;
        }
        separator.resizeRelocate(x, y, cpg.prefWidth(-1), separator.prefHeight(-1));
        y = y+separator.prefHeight(-1)+GAP;
        customColorLink.resizeRelocate(x, y, cpg.prefWidth(-1), customColorLink.prefHeight(-1));
    }
    
    @Override protected double computePrefWidth(double height) {
        return getInsets().getLeft() + cpg.prefWidth(-1) + getInsets().getRight();
    }
    
    @Override protected double computePrefHeight(double width) {
        double totalHeight = cpg.prefHeight(-1) + GAP + customColorGrid.prefHeight(-1) +
                GAP + separator.prefHeight(-1) + GAP + customColorLink.prefHeight(-1);
        return getInsets().getTop() + totalHeight + getInsets().getBottom();
    }
   
    public boolean isAddColorDialogShowing() {
        return customColorDialog.isVisible();
    }

    class ColorSquare extends StackPane {
        Rectangle rectangle;
        
        public ColorSquare(Color color) {
            // Add style class to handle selected color square
            getStyleClass().add("color-square");
            rectangle = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
            setFocusTraversable(true);
            rectangle.setFill(color == null  ? Color.WHITE : color);
//            setFill(color);
            rectangle.setSmooth(false);
//            Utils.setBlocksMouse(this, true);
            
            rectangle.setStrokeType(StrokeType.INSIDE);
            
            rectangle.getStyleClass().add("color-rect");
            addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent event) {
                    if (event.getClickCount() == 1) {
                        if (rectangle.getFill() != null) {
                            if (rectangle.getFill() instanceof Color) {
                                colorPicker.setValue((Color) rectangle.getFill());
                                colorPicker.fireEvent(new ActionEvent());
                            }
                            event.consume();
                        }
                        colorPicker.hide();
                    }
                }
            });
            addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent event) {
                    if (focusedSquare != null && focusedSquare != ColorSquare.this) {
                        focusedSquare.setHoverValue(false);
                    }
                    focusedSquare = ColorSquare.this;
                }
             });
            getChildren().add(rectangle);
        }
        
        public void setHoverValue(Boolean value) {
            setHover(value);
        }
        
        private ReadOnlyBooleanWrapper selected;
        protected final void setSelected(boolean value) {
            selectedPropertyImpl().set(value);
        }
        public final boolean isSelected() { return selected == null ? false : selected.get(); }

        public ReadOnlyBooleanProperty selectedProperty() {
            return selectedPropertyImpl().getReadOnlyProperty();
        }
        private ReadOnlyBooleanWrapper selectedPropertyImpl() {
            if (selected == null) {
                selected = new ReadOnlyBooleanWrapper() {
                    @Override protected void invalidated() {
                        impl_pseudoClassStateChanged("selected");
                    }

                    @Override
                    public Object getBean() {
                        return ColorSquare.this;
                    }

                    @Override
                    public String getName() {
                        return "selected";
                    }
                };
            }
            return selected;
        }
        private final long SELECTED_PSEUDOCLASS_STATE =
            StyleManager.getInstance().getPseudoclassMask("selected");

        public long impl_getPseudoClassState() {
            return super.impl_getPseudoClassState() | (isSelected() ? SELECTED_PSEUDOCLASS_STATE : 0);
        }
        
    }

    public void updateSelection(Color color) {
        for (ColorSquare cs : cpg.getSquares()) {
             cs.setSelected(cs.rectangle.getFill().equals(color));
        }
    }
    
    class ColorPickerGrid extends GridPane {

        private final List<ColorSquare> squares;
        Window owner;

        public ColorPickerGrid(Color initPaint) {
            getStyleClass().add("color-picker-grid");
            setId("ColorCustomizerColorGrid");
//            setGridLinesVisible(true);
            int columnIndex = 0, rowIndex = 0;

            squares = FXCollections.observableArrayList();
            int numColors = rawValues.length / 3;
            Color[] colors = new Color[numColors];
            for (int i = 0; i < numColors; i++) {
                colors[i] = new Color(rawValues[(i * 3)] / 255,
                        rawValues[(i * 3) + 1] / 255, rawValues[(i * 3) + 2] / 255,
                        1.0);
                ColorSquare cs = new ColorSquare(colors[i]);
                squares.add(cs);
            }

            for (ColorSquare square : squares) {
                add(square, columnIndex, rowIndex);
                columnIndex++;
                if (columnIndex == NUM_OF_COLUMNS) {
                    columnIndex = 0;
                    rowIndex++;
                }
            }
        }
        
        public List<ColorSquare> getSquares() {
            return squares;
        }

        double[] rawValues = {
            255, 255, 255, // first row
            242, 242, 242,
            230, 230, 230,
            204, 204, 204,
            179, 179, 179,
            153, 153, 153,
            128, 128, 128,
            102, 102, 102,
            77, 77, 77,
            51, 51, 51,
            26, 26, 26,
            0, 0, 0,
            0, 51, 51, // second row
            0, 26, 128,
            26, 0, 104,
            51, 0, 51,
            77, 0, 26,
            153, 0, 0,
            153, 51, 0,
            153, 77, 0,
            153, 102, 0,
            153, 153, 0,
            102, 102, 0,
            0, 51, 0,
            26, 77, 77, // third row
            26, 51, 153,
            51, 26, 128,
            77, 26, 77,
            102, 26, 51,
            179, 26, 26,
            179, 77, 26,
            179, 102, 26,
            179, 128, 26,
            179, 179, 26,
            128, 128, 26,
            26, 77, 26,
            51, 102, 102, // fourth row
            51, 77, 179,
            77, 51, 153,
            102, 51, 102,
            128, 51, 77,
            204, 51, 51,
            204, 102, 51,
            204, 128, 51,
            204, 153, 51,
            204, 204, 51,
            153, 153, 51,
            51, 102, 51,
            77, 128, 128, // fifth row
            77, 102, 204,
            102, 77, 179,
            128, 77, 128,
            153, 77, 102,
            230, 77, 77,
            230, 128, 77,
            230, 153, 77,
            230, 179, 77,
            230, 230, 77,
            179, 179, 77,
            77, 128, 77,
            102, 153, 153, // sixth row
            102, 128, 230,
            128, 102, 204,
            153, 102, 153,
            179, 102, 128,
            255, 102, 102,
            255, 153, 102,
            255, 179, 102,
            255, 204, 102,
            255, 255, 77,
            204, 204, 102,
            102, 153, 102,
            128, 179, 179, // seventh row
            128, 153, 255,
            153, 128, 230,
            179, 128, 179,
            204, 128, 153,
            255, 128, 128,
            255, 153, 128,
            255, 204, 128,
            255, 230, 102,
            255, 255, 102,
            230, 230, 128,
            128, 179, 128,
            153, 204, 204, // eigth row
            153, 179, 255,
            179, 153, 255,
            204, 153, 204,
            230, 153, 179,
            255, 153, 153,
            255, 179, 128,
            255, 204, 153,
            255, 230, 128,
            255, 255, 128,
            230, 230, 153,
            153, 204, 153,
            179, 230, 230, // ninth row
            179, 204, 255,
            204, 179, 255,
            230, 179, 230,
            230, 179, 204,
            255, 179, 179,
            255, 179, 153,
            255, 230, 179,
            255, 230, 153,
            255, 255, 153,
            230, 230, 179,
            179, 230, 179,
            204, 255, 255, // tenth row
            204, 230, 255,
            230, 204, 255,
            255, 204, 255,
            255, 204, 230,
            255, 204, 204,
            255, 204, 179,
            255, 230, 204,
            255, 255, 179,
            255, 255, 204,
            230, 230, 204,
            204, 255, 204
        };
        
        @Override protected double computePrefWidth(double height) {
            return (SQUARE_SIZE + 1)*12;
        }

        @Override protected double computePrefHeight(double width) {
            return (SQUARE_SIZE + 1)*10;
        }
        
    }
    
}
