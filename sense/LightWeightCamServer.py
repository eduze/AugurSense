import logging

import cv2
from flask import Flask, jsonify

from Util import restEncodeImage
from communication.ServerSession import ServerSession

cam_server_instance = None


class LightWeightCamServer:
    """
    A Light weight camera server. Stripped out of tracking
    todo: remove tracking - Imesha
    """

    def __init__(self, sense, capture, scale=(0.5, 0.5)):
        """
        Initialize
        """
        self.sense = sense
        self.capture = capture
        self.scale = scale
        self.port = 10005
        self.server_session = ServerSession(my_ip="localhost", my_port=self.port)
        self.logger = logging.getLogger("CamServer")

    def load_config(self, default_markers=None, default_map_markers=None):
        """
        Connect to server and load configurations from it
        :return:
        """

        # Obtain preview frame to be sent to server for configuration
        status, frame = self.capture.read()

        frame = cv2.resize(frame, (0, 0), fx=self.scale[0], fy=self.scale[1])

        w, h, depth = frame.shape
        self.server_session.configureMapping(frame, w, h, default_markers, default_map_markers)
        # Obtain marker points from server. Blocks until server responds marker points.
        mapping = self.server_session.obtainMapping()
        self.logger.info("Obtained mapping: %s", mapping)

        self.sense.position_mapper.screen_points = mapping.screen_space_points
        # TODO: Do scaling correction
        self.sense.position_mapper.world_points = mapping.world_space_points

    def get_map_at(self, frame_time):
        """
        Obtain map of frame at frame_time. Frame_time should be ahead of current time stamp.
        :param frame_time: Timestamp at which frame should be obtained
        :return:
        """
        logging.info("Generating map for timestamp: " + str(frame_time))

        # Obtain a sample frame with time
        ret, frame = self.capture.read()
        current_frame_time = self.capture.get(cv2.CAP_PROP_POS_MSEC)

        # Skip frames until frame_time is reached
        while current_frame_time < frame_time:
            ret, frame = self.capture.read()
            current_frame_time = self.capture.get(cv2.CAP_PROP_POS_MSEC)

        # Resize frame
        frame = cv2.resize(frame, (0, 0), fx=self.scale[0], fy=self.scale[1])

        # Obtain gray version
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

        self.sense.processFrame(frame, gray, current_frame_time)

        if self.sense.position_mapper.isReady():
            result_coordinates = []

            # Generate Response
            for sensed_person in self.sense.sensed_persons.values():
                person = sensed_person.tracked_person
                # Append to results
                result = {
                    "x": int(person.position[0]),
                    "y": int(person.position[1]),
                    "standProbability": person.stand_probability,
                    "sitProbability": 1 - person.stand_probability,
                }

                if person.head_direction is not None:
                    result["headDirectionX"] = person.head_direction[0].tolist()[0]
                    result["headDirectionY"] = person.head_direction[1].tolist()[0]

                detection = sensed_person.tracked_person.detection
                # print(detection.person_bound)
                (dc_x, dc_y, dc_w, dc_h) = map(int, detection.person_bound)
                (f_x, f_y, f_w, f_h) = detection.person_bound
                (l_x, l_y) = detection.leg_point
                (el_x, el_y) = detection.estimated_leg_point
                cv2.rectangle(frame, (f_x, f_y), (f_w, f_h), (0, 0, 0), 2)
                cv2.drawMarker(frame, (int(l_x), int(l_y)), (255, 0, 255), cv2.MARKER_CROSS, thickness=3)
                cv2.drawMarker(frame, (int(el_x), int(el_y)), (255, 0, 255), cv2.MARKER_DIAMOND)

                snap = frame[dc_y:dc_h, dc_x:dc_w]
                if snap.shape[0] > 0 and snap.shape[1] > 0:
                    logging.debug("Snapping person")
                    result["image"] = restEncodeImage(snap)
                result_coordinates.append(result)

        else:
            result_coordinates = "Mapping Not Initialized. Missing Markers."

        # Obtain camera_id from server configuration
        camera_id = self.server_session.camera_id
        result = {
            "cameraId": camera_id,
            "timestamp": current_frame_time,
            "personCoordinates": result_coordinates
        }

        cv2.imshow("output", frame)
        cv2.waitKey(1)

        return result

    def start_cam_server(self):
        """
        Initialize Camera Server which accepts requests from Analytics Data Collector
        :param cam_port: port of camera server
        :return:
        """
        app = Flask(__name__)

        _self_shaddow = self

        @app.route('/getMap/<int:frame_time>')
        def getMapFR(frame_time):
            logging.debug("Sending map for timestamp - %d", frame_time)
            return jsonify(_self_shaddow.get_map_at(frame_time))

        logging.info("Starting server at " + str(self.port))
        app.run(port=self.port)
