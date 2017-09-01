package org.eduze.fyp.rest.controllers.realtime;

import org.eduze.fyp.rest.resources.FrameInfo;
import org.eduze.fyp.rest.services.realtime.RealTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Controller which handle all the realtime data transfers
 *
 * @author Imesha Sudasingha
 */
@Path("/realtime")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RealTimeController {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeController.class);

    private RealTimeService realTimeService;

    @POST
    public Response postFrameInfo(FrameInfo frameInfo) {
        logger.debug("Received a FrameInfo request : {}", frameInfo);
        try {
            realTimeService.addFrameInfo(frameInfo);
        } catch (Exception e) {
            return Response.status(500).build();
        }

        return Response.status(200).build();
    }

    @GET
    public Response getFrameInfo() {
        logger.debug("Received frame info GET request");
        return Response.status(200).build();
    }

    public void setRealTimeService(RealTimeService realTimeService) {
        this.realTimeService = realTimeService;
    }
}
