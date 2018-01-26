import logging
from threading import RLock

import cv2
import numpy as np
import face_recognition


def initLibrary(model_path):
    '''
    Loads Python wrapper on OpenPose (OpenPersonDetectorAPI)
    :param preview: True to enable OP Preview
    :param net_width: Detector net width
    :param net_height: Detector net height
    :return: 
    '''
    if "person_detector_api" in globals():
        logging.info("Library Init Already")
        return
    global person_detector_api
    person_detector_api = None
    from detectors.TFODDetector.DetectorAPI import DetectorAPI
    person_detector_api = DetectorAPI(model_path)


MAX_VALUE = 1000000

lock = RLock()

class TFODPersonDetector:
    '''
    Person Detector and Leg Estimator based on OpenPose Project.
    OpenPose Project Repo: https://github.com/CMU-Perceptual-Computing-Lab/openpose
    '''

    def __init__(self, model_path="./detectors/TFODDetector/models/faster_rcnn_inception_v2_coco_2017_11_08/frozen_inference_graph.pb",
                 person_category=1,threshold=0.6, preview=False, useFaceDetection = False):
        '''
        Initialize Person Detector
        '''
        lock.acquire()
        self.person_category = person_category
        global person_detector_api
        self.preview = preview
        self.threshold = threshold
        self.useFaceDetection = useFaceDetection
        initLibrary(model_path)
        lock.release()

    def detectPersons(self, colour_frame, gray_frame):
        '''
        Detect persons in frame
        :param colour_frame: colour frame
        :param gray_frame: gray frame (unused)
        :return: 
        '''

        lock.acquire()

        global person_detector_api

        # Obtain results
        boxes, scores, classes, num = person_detector_api.processFrame(colour_frame)
        face_detections = {}
        rois = {}

        for i in range(len(boxes)):
            if classes[i] == self.person_category and scores[i] > self.threshold:
                box = boxes[i]
                roi = np.array(colour_frame[box[0]:box[2],box[1]:box[3]],copy=True)
                rois[i] = roi
                if self.useFaceDetection:
                    fds = face_recognition.face_locations(roi,2)
                    for top,right,bottom,left in fds:
                        face_detections[i] = (left + box[1],top + box[0],right + box[1],bottom + box[0])

        # Obtain scale factorscv2.imshow("roi"+ str(i),roi)
        scale_factor = 1

        # Preview output
        if self.preview:
            img = np.array(colour_frame,copy=True)
            for i in range(len(boxes)):
                if classes[i] == self.person_category and scores[i] > self.threshold:
                    box = boxes[i]
                    cv2.rectangle(img, (box[1], box[0]), (box[3], box[2]), (255, 0, 0), 2)
                    if i in face_detections.keys():
                        face_detection = face_detections[i]
                        print("FD:",face_detection)
                        cv2.rectangle(img, (face_detection[0], face_detection[1]), (face_detection[2], face_detection[3]), (0, 255, 0), 1)
            cv2.imshow("preview", img)

        person_detections = []

        # Process results
        for i in range(len(boxes)):
            # Add Detection
            if classes[i] == self.person_category and scores[i] > self.threshold and self.boxCheck(boxes[i],colour_frame.shape):
                box = boxes[i]
                person_detection = PersonDetection()
                person_detections.append(person_detection)

                person_detection.person_bound = (int(box[1]),int(box[0]),int(box[3]),int(box[2]))
                person_detection.upper_body_bound = (person_detection.person_bound[0],
                                                  person_detection.person_bound[1],
                                                  person_detection.person_bound[2],
                                                  int((person_detection.person_bound[3] + person_detection.person_bound[1])/2))
                person_detection.central_bound = person_detection.person_bound
                person_detection.central_point = (int((person_detection.central_bound[0] +
                                                       person_detection.central_bound[2])/2),
                                                  int((person_detection.central_bound[1] +
                                                       person_detection.central_bound[3]) / 2))
                person_detection.estimated_leg_point = (person_detection.central_point[0], person_detection.person_bound[3])
                person_detection.head_direction = None
                person_detection.head_direction_error = None
                person_detection.leg_count = 2
                person_detection.leg_point = person_detection.estimated_leg_point
                person_detection.neck_hip_ankle_ratio = None
                person_detection.neck_hip_knee_ratio = None
                person_detection.tracked_points = {}
                if i in rois.keys():
                    person_detection.roi = rois[i]

        lock.release()
        return person_detections

    def boxCheck(self, bounds, frame_shape):
        frame_height = frame_shape[0]
        frame_width = frame_shape[1]
        height = bounds[2]-bounds[0]
        width = bounds[3] - bounds[1]
        if width / frame_width > 0.2:
            return False
        if height / frame_height > 0.2:
            return False
        if width > height:
            return False
        return True



class PersonDetection:
    '''
    Detection of a person
    '''

    def __init__(self):
        self.tracked_points = {}  # Points detected by OP
        self.person_bound = None  # Boundary of person
        self.central_bound = None  # Boundary of central body of person (no hands and feet for X coordinate)
        self.upper_body_bound = None  # Boundary of upper body of person
        self.central_point = None  # Central point of person
        self.leg_point = None  # Average Feet point of person
        self.leg_count = None  # Number of detected feet
        self.estimated_leg_point = None  # Estimated feet point of person
        self.neck_hip_ankle_ratio = None
        self.neck_hip_knee_ratio = None
        self.head_direction = None
        self.head_direction_error = None
        self.roi = None
