import math
from random import randint
from time import time

import cv2
import numpy as np

from OpenPersonDetector import OpenPersonDetector
from PTEMapper import PTEMapper
from test_videos.VideoLoader import load_ntb_middle

queue_zone = ((135, 175), (250, 175), (250, 246), (135, 246))
QUEUE_RESHOLD = 50


def process_video():
    capture, markers, map_markers = load_ntb_middle()
    detector = OpenPersonDetector(preview=False)
    position_mapper = PTEMapper(markers, map_markers)

    background_image = cv2.imread("test_videos/ntb_branch.jpg")

    cv2.namedWindow("Map")
    # cv2.setMouseCallback("Map", mouse_listener)

    video_frame_time = 0
    process_time = 0
    while True:
        ret, frame = capture.read()

        while video_frame_time + process_time > capture.get(cv2.CAP_PROP_POS_MSEC) / 1000:
            ret, frame = capture.read()

        start_time = time()
        video_frame_time = capture.get(cv2.CAP_PROP_POS_MSEC) / 1000
        frame = cv2.resize(frame, (0, 0), fx=0.5, fy=0.5)

        if not ret:
            break

        detections = detector.detectPersons(frame, None)
        locations = []
        map_image = background_image.copy()
        for detection in detections:
            (map_x, map_y) = map(int, detection.leg_point)
            mapped_point = position_mapper.mapScreenToWorld(map_x, map_y)
            mapped_point = (int(mapped_point[0][0]), int(mapped_point[1][0]))

            # Drop failed mappings
            if np.math.isnan(mapped_point[0]) or np.math.isnan(mapped_point[1]):
                continue

            locations.append(mapped_point)
            cv2.drawMarker(frame, (map_x, map_y), (0, 255, 0), thickness=2)
            cv2.drawMarker(map_image, mapped_point, (0, 0, 255), thickness=2)

        for point in queue_zone:
            cv2.drawMarker(map_image, (point[0], point[1]), (255, 0, 0), thickness=2)

        inside, queues = process_queue(locations)
        for location in inside:
            cv2.drawMarker(map_image, location, (0, 255, 255), thickness=2)

        for queue in queues[:1]:
            r = randint(0, 255)
            g = randint(0, 255)
            b = randint(0, 255)
            for i in range(1, len(queue)):
                cv2.line(map_image, queue[i], queue[i - 1], color=(r, g, b), thickness=2)

        cv2.imshow("Map", map_image)
        cv2.imshow("Preview", frame)

        process_time = time() - start_time

        key = cv2.waitKey(1)
        if key & 0xFF == ord('q'):
            break

    capture.release()
    cv2.destroyAllWindows()


def process_queue(locations):
    # Find people within the queue zone
    inside = []
    for location in locations:
        dist = cv2.pointPolygonTest(np.array(queue_zone), location, True)
        # print("{} is {} far".format(location, dist))
        if dist > -20:
            inside.append(location)

    # Now, let's build the queue
    queues = []
    for point in inside:
        queue = []

        previous = point
        queue.append(previous)

        distances = [(x, math.sqrt((x[0] - previous[0]) ** 2 + (x[1] - previous[1]) ** 2)) for x in inside if
                     x is not point]

        while len(distances) > 0:
            # Get closest two
            distances.sort(key=lambda x: x[1])
            index = 0

            found = False
            while index < len(distances):
                next_location = distances[index][0]
                next_obj = distances[index]
                index += 1

                if len(queue) > 1:
                    last = np.array(queue[-1])
                    first = np.array(queue[-2])
                    current = np.array(next_location)
                    cosine = np.dot(last - first, current - last) / np.linalg.norm(last - first) / np.linalg.norm(
                        current - last)
                    angle = np.arccos(np.clip(cosine, -1, 1))
                    dist = np.linalg.norm(current - last)

                    print(angle)
                    if angle < 1 and dist < 50:
                        # Found our next item in the queue
                        queue.append(next_location)
                        distances.remove(next_obj)
                        found = True
                        break
                else:
                    queue.append(next_location)
                    distances.remove(next_obj)
                    found = True
                    break

            # Couldn't find the next person in the queue. So, stop. This queue is not working
            if not found:
                break

        queues.append(queue)

    queues.sort(key=lambda q: len(q))
    queues.reverse()
    return inside, queues


def mouse_listener(event, x, y, flags, param):
    print("Clicked ({},{})".format(x, y))


if __name__ == "__main__":
    process_video()
