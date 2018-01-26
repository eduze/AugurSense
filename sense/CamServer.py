import logging
from _thread import RLock

import cv2
from flask import Flask, jsonify

from SenseMapViewer import SenseMapViewer
from OpenPersonDetector import OpenPersonDetector
from PTEMapper import PTEMapper
from ScreenSpacePreview import ScreenSpacePreview
from Sense import Sense
from WorldSpaceTracker import WorldSpaceTracker
from communication.ServerSession import ServerSession
from experiments.AngleMapper import AngleMapper
from experiments.Snapy import Snapy
from test_videos.VideoLoader import loadOfficeRoomTest, loadCSELounge, load_ntb_middle
from communication.ServerSession import config_my_port

cam_server_instance = None


class CamServer:
    '''
    Camera Server Module
    '''

    def __init__(self, sense):
        '''
        Initialize
        '''
        self.use_server = False
        self.sense = sense
        self.capture = None
        self.default_frame_rate = 16

        self.use_denoising = False
        self.frame_msec = 0
        self.processed_frames_counter = 0

        self.capture_scale = (1, 1)

        self.map_viewer = None
        self.detection_viewer = None

        self.lock = RLock()

    def loadConfigFromServer(self, server_session, default_markers=None, default_map_markers=None):
        '''
        Connect to server and load configurations from it
        :param server_host: Server host
        :param server_port: Server port
        :return: 
        '''
        self.server_session = server_session
        self.use_server = True

        # Obtain preview frame to be sent to server for configuration
        status, frame = self.capture.read()

        frame = cv2.resize(frame, (0, 0), fx=self.capture_scale[0], fy=self.capture_scale[1])

        self.server_session.configureMapping(frame, default_markers, default_map_markers)

        # Obtain marker points from server. Blocks until server responds marker points.
        mapping = self.server_session.obtainMapping()
        self.sense.position_mapper.screen_points = mapping.screen_space_points
        # TODO: Do scaling correction
        self.sense.position_mapper.world_points = mapping.world_space_points

    def getMapFRImp(self, frame_time):
        """
        Obtain map of frame at frame_time. Frame_time should be ahead of current time stamp.
        :param frame_time: Timestamp at which frame should be obtained
        :return:
        """
        self.lock.acquire()
        logging.info("Generating map for timestamp: " + str(frame_time))

        # Obtain a sample frame with time
        ret, frame = self.capture.read()
        current_frame_time = self.capture.get(cv2.CAP_PROP_POS_MSEC)

        # Skip frames until frame_time is reached
        while current_frame_time < frame_time:
            ret, frame = self.capture.read()
            current_frame_time = self.capture.get(cv2.CAP_PROP_POS_MSEC)

        self.processed_frames_counter += 1

        # Resize frame
        frame = cv2.resize(frame, (0, 0), fx=self.capture_scale[0], fy=self.capture_scale[1])

        # Denoise if specified
        if self.use_denoising:
            frame = cv2.fastNlMeansDenoisingColored(frame)

        # Obtain gray version
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

        self.sense.processFrame(frame, gray, current_frame_time)

        if self.detection_viewer is not None:
            self.detection_viewer.renderFrame(self.sense, frame, gray)

        if self.map_viewer is not None:
            self.map_viewer.showMap([self.sense])

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

                if sensed_person.re_id_data is not None:
                    result["image"] = sensed_person.re_id_data
                result_coordinates.append(result)

        else:
            result_coordinates = "Mapping Not Initialized. Missing Markers."

        # Render outputs if required
        if self.detection_viewer is not None or self.map_viewer is not None:
            key = cv2.waitKey(1)

        # Obtain camera_id from server configuration
        camera_id = None
        if self.use_server:
            camera_id = self.server_session.camera_id
        result = {
            "cameraId": camera_id,
            "timestamp": current_frame_time,
            "personCoordinates": result_coordinates
        }
        self.lock.release()
        return result

    def startCamServer(self, cam_port):
        '''
        Initialize Camera Server which accepts requests from Analytics Data Collector
        :param cam_port: port of camera server
        :return: 
        '''
        app = Flask(__name__)

        _self_shaddow = self

        @app.route('/getMap')
        def getMap():
            '''
            Obtain map of next frame, considering standard frame rate.
            :return:
            '''
            return getMapFR(self.processed_frames_counter * 1000.0 / self.default_frame_rate)

        @app.route('/getMap/<int:frame_time>')
        def getMapFR(frame_time):
            logging.debug("Sending map for timestamp - %d", frame_time)
            return jsonify(_self_shaddow.getMapFRImp(frame_time))

        logging.info("Starting server at " + str(cam_port))
        app.run(port=cam_port)


if __name__ == "__main__":
    # Test snippet
    logging.basicConfig(level=logging.DEBUG)
    cap, markers, map_markers = load_ntb_middle()

    person_detector = OpenPersonDetector(False)
    position_mapper = PTEMapper(markers, map_markers)
    sense = Sense(person_detector, position_mapper, AngleMapper(position_mapper), WorldSpaceTracker(), Snapy())

    screen_preview = ScreenSpacePreview(position_mapper, False)
    screen_preview.setupWindows()

    background_image = cv2.imread("test_videos/ntb_branch.jpg")
    track_viewer = SenseMapViewer([position_mapper], is_editable_mapping=False, map_background=background_image)
    track_viewer.setupWindows()

    cam_server = CamServer(sense)

    server_session = ServerSession()

    cam_server.capture_scale = (0.5, 0.5)
    cam_server.map_viewer = track_viewer
    cam_server.detection_viewer = screen_preview
    cam_server.capture = cap

    cam_server.loadConfigFromServer(server_session, markers, map_markers)
    cam_server.startCamServer(config_my_port)
