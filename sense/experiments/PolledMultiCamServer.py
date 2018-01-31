import logging
from threading import Thread, Semaphore, RLock

import cv2
from flask import Flask, jsonify

from CamServer import CamServer
from PTEMapper import PTEMapper
from ScreenSpacePreview import ScreenSpacePreview
from Sense import Sense
from SenseMapViewer import SenseMapViewer
from WorldSpaceTracker import WorldSpaceTracker
from communication.ServerSession import ServerSession
from communication.ServerSession import config_my_port
from detectors.TFODDetector.TFODPersonDetector import TFODPersonDetector
from experiments.AngleMapper import AngleMapper
from experiments.Snapy import Snapy
from test_videos.VideoLoader import load_ntb_middle, load_ntb_entrance

serve_count = 2


class ProcessEvent:
    def __init__(self, frame_id, server):
        self.frame_id = frame_id
        self.notifySemapore = Semaphore(0)
        self.results = None
        self.server = server

    def process(self):
        self.results = self.server.getMapFRImp(self.frame_id)
        self.notifySemapore.release()

    def acquire(self):
        self.notifySemapore.acquire()


if __name__ == "__main__":
    # Test snippet
    logging.basicConfig(level=logging.INFO)

    person_detector = TFODPersonDetector(preview=False)

    processEventsLock = RLock()

    processEvents = []
    servers = []

    background_image = cv2.imread("test_videos/ntb_branch.jpg")

    for i in range(serve_count):
        if i == 0:
            cap, markers, map_markers = load_ntb_middle()
        else:
            cap, markers, map_markers = load_ntb_entrance()
        position_mapper = PTEMapper(markers, map_markers)
        sense = Sense(person_detector, position_mapper, AngleMapper(position_mapper), WorldSpaceTracker(), Snapy())

        screen_preview = ScreenSpacePreview(position_mapper, False)
        screen_preview.colour_window_title = "Input " + str(i)
        screen_preview.setupWindows()

        track_viewer = SenseMapViewer([position_mapper], False, map_background=background_image)
        track_viewer.map_name = "Map " + str(i)
        track_viewer.setupWindows()

        cam_server = CamServer(sense)

        server_session = ServerSession(my_port=config_my_port + i)

        cam_server.capture_scale = (0.5, 0.5)
        cam_server.map_viewer = track_viewer
        cam_server.detection_viewer = screen_preview
        cam_server.capture = cap

        cam_server.loadConfigFromServer(server_session, markers, map_markers)
        servers.append(cam_server)


        def threadFunction(cserver, cam_port):
            app = Flask(__name__)

            @app.route('/getMap')
            def getMap():
                '''
                Obtain map of next frame, considering standard frame rate.
                :return:
                '''
                return getMapFR(cserver.processed_frames_counter * 1000.0 / cserver.default_frame_rate)

            @app.route('/getMap/<int:frame_time>')
            def getMapFR(frame_time):
                with app.app_context():
                    p = ProcessEvent(frame_time, cserver)
                    processEventsLock.acquire()
                    processEvents.append(p)
                    processEventsLock.release()
                    p.acquire()

                    return jsonify(p.results)

            logging.info("Starting server at " + str(cam_port))
            app.run(port=cam_port)


        serverThread = Thread(target=threadFunction, args=(cam_server, config_my_port + i))
        serverThread.start()

    while True:
        processEventsLock.acquire()
        if (len(processEvents) == 0):
            processEventsLock.release()
            continue

        p = processEvents[0]
        del processEvents[0]
        processEventsLock.release()
        p.process()
