/*
 * Copyright 2017 Eduze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.eduze.fyp.restapi.services.realtime;

import org.eduze.fyp.core.api.MapCollector;
import org.eduze.fyp.core.api.resources.LocalMap;
import org.eduze.fyp.restapi.resources.FrameInfo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service class which is handling the real time data transfers
 *
 * @author Imesha Sudasingha
 */
public class RealtimeService {

    @Autowired
    private MapCollector mapCollector;

    public void addFrameInfo(FrameInfo frameInfo) {
        LocalMap localMap = new LocalMap();
        localMap.setCameraId(frameInfo.getCamera().getId());
        localMap.setTimestamp(frameInfo.getTimestamp());
        localMap.setPoints(frameInfo.getCoordinates());
        mapCollector.addPoints(localMap);
    }
}
