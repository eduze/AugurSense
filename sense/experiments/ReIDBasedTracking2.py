import cv2
import math
import os

import numpy as np

from experiments.ReIDBasedTracking import getReIdConfidence, loadData, showImages, storeReIdScores
from experiments.ReIDEvaluate import storeEvaluations

global_velocity_average = 0.1

class TrackedPerson:
    def __init__(self, tracking_history,timestamp):
        self.tracking_history = tracking_history
        self.score_history = []
        self.timestamp = timestamp

        self.kalman_measurementMatrices = []
        self.kalman_transitionMatrices = []
        self.kalman_processNoiseCovs = []

        self.kalman_measurementMatrices.append(np.array([[1, 0, 0, 0], [0, 1, 0, 0]], np.float32))
        self.kalman_transitionMatrices.append(
            np.array([[1, 0, 1, 0], [0, 1, 0, 1], [0, 0, 1, 0], [0, 0, 0, 1]], np.float32))
        self.kalman_processNoiseCovs.append(np.array([[1, 0, 0, 0], [0, 1, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]],
                                                     np.float32) * 0.005)

    def getKalmanError(self, new_person, timestamps,new_timestamp):
        predicted_point = self.getPredictedPoint(timestamps, new_timestamp)

        dist = math.sqrt(
            (predicted_point[0] - new_person.position[0]) ** 2 + (predicted_point[1] - new_person.position[1]) ** 2)
        return dist

    def getPredictedPoint(self, timestamps,new_timestamp):
        # Smooth using Kalman
        kalman = cv2.KalmanFilter(4, 2)
        kalman.measurementMatrix = np.array(self.kalman_measurementMatrices[0], copy=True)
        kalman.transitionMatrix = np.array(self.kalman_transitionMatrices[0], copy=True)
        kalman.processNoiseCov = np.array(self.kalman_processNoiseCovs[0], copy=True)

        pre = np.copy(kalman.statePre)
        pre[0] = np.float32(self.tracking_history[0].position[0])
        pre[1] = np.float32(self.tracking_history[0].position[1])

        kalman.statePre = pre
        kalman.correct(np.array(
            [[np.float32(self.tracking_history[0].position[0])], [np.float32(self.tracking_history[0].position[1])]]))

        current_time_index = timestamps.index(self.tracking_history[0].timestamp)
        predicted_point = (self.tracking_history[0].position[0],self.tracking_history[0].position[1])
        for p in self.tracking_history[1:]:
            target_time_index = timestamps.index(p.timestamp)
            while current_time_index < target_time_index:
                predicted_point = kalman.predict()
                current_time_index += 1
            kalman.correct(np.array([[np.float32(p.position[0])], [np.float32(p.position[1])]]))

        target_time_index = timestamps.index(new_timestamp)
        while current_time_index < target_time_index:
            predicted_point = kalman.predict()
            current_time_index += 1

        predicted_point = (int(predicted_point[0]),int(predicted_point[1]))
        return predicted_point

    def getVelocity(self):
        global global_velocity_average
        if len(self.tracking_history) <= 1:
            return global_velocity_average
        velocity_factor = 1
        total_velocity_factor = 0
        total_velocity = 0

        last_p = self.tracking_history[-1]
        for p in self.tracking_history[-2::-1]:
            v = math.sqrt((p.position[0]-last_p.position[0])**2 + (p.position[1]-last_p.position[1])**2)
            v = v / (last_p.timestamp - p.timestamp)
            last_p = p
            total_velocity += v
            total_velocity_factor += velocity_factor
            velocity_factor *= 0.9

        average_v = total_velocity / total_velocity_factor
        global_velocity_average = global_velocity_average * 0.5 + average_v * 0.5
        # print("Velocities",str(average_v),str(global_velocity_average))
        return average_v

    def compare(self,person, timestamps,new_timestamp):
        image_dif = self.compareImage(person)
        kalman_dif = self.getKalmanError(person,timestamps,new_timestamp)

        #return image_dif + (1 / (1 + kalman_dif)) * 0.8, image_dif, kalman_dif # Optimum for PETS
        return image_dif + (1 / (1 + kalman_dif)) * 0.8, image_dif, kalman_dif
        #return image_dif * (1/(1+kalman_dif)) * 1.5

    def compareImage(self, person):
        score_factor = 1
        total_score_factor = 0
        total_score = 0
        iter_count = 0
        for p in self.tracking_history[::-3]:
            if score_factor < 0.7:
                print("Terminated after " + str(iter_count))
                break
            total_score += getReIdConfidence(person,p) * score_factor
            total_score_factor += score_factor
            score_factor *= 0.95
            iter_count += 1
        average_score = total_score/total_score_factor #* math.log(math.sqrt(len(self.tracking_history)) + 1)
        if len(self.tracking_history) < 2:
            average_score *= 0.95
        return average_score

    def clone(self):
        r = TrackedPerson([p for p in self.tracking_history],self.timestamp)
        r.score_history = [s for s in self.score_history]
        return r

    def distance2(self, person):
        d = (person.position[0] - self.tracking_history[-1].position[0])**2 + (person.position[1] - self.tracking_history[-1].position[1])**2
        return d

def updateTracking(tracked_persons, persons,timestamp, untracked, timeframes):
    tuples = []
    for tp in tracked_persons:
        for p in persons:
            if tp.distance2(p) < 20*20:
                score, image_dif, kalman_dif =tp.compare(p,timeframes,timestamp)
                tuple = (tp,p,score, image_dif, kalman_dif)
                tuples.append(tuple)

    tuples = sorted(tuples,key=lambda x:x[2], reverse=True)

    new_tracked_persons = []
    added_tracked_persons = {}
    added_persons = {}

    for tuple in tuples:
        tracked_person = tuple[0]
        person = tuple[1]
        score = tuple[2]
        image_dif = tuple[3]
        kalman_dif = tuple[4]
        if person not in added_persons.keys() and tracked_person not in added_tracked_persons.keys() and score > 0.5:
            added_tracked_persons[tracked_person] = True
            added_persons[person] = True

            print("Kalman:",person.position,tracked_person.getKalmanError(person,timeframes,timestamp))

            tracked_person.tracking_history.append(person)
            tracked_person.score_history.append((score,image_dif,kalman_dif))
            tracked_person.timestamp = timestamp
            new_tracked_persons.append(tracked_person)

    new_untracked = []
    new_untracked.extend(untracked)
    for tp in tracked_persons:
        if tp not in added_tracked_persons.keys():
            new_untracked.append(tp)

    # make a new set of tuples with untracked
    tuples = []
    for utp in new_untracked:
        if len(utp.tracking_history) > 4: # do not attach with very small histories.
            for p in persons:
                if p not in added_persons.keys():
                    time_dif = timestamp - utp.timestamp
                    if time_dif < 1000:
                        candidate_radius = utp.getVelocity() * time_dif
                        if utp.distance2(p) < candidate_radius**2:
                            score, image_dif, kalman_dif = utp.compare(p,timeframes,timestamp)
                            tuples.append((utp,p,score,image_dif,kalman_dif))

    tuples = sorted(tuples, key=lambda x: x[2], reverse=True)
    past_retracks = []
    for tuple in tuples:
        tracked_person = tuple[0]
        person = tuple[1]
        score = tuple[2]
        image_dif = tuple[3]
        kalman_dif = tuple[4]
        if person not in added_persons.keys() and tracked_person not in added_tracked_persons.keys() and score > 1:
            added_tracked_persons[tracked_person] = True
            added_persons[person] = True
            tracked_person.tracking_history.append(person)
            tracked_person.score_history.append((score,image_dif,kalman_dif))
            tracked_person.timestamp = timestamp
            new_tracked_persons.append(tracked_person)
            if tracked_person in untracked:
                past_retracks.append(tracked_person)
            print("Retracked", person.file_name)

    new_untracked = []
    for past_untracked in untracked:
        if past_untracked not in past_retracks:
            new_untracked.append(past_untracked)
    #new_untracked.extend(untracked)
    for tp in tracked_persons:
        if tp not in added_tracked_persons.keys():
            new_untracked.append(tp.clone())

    for p in persons:
        if p not in added_persons.keys():
            # add a new tracked person to newly tracked guy
            tp = TrackedPerson([p],timestamp)
            tp.score_history.append((0,0,0))
            new_tracked_persons.append(tp)

    return new_tracked_persons, new_untracked

def showOutput(tracked_persons, timeframe, selected_tp_index=-1):
    #output = cv2.imread("test_videos/PETSMap.png")
    output = np.zeros((800,600,3))
    cv2.putText(output, "Time: " + str(timeframe), (10, 20), cv2.FONT_HERSHEY_COMPLEX, 0.5, (255, 255, 255))

    for tp in tracked_persons:
        colour = (255,0,0)
        if tracked_persons.index(tp) == selected_tp_index:
            colour = (255,255,0)
            showImages(tp.tracking_history, annotations=[t for t in tp.score_history])
            if len(tp.score_history) > 0:
                cv2.putText(output, str(tp.score_history[-1][0]), (10, 40), cv2.FONT_HERSHEY_COMPLEX, 0.5, (255, 255, 255))
                cv2.putText(output, str(tp.score_history[-1][1]), (10, 60), cv2.FONT_HERSHEY_COMPLEX, 0.5,
                            (255, 255, 255))
                cv2.putText(output, str(tp.score_history[-1][2]), (10, 80), cv2.FONT_HERSHEY_COMPLEX, 0.5,
                            (255, 255, 255))
        head = tp.tracking_history[-1]
        cv2.drawMarker(output,head.position,colour,markerSize=15, markerType=cv2.MARKER_STAR)

        current_p = head
        for prev_p in tp.tracking_history[::-1]:
            cv2.line(output,current_p.position,prev_p.position,colour,1)
            current_p = prev_p

    cv2.namedWindow("Output",cv2.WINDOW_FREERATIO)
    cv2.imshow("Output",output)

past_loads = {}

def loadTrackedPersons(view, time_frame_end, time_frames):
    tracked_persons = []
    untracked = []
    for frame_t in time_frames:
        frame = view.frames[frame_t]
        if frame.timestamp > time_frame_end:
            break
        if frame_t not in past_loads.keys():
            print("Updating", frame_t)
            tracked_persons,untracked = updateTracking(tracked_persons,frame.persons,frame_t,untracked,time_frames)
            past_loads[frame_t] = ([t.clone() for t in tracked_persons],[t.clone() for t in untracked])
        else:
            tracked_persons = [t.clone() for t in past_loads[frame_t][0]]
            untracked = [t.clone() for t in past_loads[frame_t][1]]

    return tracked_persons,untracked

def save(tracked_persons,timestamp):
    if not os.path.isdir("re_id_saves/" + str(timestamp)):
        os.mkdir("re_id_saves/" + str(timestamp))
    storeEvaluations(all_tracks, time_frames[current_frame_index])
    for tp in tracked_persons:
        ind = tracked_persons.index(tp)
        if not os.path.isdir("re_id_saves/" + str(timestamp) + "/" + str(ind)):
            os.mkdir("re_id_saves/" + str(timestamp) + "/" + str(ind))
        print("Saving ",ind, len(tracked_persons))
        for p in tp.tracking_history:
            img_clone = np.array(p.image,copy=True)
            cv2.putText(img_clone, str(int(tp.score_history[tp.tracking_history.index(p)][0] * 1000)), (5,  10), cv2.FONT_HERSHEY_COMPLEX, 0.3, (255, 255, 255))
            cv2.putText(img_clone, str(int(tp.score_history[tp.tracking_history.index(p)][1] * 1000)), (5,  25), cv2.FONT_HERSHEY_COMPLEX, 0.3, (255, 255, 255))
            cv2.putText(img_clone, str(int(tp.score_history[tp.tracking_history.index(p)][2] * 1000)), (5,  40), cv2.FONT_HERSHEY_COMPLEX, 0.3, (255, 255, 255))
            cv2.imwrite("re_id_saves/" + str(timestamp) + "/" + str(ind) + "/" + p.file_name,p.image)


if __name__ == "__main__":
    firstRun = True
    data = loadData("output_officeroom/")
    print(data)
    view = data[0]
    time_frames = sorted(list(view.frames.keys()))

    current_frame_index = 0
    current_frame_index = len(time_frames)-1
    selected_person_index = -1

    while True:
        tracked_persons,untracked = loadTrackedPersons(view, time_frames[current_frame_index], time_frames)
        storeReIdScores()
        showOutput(tracked_persons, time_frames[current_frame_index], selected_person_index)

        if firstRun:
            all_tracks = list(untracked)
            all_tracks.extend(tracked_persons)
            save(all_tracks, time_frames[current_frame_index])

            firstRun = False

        k = cv2.waitKey(0)
        if k == ord("d"):
            current_frame_index += 1
        elif k == ord("a"):
            current_frame_index -= 1
        elif k == ord("w"):
            selected_person_index += 1
        elif k == ord("s"):
            selected_person_index -= 1
        elif k == ord("z"):
            all_tracks = list(untracked)
            all_tracks.extend(tracked_persons)
            save(all_tracks,time_frames[current_frame_index])
            storeEvaluations(all_tracks, time_frames[current_frame_index])
        elif k == ord("e"):
            current_frame_index = len(time_frames) - 1
        elif k == ord("q"):
            break
        else:
            for i in range(10):
                if k == ord(str(i)):
                    current_frame_index = int(i / 9 * (len(time_frames)-1))
                    break