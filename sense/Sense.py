import numpy as np

from WorldSpaceTracker import MappedPerson
from experiments.StandSitPredictor import predictStandProbability


class SensedPerson:
    '''
    Persen identified by Sense
    '''

    def __init__(self):
        self.tracked_person = None
        self.re_id_data = None
        self.tracking_history = []


class Sense:
    '''
    Core engine which interconnects detection, mapping, tracking and re_id
    '''

    def __init__(self, detector, position_mapper, angle_mapper, tracker, re_id):
        '''
        Initialize Sense
        :param detector: person detector  
        :param position_mapper: PTEMapper
        :param angle_mapper: AngleMapper
        :param tracker: World space tracker
        :param re_id: Re_Id support tool (Snapy)
        '''
        self.detector = detector
        self.position_mapper = position_mapper
        self.angle_mapper = angle_mapper
        self.tracker = tracker
        self.re_id = re_id
        self.frameTime = 0

        self.detections = []
        self.sensed_persons = {}
        self._tracked_trails = {}

    def clearTrackingHistory(self):
        '''
        Clear tracking history
        :return: 
        '''
        self._tracked_trails.clear()

    def processFrame(self, colour_frame, gray_frame, frameTime):
        '''
        Process a frame using sense and update sensed_persons and detections fields
        :param colour_frame: colour image
        :param gray_frame: gray image
        :param frameTime: time of frame
        :return: 
        '''
        self.frameTime = frameTime
        self.sensed_persons.clear()
        self.detections.clear()

        new_tracked_trails = {}

        # Detect persons
        persons = self.detector.detectPersons(colour_frame, gray_frame)

        if self.position_mapper.isReady():
            mapped_persons = []
            for person in persons:
                self.detections.append(person)
                # Do Perspective Transform Estimation
                if person.leg_count == 0:  # Do not map when no logs are visible
                    continue
                (map_x, map_y) = person.leg_point

                mapped_person_point = self.position_mapper.mapScreenToWorld(map_x, map_y)

                # Drop failed mappings
                if np.math.isnan(mapped_person_point[0]) or np.math.isnan(mapped_person_point[1]):
                    continue

                mapped_person_point = (int(mapped_person_point[0]), int(mapped_person_point[1]))

                # Map head direction

                head_direction = self.angle_mapper.transformAngle((map_x, map_y), person.head_direction)

                head_direction_min = None
                head_direction_max = None

                if person.head_direction is not None and person.head_direction_error is not None:
                    head_direction_min = self.angle_mapper.transformAngle((map_x, map_y),
                                                                          person.head_direction - person.head_direction_error)
                    head_direction_max = self.angle_mapper.transformAngle((map_x, map_y),
                                                                          person.head_direction + person.head_direction_error)

                # Store mappings
                mapped_person = MappedPerson(mapped_person_point, person, head_direction)
                mapped_person.head_direction_max = head_direction_max
                mapped_person.head_direction_min = head_direction_min
                mapped_person.stand_probability = predictStandProbability(person.neck_hip_knee_ratio)
                mapped_persons.append(mapped_person)

            # Track with previous frame
            self.tracker.nextFrame(mapped_persons, frameTime)

            # Obtain post track frame
            tracked_frame = self.tracker.current_frame
            new_coordinates = {}

            for person in tracked_frame.persons.values():
                # Maintain tracking trail
                if person.label in self._tracked_trails.keys():
                    self._tracked_trails[person.label].append(person)
                    new_tracked_trails[person.label] = self._tracked_trails[person.label]
                else:
                    new_tracked_trails[person.label] = [person]

            # Obtain re_id data
            re_id_results = self.re_id.processFrame(colour_frame, frameTime, tracked_frame.persons.values())

            for person in tracked_frame.persons.values():
                sensed_person = SensedPerson()
                sensed_person.tracked_person = person
                sensed_person.tracking_history = new_tracked_trails[person.label]
                if person.label in re_id_results.keys():
                    sensed_person.re_id_data = re_id_results[person.label]
                self.sensed_persons[person.label] = sensed_person

        self._tracked_trails = new_tracked_trails
