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
package org.augur.sense.web.controllers;

import org.augur.sense.api.model.CameraConfig;
import org.augur.sense.api.model.CameraGroup;
import org.augur.sense.api.model.Zone;
import org.augur.sense.api.model.CameraConfig;
import org.augur.sense.api.model.CameraGroup;
import org.augur.sense.api.model.Zone;
import org.augur.sense.api.resources.Camera;
import org.augur.sense.web.services.ConfigService;
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
    public Response getCameraConfig(@PathParam("cameraId") int cameraId) {
        CameraConfig cameraConfig;
        try {
            cameraConfig = configService.getCameraConfig(cameraId);
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map", e);
            return Response.status(500).build();
        }

        if (cameraConfig == null) {
            return Response.status(404).build();
        }

        return Response.ok(cameraConfig).build();
    }

    @GET
    @Path("/cameraGroups")
    public Response getCameraGroups() {
        try {
            return Response.ok(configService.getCameraGroups()).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining camera configs", e);
            return Response.status(500).build();
        }
    }

    @POST
    @Path("/cameraGroups")
    public Response addCameraGroup(CameraGroup cameraGroup) {
        try {
            configService.addCameraGroup(cameraGroup);
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining camera configs", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/cameraGroups/{cameraGroupId}/zones")
    public Response getCameraGroupZones(@PathParam("cameraGroupId") int cameraGroupId) {
        return Response.ok(configService.getCameraGroupZones(cameraGroupId)).build();
    }

    @POST
    @Path("/cameraGroups/zones")
    public Response addZone(Zone zone) {
        logger.debug("Adding zone {}", zone);
        try {
            return Response.ok(configService.addZone(zone)).build();
        } catch (Exception e) {
            logger.error("Error occurred when adding zone - {}", zone, e);
            return Response.status(500).build();
        }
    }

    @PUT
    @Path("/cameraGroups/zones")
    public Response updateZone(Zone zone) {
        logger.debug("Updating zone - {}", zone);
        configService.updateZone(zone);
        return Response.ok().build();
    }

    @DELETE
    @Path("/cameraGroups/zones/{zoneId}")
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
            return Response.ok(configService.addCameraConfig(cameraConfig)).build();
        } catch (Exception e) {
            logger.error("Error occurred when adding camera config : {}", cameraConfig, e);
            return Response.status(500).build();
        }
    }

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
}
