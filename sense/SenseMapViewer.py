import cv2
import numpy as np


class SenseMapViewer:
    '''
    Previews tracker outputs. Uses Kalman Filter to smooth outputs.
    '''

    def __init__(self, transform_mappers=None, is_editable_mapping=False, map_background = None):
        self.outputWidth = 640
        self.outputHeight = 480
        self.outputMap = None
        self.map_name = "Map"
        self.transform_mappers = transform_mappers
        self.active_transform_mapper_index = 0

        self.kalman_measurementMatrices = None
        self.kalman_transitionMatrices = None
        self.kalman_processNoiseCovs = None

        self.__mouse_move_x = 0
        self.__mouse_move_y = 0
        self.__mouse_l_down = False
        self.is_editable_mapping = is_editable_mapping

        self.map_background = map_background

    def clearMap(self):
        if self.map_background is None:
            self.outputMap = np.ones((self.outputHeight, self.outputWidth, 3), np.uint8) * 255
        else:
            self.outputMap = np.array(self.map_background,copy=True)

    def __getMouseListener(self):
        __self = self

        def clickListenerMap(event, x, y, flags, param):
            '''
            Mouse Click Listener for Map
            :param event: 
            :param x: 
            :param y: 
            :param flags: 
            :param param: 
            :return: 
            '''
            if __self.transform_mappers is None:
                return

            transform_mapper = __self.transform_mappers[__self.active_transform_mapper_index]

            if not __self.is_editable_mapping or transform_mapper is None:
                return

            if event == cv2.EVENT_LBUTTONDOWN:
                # Clear
                if len(transform_mapper.world_points) == 4:
                    transform_mapper.world_points.clear()
                transform_mapper.world_points.append((x, y))
                __self.__mouse_l_down = True
            elif event == cv2.EVENT_LBUTTONUP:
                __self.__mouse_l_down = False
            elif event == cv2.EVENT_MOUSEMOVE:
                __self.__mouse_move_x = x
                __self.__mouse_move_y = y
                if __self.__mouse_l_down:
                    transform_mapper.world_points[-1] = (x, y)
            elif event == cv2.EVENT_RBUTTONDOWN:
                __self.active_transform_mapper_index += 1
                __self.active_transform_mapper_index = __self.active_transform_mapper_index % len(
                    __self.transform_mappers)

        return clickListenerMap

    def setupWindows(self):
        cv2.namedWindow(self.map_name, cv2.WINDOW_FREERATIO)
        cv2.resizeWindow(self.map_name, self.outputWidth, self.outputHeight)

        cv2.setMouseCallback(self.map_name, self.__getMouseListener())

    def showMap(self, senses):

        if self.kalman_measurementMatrices is None:
            self.kalman_measurementMatrices = []
            self.kalman_transitionMatrices = []
            self.kalman_processNoiseCovs = []

            for i in range(len(senses)):
                self.kalman_measurementMatrices.append(np.array([[1, 0, 0, 0], [0, 1, 0, 0]], np.float32))
                self.kalman_transitionMatrices.append(
                    np.array([[1, 0, 1, 0], [0, 1, 0, 1], [0, 0, 1, 0], [0, 0, 0, 1]], np.float32))
                self.kalman_processNoiseCovs.append(np.array([[1, 0, 0, 0], [0, 1, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]],
                                                             np.float32) * 0.01)
        # self.outputMap = np.ones((self.outputHeight, self.outputWidth, 3), np.uint8) * 255
        self.clearMap()

        cv2.putText(self.outputMap, str(self.__mouse_move_x) + ", " + str(self.__mouse_move_y), (20, 20),
                    cv2.FONT_HERSHEY_COMPLEX, 0.5, (255, 0, 0))

        if self.transform_mappers is not None:
            index_colour = 0
            if len(senses) > 1:
                index_colour = self.active_transform_mapper_index * 255 / (len(self.transform_mappers) - 1)

            cv2.putText(self.outputMap, "Active " + str(self.active_transform_mapper_index), (20, 40),
                        cv2.FONT_HERSHEY_COMPLEX, 0.5, (index_colour, 0, 255))

        for i in range(len(senses)):
            index_colour = 0
            if len(senses) > 1:
                index_colour = i * 255 / (len(senses) - 1)

            sense = senses[i]

            transform_mapper = None

            if self.transform_mappers is not None:
                transform_mapper = self.transform_mappers[i]

            sensed_persons = sense.sensed_persons

            if transform_mapper is not None:
                for map_marker in transform_mapper.world_points:
                    cv2.drawMarker(self.outputMap, map_marker, (index_colour, 0, 255), thickness=2)

            for k, sensed_person in sensed_persons.items():
                prev_tracked_persons = sensed_person.tracking_history
                prev_point = None
                t_label = k * 25

                # Smooth using Kalman
                kalman = cv2.KalmanFilter(4, 2)
                kalman.measurementMatrix = np.array(self.kalman_measurementMatrices[i], copy=True)
                kalman.transitionMatrix = np.array(self.kalman_transitionMatrices[i], copy=True)
                kalman.processNoiseCov = np.array(self.kalman_processNoiseCovs[i], copy=True)

                pre = np.copy(kalman.statePre)
                pre[0] = np.float32(prev_tracked_persons[0].position[0])
                pre[1] = np.float32(prev_tracked_persons[0].position[1])

                kalman.statePre = pre

                for person in prev_tracked_persons:
                    kalman.correct(np.array([[np.float32(person.position[0])], [np.float32(person.position[1])]]))
                    new_point = kalman.predict()
                    if prev_point is not None:
                        cv2.line(self.outputMap, (int(prev_point[0]), int(prev_point[1])),
                                 (int(new_point[0]), int(new_point[1])),
                                 (index_colour, (t_label / 256) % 256, t_label % 256))
                    prev_point = (int(new_point[0]), int(new_point[1]))

                end_person = prev_tracked_persons[-1]

                # cv2.drawMarker(self.outputMap, (int(end_person.position[0]), int(end_person.position[1])), (0, 0, 255), cv2.MARKER_STAR)

                # draw angle
                cv2.drawMarker(self.outputMap, (int(person.position[0]), int(person.position[1])),
                               (index_colour, 0, 255),
                               thickness=2)
                if end_person.head_direction is not None:
                    head_dir_line_end = (end_person.position[0] + end_person.head_direction[0] * 20,
                                         end_person.position[1] + end_person.head_direction[1] * 20)
                    cv2.arrowedLine(self.outputMap, (int(end_person.position[0]), int(end_person.position[1])),
                                    (int(head_dir_line_end[0]), int(head_dir_line_end[1])),
                                    (index_colour, 0, 255), 2)

                    head_dir_line_end = (end_person.position[0] + end_person.head_direction_min[0] * 20,
                                         end_person.position[1] + end_person.head_direction_min[1] * 20)
                    cv2.arrowedLine(self.outputMap, (int(end_person.position[0]), int(end_person.position[1])),
                                    (int(head_dir_line_end[0]), int(head_dir_line_end[1])),
                                    (index_colour, 255, 0), 2)

                    head_dir_line_end = (end_person.position[0] + end_person.head_direction_max[0] * 20,
                                         end_person.position[1] + end_person.head_direction_max[1] * 20)
                    cv2.arrowedLine(self.outputMap, (int(end_person.position[0]), int(end_person.position[1])),
                                    (int(head_dir_line_end[0]), int(head_dir_line_end[1])),
                                    (index_colour, 255, 0), 2)

        cv2.imshow(self.map_name, self.outputMap)
