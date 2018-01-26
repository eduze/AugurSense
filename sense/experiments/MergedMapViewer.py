import cv2
import numpy as np

from re_id.cvpr_reid.ReIDAPI import ReIDAPI


class MergedMapViewer:
    '''
    Previews merged map. Uses Kalman Filter to smooth outputs.
    '''

    def __init__(self, transform_mappers=None, map_background = None):
        '''
        Initialize
        :param transform_mappers: 
        '''
        self.outputWidth = 640
        self.outputHeight = 480
        self.outputMap = None
        self.map_name = "SimpleMap"
        self.transform_mappers = transform_mappers

        self.kalman_measurementMatrices = None
        self.kalman_transitionMatrices = None
        self.kalman_processNoiseCovs = None
        self.map_background = map_background
        # self.re_id_api = ReIDAPI()

    def clearMap(self):
        if self.map_background is None:
            self.outputMap = np.ones((self.outputHeight, self.outputWidth, 3), np.uint8) * 255
        else:
            self.outputMap = np.array(self.map_background,copy=True)

    def setupWindows(self):
        '''
        Show window
        :return: 
        '''
        cv2.namedWindow(self.map_name, cv2.WINDOW_FREERATIO)
        cv2.resizeWindow(self.map_name, self.outputWidth, self.outputHeight)

    def showMap(self, persons, mapped_persons):
        '''
        Show map in window
        :param persons: 
        :return: 
        '''

        # setup kalman
        if self.kalman_measurementMatrices is None:
            self.kalman_measurementMatrices = []
            self.kalman_transitionMatrices = []
            self.kalman_processNoiseCovs = []

            for i in range(1):
                self.kalman_measurementMatrices.append(np.array([[1, 0, 0, 0], [0, 1, 0, 0]], np.float32))
                self.kalman_transitionMatrices.append(
                    np.array([[1, 0, 1, 0], [0, 1, 0, 1], [0, 0, 1, 0], [0, 0, 0, 1]], np.float32))
                self.kalman_processNoiseCovs.append(np.array([[1, 0, 0, 0], [0, 1, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]],
                                                             np.float32) * 0.005)

        # clear map
        self.clearMap()

        # draw markers
        for i in range(len(self.transform_mappers)):
            index_colour = 0
            if len(self.transform_mappers) > 1:
                index_colour = i * 255 / (len(self.transform_mappers) - 1)

            transform_mapper = None
            if self.transform_mappers is not None:
                transform_mapper = self.transform_mappers[i]

            if transform_mapper is not None:
                for map_marker in transform_mapper.world_points:
                    cv2.drawMarker(self.outputMap, map_marker, (index_colour, 0, 255), thickness=2)

        for person in mapped_persons:
            cv2.drawMarker(self.outputMap, (int(person.position[0]), int(person.position[1])), (255, 0, 0), thickness=2)
        # draw persons

        merged_compare = np.zeros((768, 1366, 3), np.uint8)
        mc_x = 0
        mc_y = 0

        for person in persons:
            cv2.drawMarker(self.outputMap, (int(person.position[0]), int(person.position[1])), (0, 0, 255), thickness=2)
            index_colour = 0

            prev_tracked_persons = person.tracking_history
            prev_point = None
            t_label = person.label

            # Smooth using Kalman
            kalman = cv2.KalmanFilter(4, 2)
            kalman.measurementMatrix = np.array(self.kalman_measurementMatrices[0], copy=True)
            kalman.transitionMatrix = np.array(self.kalman_transitionMatrices[0], copy=True)
            kalman.processNoiseCov = np.array(self.kalman_processNoiseCovs[0], copy=True)

            pre = np.copy(kalman.statePre)
            pre[0] = np.float32(prev_tracked_persons[0].position[0])
            pre[1] = np.float32(prev_tracked_persons[0].position[1])

            kalman.statePre = pre

            # draw tracking
            for p_person in prev_tracked_persons:
                kalman.correct(np.array([[np.float32(p_person.position[0])], [np.float32(p_person.position[1])]]))
                new_point = kalman.predict()
                if prev_point is not None:
                    cv2.line(self.outputMap, (int(prev_point[0]), int(prev_point[1])),
                             (int(new_point[0]), int(new_point[1])),
                             (index_colour, (t_label / 256) % 256, t_label % 256))
                prev_point = (int(new_point[0]), int(new_point[1]))

            # draw multi-tracking
            candidates = []
            self.drawMultiTrack(self.outputMap,person,(index_colour, (t_label / 256) % 256, t_label % 256),candidates = candidates,ttl=0, visited={})

            candidates_gallery = []
            for c in candidates:
                for d in c.detection:
                    candidates_gallery.append((d.roi,c))

            look_fors = []
            for d in person.detection:
                look_fors.append(d.roi)

            # match_results = self.re_id_api.findBestMatches(candidates_gallery,look_fors)
            # print("Matches", match_results)
            # if len(match_results) > 0:
            #     d = person.detection[0]
            #     h1, w1 = d.roi.shape[:2]
            #
            #     height = 0
            #     for m,s in match_results:
            #         height = max(height, m.detection[0].roi.shape[0])
            #
            #     if mc_y + max(h1, height) > merged_compare.shape[0]:
            #         mc_y = 0
            #         mc_x += 400
            #
            #     merged_compare[mc_y:max(h1, height) + mc_y, mc_x: w1 + mc_x, :] = 125
            #     try:
            #         merged_compare[mc_y:h1 + mc_y, mc_x:w1 + mc_x] = d.roi
            #     except:
            #         print("Overflow!")
            #     accuw = 0
            #     for ind in range(1):
            #         match,score = match_results[ind]
            #
            #         if score>0.5:
            #             cv2.drawMarker(self.outputMap, (int(match.position[0]), int(match.position[1])), (255, 255, 0),
            #                                     cv2.MARKER_DIAMOND)
            #             h2, w2 = match.detection[0].roi.shape[:2]
            #
            #             #vis = np.zeros((max(h1, h2), w1 + w2,3), np.uint8)
            #             try:
            #                 merged_compare[mc_y:h2+mc_y, mc_x+w1 + accuw:mc_x+w1 + w2 + accuw] = match.detection[0].roi
            #             except:
            #                 print("Overflow")
            #                 pass
            #             cv2.putText(merged_compare,str(int(score*100)),(mc_x+w1 + accuw,mc_y + int(max(h1,h2)/2)),cv2.FONT_HERSHEY_COMPLEX,1,(255,255,255))
            #
            #             accuw += w2
            #     mc_y += max(h1, height) + 10

            # for c in candidates:
            #     matched = False
            #     for d1 in person.detection:
            #         for d2 in c.detection:
            #             m = self.re_id_api.match(d1.roi,d2.roi)
            #             if m:
            #                 matched = True
            #             print("Check Match:", m)
            #
            #     cv2.drawMarker(self.outputMap, (int(c.position[0]), int(c.position[1])), (255, 255, 0), cv2.MARKER_STAR)
            #     if matched:
            #         cv2.drawMarker(self.outputMap, (int(c.position[0]), int(c.position[1])), (255, 255, 0),
            #                        cv2.MARKER_DIAMOND)
            # draw head direction
            if person.head_direction is not None:
                head_dir_line_end = (person.position[0] + person.head_direction[0] * 20,
                                     person.position[1] + person.head_direction[1] * 20)
                cv2.arrowedLine(self.outputMap, (int(person.position[0]), int(person.position[1])),
                                (int(head_dir_line_end[0]), int(head_dir_line_end[1])),
                                (index_colour, 0, 255), 2)

                head_dir_line_end = (person.position[0] + person.head_direction_min[0] * 20,
                                     person.position[1] + person.head_direction_min[1] * 20)
                cv2.arrowedLine(self.outputMap, (int(person.position[0]), int(person.position[1])),
                                (int(head_dir_line_end[0]), int(head_dir_line_end[1])),
                                (index_colour, 255, 0), 2)

                head_dir_line_end = (person.position[0] + person.head_direction_max[0] * 20,
                                     person.position[1] + person.head_direction_max[1] * 20)
                cv2.arrowedLine(self.outputMap, (int(person.position[0]), int(person.position[1])),
                                (int(head_dir_line_end[0]), int(head_dir_line_end[1])),
                                (index_colour, 255, 0), 2)

            colour = (0, 255, 255)
            pose_text = ""
            if person.stand_probability == 1:
                colour = (0, 0, 255)  # Colour indicating standing
                pose_text = "Stand"
            if person.stand_probability == 0:
                colour = (0, 0, 255)  # Colour indicating sitting
                pose_text = "Sit"

            # cv2.putText(colour_frame, str(person.label),
            #             (int(person.detection.central_point[0]), int(person.detection.central_point[1])),
            #             cv2.FONT_HERSHEY_COMPLEX, 0.4, colour)

            cv2.putText(self.outputMap, pose_text,
                        (int(person.position[0]), int(person.position[1])-10),
                        cv2.FONT_HERSHEY_COMPLEX, 0.4, colour)

        cv2.imshow("merged", merged_compare)

        cv2.imshow(self.map_name, self.outputMap)

    def drawMultiTrack(self, outputMap, person, colour, ttl=0, candidates = [], done = False, visited = {}):
        if ttl == 0:
            return
        if person in visited.keys():
            return
        visited[person] = True
        for prev_person in person.multiPrevious:
            if len(person.multiPrevious) == 1:
                cv2.line(outputMap,(int(prev_person.position[0]),int(prev_person.position[1])),(int(person.position[0]),int(person.position[1])),(colour[0],colour[1],colour[2]))
                if not done:
                    if person not in candidates:
                        candidates.append(person)
                    #done = True
            else:
                cv2.line(outputMap, (int(prev_person.position[0]), int(prev_person.position[1])),
                         (int(person.position[0]), int(person.position[1])), (255, colour[1], colour[2]))
            self.drawMultiTrack(outputMap,prev_person,colour, ttl-1, candidates,done, visited=visited)