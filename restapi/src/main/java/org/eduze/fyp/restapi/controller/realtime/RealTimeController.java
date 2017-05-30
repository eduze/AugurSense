package org.eduze.fyp.restapi.controller.realtime;

import org.eduze.fyp.restapi.resources.FrameInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

    @POST
    public Response postFrameInfo(FrameInfo frameInfo) {
        logger.info("Received a frame info");
        return Response.status(200).build();
    }
}
