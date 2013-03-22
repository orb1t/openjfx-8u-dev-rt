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
#ifndef GLASS_WINDOW_H
#define        GLASS_WINDOW_H

#include <gtk/gtk.h>
#include <X11/Xlib.h>

#include <jni.h>
#include <set>
#include <vector>

#include "glass_view.h"

enum WindowFrameType {
    TITLED,
    UNTITLED,
    TRANSPARENT
};

enum WindowType {
    NORMAL,
    UTILITY,
    POPUP
};

enum request_type {
    REQUEST_NONE,
    REQUEST_RESIZABLE,
    REQUEST_NOT_RESIZABLE
};

struct WindowFrameExtents {
    int top;
    int left;
    int bottom;
    int right;
};

enum BoundsType {
    BOUNDSTYPE_CONTENT,
    BOUNDSTYPE_WINDOW
};

struct WindowGeometry {
    WindowGeometry(): final_width(), final_height(),
    refx(), refy(), gravity_x(), gravity_y(), current_width(), current_height(), extents() {}
    // estimate of the final width the window will get after all pending
    // configure requests are processed by the window manager
    struct {
        int value;
        BoundsType type;
    } final_width;

    struct {
        int value;
        BoundsType type;
    } final_height;

    float refx;
    float refy;
    float gravity_x;
    float gravity_y;

    // the last width which was configured or obtained from configure
    // notification
    int current_width;

    // the last height which was configured or obtained from configure
    // notification
    int current_height;

    WindowFrameExtents extents;

};

class WindowContextChild;
class WindowContextTop;

class WindowContext {
public:
    virtual bool isEnabled() = 0;
    virtual bool hasIME() = 0;
    virtual bool filterIME(GdkEvent *) = 0;
    virtual void enableOrResetIME() = 0;
    virtual void disableIME() = 0;
    virtual void paint(void* data, jint width, jint height) = 0;
    virtual WindowFrameExtents get_frame_extents() = 0;

    virtual void enter_fullscreen() = 0;
    virtual void exit_fullscreen() = 0;
    virtual void show_or_hide_children(bool) = 0;
    virtual void set_visible(bool) = 0;
    virtual bool is_visible() = 0;
    virtual void set_bounds(int, int, bool, bool, int, int, int, int) = 0;
    virtual void set_resizable(bool) = 0;
    virtual void request_focus() = 0;
    virtual void set_focusable(bool)= 0;
    virtual bool grab_focus() = 0;
    virtual void ungrab_focus() = 0;
    virtual void set_title(const char*) = 0;
    virtual void set_alpha(double) = 0;
    virtual void set_enabled(bool) = 0;
    virtual void set_minimum_size(int, int) = 0;
    virtual void set_maximum_size(int, int) = 0;
    virtual void set_minimized(bool) = 0;
    virtual void set_maximized(bool) = 0;
    virtual void set_icon(GdkPixbuf*) = 0;
    virtual void restack(bool) = 0;
    virtual void set_cursor(GdkCursor*) = 0;
    virtual void set_modal(bool, WindowContext* parent = NULL) = 0;
    virtual void set_gravity(float, float) = 0;
    virtual void set_level(int) = 0;
    virtual void set_background(float, float, float) = 0;

    virtual void process_property_notify(GdkEventProperty*) = 0;
    virtual void process_configure(GdkEventConfigure*) = 0;
    virtual void process_map() = 0;
    virtual void process_focus(GdkEventFocus*) = 0;
    virtual void process_destroy() = 0;
    virtual void process_delete() = 0;
    virtual void process_expose(GdkEventExpose*) = 0;
    virtual void process_mouse_button(GdkEventButton*) = 0;
    virtual void process_mouse_motion(GdkEventMotion*) = 0;
    virtual void process_mouse_scroll(GdkEventScroll*) = 0;
    virtual void process_mouse_cross(GdkEventCrossing*) = 0;
    virtual void process_key(GdkEventKey*) = 0;
    virtual void process_state(GdkEventWindowState*) = 0;

    virtual void add_child(WindowContextTop* child) = 0;
    virtual void remove_child(WindowContextTop* child) = 0;
    virtual void set_view(jobject) = 0;

    virtual GdkWindow *get_gdk_window() = 0;
    virtual GtkWindow *get_gtk_window() = 0;
    virtual jobject get_jview() = 0;
    virtual jobject get_jwindow() = 0;

    virtual int getEmbeddedX() = 0;
    virtual int getEmbeddedY() = 0;

    virtual ~WindowContext() {}
};

class WindowContextBase: public WindowContext {
    std::set<WindowContextTop*> children;

    struct _XIM{
        XIM im;
        XIC ic;
        bool enabled;
    } xim;

protected:
    jobject jwindow;
    jobject jview;
    GtkWidget* gtk_widget;
    GdkWindow* gdk_window;

    static WindowContext * sm_grab_window;
public:
    bool isEnabled();
    bool hasIME();
    bool filterIME(GdkEvent *);
    void enableOrResetIME();
    void disableIME();
    void paint(void*, jint, jint);
    GdkWindow *get_gdk_window();
    jobject get_jwindow();
    jobject get_jview();

    void add_child(WindowContextTop*);
    void remove_child(WindowContextTop*);
    void show_or_hide_children(bool);
    void reparent_children(WindowContext* parent);
    void set_visible(bool);
    bool is_visible();
    void set_view(jobject);
    bool grab_focus();
    void ungrab_focus();
    void set_cursor(GdkCursor*);
    void set_level(int) {}
    void set_background(float, float, float);

    void process_map() {}
    void process_focus(GdkEventFocus*);
    void process_destroy();
    void process_delete();
    void process_expose(GdkEventExpose*);
    void process_mouse_button(GdkEventButton*);
    void process_mouse_motion(GdkEventMotion*);
    void process_mouse_scroll(GdkEventScroll*);
    void process_mouse_cross(GdkEventCrossing*);
    void process_key(GdkEventKey*);

    int getEmbeddedX() { return 0; }
    int getEmbeddedY() { return 0; }

    ~WindowContextBase();
protected:
    virtual void applyShapeMask(cairo_surface_t*, uint width, uint height) = 0;
private:
    bool im_filter_keypress(GdkEventKey*);
};

class WindowContextPlug: public WindowContextBase {
    WindowContext* parent;
public:
    void set_view(jobject);
    void set_bounds(int, int, bool, bool, int, int, int, int);
    //WindowFrameExtents get_frame_extents() { return WindowFrameExtents{0, 0, 0, 0}; };
    WindowFrameExtents get_frame_extents() { WindowFrameExtents ext = {0, 0, 0, 0}; return ext;}

    void enter_fullscreen() {}
    void exit_fullscreen() {}
    void set_resizable(bool) {}
    void request_focus() {}
    void set_focusable(bool) {}
    void set_title(const char*) {}
    void set_alpha(double) {}
    void set_enabled(bool) {}
    void set_minimum_size(int, int) {}
    void set_maximum_size(int, int) {}
    void set_minimized(bool) {}
    void set_maximized(bool) {}
    void set_icon(GdkPixbuf*) {}
    void restack(bool) {}
    void set_modal(bool, WindowContext*) {}
    void set_gravity(float, float) {}
    void process_property_notify(GdkEventProperty*) {}
    void process_configure(GdkEventConfigure*);
    void process_gtk_configure(GdkEventConfigure*);
    void process_state(GdkEventWindowState*);

    void applyShapeMask(cairo_surface_t*, uint width, uint height) {}
    GtkWindow *get_gtk_window(); // TODO, get window from parent

    WindowContextPlug(jobject, void*);
    GtkWidget* gtk_container;
    std::vector<WindowContextChild *> embedded_children;
private:
    //HACK: remove once set_bounds is implemented correctly
    void window_configure(XWindowChanges *, unsigned int);
    WindowContextPlug(WindowContextPlug&);
    WindowContextPlug& operator= (const WindowContextPlug&);
};

class WindowContextChild: public WindowContextBase {
    WindowContextPlug* parent;
    WindowContextTop* full_screen_window;
    GlassView* view; // not null while in Full Screen
public:
    void process_mouse_button(GdkEventButton*);
    void set_view(jobject);
    void set_bounds(int, int, bool, bool, int, int, int, int);
    //WindowFrameExtents get_frame_extents() { return WindowFrameExtents{0, 0, 0, 0}; };
    WindowFrameExtents get_frame_extents() { WindowFrameExtents ext = {0, 0, 0, 0}; return ext;}

    void enter_fullscreen();
    void exit_fullscreen();
    void set_resizable(bool) {}
    void request_focus() {}
    void set_focusable(bool) {}
    void set_title(const char*) {}
    void set_alpha(double) {}
    void set_enabled(bool) {}
    void set_minimum_size(int, int) {}
    void set_maximum_size(int, int) {}
    void set_minimized(bool) {}
    void set_maximized(bool) {}
    void set_icon(GdkPixbuf*) {}
    void restack(bool);
    void set_modal(bool, WindowContext*) {}
    void set_gravity(float, float) {}
    void process_property_notify(GdkEventProperty*) {}
    void process_configure(GdkEventConfigure*);
    void process_state(GdkEventWindowState*);
    void set_visible(bool visible);

    int getEmbeddedX();
    int getEmbeddedY();

    void applyShapeMask(cairo_surface_t*, uint width, uint height) {}
    GtkWindow *get_gtk_window(); // TODO, get window from parent

    WindowContextChild(jobject, void*, GtkWidget *parent_widget, WindowContextPlug *parent_context);
    ~WindowContextChild();
private:
    WindowContextChild(WindowContextChild&);
    WindowContextChild& operator= (const WindowContextChild&);
};

class WindowContextTop: public WindowContextBase {
    jlong screen;
    WindowFrameType frame_type;
    struct WindowContext *owner;
    WindowGeometry geometry;
    int stale_config_notifications;
    struct _Resizable{// we can't use set/get gtk_window_resizable function
        _Resizable(): request(REQUEST_NONE), value(true), prev(false),
                minw(-1), minh(-1), maxw(-1), maxh(-1){}
        request_type request; //request for future setResizable
        bool value; //actual value of resizable for a window
        bool prev; //former resizable value (used in setEnabled for parents of modal window)
        int minw, minh, maxw, maxh; //minimum and maximum window width/height;
    } resizable;
    struct {
        Pixmap pixmap; //Pixmap for the xshape mask
        cairo_surface_t *surface; //surface for the pixmap
        uint width, height; //bounds of the pixmap
    } xshape;

    bool frame_extents_initialized;
    bool map_received;
public:
    WindowContextTop(jobject, WindowContext*, long, WindowFrameType, WindowType);
    void process_map();
    void process_property_notify(GdkEventProperty*);
    void process_configure(GdkEventConfigure*);
    void process_state(GdkEventWindowState*);
    WindowFrameExtents get_frame_extents();

    void set_minimized(bool);
    void set_maximized(bool);
    void set_bounds(int, int, bool, bool, int, int, int, int);
    void set_resizable(bool);
    void request_focus();
    void set_focusable(bool);
    void set_title(const char*);
    void set_alpha(double);
    void set_enabled(bool);
    void set_minimum_size(int, int);
    void set_maximum_size(int, int);
    void set_icon(GdkPixbuf*);
    void restack(bool);
    void set_modal(bool, WindowContext* parent = NULL);
    void set_gravity(float, float);
    void set_level(int);

    void enter_fullscreen();
    void exit_fullscreen();

    void set_owner(WindowContext*);

    GtkWindow *get_gtk_window();
    void detach_from_java();
    ~WindowContextTop();
protected:
    void applyShapeMask(cairo_surface_t*, uint width, uint height);
private:
    bool get_frame_extents_property(int *, int *, int *, int *);
    void request_frame_extents();
    void initialize_frame_extents();
    void window_configure(XWindowChanges *, unsigned int);
    void update_window_constraints();
    void set_window_resizable(bool, bool);
    WindowContextTop(WindowContextTop&);
    WindowContextTop& operator= (const WindowContextTop&);
};


#endif        /* GLASS_WINDOW_H */
