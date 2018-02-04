from time import time

import cv2
import face_recognition

from detectors.TFODDetector.TFODPersonDetector import TFODPersonDetector

if __name__ == "__main__":
    cap = cv2.VideoCapture("/home/imesha/Desktop/city branch/Organized/Cash_Counter_1-1.dav")

    person_detector = TFODPersonDetector()

    cv2.namedWindow("output", cv2.WINDOW_AUTOSIZE)

    last_frame_time = None
    video_frame_time = None

    while True:
        if video_frame_time is None:
            r, frame = cap.read()
        else:
            skip_count = 0
            while video_frame_time + last_frame_time > cap.get(cv2.CAP_PROP_POS_MSEC) / 1000:
                r, frame = cap.read()
                if skip_count > 24:
                    break
                skip_count += 1
            print("Skipped Frames:", str(skip_count - 1))

        video_frame_time = cap.get(cv2.CAP_PROP_POS_MSEC) / 1000
        frame_start_time = time()

        frame = cv2.resize(frame, (0, 0), fx=0.4, fy=0.4)

        person_detections = person_detector.detectPersons(frame, frame)

        for detection in person_detections:
            cv2.rectangle(frame, (detection.person_bound[0], detection.person_bound[1]),
                          (detection.person_bound[2], detection.person_bound[3]), (0, 0, 255), 2)

            cropped_frame = frame[detection.person_bound[1]:detection.person_bound[3], detection.person_bound[0]:int(
                (detection.person_bound[2] + detection.person_bound[2]) * 2 / 4)]

            face_detections = face_recognition.face_locations(cropped_frame, number_of_times_to_upsample=2, model="cnn")
            for face_detection in face_detections:
                top, right, bottom, left = face_detection
                cv2.rectangle(frame, (left + detection.person_bound[0], top + detection.person_bound[1]),
                              (right + detection.person_bound[0], bottom + detection.person_bound[1]), (0, 255, 0), 2)

        cv2.imshow("output", frame)
        key = cv2.waitKey(1)

        frame_end_time = time()

        last_frame_time = frame_end_time - frame_start_time
        print("Last Frame Time:", (frame_end_time - frame_start_time))

        if key & 0xFF == ord('q'):
            break
