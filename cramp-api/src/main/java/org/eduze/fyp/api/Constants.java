/*
 * Copyright 2018 Eduze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package org.eduze.fyp.api;

public class Constants {

    private Constants() { }

    /** Interval in which we ask from cameras to process a new frame */
    public static final long FRAME_PROCESSING_INTERVAL = 3000;
    /** The suffix in the URL to which we notify to start processing a new frame */
    public static final String CAMERA_NOTIFICATION_PATH_PATTERN = "/getMap/%d";
    public static final String CAMERA_NOTIFICATION_PATH = "/getMap/";

    /** Threshold to be used to determine whether two points are the same */
    public static final double DISTANCE_THRESHOLD = 100;

    public static final double DISTANCE_CONFLICT_THRESHOLD = 15;

    /** Global map refresh interval */
    public static final long MAP_REFRESH_INTERVAL = 30000;
    public static final long MAP_REFRESH_THRESHOLD = 3000;

    /** Time out for notifying cameras by camera coordinator */
    public static final int CAMERA_NOTIFY_TIMEOUT = 5000;

    /** Width and hight of the standardized camera views */
    public static final int CAMERA_VIEW_WIDTH = 500;
    public static final int CAMERA_VIEW_HEIGHT = 500;
    /** Width and height of map images */
    public static final int MAP_IMAGE_WIDTH = 500;
    public static final int MAP_IMAGE_HEIGHT = 500;

    /** Represents the zone -> world */
    public static final String ZONE_NAME_WORLD = "World";

    public static class Properties {
        public static final String FLOOR_MAP_IMAGE = "org.eduze.fyp.config.map";
        public static final String STARTING_MODE = "org.eduze.fyp.mode";
    }
}
