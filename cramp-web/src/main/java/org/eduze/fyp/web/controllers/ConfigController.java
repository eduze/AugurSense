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
import org.eduze.fyp.api.resources.Camera;
import org.eduze.fyp.api.resources.CameraConfig;
import org.eduze.fyp.web.resources.MapConfiguration;
import org.eduze.fyp.web.resources.Status;
import org.eduze.fyp.web.services.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/config")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    private ConfigService configService;

    @GET
    @Path("/cameraId")
    public Response getCameraId() {
        try {
            Camera camera = configService.getCameraId();
            return Response.status(200).entity(camera).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining next camera ID", e);
            return Response.status(500).build();
        }
    }

    @POST
    @Path("/zone")
    public Response addZone(Zone zone) {
        logger.debug("Adding zone {}", zone);
        try {
            return Response.status(200).entity(configService.addZone(zone)).build();
        } catch (Exception e) {
            logger.error("Error occurred when adding zone - {}", zone, e);
            return Response.status(500).build();
        }
    }

    @PUT
    @Path("/zone")
    public Response updateZone(Zone zone) {
        logger.debug("Updating zone - {}", zone);
        try {
            configService.updateZone(zone);
            return Response.status(200).build();
        } catch (Exception e) {
            logger.error("Error occurred when updating zone - {}", zone, e);
            return Response.status(500).build();
        }
    }

    @DELETE
    @Path("/zone/{zoneId}")
    public Response deleteZone(@PathParam("zoneId") int zoneId) {
        logger.debug("Deleting zone {}", zoneId);
        try {
            configService.deleteZone(zoneId);
            return Response.status(200).build();
        } catch (Exception e) {
            logger.error("Error occurred when updating zone - {}", zoneId, e);
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/zones")
    public Response getZones() {
        try {
            List<Zone> zonesList = configService.getZones();
            logger.info("{} zones", zonesList);
            return Response.ok().entity(zonesList).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining zones", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/getMap")
    public Response getMap() {
        try {
            Map<String, byte[]> map = configService.getMap();
            return Response.ok(map).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/{cameraId}")
    public Response getMap(@PathParam("cameraId") int cameraId) {
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

    @GET
    @Path("/cameraConfigs")
    public Response getCameraConfigs() {
        try {
            Map<Integer, CameraConfig> cameraConfigs = configService.getCameraConfigs();
            return Response.ok(cameraConfigs).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining camera configs", e);
            return Response.status(500).build();
        }
    }

    @POST
    @Path("/cameraConfig")
    public Response postCameraConfig(CameraConfig cameraConfig) {
        try {
            configService.addCameraConfig(cameraConfig);
        } catch (Exception e) {
            logger.error("Error occurred when adding camera config : {}", cameraConfig, e);
            return Response.status(500).build();
        }
        return Response.status(200).build();
    }

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
}
