/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
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

package hello;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import alertdialog.Alert;

public class HelloAlert extends Application {

    @Override public void start(final Stage stage) {
        stage.setTitle("Alert Test");

        Group root = new Group();
        Scene scene = new Scene(root, 600, 450);
        scene.setFill(Color.ANTIQUEWHITE);

        final Rectangle rect = new Rectangle();
        rect.setX(100);
        rect.setY(40);
        rect.setWidth(100);
        rect.setHeight(50);
        rect.setFill(Color.RED);
        root.getChildren().add(rect);
        stage.setScene(scene);
        stage.show();

        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        final KeyValue kv = new KeyValue(rect.xProperty(), 200);
        final KeyFrame kf = new KeyFrame(Duration.millis(4000), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();

        final Button button1 = new Button();
        button1.setText("Toggle color");
        button1.setLayoutX(25);
        button1.setLayoutY(40);
        button1.setOnAction(e -> {
            boolean answer = Alert.confirm(stage, "Verify Change",
                    "Really toggle the color?");
            if (answer) {
                Color newColor = Color.RED.equals(rect.getFill())
                        ? Color.GREEN : Color.RED;
                rect.setFill(newColor);
            } else {
                System.err.println("Color change canceled");
            }
        });

        root.getChildren().add(button1);

        final Button button2 = new Button();
        button2.setText("Question");
        button2.setLayoutX(25);
        button2.setLayoutY(80);
        button2.setOnAction(e -> {
            boolean answer = Alert.question(stage, "SF Giants",
                    "How about those Giants?");
            if (answer) {
                System.err.println("Good answer");
            } else {
                System.err.println("What do you mean 'NO' ???");
            }
        });

        root.getChildren().add(button2);

        final Button button3 = new Button();
        button3.setText("Dialog");
        button3.setLayoutX(25);
        button3.setLayoutY(120);
        button3.setOnAction(e -> {
            Alert.inform(stage, "Hi, I'll be your modal dialog today");
            System.err.println("Continue");
        });

        root.getChildren().add(button3);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}