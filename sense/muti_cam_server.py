import logging
import multiprocessing
import time

import cv2
from flask import Flask, jsonify

from OpenPersonDetector import OpenPersonDetector
from PTEMapper import PTEMapper
from Sense import Sense
from WorldSpaceTracker import WorldSpaceTracker
from communication.ServerSession import ServerSession
from experiments.AngleMapper import AngleMapper
from experiments.Snapy import Snapy
from test_videos.VideoLoader import load_ntb_middle

logging.basicConfig(level=logging.DEBUG)


def start_server(x, person_detector):
    """
    Not working due to multiprocessing. Cannot share OpenPose person detector among two processors
    :param x:
    :param person_detector:
    :return:
    """
    if x == 0:
        capture, markers, map_markers = load_ntb_middle()
    else:
        capture, markers, map_markers = load_ntb_middle()

    port = 10000 + x

    position_mapper = PTEMapper(markers, map_markers)
    sense = Sense(person_detector, position_mapper, AngleMapper(position_mapper), WorldSpaceTracker(), Snapy())

    # Setup point mappings
    # Obtain preview frame to be sent to server for configuration
    status, frame = capture.read()

    frame = cv2.resize(frame, (0, 0), fx=1, fy=1)

    server_session = ServerSession(my_ip="localhost", my_port=port)
    server_session.configureMapping(frame, markers, map_markers)

    # Obtain marker points from server. Blocks until server responds marker points.
    mapping = server_session.obtainMapping()
    sense.position_mapper.screen_points = mapping.screen_space_points
    # TODO: Do scaling correction
    sense.position_mapper.world_points = mapping.world_space_points

    # Flask Server
    app = Flask(__name__)

    @app.route('/getMap/<int:frame_time>')
    def get_map(frame_time):
        logging.debug("Sending map for timestamp - %d", frame_time)
        return get_map_at(capture, sense, frame_time, server_session)

    logging.info("Starting server at: %d", port)
    app.run(port=port)


def get_map_at(capture, sense, frame_time, server_session):
    logging.info("Generating map for timestamp: " + str(frame_time))

    # Obtain a sample frame with time
    ret, frame = capture.read()
    current_frame_time = capture.get(cv2.CAP_PROP_POS_MSEC)

    # Skip frames until frame_time is reached
    while current_frame_time < frame_time:
        ret, frame = capture.read()
        current_frame_time = capture.get(cv2.CAP_PROP_POS_MSEC)

    # Resize frame
    frame = cv2.resize(frame, (0, 0), fx=1, fy=1)

    # Obtain gray version
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

    sense.processFrame(frame, gray, current_frame_time)

    # if self.detection_viewer is not None:
    #     self.detection_viewer.renderFrame(self.sense, frame, gray)
    #
    # if self.map_viewer is not None:
    #     self.map_viewer.showMap([self.sense])

    if sense.position_mapper.isReady():
        result_coordinates = []

        # Generate Response
        for sensed_person in sense.sensed_persons.values():
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
    # if detection_viewer is not None or self.map_viewer is not None:
    #     key = cv2.waitKey(1)

    # Obtain camera_id from server configuration
    camera_id = server_session.camera_id
    result = {
        "cameraId": camera_id,
        "timestamp": current_frame_time,
        "personCoordinates": result_coordinates
    }
    return jsonify(result)


if __name__ == "__main__":
    person_detector = OpenPersonDetector(False)

    processes = []
    for x in range(1):
        process = multiprocessing.Process(target=start_server, args=(x, person_detector))
        process.daemon = True
        process.start()
        processes.append(process)

    running = True
    while running:
        for process in processes:
            if not process.is_alive():
                running = False
                break

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break
        time.sleep(5)

    for process in processes:
        process.terminate()
