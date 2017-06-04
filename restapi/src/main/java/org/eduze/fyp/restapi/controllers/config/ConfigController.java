/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.restapi.controllers.config;

import org.eduze.fyp.restapi.resources.Camera;
import org.eduze.fyp.restapi.resources.CameraView;
import org.eduze.fyp.restapi.resources.MapConfiguration;
import org.eduze.fyp.restapi.services.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/config")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    private ConfigService configService = new ConfigService();

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
    public Response getMap() {
        try {
            MapConfiguration mapConfiguration = configService.getMap();
            return Response.status(200).entity(mapConfiguration).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map", e);
            return Response.status(404).build();
        }
    }

    @POST
    public Response postCameraView(CameraView cameraView) {
        try {
            configService.configureCameraView(cameraView);
        } catch (Exception e) {
            logger.error("Error occurred when configuring camera view : {}", cameraView, e);
            return Response.status(500).build();
        }
        return Response.status(200).build();
    }
}
