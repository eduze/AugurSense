/*
 * Copyright 2017 Eduze
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
package org.eduze.fyp.web.controllers;

import org.eduze.fyp.api.model.Zone;
import org.eduze.fyp.web.resources.Camera;
import org.eduze.fyp.web.resources.CameraConfig;
import org.eduze.fyp.web.resources.MapConfiguration;
import org.eduze.fyp.web.resources.Status;
import org.eduze.fyp.web.services.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.ws.rs.core.Response;
import java.util.List;

@RequestMapping("/config")
public class ConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    private ConfigService configService;

    @GetMapping("/cameraId")
    public Response getCameraId() {
        try {
            Camera camera = configService.getCameraId();
            return Response.status(200).entity(camera).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining next camera ID", e);
            return Response.status(500).build();
        }
    }

    @GetMapping("/zones")
    public Response getZones() {
        try {
            List<Zone> zonesList = configService.getZones();
            logger.info("{} zones", zonesList);
            return Response.status(200).entity(zonesList).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining zones", e);
            return Response.status(500).build();
        }
    }

    @GetMapping("/getMap")
    public Response getMap() {
        try {
            return Response.ok(configService.getMap()).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }
    }

    @GetMapping("/{cameraId}")
    public Response getMap(@PathVariable("cameraId") int cameraId) {
        MapConfiguration mapConfiguration;
        try {
            mapConfiguration = configService.getMap(cameraId);
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map", e);
            return Response.status(500).build();
        }

        if (mapConfiguration == null) {
            return Response.status(200).entity(new Status(false)).build();
        }

        return Response.status(200).entity(mapConfiguration).build();
    }

    @GetMapping("/views")
    public Response getViews() {
        try {
            return Response.ok(configService.getCameraViews()).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining next camera ID", e);
            return Response.status(500).build();
        }
    }


    @PostMapping("/cameraConfig")
    public Response postCameraView(CameraConfig cameraConfig) {
        try {
            configService.configureCameraView(cameraConfig);
        } catch (Exception e) {
            logger.error("Error occurred when configuring camera view : {}", cameraConfig, e);
            return Response.status(500).build();
        }
        return Response.status(200).build();
    }

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
}
