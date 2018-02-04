from __future__ import print_function

import cv2
import imutils
import numpy as np
from cv2 import HOGDescriptor
from imutils.object_detection import non_max_suppression

from test_videos.VideoLoader import load_ntb_middle


def run_hog():
    # initialize the HOG descriptor/person detector
    hog = HOGDescriptor()
    hog.winSize = (48, 96)
    hog.setSVMDetector(cv2.HOGDescriptor_getDaimlerPeopleDetector())

    capture, markers, map_markers = load_ntb_middle()
    while True:
        ret, image = capture.read();

        # load the image and resize it to (1) reduce detection time
        # and (2) improve detection accuracy
        image = imutils.resize(image, width=min(400, image.shape[1]))
        orig = image.copy()

        # detect people in the image
        (rects, weights) = hog.detectMultiScale(image, winStride=(4, 4), padding=(8, 8), scale=1.05)

        # draw the original bounding boxes
        for (x, y, w, h) in rects:
            cv2.rectangle(orig, (x, y), (x + w, y + h), (0, 0, 255), 2)

        # apply non-maxima suppression to the bounding boxes using a
        # fairly large overlap threshold to try to maintain overlapping
        # boxes that are still people
        rects = np.array([[x, y, x + w, y + h] for (x, y, w, h) in rects])
        pick = non_max_suppression(rects, probs=None, overlapThresh=0.65)

        # draw the final bounding boxes
        for (xA, yA, xB, yB) in pick:
            cv2.rectangle(image, (xA, yA), (xB, yB), (0, 255, 0), 2)

        # show some information on the number of bounding boxes
        print("[INFO] : {} original boxes, {} after suppression".format(len(rects), len(pick)))

        # show the output images
        cv2.imshow("Before NMS", orig)
        cv2.imshow("After NMS", image)

        key = cv2.waitKey(1)
        if key & 0xFF == ord('q'):
            break
    capture.release()

# def run_face_recognition():



if __name__ == "__main__":
    run_hog()
