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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    private AnalyticsService analyticsService;
    private ReIDSearchService reIDSearchService;
    private DirectionAnalyticsService directionAnalyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
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

    @GetMapping("/timestampCount/{from}/{to}")
    public Response getTimestampCount(@PathVariable("from") long from, @PathVariable("to") long to) {
        try {
            return Response.ok(analyticsService.getTimestampCount(from, to)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }
    }

    @GetMapping("/route/{from}/{to}/{uuid}")
    public Response getTrackFromUUID(@PathVariable("from") long from, @PathVariable("to") long to,
            @PathVariable("uuid") String uuid) {
        try {
            return Response.ok(analyticsService.getTrackingRouteFromUUID(new Date(from), new Date(to), uuid, false)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GetMapping("/route/{from}/{to}/{uuid}/segmented")
    public Response getSegmentedTrackFromUUID(@PathVariable("from") long from, @PathVariable("to") long to, @PathVariable("uuid") String uuid) {
        try {
            return Response.ok(analyticsService.getTrackingRouteFromUUID(new Date(from), new Date(to), uuid, true)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GetMapping("/re_id/invoke/{from}/{to}/{uuid}")
    public Response invokeReIdSearch(@PathVariable("from") long from, @PathVariable("to") long to, @PathVariable("uuid") String uuid) {
        try {
            reIDSearchService.invokeSearch(uuid, new Date(from), new Date(to), false);
            return Response.ok(true).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GetMapping("/re_id/invoke/{from}/{to}/{uuid}/segmented")
    public Response invokeReIdSearchSegmented(@PathVariable("from") long from, @PathVariable("to") long to, @PathVariable("uuid") String uuid) {
        try {
            reIDSearchService.invokeSearch(uuid, new Date(from), new Date(to), true);
            return Response.ok(true).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GetMapping("/re_id/results/{from}/{to}/{uuid}")
    public Response obtainReIDResults(@PathVariable("from") long from, @PathVariable("to") long to, @PathVariable("uuid") String uuid) {
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

    @GetMapping("/re_id/results/{from}/{to}/{uuid}/segmented")
    public Response obtainReIDResultsSegmented(@PathVariable("from") long from, @PathVariable("to") long to, @PathVariable("uuid") String uuid) {
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

    @GetMapping("/profile/{uuid}")
    public Response obtainResults(@PathVariable("uuid") String uuid) {
        try {
            return Response.ok(analyticsService.getProfile(uuid)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }

    @GetMapping("/zoneStatistics/{from}/{to}")
    public Response getZoneStatistics(@PathVariable("from") long from, @PathVariable("to") long to) {
        try {
            return Response.ok(analyticsService.getZoneStatistics(from, to)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }


    @GetMapping("/zoneStatistics/{from}/{to}/{zoneId}/inflow")
    public Response getZoneInflowPhotos(@PathVariable("from") long from, @PathVariable("to") long to, @PathVariable("zoneId") long zoneId) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            return Response.ok(analyticsService.getAllPastPhotosOfZoneFlow(fromD, toD, (int) zoneId, true, false, false)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining timebound photos. {}", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/zoneTimeline/{trackId}")
    public Response getZoneTimeline(@PathVariable("trackId") int trackId) {
        try {
            return Response.ok(analyticsService.getTimelineZonesFromTrackId(trackId, 0, false)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining zoneTimeline. {}", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/zonedTimeVelocity/{from}/{to}/{zoneId}/{interval}")
    public Response getZonedTimeVelocityDistribution(@PathVariable("from") long from, @PathVariable("to") long to, @PathVariable("zoneId") int zoneId, @PathVariable("interval") long interval) {
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


    @GetMapping("/zonedVelocityFrequency/{from}/{to}/{zoneId}/{interval}")
    public Response getZonedVelocityFrequency(@PathVariable("from") long from, @PathVariable("to") long to, @PathVariable("zoneId") int zoneId, @PathVariable("interval") long interval) {
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


    @GetMapping("/zonedTimeVelocity/{from}/{to}/{zoneId}/{interval}/segmented")
    public Response getZonedTimeVelocityDistributionSegmented(@PathVariable("from") long from, @PathVariable("to") long to, @PathVariable("zoneId") int zoneId, @PathVariable("interval") long interval) {
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


    @GetMapping("/zonedVelocityFrequency/{from}/{to}/{zoneId}/{interval}/segmented")
    public Response getZonedVelocityFrequencySegmented(@PathVariable("from") long from, @PathVariable("to") long to, @PathVariable("zoneId") int zoneId, @PathVariable("interval") long interval) {
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


    @GetMapping("/zoneTimeline/{trackId}/segmented/{segmentId}")
    public Response getZoneTimeline(@PathVariable("trackId") int trackId, @PathVariable("segmentId") int segmentId) {
        try {
            return Response.ok(analyticsService.getTimelineZonesFromTrackId(trackId, segmentId, true)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining zoneTimeline. {}", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/directionMap/{from}/{to}/{cellSize}/{directionCount}")
    public Response getZoneTimeline(@PathVariable("from") long from, @PathVariable("to") long to, @PathVariable("cellSize") int cellSize, @PathVariable("directionCount") int directionCount) {
        try {
            Date start = new Date(from);
            Date end = new Date(to);
            return Response.ok(directionAnalyticsService.getDirectionAnalytics(start, end, cellSize, directionCount).getPointDirections()).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining direction map. {}", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/zoneStatistics/{from}/{to}/{zoneId}/outflow")
    public Response getZoneOutflowPhotos(@PathVariable("from") long from, @PathVariable("to") long to, @PathVariable("zoneId") long zoneId) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            return Response.ok(analyticsService.getAllPastPhotosOfZoneFlow(fromD, toD, (int) zoneId, false, true, false)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining timebound photos. {}", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/zoneStatistics/{from}/{to}/{zoneId}/inflow/segmented")
    public Response getZoneInflowPhotosSegmented(@PathVariable("from") long from, @PathVariable("to") long to, @PathVariable("zoneId") long zoneId) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            return Response.ok(analyticsService.getAllPastPhotosOfZoneFlow(fromD, toD, (int) zoneId, true, false, true)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining timebound photos. {}", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/zoneStatistics/{from}/{to}/{zoneId}/outflow/segmented")
    public Response getZoneOutflowPhotosSegmented(@PathVariable("from") long from, @PathVariable("to") long to, @PathVariable("zoneId") long zoneId) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            return Response.ok(analyticsService.getAllPastPhotosOfZoneFlow(fromD, toD, (int) zoneId, false, true, true)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining timebound photos. {}", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/crossCounts/{from}/{to}")
    public Response getCrossCounts(@PathVariable("from") long from, @PathVariable("to") long to) {
        try {
            return Response.ok(analyticsService.getCrossCount(from, to)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }

    }


    @GetMapping("/getMap")
    public Response getMap() {
        try {
            return Response.ok(analyticsService.getMap()).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining map. {}", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/realTimeMap")
    public Response getRealTimeMap() {
        try {
            return Response.ok(analyticsService.getRealTimeMap()).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining real time map. {}", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/timeBoundMap/{from}/{to}")
    public Response getTimeboundMap(@PathVariable("from") long from, @PathVariable("to") long to) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            return Response.ok(analyticsService.getTimeBoundMovements(fromD, toD, false)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining real time map. {}", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/timeBoundMap/{from}/{to}/trackSegmented")
    public Response getTimeboundMapWithSegments(@PathVariable("from") long from, @PathVariable("to") long to) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            return Response.ok(analyticsService.getTimeBoundMovements(fromD, toD, true)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining real time map. {}", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/timeBoundMap/{from}/{to}/photos")
    public Response getTimeboundPhotos(@PathVariable("from") long from, @PathVariable("to") long to) {
        try {
            Date fromD = new Date(from);
            Date toD = new Date(to);

            return Response.ok(analyticsService.getAllPastPhotos(fromD, toD)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining timebound photos. {}", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/trackingSnaps/{id}")
    public Response getTrackingSnaps(@PathVariable("id") int id) {
        try {
            return Response.ok(analyticsService.getPastPhotos(id, -1)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining tracking route snaps. {}", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/trackingSnaps/{id}/{segmentIndex}")
    public Response getTrackingSnapsWithSegments(@PathVariable("id") int id, @PathVariable("segmentIndex") int segmentIndex) {
        try {
            return Response.ok(analyticsService.getPastPhotos(id, segmentIndex)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining tracking route snaps. {}", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/realTimeMap/{id}")
    public Response getRealTimeInfo(@PathVariable("id") int id) {
        try {
            return Response.ok(analyticsService.getRealtimePhotos(id)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining real time info of track. {}", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/realTimeMap/all")
    public Response getRealTimeInfoAll() {
        try {
            return Response.ok(analyticsService.getRealtimePhotosAll()).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining real time info of all. {}", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/heatMap/{from}/{to}")
    public Response getHeatMap(@PathVariable("from") long from, @PathVariable("to") long to) {
        try {

            return Response.ok(analyticsService.getHeatMap(from, to)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining heat map", e);
            return Response.status(500).build();
        }
    }

    @GetMapping("/count/{from}/{to}")
    public Response getCount(@PathVariable("from") long from, @PathVariable("to") long to) {
        try {
            return Response.status(200).entity(analyticsService.getCount(from, to)).build();
        } catch (Exception e) {
            logger.error("Error occurred when obtaining heat map", e);
            return Response.status(500).build();
        }
    }


    @GetMapping("/stoppoints/{from}/{to}/{radius}/{time}/{height}/{width}")
    public Response getStopPoints(@PathVariable("from") long from, @PathVariable("to") long to, @PathVariable("radius") int radius,
            @PathVariable("time") int time, @PathVariable("height") int height, @PathVariable("width") int width) {
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
}
