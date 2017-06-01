/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.restapi.controller.config;

import org.eduze.fyp.restapi.resources.CameraView;
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

    @GET
    public Response getMap() {
        return Response.status(200).build();
    }

    @POST
    public Response postCameraView(CameraView cameraView) {

        return Response.status(200).build();
    }
}
