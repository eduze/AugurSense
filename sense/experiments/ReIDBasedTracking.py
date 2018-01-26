import json
import os
import cv2
from re_id.cvpr_reid.ReIDAPI import ReIDAPI

import numpy as np
import math

re_id = ReIDAPI()


class Person:
    def __init__(self, position, timestamp):
        self.position = position
        self.timestamp = timestamp
        self.image = None
        self.prev = []
        self.file_name = None

    def getDistance(self, other):
        if isinstance(other, tuple):
            d = math.sqrt((other[0] - self.position[0]) ** 2 + (other[1] - self.position[1]) ** 2)
        elif isinstance(other, Person):
            d = math.sqrt((other.position[0] - self.position[0]) ** 2 + (other.position[1] - self.position[1]) ** 2)
        else:
            assert False
        return d

    def __repr__(self):
        return self.file_name

    def getMatchScoreAgainst(self, p2):
        return getReIdConfidence(self, p2)


class Frame:
    def __init__(self, timestamp):
        self.persons = []
        self.timestamp = timestamp


class View:
    def __init__(self, camera_id):
        self.frames = {}
        self.camera_id = camera_id


def loadData(path):
    views = {}
    files = os.listdir(path)
    for file in files:
        p_string = file.split(".")[0]
        params = p_string.split("_")
        time_frame = int(params[0])
        x_coord = int(params[1])
        y_coord = int(params[2])
        cam_id = int(params[3])
        image = cv2.imread(os.path.join(path, file))

        if cam_id not in views.keys():
            views[cam_id] = View(cam_id)

        if time_frame not in views[cam_id].frames.keys():
            views[cam_id].frames[time_frame] = Frame(time_frame)

        p = Person((x_coord, y_coord), time_frame)
        p.image = image
        p.file_name = file
        views[cam_id].frames[time_frame].persons.append(p)

    return views


def generatePrev(view, time_frames, threshold=40 * 40):
    prev_map = []
    new_map = []
    for t in time_frames:
        for p in view.frames[t].persons:
            for prev_p in prev_map:
                d = (p.position[0] - prev_p.position[0]) ** 2 + (p.position[1] - prev_p.position[1]) ** 2
                if d < threshold:
                    p.prev.append((d, prev_p))
            new_map.append(p)
            p.prev = sorted(p.prev, key=lambda x: x[0])

        prev_map = new_map
        new_map = []


matched_pairs = {}


def loadReIdScores():
    global matched_pairs
    if os.path.isfile("re_id_cache.json"):
        with open("re_id_cache.json", "r") as f:
            matched_pairs = json.loads("\n".join(f.readlines()))


loadReIdScores()


def storeReIdScores():
    store_text = json.dumps(matched_pairs)
    if os.path.exists("re_id_cache.json"):
        if os.path.exists("re_id_cache.json.bak"):
            os.remove("re_id_cache.json.bak")
        os.rename("re_id_cache.json", "re_id_cache.json.bak")

    with open("re_id_cache.json", "w") as f:
        f.write(store_text)


def getReIdConfidence(p1, p2):
    if (p1.file_name + "_" + p2.file_name) in matched_pairs:
        return matched_pairs[p1.file_name + "_" + p2.file_name]
    print(p1.file_name, p2.file_name, end="\r")
    matched_pairs[p1.file_name + "_" + p2.file_name] = np.asscalar(re_id.getConfidence(p1.image, p2.image))

    return matched_pairs[p1.file_name + "_" + p2.file_name]


def showView(view, timeframe, selectedPersonIndex=None, use_re_id=False, limited_re_id=False, show_best_routes=True):
    # output = np.zeros((500,500),np.uint8)
    output = cv2.imread("test_videos/PETSMap.png")
    cv2.putText(output, "Time: " + str(timeframe), (10, 20), cv2.FONT_HERSHEY_COMPLEX, 0.5, (255, 255, 255))

    frame = view.frames[timeframe]

    for person in frame.persons:
        color = (255, 0, 0)
        index = frame.persons.index(person)
        if index == selected_person_index:
            color = (255, 255, 0)
        cv2.drawMarker(output, person.position, color, markerSize=15, markerType=cv2.MARKER_STAR)
        _shaddow_person = person

        def drawPrev(person, ttl=15):
            if ttl == 0:
                return

            for d, p in person.prev:
                if use_re_id:
                    if (not limited_re_id) or len(p.prev) <= 1:
                        confidence = getReIdConfidence(_shaddow_person, p)
                        print(_shaddow_person.file_name, p.file_name, "=>", confidence)
                        cv2.line(output, person.position, p.position, (0, confidence * 255, 0))
                        if len(p.prev) <= 1:
                            cv2.drawMarker(output, p.position, (0, confidence * 255, 0), markerType=cv2.MARKER_DIAMOND,
                                           markerSize=10)
                    else:
                        cv2.line(output, person.position, p.position, color)
                else:
                    cv2.line(output, person.position, p.position, color)
                drawPrev(p, ttl - 1)

        if index == selectedPersonIndex or selectedPersonIndex == -1:
            drawPrev(person)
            person = _shaddow_person
            if show_best_routes:
                best_route = findBestRoute(person)
                if best_route is not None:
                    print("Candidate:", person.file_name)
                    for p in best_route:
                        if p is not None:
                            cv2.drawMarker(output, p.position, (0, 0, 255), cv2.MARKER_TRIANGLE_UP, markerSize=10)
                            print("BestMatch:", p.file_name)
                    showImages(best_route)

            # results = []
            # findFreeParents(person,results,[])
            #
            # results=sorted(results,key=lambda x: (getReIdConfidence(x[0],person),findRouteWeight(person, x[1])), reverse=True)
            # print("Candidate:",person.file_name)
            # if len(results)>0:
            #     end,route = results[0]
            #     for p in route:
            #         cv2.drawMarker(output,p.position,(0,0,255),cv2.MARKER_TRIANGLE_UP,markerSize=10)
            #         print("BestMatch:",p.file_name)

    cv2.namedWindow("View " + str(view.camera_id), cv2.WINDOW_FREERATIO)

    cv2.imshow("View " + str(view.camera_id), output)


def showImages(persons, width=1280, height=720, title="Previews", annotations=None):
    results = np.zeros((height, width, 3), np.uint8)
    x = 0
    y = 0
    row_max = 0
    for person in persons:
        if person is None:
            x = 0
            y += row_max
            row_max = 0
            continue

        image = person.image
        if x + image.shape[1] >= width:
            x = 0
            y += row_max
            row_max = 0

        if y + image.shape[0] >= height:
            print("Overflow")
        else:
            results[y:y + image.shape[0], x:x + image.shape[1]] = image
            if annotations is not None:
                annot = annotations[persons.index(person)]
                cv2.putText(results, str(int(annot[0] * 1000)), (x, y + 10), cv2.FONT_HERSHEY_COMPLEX, 0.3,
                            (255, 255, 255))
                cv2.putText(results, str(int(annot[1] * 1000)), (x, y + 25), cv2.FONT_HERSHEY_COMPLEX, 0.3,
                            (255, 255, 255))
                cv2.putText(results, str(int(annot[2] * 1000)), (x, y + 40), cv2.FONT_HERSHEY_COMPLEX, 0.3,
                            (255, 255, 255))
            row_max = max(row_max, image.shape[0])
            x += image.shape[1]
    cv2.namedWindow(title, cv2.WINDOW_FREERATIO)
    cv2.imshow(title, results)


def findBestRoute(person):
    results = []
    best_route = findBestRouteToOpen(person)

    if best_route is None:
        return None

    results.extend(best_route)
    results.append(None)

    while best_route is not None and len(best_route) > 1:
        best_route = findBestRouteToOpen(best_route[-1])
        if best_route is not None:
            results.extend(best_route)
            results.append(None)

    return results


def findBestRouteToOpen(person):
    results = []
    findFreeParents(person, results, [])
    results = sorted(results, key=lambda x: (getReIdConfidence(x[0], person), findRouteWeight(person, x[1])),
                     reverse=True)
    if len(results) > 0:
        return results[0][1]
    return None


def findRouteWeight(target, route):
    score = 0
    for t in route:
        score += getReIdConfidence(target, t)
    return score


def findFreeParents(person, results, back_track):
    back_track = back_track + [person]
    for p in person.prev:
        if len(p[1].prev) <= 1:
            back_track = back_track + [p[1]]
            results.append((p[1], list(back_track[:])))
            return
        findFreeParents(p[1], results, back_track)


if __name__ == "__main__":
    data = loadData("output/")
    print(data)
    view = data[0]
    time_frames = sorted(list(view.frames.keys()))
    current_frame_index = 0
    selected_person_index = -1
    use_re_id = False
    show_best_routes = False

    generatePrev(view, time_frames)

    while True:
        showView(data[0], time_frames[current_frame_index], selectedPersonIndex=selected_person_index,
                 use_re_id=use_re_id, show_best_routes=show_best_routes)
        k = cv2.waitKey(0)
        if k == ord("d"):
            current_frame_index += 1
        elif k == ord("a"):
            current_frame_index -= 1
        elif k == ord("w"):
            selected_person_index += 1
        elif k == ord("s"):
            selected_person_index -= 1
        elif k == ord("r"):
            use_re_id = not use_re_id
        elif k == ord("z"):
            show_best_routes = not show_best_routes
        elif k == ord("q"):
            break
