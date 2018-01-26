import cv2


class ScreenSpacePreview:
    '''
    Provides preview on screen space
    '''

    def __init__(self, transform_mapper=None, is_editable_markers=False):
        '''
        Initialize ScreenSpacePreview
        :param transform_mapper: PTE used for mapping from screen space to world space
        :param is_editable_markers: Are the markers in PTE editable by clicking on preview ui?
        '''
        self.is_editable_markers = is_editable_markers
        self.transform_mapper = transform_mapper
        self.__mouse_l_down = False
        self.__mouse_known_x = 0
        self.__mouse_known_y = 0
        self.colour_window_title = "colour"
        self.gray_window_title = None
        self.window_size = (640, 480)
        self.setup_done = False

    def setupWindows(self):
        '''
        Initialize UI
        :return: 
        '''
        if self.colour_window_title is not None:
            cv2.namedWindow(self.colour_window_title, cv2.WINDOW_FREERATIO)
            cv2.resizeWindow(self.colour_window_title, self.window_size[0], self.window_size[1])
            cv2.setMouseCallback(self.colour_window_title, self.__generateMouseListener())

        if self.gray_window_title is not None:
            cv2.namedWindow(self.gray_window_title, cv2.WINDOW_FREERATIO)
            cv2.resizeWindow(self.gray_window_title, self.window_size[0], self.window_size[1])

        self.setup_done = True

    def renderFrame(self, sense, colour_frame, gray_frame=None):
        # Render detections
        for person in sense.detections:
            (f_x, f_y, f_w, f_h) = person.person_bound
            (l_x, l_y) = person.leg_point
            (el_x, el_y) = person.estimated_leg_point
            cv2.rectangle(colour_frame, (f_x, f_y), (f_w, f_h), (0, 0, 0), 2)
            cv2.drawMarker(colour_frame, (int(l_x), int(l_y)), (255, 0, 255), cv2.MARKER_CROSS,thickness=3)
            cv2.drawMarker(colour_frame, (int(el_x), int(el_y)), (255, 0, 255), cv2.MARKER_DIAMOND)

            for k,v in person.tracked_points.items():
                cv2.drawMarker(colour_frame, (v[0],v[1]), (0, 0, 255),cv2.MARKER_TILTED_CROSS,10)

            # cv2.putText(frame, str(person.head_direction), (int(person.central_point[0]), int(person.central_point[1])),
            #        cv2.FONT_HERSHEY_COMPLEX, 0.4, (0, 255, 0))

        if self.transform_mapper is not None:
            # Render markers
            for marker in self.transform_mapper.screen_points:
                cv2.drawMarker(colour_frame, marker, (0, 0, 255))

        for sensed_person in sense.sensed_persons.values():
            person = sensed_person.tracked_person

            colour = (0, 255, 255)
            pose_text = ""
            if person.stand_probability == 1:
                colour = (0, 0, 255)  # Colour indicating standing
                pose_text = "Stand"
            if person.stand_probability == 0:
                colour = (0, 255, 0)  # Colour indicating sitting
                pose_text = "Sit"



            # cv2.putText(colour_frame, str(person.label),
            #             (int(person.detection.central_point[0]), int(person.detection.central_point[1])),
            #             cv2.FONT_HERSHEY_COMPLEX, 0.4, colour)

            cv2.putText(colour_frame, pose_text,
                        (int(person.detection.central_point[0]), int(person.detection.central_point[1] + 20)),
                        cv2.FONT_HERSHEY_COMPLEX, 0.4, colour)

        # Show outputs
        if gray_frame is not None and self.gray_window_title is not None:
            cv2.imshow(self.gray_window_title, gray_frame)
        cv2.putText(colour_frame, str(self.__mouse_known_x) + ", " + str(self.__mouse_known_y), (20, 20),
                    cv2.FONT_HERSHEY_COMPLEX, 0.5, (255, 0, 0))
        cv2.imshow(self.colour_window_title, colour_frame)

    def __generateMouseListener(self):
        '''
        Obtain mouse listener
        :return: 
        '''
        __self = self

        def __clickListener(event, x, y, flags, param):
            '''
            Mouse Click Listener for Camera Output
            :param event: 
            :param x: 
            :param y: 
            :param flags: 
            :param param: 
            :return: 
            '''

            if not __self.is_editable_markers or __self.transform_mapper is None:
                return

            if event == cv2.EVENT_LBUTTONDOWN:
                # Clear
                if len(__self.transform_mapper.screen_points) == 4:
                    __self.transform_mapper.screen_points.clear()
                __self.transform_mapper.screen_points.append((x, y))
                __self.__mouse_l_down = True
            elif event == cv2.EVENT_LBUTTONUP:
                __self.__mouse_l_down = False
            elif event == cv2.EVENT_MOUSEMOVE:
                __self.__mouse_known_x = x
                __self.__mouse_known_y = y
                if __self.__mouse_l_down:
                    __self.transform_mapper.screen_points[-1] = (x, y)

        return __clickListener
