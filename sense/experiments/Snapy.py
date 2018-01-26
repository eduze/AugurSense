'''
Obtains snaps of persons when required for Re-Id
'''
import cv2
import logging

from Util import restEncodeImage

capture_times = [1000, 5000]  # in miliseconds


class SnappedPerson:
    '''
    A person snapped by Snapy
    '''

    def __init__(self, tracked_person, detected_time):
        '''
        :param tracked_person: from TrackerFrame
        :param detected_time: time of first appearance
        '''
        self.snaps = []
        self.tracked_person = tracked_person
        self.detected_time = detected_time


class Snap:
    '''
    A snap of a snapped person
    '''

    def __init__(self, image, time):
        '''
        Constructor
        :param image: Snapped image 
        :param time: Time of snap
        '''
        self.image = image
        self.time = time


class Snapy:
    '''
    Obtains snapshots for re-id purpose
    '''

    def __init__(self):
        '''
        Initialize
        '''
        self.snapped_persons = {}

    def processFrame(self, frame, frame_time, tracked_persons):
        '''
        Processes a Frame and returns snapped (if any) obtained for the frame
        :param frame: frame to be processed
        :param frame_time: time
        :param tracked_persons: tracked persons in frame 
        :return: Dictionary of tracker_label, newly snapped image key value pairs
        '''
        results = {}  # Contains dictionary of images of new persons to be reported
        for tracked_person in tracked_persons:
            if tracked_person.label in self.snapped_persons.keys():
                snapped_person = self.snapped_persons[tracked_person.label]  # find matching snapped person

                for capture_time in capture_times:  # Iterate each capture rule
                    if (frame_time - snapped_person.detected_time) > capture_time and (
                            len(snapped_person.snaps) == 0 or (
                            snapped_person.snaps[-1].time - snapped_person.detected_time) < capture_time):
                        # print(tracked_person.label,frame_time-snapped_person.detected_time, capture_time, len(snapped_person.snaps))
                        # Time to take a new snap
                        detection = tracked_person.detection
                        (dc_x, dc_y, dc_w, dc_h) = map(int, detection.person_bound)

                        # Expand bounds
                        width = (dc_w - dc_x) / 2
                        height = (dc_h - dc_y) / 2

                        (dc_x, dc_y, dc_w, dc_h) = map(int, (
                            dc_x - width / 2, dc_y - height / 2, dc_w + width / 2, dc_h + height / 2))

                        if dc_x < 0 or dc_y < 0 or dc_w >= frame.shape[1] or dc_h >= frame.shape[0]:
                            continue

                        snap = frame[dc_y:dc_h, dc_x:dc_w]
                        if snap.shape[0] > 0 and snap.shape[1] > 0:
                            logging.info("Snapped " + str(tracked_person.label) + " " + str(
                                frame_time - snapped_person.detected_time) + " " + str(capture_time) + " " + str(
                                len(snapped_person.snaps)))
                            snapped_person.snaps.append(Snap(snap, frame_time))
                            results[tracked_person.label] = restEncodeImage(snap)
                            # cv2.imshow(str(tracked_person.label), snap)

            else:
                self.snapped_persons[tracked_person.label] = SnappedPerson(tracked_person, frame_time)

        return results
