import logging
import time

import cv2

from PTEMapper import PTEMapper
from ScreenSpacePreview import ScreenSpacePreview
from Sense import Sense
from WorldSpaceTracker import WorldSpaceTracker
from detectors.TFODDetector.TFODPersonDetector import TFODPersonDetector
from experiments.AngleMapper import AngleMapper
from experiments.Snapy import Snapy
from test_videos.VideoLoader import load_ntb_entrance


def app():
    person_detector = TFODPersonDetector(preview=True)
    (cap, markers, map_markers) = load_ntb_entrance()

    mapper = PTEMapper(markers, map_markers)

    screen_preview = ScreenSpacePreview(mapper)
    screen_preview.colour_window_title = "Screen Space Preview"
    screen_preview.setupWindows()

    world_space_tracker = WorldSpaceTracker()
    mapper = PTEMapper(markers, map_markers)
    re_id = Snapy()
    sense = Sense(person_detector, mapper, AngleMapper(mapper), world_space_tracker, re_id)

    while True:
        ret, frame = cap.read()

        frame_msec = cap.get(cv2.CAP_PROP_POS_MSEC)

        frame = cv2.resize(frame, (0, 0), fx=0.5, fy=0.5)

        # Obtain grayscale
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

        start = time.time()
        sense.processFrame(frame, gray, frame_msec)
        logging.info("Time: %f", time.time() - start)

        screen_preview.renderFrame(sense, frame, gray)

        key = cv2.waitKey(1)
        if key & 0xFF == ord('q'):
            break


if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)
    app()
