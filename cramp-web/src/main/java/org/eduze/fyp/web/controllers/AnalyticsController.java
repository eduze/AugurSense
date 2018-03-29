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

import org.eduze.fyp.api.resources.PersonCoordinate;
import org.eduze.fyp.web.resources.ReIDStatus;
import org.eduze.fyp.web.services.AnalyticsService;
import org.eduze.fyp.web.services.DirectionAnalyticsService;
import org.eduze.fyp.web.services.ReIDSearchService;
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
    private DirectionAnalyticsService directionAnalyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GET
    @Path("/timestampCount/{from}/{to}")
    public Response getTimestampCount(@PathParam("from") long from, @PathParam("to") long to) {
        try {
            return Response.ok(analyticsService.getTimestampCount(from, to)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/route/{from}/{to}/{uuid}")
    public Response getTrackFromUUID(@PathParam("from") long from, @PathParam("to") long to, @PathParam("uuid") String uuid) {
        try {
            return Response.ok(analyticsService.getTrackingRouteFromUUID(new Date(from), new Date(to), uuid, false)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/route/{from}/{to}/{uuid}/segmented")
    public Response getSegmentedTrackFromUUID(@PathParam("from") long from, @PathParam("to") long to, @PathParam("uuid") String uuid) {
        try {
            return Response.ok(analyticsService.getTrackingRouteFromUUID(new Date(from), new Date(to), uuid, true)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/re_id/invoke/{from}/{to}/{uuid}")
    public Response invokeReIdSearch(@PathParam("from") long from, @PathParam("to") long to, @PathParam("uuid") String uuid) {
        try {
            reIDSearchService.invokeSearch(uuid, new Date(from), new Date(to), false);
            return Response.ok(true).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/re_id/invoke/{from}/{to}/{uuid}/segmented")
    public Response invokeReIdSearchSegmented(@PathParam("from") long from, @PathParam("to") long to, @PathParam("uuid") String uuid) {
        try {
            reIDSearchService.invokeSearch(uuid, new Date(from), new Date(to), true);
            return Response.ok(true).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }


    @GET
    @Path("/re_id/results/{from}/{to}/{uuid}")
    public Response obtainReIDResults(@PathParam("from") long from, @PathParam("to") long to, @PathParam("uuid") String uuid) {
        try {
            ReIDStatus reIDStatus = null;

            if (!reIDSearchService.verify(uuid, new Date(from), new Date(to), false)) {
                reIDStatus = new ReIDStatus(new ArrayList<>(), false, false, true);
            } else {
                List<PersonCoordinate> results = reIDSearchService.obtainSearchResults(uuid, new Date(from), new Date(to), false);

                if (results == null) {
                    reIDStatus = new ReIDStatus(new ArrayList<>(), true, false, false);
                } else {
                    reIDStatus = new ReIDStatus(results, false, true, false);
                }

            }

            return Response.ok(reIDStatus).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/re_id/results/{from}/{to}/{uuid}/segmented")
    public Response obtainReIDResultsSegmented(@PathParam("from") long from, @PathParam("to") long to, @PathParam("uuid") String uuid) {
        try {
            ReIDStatus reIDStatus = null;

            if (!reIDSearchService.verify(uuid, new Date(from), new Date(to), true)) {
                reIDStatus = new ReIDStatus(new ArrayList<>(), false, false, true);
            } else {
                List<PersonCoordinate> results = reIDSearchService.obtainSearchResults(uuid, new Date(from), new Date(to), true);

                if (results == null) {
                    reIDStatus = new ReIDStatus(new ArrayList<>(), true, false, false);
                } else {
                    reIDStatus = new ReIDStatus(results, false, true, false);
                }

            }

            return Response.ok(reIDStatus).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/profile/{uuid}")
    public Response obtainResults(@PathParam("uuid") String uuid) {
        try {
            return Response.ok(analyticsService.getProfile(uuid)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }


    @GET
    @Path("/zoneStatistics/{cameraGroupId}/{from}/{to}")
    public Response getZoneStatistics(@PathParam("cameraGroupId") int cameraGroupId,
            @PathParam("from") long from, @PathParam("to") long to) {
        try {
            return Response.ok(analyticsService.getZoneStatistics(cameraGroupId, from, to)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining zone statistics:", e);
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/zoneStatistics/{from}/{to}/{zoneId}/inflow")
    public Response getZoneInflowPhotos(@PathParam("from") long from, @PathParam("to") long to, @PathParam("zoneId") long zoneId) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            return Response.ok(analyticsService.getAllPastPhotosOfZoneFlow(fromD, toD, (int) zoneId, true, false, false)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining timebound photos. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/zoneTimeline/{trackId}")
    public Response getZoneTimeline(@PathParam("trackId") int trackId) {
        try {
            return Response.ok(analyticsService.getTimelineZonesFromTrackId(trackId, 0, false)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining zoneTimeline. {}", e);
            return Response.status(500).build();
        }
    }


    @GET
    @Path("/zonedTimeVelocity/{from}/{to}/{zoneId}/{interval}")
    public Response getZonedTimeVelocityDistribution(@PathParam("from") long from, @PathParam("to") long to, @PathParam("zoneId") int zoneId, @PathParam("interval") long interval) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);
            if (zoneId == 0)
                return Response.ok(analyticsService.getOverallTimeVelocityDistribution(fromD, toD, interval, false, 10)).build();
            else
                return Response.ok(analyticsService.getZonedTimeVelocityDistribution(fromD, toD, zoneId, interval, false, 10)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining getZonedTimeVelocityDistribution. {}", e);
            return Response.status(500).build();
        }
    }


    @GET
    @Path("/zonedVelocityFrequency/{from}/{to}/{zoneId}/{interval}")
    public Response getZonedVelocityFrequency(@PathParam("from") long from, @PathParam("to") long to, @PathParam("zoneId") int zoneId, @PathParam("interval") long interval) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);
            if (zoneId == 0)
                return Response.ok(analyticsService.getOverallVelocityFrequency(fromD, toD, interval, false, 10)).build();
            else
                return Response.ok(analyticsService.getZonedVelocityFrequency(fromD, toD, zoneId, interval, false, 10)).build();

        } catch (Exception e) {
            logger.error("Error occurred when obtaining overallVelocityFrequency. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/zonedTimeVelocity/{from}/{to}/{zoneId}/{interval}/segmented")
    public Response getZonedTimeVelocityDistributionSegmented(@PathParam("from") long from, @PathParam("to") long to, @PathParam("zoneId") int zoneId, @PathParam("interval") long interval) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            if (zoneId == 0)
                return Response.ok(analyticsService.getOverallTimeVelocityDistribution(fromD, toD, interval, true, 10)).build();
            else
                return Response.ok(analyticsService.getZonedTimeVelocityDistribution(fromD, toD, zoneId, interval, true, 10)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining getZonedTimeVelocityDistribution. {}", e);
            return Response.status(500).build();
        }
    }


    @GET
    @Path("/zonedVelocityFrequency/{from}/{to}/{zoneId}/{interval}/segmented")
    public Response getZonedVelocityFrequencySegmented(@PathParam("from") long from, @PathParam("to") long to, @PathParam("zoneId") int zoneId, @PathParam("interval") long interval) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            if (zoneId == 0)
                return Response.ok(analyticsService.getOverallVelocityFrequency(fromD, toD, interval, true, 10)).build();
            else
                return Response.ok(analyticsService.getZonedVelocityFrequency(fromD, toD, zoneId, interval, true, 10)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining overallVelocityFrequency. {}", e);
            return Response.status(500).build();
        }
    }


    @GET
    @Path("/zoneTimeline/{trackId}/segmented/{segmentId}")
    public Response getZoneTimeline(@PathParam("trackId") int trackId, @PathParam("segmentId") int segmentId) {
        try {
            return Response.ok(analyticsService.getTimelineZonesFromTrackId(trackId, segmentId, true)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining zoneTimeline. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/directionMap/{from}/{to}/{cellSize}/{directionCount}")
    public Response getZoneTimeline(@PathParam("from") long from, @PathParam("to") long to, @PathParam("cellSize") int cellSize, @PathParam("directionCount") int directionCount) {
        try {
            Date start = new Date(from);
            Date end = new Date(to);
            return Response.ok(directionAnalyticsService.getDirectionAnalytics(start, end, cellSize, directionCount).getPointDirections()).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining direction map. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/zoneStatistics/{from}/{to}/{zoneId}/outflow")
    public Response getZoneOutflowPhotos(@PathParam("from") long from, @PathParam("to") long to, @PathParam("zoneId") long zoneId) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            return Response.ok(analyticsService.getAllPastPhotosOfZoneFlow(fromD, toD, (int) zoneId, false, true, false)).build();
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

            return Response.ok(analyticsService.getAllPastPhotosOfZoneFlow(fromD, toD, (int) zoneId, true, false, true)).build();
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

            return Response.ok(analyticsService.getAllPastPhotosOfZoneFlow(fromD, toD, (int) zoneId, false, true, true)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining timebound photos. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/crossCounts/{from}/{to}")
    public Response getCrossCounts(@PathParam("from") long from, @PathParam("to") long to) {
        try {
            return Response.ok(analyticsService.getCrossCount(from, to)).build();
        } catch (Exception e) {
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
    @Path("/realTimeMap/{cameraGroupId}")
    public Response getRealTimeMap(@PathParam("cameraGroupId") int cameraGroupId) {
        try {
            return Response.ok(analyticsService.getRealTimeMap(cameraGroupId)).build();
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

            return Response.ok(analyticsService.getTimeBoundMovements(fromD, toD, false)).build();
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

            return Response.ok(analyticsService.getTimeBoundMovements(fromD, toD, true)).build();
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

            return Response.ok(analyticsService.getAllPastPhotos(fromD, toD)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining timebound photos. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/trackingSnaps/{id}")
    public Response getTrackingSnaps(@PathParam("id") int id) {
        try {
            return Response.ok(analyticsService.getPastPhotos(id, -1)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining tracking route snaps. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/trackingSnaps/{id}/{segmentIndex}")
    public Response getTrackingSnapsWithSegments(@PathParam("id") int id, @PathParam("segmentIndex") int segmentIndex) {
        try {
            return Response.ok(analyticsService.getPastPhotos(id, segmentIndex)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining tracking route snaps. {}", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/realTimePhotos/{id}")
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
    @Path("/heatMap/{cameraGroupId}/{from}/{to}")
    public Response getHeatMap(@PathParam("cameraGroupId") int cameraGroupId, @PathParam("from") long from,
            @PathParam("to") long to) {
        try {
            return Response.ok(analyticsService.getHeatMap(cameraGroupId, from, to)).build();
        } catch (Exception e) {
            logger.error("Error generating heatmap", e);
            return Response.serverError().entity(e.getMessage()).build();
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

    public DirectionAnalyticsService getDirectionAnalyticsService() {
        return directionAnalyticsService;
    }

    public void setDirectionAnalyticsService(DirectionAnalyticsService directionAnalyticsService) {
        this.directionAnalyticsService = directionAnalyticsService;
    }

    public void setReIDSearchService(ReIDSearchService reIDSearchService) {
        this.reIDSearchService = reIDSearchService;
    }

    public ReIDSearchService getReIDSearchService() {
        return reIDSearchService;
    }
}
