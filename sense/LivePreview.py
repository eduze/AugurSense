import logging
import time

import cv2

from OpenPersonDetector import OpenPersonDetector
from PTEMapper import PTEMapper
from ScreenSpacePreview import ScreenSpacePreview
from Sense import Sense
from SenseMapViewer import SenseMapViewer
from WorldSpaceTracker import WorldSpaceTracker
from detectors.TFODDetector.TFODPersonDetector import TFODPersonDetector
from experiments.AngleMapper import AngleMapper
# from experiments.HumanDensityHeatmapViewer import HumanDensityHeatmapViewer
from experiments.MapAggregator import MapAggregator
from experiments.MergedMapViewer import MergedMapViewer
from experiments.Snapy import Snapy
from test_videos.VideoLoader import loadPETS09S2L1V5, load_ntb_middle

# logging.basicConfig(level=INFO)

screen_width = 1900
screen_height = 1050


def app():
    '''
    Sample App demonstrating Detection, Tracking and Mapping
    :return:
    '''

    # cap = cv2.VideoCapture(0)

    # Setup trackers and detectors

    person_detector = TFODPersonDetector(preview=True)

    caps = []
    senses = []
    screen_previews = []
    frame_ids = []

    paused = False

    frames = []
    position_mappers = []

    background_image = cv2.imread("test_videos/ntb_branch.png")

    for i in range(1):
        if i == 0:
            # (cap, markers, map_markers) = loadCSELounge()
            # cap.set(cv2.CAP_PROP_POS_MSEC,55000)

            # background_image = cv2.resize(background_image, (0,0), fx=0.7, fy=0.7)

            (cap, markers, map_markers) = load_ntb_middle()
            # (cap, markers, map_markers) = loadCSELounge()
        elif i == 1:
            (cap, markers, map_markers) = loadPETS09S2L1V5()
            # (cap, markers) = loadAroundRectangleTest2()
        caps.append(cap)

        world_space_tracker = WorldSpaceTracker()
        mapper = PTEMapper(markers, map_markers)
        re_id = Snapy()
        sense = Sense(person_detector, mapper, AngleMapper(mapper), world_space_tracker, re_id)
        senses.append(sense)

        screen_preview = ScreenSpacePreview(mapper, True)
        screen_preview.colour_window_title = "Screen " + str(i)
        screen_preview.setupWindows()
        frame_ids.append(0)
        screen_previews.append(screen_preview)
        frames.append(None)
        position_mappers.append(mapper)

        cv2.moveWindow(screen_preview.colour_window_title, 0, int(screen_width / 2 * i))
        cv2.resizeWindow(screen_preview.colour_window_title, int(screen_width / 2), int(screen_height / 2))

    track_viewer = SenseMapViewer(position_mappers, True, map_background=background_image)
    track_viewer.setupWindows()

    merged_map_viewer = MergedMapViewer(position_mappers, map_background=background_image)
    merged_map_viewer.setupWindows()
    map_merge = MapAggregator()
    # hmdv = HumanDensityHeatmapViewer(50)
    # hmdv.setupWindow()

    global_map_tracker = WorldSpaceTracker()
    # \global_map_tracker.detection_radius2 = 300

    frame_msec = 0
    tracked_trails = {}

    cv2.moveWindow(merged_map_viewer.map_name, int(screen_width / 2), 0)
    cv2.resizeWindow(merged_map_viewer.map_name, int(screen_width / 2), int(screen_height / 2))

    cv2.moveWindow(track_viewer.map_name, int(screen_width / 2), int(screen_height / 2))
    cv2.resizeWindow(track_viewer.map_name, int(screen_width / 2), int(screen_height / 2))

    while (True):
        new_tracked_trails = {}

        for i in range(len(senses)):
            # Capture frame-by-frame
            if not paused:
                ret, frames[i] = caps[i].read()
            frame = frames[i]
            frame_msec = caps[i].get(cv2.CAP_PROP_POS_MSEC)
            frame_ids[i] += 1

            # Frame Skip
            if frame_ids[i] % 1 != 0:
                continue

            # Scale frame

            frame = cv2.resize(frame, (0, 0), fx=0.5, fy=0.5)
            # frame = frame[0:int(frame.shape[0]/2), 0:int(frame.shape[1]/2)]
            # Denoise frame
            # frame = cv2.fastNlMeansDenoisingColored(frame)

            # Obtain grayscale
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

            # Denoise gray
            # gray = cv2.fastNlMeansDenoising(gray)

            start = time.time()
            senses[i].processFrame(frame, gray, frame_msec)
            logging.info("Time: %f", time.time() - start)

            screen_previews[i].renderFrame(senses[i], frame, gray)

        # sense = senses[1]
        # for sensed_person in sense.sensed_persons.values():
        #     print(str(sensed_person.tracked_person.position[0]), "," , str(sensed_person.tracked_person.position[1]) , ",", end="")
        # print()
        track_viewer.showMap(senses)

        # Use track_viewer to render entire tracking trail

        map_merge.mergeMaps(senses)

        global_map_tracker.nextFrame(map_merge.merged_map, frame_msec)

        tracked_frame = global_map_tracker.current_frame

        for person in tracked_frame.persons.values():
            # Maintain tracking trail
            if person.label in tracked_trails.keys():
                tracked_trails[person.label].append(person)
                new_tracked_trails[person.label] = tracked_trails[person.label]
            else:
                new_tracked_trails[person.label] = [person]

        for person in tracked_frame.persons.values():
            person.tracking_history = new_tracked_trails[person.label]

        tracked_trails = new_tracked_trails

        for person in tracked_frame.persons.values():
            for i in range(len(person.detection)):
                d = person.detection[i]
                # cv2.imwrite("output/" + str(int(frame_msec)) + "_" + str(int(person.position[0])) + "_" + str(
                #     int(person.position[1])) + "_" + str(int(i)) + ".jpg", d.roi)

        # merged_map_viewer.showMap(tracked_frame.persons.values(), map_merge.merged_map)

        # hmdv.processFrame(list(tracked_frame.persons.values()))

        key = cv2.waitKey(1)
        if key & 0xFF == ord('q'):
            break
        elif key & 0xFF == ord('m'):
            for sense in senses:
                print("Input:", senses.index(sense))
                print("Screen: ", sense.position_mapper.screen_points)
                print("World: ", sense.position_mapper.world_points)
        elif key & 0xFF == ord('p'):
            paused = not paused
        elif key & 0xFF == ord("r"):
            for sense in senses:
                sense.clearTrackingHistory()
                tracked_trails.clear()
        elif key & 0xFF == ord("b"):
            for cap in caps:
                cap.set(cv2.CAP_PROP_POS_MSEC, frame_msec - 1000)


if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)
    app()
