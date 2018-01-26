import math


class MergedPerson:
    '''
    Merged person from multiple views
    '''

    def __init__(self, sensed_persons):
        '''
        Initialize
        :param sensed_persons: Sensed persons involved in merge
        '''
        self.sensed_persons = sensed_persons
        self.position = None
        self.head_direction = None
        self.head_direction_min = None
        self.head_direction_max = None
        self.stand_probability = None
        self.detection = []
        self.id = None
        self.__updateMergedResults()

    def __updateMergedResults(self):
        '''
        Generate fields from sensed persons
        :return: 
        '''
        self.position = (0, 0)
        self.head_direction = None
        self.head_direction_min = None
        self.head_direction_max = None

        # obtain means of sensed persons fields
        count = 0
        stand_probability_count = 0
        for sensed_person in self.sensed_persons:
            if sensed_person is not None:
                self.position = (self.position[0] + sensed_person.tracked_person.position[0],
                                 self.position[1] + sensed_person.tracked_person.position[1])

                self.detection.append(sensed_person.tracked_person.detection)

                if sensed_person.tracked_person.stand_probability is not None:
                    if self.stand_probability is None:
                        self.stand_probability = sensed_person.tracked_person.stand_probability
                    else:
                        self.stand_probability += sensed_person.tracked_person.stand_probability
                    stand_probability_count += 1

                if sensed_person.tracked_person.head_direction is not None:
                    if self.head_direction is None:
                        self.head_direction = (
                        sensed_person.tracked_person.head_direction[0], sensed_person.tracked_person.head_direction[1])
                    else:
                        self.head_direction = (self.head_direction[0] + sensed_person.tracked_person.head_direction[0],
                                               self.head_direction[1] + sensed_person.tracked_person.head_direction[1])

                if sensed_person.tracked_person.head_direction_min is not None:
                    if self.head_direction_min is None:
                        self.head_direction_min = (sensed_person.tracked_person.head_direction_min[0],
                                                   sensed_person.tracked_person.head_direction_min[1])
                    else:
                        self.head_direction_min = (
                            self.head_direction_min[0] + sensed_person.tracked_person.head_direction_min[0],
                            self.head_direction_min[1] + sensed_person.tracked_person.head_direction_min[1])

                if sensed_person.tracked_person.head_direction_max is not None:
                    if self.head_direction_max is None:
                        self.head_direction_max = (sensed_person.tracked_person.head_direction_max[0],
                                                   sensed_person.tracked_person.head_direction_max[1])
                    else:
                        self.head_direction_max = (
                            self.head_direction_max[0] + sensed_person.tracked_person.head_direction_max[0],
                            self.head_direction_max[1] + sensed_person.tracked_person.head_direction_max[1])

                count += 1
        self.position = (self.position[0] / count, self.position[1] / count)

        # Normalize results
        if self.head_direction is not None:
            head_direction_length = math.sqrt(self.head_direction[0] ** 2 + self.head_direction[1] ** 2)
            self.head_direction = (
            self.head_direction[0] / head_direction_length, self.head_direction[1] / head_direction_length)

        if self.head_direction_min is not None:
            head_direction_min_length = math.sqrt(self.head_direction_min[0] ** 2 + self.head_direction_min[1] ** 2)
            self.head_direction_min = (self.head_direction_min[0] / head_direction_min_length,
                                       self.head_direction_min[1] / head_direction_min_length)

        if self.head_direction_max is not None:
            head_direction_max_length = math.sqrt(self.head_direction_max[0] ** 2 + self.head_direction_max[1] ** 2)
            self.head_direction_max = (self.head_direction_max[0] / head_direction_max_length,
                                       self.head_direction_max[1] / head_direction_max_length)

        if self.stand_probability is not None:
            self.stand_probability = self.stand_probability / stand_probability_count


class MapAggregator:
    '''
    Merges multiple single camera maps
    '''

    def __init__(self):
        '''
        Initialize
        '''
        self.merged_map = None
        self.distance_threshold = 60

    def mergeMaps(self, sensors):
        '''
        Merge maps from multiple sensors
        :param sensors: List of sensors to be considered
        :return: 
        '''
        sort_tuples = []
        # Add none to each sensed
        _self_copy = self

        def _makeTuples(index, history_tuple):
            '''
            Generate tuples having combinations of persons from different sensors
            :param index: current map index
            :param history_tuple: history of persons considered
            :return:
            '''
            if index == len(sensors):
                # now obtain midpoint and distance
                mean = (0, 0)
                count = 0
                for sensed_person in history_tuple:
                    if sensed_person is not None:
                        count += 1
                        mean = (mean[0] + sensed_person.tracked_person.position[0],
                                mean[1] + sensed_person.tracked_person.position[1])
                if count > 0:
                    # its not all None
                    mean = (mean[0] / count, mean[1] / count)
                    # find distance from median
                    distance_check_passed = True
                    for sensed_person in history_tuple:
                        if sensed_person is not None:
                            distance = math.sqrt((sensed_person.tracked_person.position[0] - mean[0]) ** 2 + (
                            sensed_person.tracked_person.position[1] - mean[1]) ** 2)
                            if distance > self.distance_threshold:
                                distance_check_passed = False
                                break
                    # add only if distance to each point from median is less than threshold
                    if distance_check_passed:
                        sort_tuples.append(history_tuple)
            else:
                sensor = sensors[index]
                for sensed_person in (list(sensor.sensed_persons.values()) + [None]):
                    new_tuple = history_tuple + (sensed_person,)
                    _makeTuples(index + 1, new_tuple)

        # make tuples combining persons from different sensors
        _makeTuples(0, ())

        def _sortKey(sort_tuple):
            '''
            Comparator used to sort tuples.
            First sort by number of matching persons, in decreasing order. Then by distance in increasing order.
            :param sort_tuple: 
            :return: 
            '''
            distance_score = 0
            count = 0
            for item in sort_tuple:
                if item is not None:
                    count += 1

            centroid = (0, 0)
            for sensed_person in sort_tuple:
                if sensed_person is not None:
                    centroid = (centroid[0] + sensed_person.tracked_person.position[0],
                                centroid[1] + sensed_person.tracked_person.position[1])
            centroid = (centroid[0] / float(count), centroid[1] / float(count))

            for sensed_person in sort_tuple:
                if sensed_person is not None:
                    distance_score += math.sqrt((centroid[0] - sensed_person.tracked_person.position[0]) ** 2 + (
                        centroid[1] - sensed_person.tracked_person.position[1]) ** 2)
            return 1.0 / count, distance_score

        # sort in distance increasing order
        sorted_tuples = sorted(sort_tuples, key=_sortKey)

        # list of lists holding used sensed persons
        used_sensed_persons = []
        for sensor in sensors:
            used_sensed_persons.append([])

        merged_persons = []

        for sorted_tuple in sorted_tuples:
            unmapped_tuple = True
            for i in range(len(sensors)):
                if sorted_tuple[i] in used_sensed_persons[i]:
                    # this point has been mapped already. Drop it
                    unmapped_tuple = False
                    break
            if unmapped_tuple:
                # mark the tuple persons as mapped
                for i in range(len(sensors)):
                    if sorted_tuple[i] is not None:
                        used_sensed_persons[i].append(sorted_tuple[i])
                mp = MergedPerson(sorted_tuple)
                merged_persons.append(mp)

        self.merged_map = merged_persons
