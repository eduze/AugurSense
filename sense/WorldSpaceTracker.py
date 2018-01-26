label_count = 0


def generateLabel():
    '''
    Generates a new tracking label
    :return: 
    '''
    global label_count
    label_count += 1
    return label_count


class MappedPerson:
    '''
    Mapped person
    '''

    def __init__(self, position, detection, head_direction):
        self.position = position
        self.head_direction = head_direction
        self.id = None
        self.detection = detection
        self.head_direction_min = head_direction
        self.head_direction_max = head_direction
        self.stand_probability = 0.5


class TrackedPerson:
    '''
    Tracked person
    '''

    def __init__(self, position, label, detection, head_direction):
        self.position = position
        self.head_direction = head_direction
        self.label = label
        self.predictor = None
        self.detection = detection
        self.age = 0
        self.head_direction_min = head_direction
        self.head_direction_max = head_direction
        self.stand_probability = 0.5
        self.time = None
        self.previous = None

        self.multiPrevious = []


class WorldSpaceFrame:
    '''
    World space time frame of persons
    '''

    def __init__(self):
        self.time = None
        self.persons = {}


class AbstractPredictor:
    '''
    Abstract Predictor
    '''

    def __init__(self):
        pass

    def predict(self, elapsed_time):
        pass

    def correct(self, coordinates):
        pass


class NaivePredictor(AbstractPredictor):
    '''
    Very simple predictor which works fine
    '''

    def __init__(self, position, elapsed_time):
        AbstractPredictor.__init__(self)
        self.position = position

    def predict(self, elapsed_time):
        return self.position

    def correct(self, coordinates):
        self.position = coordinates


class WorldSpaceTracker:
    '''
    World Space Tracker using NaivePredictor
    '''

    def __init__(self):
        self.current_frame = None
        self.default_predictor = NaivePredictor
        self.detection_radius2 = 40 * 40
        self.close_radius2 = 10 * 10
        self.close_radius2 = None
        self.multitrack_radius2 = 40 * 40

    def getSearchRadius2(self, elapsed_time, predictor):
        '''
        Return search area
        :param elapsed_time: 
        :param predictor: 
        :return: 
        '''
        return self.detection_radius2

    def nextFrame(self, persons, new_time):
        '''
        Generates tracking labels for persons at new_time
        :param persons: 
        :param new_time: 
        :return: 
        '''
        if self.current_frame is None:
            # No previous trackings.
            self.current_frame = WorldSpaceFrame()
            self.current_frame.time = new_time
            for person in persons:
                tracked_person = TrackedPerson(person.position, generateLabel(), person.detection,
                                               person.head_direction)
                tracked_person.head_direction_min = person.head_direction_min
                tracked_person.head_direction_max = person.head_direction_max
                tracked_person.stand_probability = person.stand_probability
                tracked_person.predictor = self.default_predictor(person.position, elapsed_time=new_time)
                tracked_person.time = new_time
                tracked_person.previous = None
                self.current_frame.persons[tracked_person.label] = tracked_person
        else:
            # obtain next predictions of each current tracked person
            delta_time = new_time - self.current_frame.time
            predicted_frame = WorldSpaceFrame()
            predicted_frame.time = new_time
            for person in self.current_frame.persons.values():
                predicted_person = TrackedPerson(person.predictor.predict(delta_time), person.label, person.detection,
                                                 person.head_direction)
                predicted_person.head_direction_min = person.head_direction_min
                predicted_person.head_direction_max = person.head_direction_max
                predicted_person.stand_probability = person.stand_probability
                predicted_person.predictor = person.predictor
                predicted_person.age = person.age
                predicted_person.time = new_time
                predicted_person.previous = person
                predicted_frame.persons[predicted_person.label] = predicted_person

            new_persons = persons
            # make pairs of predicted persons and new persons
            person_pairs = []

            next_new_person_id = 0
            for new_person in new_persons:
                new_person.id = next_new_person_id
                next_new_person_id += 1

                for predicted_person in predicted_frame.persons.values():
                    max_radius2 = self.getSearchRadius2(delta_time, predicted_person.predictor)
                    distance2 = ((new_person.position[0] - predicted_person.position[0]) ** 2 + (
                            new_person.position[1] - predicted_person.position[1]) ** 2)
                    if distance2 < max_radius2:
                        person_pairs.append((distance2, predicted_person, new_person))

            # sort pairs
            mapped_prediction_labels = {}
            mapped_person_ids = {}
            person_pairs = sorted(person_pairs, key=lambda x: x[0])

            new_frame = WorldSpaceFrame()
            new_frame.time = new_time

            # find labels of new_persons using predictions
            for distance2, predicted_person, new_person in person_pairs:
                if predicted_person.label not in mapped_prediction_labels.keys():
                    if new_person.id not in mapped_person_ids.keys():
                        # mark used labels
                        mapped_prediction_labels[predicted_person.label] = new_person.id
                        mapped_person_ids[new_person.id] = predicted_person.label

                        # Generate tracked person
                        new_tracked_person = TrackedPerson(new_person.position, predicted_person.label,
                                                           new_person.detection, new_person.head_direction)
                        new_tracked_person.head_direction_min = new_person.head_direction_min
                        new_tracked_person.head_direction_max = new_person.head_direction_max
                        new_tracked_person.stand_probability = new_person.stand_probability
                        new_tracked_person.predictor = predicted_person.predictor
                        new_tracked_person.age = predicted_person.age + 1
                        new_tracked_person.predictor.correct(new_tracked_person.position)
                        new_tracked_person.time = new_time
                        new_tracked_person.previous = predicted_person.previous

                        # adding multi-track
                        if self.multitrack_radius2 is not None:
                            for p_person in predicted_frame.persons.values():
                                distance__2 = ((new_person.position[0] - p_person.position[0]) ** 2 + (
                                        new_person.position[1] - p_person.position[1]) ** 2)
                                if distance__2 < self.multitrack_radius2:
                                    new_tracked_person.multiPrevious.append(p_person.previous)

                        new_frame.persons[new_tracked_person.label] = new_tracked_person
                    else:
                        # the person is already assigned to a track
                        distance_2 = ((new_person.position[0] - predicted_person.position[0]) ** 2 + (
                                new_person.position[1] - predicted_person.position[1]) ** 2)

                        # # adding multi-track
                        # if distance_2 < self.multitrack_radius2:
                        #     new_frame.persons[mapped_person_ids[new_person.id]].multiPrevious.append(
                        #         predicted_person.previous)

                        if self.close_radius2 is not None and distance_2 < self.close_radius2:
                            if new_person.id in mapped_person_ids.keys() and mapped_person_ids[
                                new_person.id] in new_frame.persons.keys():
                                del new_frame.persons[mapped_person_ids[new_person.id]]
                                del mapped_person_ids[new_person.id]  # TODO: Check indentation here

                else:
                    # the prediction already has a mapping
                    distance_2 = ((new_person.position[0] - predicted_person.position[0]) ** 2 + (
                            new_person.position[1] - predicted_person.position[1]) ** 2)

                    if self.close_radius2 is not None and distance_2 < self.close_radius2:
                        del mapped_prediction_labels[predicted_person.label]
                        if predicted_person.label in new_frame.persons.keys():
                            if new_person.id in mapped_person_ids.keys():
                                del mapped_person_ids[new_person.id]  # TODO: Check logic to remove mapped person id
                            del new_frame.persons[predicted_person.label]

            # now identify new persons
            for new_person in new_persons:
                if new_person.id not in mapped_person_ids:
                    new_tracked_person = TrackedPerson(new_person.position, generateLabel(), new_person.detection,
                                                       new_person.head_direction)
                    new_tracked_person.head_direction_min = new_person.head_direction_min
                    new_tracked_person.head_direction_max = new_person.head_direction_max
                    new_tracked_person.stand_probability = new_person.stand_probability
                    new_tracked_person.predictor = self.default_predictor(new_person.position, elapsed_time=new_time)
                    new_tracked_person.age = 0
                    new_tracked_person.time = new_time
                    new_tracked_person.previous = None

                    # adding multi-track
                    if self.multitrack_radius2 is not None:
                        for p_person in predicted_frame.persons.values():
                            distance__2 = ((new_person.position[0] - p_person.position[0]) ** 2 + (
                                    new_person.position[1] - p_person.position[1]) ** 2)
                            if distance__2 < self.multitrack_radius2:
                                new_tracked_person.multiPrevious.append(p_person.previous)

                    new_frame.persons[new_tracked_person.label] = new_tracked_person

            self.current_frame = new_frame
