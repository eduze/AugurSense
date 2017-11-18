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

package org.eduze.fyp.rest.controllers;

import org.eduze.fyp.api.resources.PersonCoordinate;
import org.eduze.fyp.impl.db.model.Person;
import org.eduze.fyp.rest.resources.ReIDStatus;
import org.eduze.fyp.rest.services.AnalyticsService;
import org.eduze.fyp.rest.services.ReIDSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Path("/analytics")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    private AnalyticsService analyticsService;

    private ReIDSearchService reIDSearchService;

    public void setReIDSearchService(ReIDSearchService reIDSearchService) {
        this.reIDSearchService = reIDSearchService;
    }

    public ReIDSearchService getReIDSearchService() {
        return reIDSearchService;
    }

    @GET
    @Path("/timestampCount/{from}/{to}")
    public Response getTimestampCount(@PathParam("from") long from, @PathParam("to") long to){
        try {
            return Response.ok(analyticsService.getTimestampCount(from, to)).build();
        }
         catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/route/{from}/{to}/{uuid}")
    public Response getTrackFromUUID(@PathParam("from") long from, @PathParam("to") long to, @PathParam("uuid") String uuid){
        try {
            return Response.ok(analyticsService.getTrackingRouteFromUUID(new Date(from),new Date(to),uuid,false)).build();
        }
        catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/route/{from}/{to}/{uuid}/segmented")
    public Response getSegmentedTrackFromUUID(@PathParam("from") long from, @PathParam("to") long to, @PathParam("uuid") String uuid){
        try {
            return Response.ok(analyticsService.getTrackingRouteFromUUID(new Date(from),new Date(to),uuid, true)).build();
        }
        catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/re_id/invoke/{from}/{to}/{uuid}")
    public Response invokeReIdSearch(@PathParam("from") long from, @PathParam("to") long to,@PathParam("uuid") String uuid){
        try {
            reIDSearchService.invokeSearch(uuid,new Date(from),new Date(to),false);
            return Response.ok(true).build();
        }
        catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/re_id/invoke/{from}/{to}/{uuid}/segmented")
    public Response invokeReIdSearchSegmented(@PathParam("from") long from, @PathParam("to") long to,@PathParam("uuid") String uuid){
        try {
            reIDSearchService.invokeSearch(uuid,new Date(from),new Date(to),true);
            return Response.ok(true).build();
        }
        catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }


    @GET
    @Path("/re_id/results/{from}/{to}/{uuid}")
    public Response obtainReIDResults(@PathParam("from") long from, @PathParam("to") long to,@PathParam("uuid") String uuid){
        try {
            ReIDStatus reIDStatus = null;

            if(!reIDSearchService.verify(uuid,new Date(from), new Date(to),false))
            {
                reIDStatus = new ReIDStatus(new ArrayList<>(),false,false,true);
            }
            else{
                List<PersonCoordinate> results = reIDSearchService.obtainSearchResults(uuid,new Date(from), new Date(to),false);

                if(results == null)
                {
                    reIDStatus = new ReIDStatus(new ArrayList<>(),true,false,false);
                }
                else{
                    reIDStatus = new ReIDStatus(results,false,true,false);
                }

            }

            return Response.ok(reIDStatus).build();
        }
        catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/re_id/results/{from}/{to}/{uuid}/segmented")
    public Response obtainReIDResultsSegmented(@PathParam("from") long from, @PathParam("to") long to,@PathParam("uuid") String uuid){
        try {
            ReIDStatus reIDStatus = null;

            if(!reIDSearchService.verify(uuid,new Date(from), new Date(to),true))
            {
                reIDStatus = new ReIDStatus(new ArrayList<>(),false,false,true);
            }
            else{
                List<PersonCoordinate> results = reIDSearchService.obtainSearchResults(uuid,new Date(from), new Date(to),true);

                if(results == null)
                {
                    reIDStatus = new ReIDStatus(new ArrayList<>(),true,false,false);
                }
                else{
                    reIDStatus = new ReIDStatus(results,false,true,false);
                }

            }

            return Response.ok(reIDStatus).build();
        }
        catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/profile/{uuid}")
    public Response obtainResults(@PathParam("uuid") String uuid){
        try {
            return Response.ok(analyticsService.getProfile(uuid)).build();
        }
        catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }


    @GET
    @Path("/zoneStatistics/{from}/{to}")
    public Response getZoneStatistics(@PathParam("from") long from, @PathParam("to") long to){
        try {
            return Response.ok(analyticsService.getZoneStatistics(from, to)).build();
        }
        catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/zoneStatistics/{from}/{to}/{zoneId}/inflow")
    public Response getZoneInflowPhotos(@PathParam("from") long from, @PathParam("to") long to, @PathParam("zoneId") long zoneId) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            return Response.ok(analyticsService.getAllPastPhotosOfZoneFlow(fromD,toD, (int) zoneId,true,false,false)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining timebound photos. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/zoneStatistics/{from}/{to}/{zoneId}/outflow")
    public Response getZoneOutflowPhotos(@PathParam("from") long from, @PathParam("to") long to, @PathParam("zoneId") long zoneId) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            return Response.ok(analyticsService.getAllPastPhotosOfZoneFlow(fromD,toD, (int) zoneId,false,true,false)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining timebound photos. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/zoneStatistics/{from}/{to}/{zoneId}/inflow/segmented")
    public Response getZoneInflowPhotosSegmented(@PathParam("from") long from, @PathParam("to") long to, @PathParam("zoneId") long zoneId) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            return Response.ok(analyticsService.getAllPastPhotosOfZoneFlow(fromD,toD, (int) zoneId,true,false,true)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining timebound photos. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/zoneStatistics/{from}/{to}/{zoneId}/outflow/segmented")
    public Response getZoneOutflowPhotosSegmented(@PathParam("from") long from, @PathParam("to") long to, @PathParam("zoneId") long zoneId) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            return Response.ok(analyticsService.getAllPastPhotosOfZoneFlow(fromD,toD, (int) zoneId,false,true,true)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining timebound photos. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/crossCounts/{from}/{to}")
    public Response getCrossCounts(@PathParam("from") long from, @PathParam("to") long to){
        try {
            return Response.ok(analyticsService.getCrossCount(from, to)).build();
        }
        catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }


    @GET
    @Path("/getMap")
    public Response getMap() {
        try {
            return Response.ok(analyticsService.getMap()).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/realTimeMap")
    public Response getRealTimeMap() {
        try {
            return Response.ok(analyticsService.getRealTimeMap()).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining real time map. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/timeBoundMap/{from}/{to}")
    public Response getTimeboundMap(@PathParam("from") long from, @PathParam("to") long to) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            return Response.ok(analyticsService.getTimeBoundMovements(fromD,toD,false)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining real time map. {}", e);
            return Response.status(500).build();
        }
    }
    @GET
    @Path("/timeBoundMap/{from}/{to}/trackSegmented")
    public Response getTimeboundMapWithSegments(@PathParam("from") long from, @PathParam("to") long to) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            return Response.ok(analyticsService.getTimeBoundMovements(fromD,toD,true)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining real time map. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/timeBoundMap/{from}/{to}/photos")
    public Response getTimeboundPhotos(@PathParam("from") long from, @PathParam("to") long to) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            return Response.ok(analyticsService.getAllPastPhotos(fromD,toD)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining timebound photos. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/trackingSnaps/{id}")
    public Response getTrackingSnaps(@PathParam("id") int id) {
        try {
            return Response.ok(analyticsService.getPastPhotos(id,-1)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining tracking route snaps. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/trackingSnaps/{id}/{segmentIndex}")
    public Response getTrackingSnapsWithSegments(@PathParam("id") int id,@PathParam("segmentIndex") int segmentIndex) {
        try {
            return Response.ok(analyticsService.getPastPhotos(id,segmentIndex)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining tracking route snaps. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/realTimeMap/{id}")
    public Response getRealTimeInfo(@PathParam("id") int id) {
        try {
            return Response.ok(analyticsService.getRealtimePhotos(id)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining real time info of track. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/realTimeMap/all")
    public Response getRealTimeInfoAll() {
        try {
            return Response.ok(analyticsService.getRealtimePhotosAll()).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining real time info of all. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/heatMap/{from}/{to}")
    public Response getHeatMap(@PathParam("from") long from, @PathParam("to") long to) {
        try {

            return Response.ok(analyticsService.getHeatMap(from, to)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining heat map", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/count/{from}/{to}")
    public Response getCount(@PathParam("from") long from, @PathParam("to") long to) {
        try {
            return Response.status(200).entity(analyticsService.getCount(from, to)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining heat map", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/stoppoints/{from}/{to}/{radius}/{time}/{height}/{width}")
    public Response getStopPoints(@PathParam("from") long from, @PathParam("to") long to, @PathParam("radius") int radius,
                                  @PathParam("time") int time, @PathParam("height") int height, @PathParam("width") int width) {
        try {
            return Response.status(200).entity(analyticsService.getStopPoints(from, to, radius, time, height, width)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining heat map", e);
            return Response.status(500).build();
        }
    }


    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }
}
