import os
from collections import Counter

import cv2
import numpy as np

from experiments.ReIDBasedTracking import Person, storeReIdScores

TRACK_SEGMENTS_LOAD_PATH="re_id_saves/113571"

global_speed_readings = []
global_average_speed = 0
global_std_speed = 0
global_speed_max = 0

class TrackSegment:
    def __init__(self):
        self.tracking_history = []

    def __repr__(self):
        return str(self.tracking_history[0]) + " : " + str(self.tracking_history[-1])

    def merge(self, other):
        timeline = {}
        for p in self.tracking_history:
            timeline[p.timestamp] = True

        for p in other.tracking_history:
            if p.timestamp not in timeline.keys():
                self.tracking_history.append(p)

        self.tracking_history = sorted(self.tracking_history,key=lambda x:x.timestamp)

    def getMaxEucledianError(self, other):
        less = None
        more = None
        overlapped = False

        if self.tracking_history[-1].timestamp < other.tracking_history[0].timestamp:
            less = self
            more = other
        elif other.tracking_history[-1].timestamp < self.tracking_history[0].timestamp:
            less = other
            more = self
        else:
            overlapped = True

        if not overlapped:
            timeline = obtainTimeline([less,more])
            chopped_timeline = []
            for t in timeline:
                if t <= more.tracking_history[0].timestamp:
                    chopped_timeline.append(t)
            extended_less = less.fillAndExtend(chopped_timeline)

            c1 = extended_less[-1]
            c2 = more.tracking_history[0]
            return c1.getDistance(c2), 0
        else:
            less = None
            more = None
            swallowed = True
            if self.tracking_history[0].timestamp <= other.tracking_history[0].timestamp and self.tracking_history[-1].timestamp <= other.tracking_history[-1].timestamp:
                less = self
                more = other
                swallowed = False
            elif self.tracking_history[0].timestamp > other.tracking_history[0].timestamp and self.tracking_history[-1].timestamp > other.tracking_history[-1].timestamp:
                less = other
                more = self
                swallowed = False

            if not swallowed:
                timeline = obtainTimeline([less,more])
                extended_less = less.fillAndExtend(timeline)
                extended_more = more.fillAndExtend(timeline)

                extended_more_timemap = {}
                for p in extended_more:
                    extended_more_timemap[p.timestamp] = p

                extended_less_timemap = {}
                for p in extended_less:
                    extended_less_timemap[p.timestamp] = p

                maxD = 0
                for p in less.tracking_history:
                    if p.timestamp >= more.tracking_history[0].timestamp:
                        d = p.getDistance(extended_more_timemap[p.timestamp])
                        maxD = max(maxD,d)
                for p in more.tracking_history:
                    if p.timestamp <= less.tracking_history[-1].timestamp:
                        d = p.getDistance(extended_less_timemap[p.timestamp])
                        maxD = max(maxD,d)

                return maxD, 1
            else:
                #swallowd case
                outer = None
                inner = None
                if self.tracking_history[0].timestamp > other.tracking_history[0].timestamp and self.tracking_history[-1].timestamp < other.tracking_history[-1].timestamp:
                    inner = self
                    outer = other
                else:
                    inner = other
                    outer = self
                timeline = obtainTimeline([inner, outer])
                extended_outer = outer.fillAndExtend(timeline)
                maxD = 0
                for p in inner.tracking_history:
                    # find closest from outer
                    min_time_dif = 1000000
                    closest_outer = None
                    for p2 in extended_outer:
                        if abs(p2.timestamp-p.timestamp) < min_time_dif:
                            min_time_dif = abs(p2.timestamp-p.timestamp)
                            closest_outer = p2

                    d = closest_outer.getDistance(p)
                    maxD = max(d,maxD)
                return maxD,2
        assert False, "Should not be reachable"
    def clone(self):
        r = TrackSegment()
        r.tracking_history = [p for p in self.tracking_history]
        return r
    def compareAndPrint(self, other):
        re_id_score = self.reIDCompare(other)
        print("Time Difference:",self.getTimeDifference(other))
        print("Frame Collision Count:",self.getFrameCollisionsCount(other), len(self.tracking_history), len(other.tracking_history))
        v = self.getMergedSpeedProfile(other, True)
        if len(v) > 0:
            print("Max Velocity (KC):",max(list(v.values())), getSpeedPercentile(max(list(v.values()))))
        print("Speed Distribution (KC):", list(v.values()))

        v = self.getMergedSpeedProfile(other, False)
        if len(v) > 0:
            print("Max Velocity:", max(list(v.values())), getSpeedPercentile(max(list(v.values()))))
        print("Speed Distribution:", list(v.values()))

        print("Max Eucledian Distance:", self.getMaxEucledianError(other))

        print("ReId Score", re_id_score)

    def fillAndExtend(self, timestamps):
        results = []
        tracking_history_time_map = {}
        for h in self.tracking_history:
            tracking_history_time_map[h.timestamp] = h
        fill_start_time = self.tracking_history[0].timestamp
        fill_end_time = self.tracking_history[-1].timestamp
        timestamps = sorted(timestamps)

        reverse_kalman_measurementMatrices = []
        revers_kalman_transitionMatrices = []
        reverse_kalman_processNoiseCovs = []

        reverse_kalman_measurementMatrices.append(np.array([[1, 0, 0, 0], [0, 1, 0, 0]], np.float32))
        revers_kalman_transitionMatrices.append(
            np.array([[1, 0, 1, 0], [0, 1, 0, 1], [0, 0, 1, 0], [0, 0, 0, 1]], np.float32))
        reverse_kalman_processNoiseCovs.append(np.array([[1, 0, 0, 0], [0, 1, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]],
                                                np.float32) * 0.005)

        reverse_kalman = cv2.KalmanFilter(4, 2)
        reverse_kalman.measurementMatrix = np.array(reverse_kalman_measurementMatrices[0], copy=True)
        reverse_kalman.transitionMatrix = np.array(revers_kalman_transitionMatrices[0], copy=True)
        reverse_kalman.processNoiseCov = np.array(reverse_kalman_processNoiseCovs[0], copy=True)

        pre = np.copy(reverse_kalman.statePre)
        pre[0] = np.float32(self.tracking_history[0].position[0])
        pre[1] = np.float32(self.tracking_history[0].position[1])

        reverse_kalman.statePre = pre

        for t in timestamps:
            # Interpolate intermediate values
            if t in tracking_history_time_map.keys():
                results.append(tracking_history_time_map[t])
                reverse_kalman.predict()
                reverse_kalman.correct(np.array(
                    [[np.float32(tracking_history_time_map[t].position[0])],
                     [np.float32(tracking_history_time_map[t].position[1])]]))

            elif fill_start_time < t < fill_end_time:
                t_less = timestamps[0]
                t_high = timestamps[-1]

                p_less = None
                p_high = None
                # Obtain tight bounds
                for t2 in self.tracking_history:
                    if t2.timestamp <= t and t2.timestamp >= t_less:
                        t_less = t2.timestamp
                        p_less = t2
                    if t2.timestamp >= t and t2.timestamp <= t_high:
                        t_high = t2.timestamp
                        p_high = t2

                y_coord = int(p_less.position[1] + (t - t_less) * (p_high.position[1] - p_less.position[1])/(t_high-t_less))
                x_coord = int(p_less.position[0] + (t-t_less) * (p_high.position[0] - p_less.position[0])/(t_high-t_less))
                p = Person((x_coord,y_coord),t)
                p.file_name = "LIGenerated_" + str(t) + "_" + str(x_coord) + "_" + str(y_coord)
                results.append(p)
                reverse_kalman.predict()
            elif t > fill_end_time:
                new_point = reverse_kalman.predict()
                p = Person((int(new_point[0]),int(new_point[1])),t)
                p.file_name = "KalmanGenerated_" + str(t) + "_" + str(int(new_point[0])) + "_" + str(int(new_point[1]))
                results.append(p)

        reverse_kalman_measurementMatrices = []
        revers_kalman_transitionMatrices = []
        reverse_kalman_processNoiseCovs = []

        reverse_kalman_measurementMatrices.append(np.array([[1, 0, 0, 0], [0, 1, 0, 0]], np.float32))
        revers_kalman_transitionMatrices.append(
            np.array([[1, 0, 1, 0], [0, 1, 0, 1], [0, 0, 1, 0], [0, 0, 0, 1]], np.float32))
        reverse_kalman_processNoiseCovs.append(np.array([[1, 0, 0, 0], [0, 1, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]],
                                                        np.float32) * 0.005)

        reverse_kalman = cv2.KalmanFilter(4, 2)
        reverse_kalman.measurementMatrix = np.array(reverse_kalman_measurementMatrices[0], copy=True)
        reverse_kalman.transitionMatrix = np.array(revers_kalman_transitionMatrices[0], copy=True)
        reverse_kalman.processNoiseCov = np.array(reverse_kalman_processNoiseCovs[0], copy=True)

        pre = np.copy(reverse_kalman.statePre)
        pre[0] = np.float32(self.tracking_history[-1].position[0])
        pre[1] = np.float32(self.tracking_history[-1].position[1])

        reverse_kalman.statePre = pre

        prep_results = []
        for t in timestamps[::-1]:
            if t in tracking_history_time_map.keys():
                reverse_kalman.predict()
                reverse_kalman.correct(np.array(
                    [[np.float32(tracking_history_time_map[t].position[0])],
                     [np.float32(tracking_history_time_map[t].position[1])]]))
            elif fill_start_time < t < fill_end_time:
                reverse_kalman.predict()
            elif t < fill_start_time:
                new_point = reverse_kalman.predict()
                p = Person((int(new_point[0]), int(new_point[1])), t)
                p.file_name = "ReverseKalmanGenerated_"+ str(t) + "_" + str(int(new_point[0])) + "_" + str(int(new_point[1]))
                prep_results.insert(0,p)

        prep_results.extend(results)
        return prep_results

    def reIDCompare(self, other):
        timeline = {}
        for p in self.tracking_history:
            if p.timestamp in timeline.keys():
                assert False, "Not allowed to have repeated timeframes in same segment"
            timeline[p.timestamp] = []
            timeline[p.timestamp].append(p)

        for p in other.tracking_history:
            if p.timestamp not in timeline.keys():
                timeline[p.timestamp] = []
            timeline[p.timestamp].append(p)

        time_points = sorted(list(timeline.keys()))

        total_score = 0
        total_score_factor = 0

        for p1 in self.tracking_history:
            for p2 in other.tracking_history:
                score = p1.getMatchScoreAgainst(p2)
                time_point1 = time_points.index(p1.timestamp)
                time_point2 = time_points.index(p2.timestamp)
                time_dif = abs(time_point1-time_point2)
                score_factor = 0.999 ** time_dif
                total_score += score * score_factor
                total_score_factor += score_factor
        storeReIdScores()
        return total_score / total_score_factor


    def getTimeDifference(self, other,ignoreZero=True):
        time_pairs = []
        for p1 in self.tracking_history:
            for p2 in other.tracking_history:
                pair = (abs(p2.timestamp-p1.timestamp), p1,p2)
                if not ignoreZero or pair[0] != 0:
                    time_pairs.append(pair)
        time_pairs = sorted(time_pairs,key=lambda x:x[0])
        if len(time_pairs) > 0:
            return time_pairs[0]
        return None

    def getFrameCollisionsCount(self, other):
        timestamps = {}
        count = 0
        for p in self.tracking_history:
            timestamps[p.timestamp] = True
        for p in other.tracking_history:
            if p.timestamp in timestamps.keys():
                count += 1
        return count

    def getMergedSpeedProfile(self,other, useKalman = False):
        timeline = {}
        for p in self.tracking_history:
            if p.timestamp in timeline.keys():
                assert False,"Not allowed to have repeated timeframes in same segment"
            timeline[p.timestamp] = []
            timeline[p.timestamp].append(p)

        for p in other.tracking_history:
            if p.timestamp not in timeline.keys():
                timeline[p.timestamp] = []
            timeline[p.timestamp].append(p)

        time_points = sorted(list(timeline.keys()))

        predicted_timeline = {}
        if useKalman:
            predicted_timeline = getKalmanPredictedPoints(time_points,timeline)

        velocities = {}

        for i in range(1,len(time_points)):
            prev_point = time_points[i-1]
            current_point = time_points[i]
            pairs = []
            for p1 in timeline[current_point]:
                if useKalman:
                    predicted_point_set = predicted_timeline[prev_point]
                else:
                    predicted_point_set = [p.position for p in timeline[prev_point]]

                for p2 in predicted_point_set:
                    pairs.append((p1,p2,p1.getDistance(p2)/(current_point-prev_point)))
            pairs = sorted(pairs,key=lambda x:x[2],reverse=True)
            velocity = pairs[0][2]
            velocities[str(prev_point) + "_" + str(current_point)] = velocity
        return velocities

    def getSpeedProfile(self):
        timeline = {}
        for p in self.tracking_history:
            if p.timestamp in timeline.keys():
                assert False,"Not allowed to have repeated timeframes in same segment"
            timeline[p.timestamp] = p

        time_points = sorted(list(timeline.keys()))

        velocities = {}

        for i in range(1,len(time_points)):
            prev_point = time_points[i-1]
            current_point = time_points[i]
            p1 = timeline[prev_point]
            p2 = timeline[current_point]
            v = p1.getDistance(p2)/(current_point-prev_point)
            velocities[str(prev_point) + "_" + str(current_point)] = v
        return velocities


def obtainTimeline(segments):
    results_dic = {}

    time_difs = []

    for segment in segments:
        for p in segment.tracking_history:
            results_dic[p.timestamp] = True
            if segment.tracking_history.index(p) > 0:
                time_dif = p.timestamp - segment.tracking_history[segment.tracking_history.index(p)-1].timestamp
                time_difs.append(time_dif)

    c = Counter(time_difs)
    common_time_dif = c.most_common()[0][0]

    results = [r for r in results_dic.keys()]
    results = sorted(results)

    i = 1
    while i < len(results):
        d = results[i] - results[i-1]
        if d <= common_time_dif * 5/4:
            i += 1
        elif d > common_time_dif * 5/4:
            results.insert(i,results[i-1] + common_time_dif)

    return results

def getAveragePoint(points):
    x = 0
    y = 0
    for p in points:
        x += p.position[0]
        y += p.position[1]
    return x / len(points), y / len(points)

def getKalmanPredictedPoints(time_points, point_map):
    kalman_measurementMatrices = []
    kalman_transitionMatrices = []
    kalman_processNoiseCovs = []

    kalman_measurementMatrices.append(np.array([[1, 0, 0, 0], [0, 1, 0, 0]], np.float32))
    kalman_transitionMatrices.append(
        np.array([[1, 0, 1, 0], [0, 1, 0, 1], [0, 0, 1, 0], [0, 0, 0, 1]], np.float32))
    kalman_processNoiseCovs.append(np.array([[1, 0, 0, 0], [0, 1, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]],
                                                 np.float32) * 0.005)

    kalman = cv2.KalmanFilter(4, 2)
    kalman.measurementMatrix = np.array(kalman_measurementMatrices[0], copy=True)
    kalman.transitionMatrix = np.array(kalman_transitionMatrices[0], copy=True)
    kalman.processNoiseCov = np.array(kalman_processNoiseCovs[0], copy=True)

    pre = np.copy(kalman.statePre)
    start_p = getAveragePoint(point_map[time_points[0]])
    pre[0] = np.float32(start_p[0])
    pre[1] = np.float32(start_p[1])

    kalman.statePre = pre

    results = {}
    results[time_points[0]] = [start_p]

    kalman.correct(np.array(
        [[np.float32(start_p[0])], [np.float32(start_p[1])]]))

    for i in range(1,len(time_points)):
        p = point_map[time_points[i]]
        prediction = kalman.predict()
        results[time_points[i]] = [((int(prediction[0]),int(prediction[1])))]

        avp = getAveragePoint(p)
        kalman.correct(np.array(
            [[np.float32(avp[0])], [np.float32(avp[1])]]))

    return results

def getSpeedPercentile(speed):
    import scipy.stats
    z = (speed - global_average_speed)/global_std_speed
    p = scipy.stats.norm.cdf(z)
    return np.asscalar(p)

def loadGlobalSpeedReadings(track_segments):
    global global_speed_readings
    global global_average_speed
    global global_std_speed
    global global_speed_max

    for track_segment in track_segments:
        global_speed_readings.extend(list(track_segment.getSpeedProfile().values()))
    global_speed_readings = sorted(global_speed_readings)
    global_speed_readings_np = np.array(global_speed_readings)

    global_average_speed = np.mean(global_speed_readings_np,axis=0)
    global_std_speed = np.std(global_speed_readings_np,axis=0)
    global_speed_max = np.max(global_speed_readings_np,axis=0)

    #print("Global Speed Distribution",global_speed_readings)
    print("Global Speed Max", global_speed_max)
    print("Global Mean Speed", global_average_speed)
    print("Global Speed Std",global_std_speed)
    print("Global Speed Max",global_speed_max)

def findBestMatches(target_segment, track_segments):
    max_time_dif = 2000
    #max_speed_fraction = 1
    max_speed_limit = global_speed_max  # 0.5 since kalman correction is used
    max_frame_collision_fraction = 0.2

    passed = []

    for other in track_segments:
        if other == target_segment:
            continue
        time_dif = target_segment.getTimeDifference(other)[0]
        if time_dif > max_time_dif:
            continue
        v = target_segment.getMergedSpeedProfile(other, useKalman=False)
        speed = max(list(v.values()))


        #speed_fraction = getSpeedPercentile(speed)
        #if speed_fraction >= max_speed_fraction:
        #    continue
        if speed > max_speed_limit:
            continue
        collision_count = target_segment.getFrameCollisionsCount(other)
        collision_fraction = collision_count / min(len(target_segment.tracking_history),len(other.tracking_history))
        if collision_fraction > max_frame_collision_fraction:
            continue

        cases = ["Less-Greater", "Overlap", "Swallowed"]
        max_distance, case = target_segment.getMaxEucledianError(other)

        if max_distance > 50:
            print(cases[case],":", "Broken by Kalman",max_distance,target_segment,other)
            continue

        print("Passed",track_segments.index(other),other)
        passed.append(other)

    print(len(passed),"items passed.")
    results = []
    for other in passed:
        r_score = target_segment.reIDCompare(other)
        results.append((r_score,other))
        print("Compared", passed.index(other), "(", track_segments.index(other), ")", "of", len(passed), other, ":", r_score)

    results = sorted(results,key=lambda x:x[0],reverse=True)
    for score, result in results:
        print("Sorted", track_segments.index(result),score,result)
    return results

def loadTrackSegments(data_path=TRACK_SEGMENTS_LOAD_PATH):
    _segment_ids = os.listdir(data_path)
    segment_ids = []
    for sid in _segment_ids:
        if os.path.isdir(os.path.join(data_path,sid)):
            segment_ids.append(sid)
    segment_ids = sorted(segment_ids,key=lambda x: int(x))
    track_segments = []

    for id in segment_ids:
        track_segment = TrackSegment()
        track_segments.append(track_segment)

        frame_files = os.listdir(data_path + "/" + id +"/")

        for file_name in frame_files:
            p_string = file_name.split(".")[0]
            params = p_string.split("_")
            time_frame = int(params[0])
            x_coord = int(params[1])
            y_coord = int(params[2])
            cam_id = int(params[3])
            image = cv2.imread(os.path.join(os.path.join(data_path,id), file_name))
            p = Person((x_coord,y_coord),time_frame)
            p.file_name = file_name
            p.image = image
            track_segment.tracking_history.append(p)

        track_segment.tracking_history = sorted(track_segment.tracking_history, key=lambda x:x.timestamp)
    return track_segments

track_segments = loadTrackSegments()
loadGlobalSpeedReadings(track_segments)

def recursiveFindBestMatches(start_segment, track_segments):
    current_segment = start_segment.clone()
    segments_clone = track_segments[:]
    segments_clone.remove(start_segment)
    merged_segments = []
    while len(segments_clone) > 0:
        best_matches = findBestMatches(current_segment,segments_clone)
        if len(best_matches) > 0:
            best_match = best_matches[0][1]
            if best_matches[0][0] < 0.7:
                print("Threshold fell below 0.7")
                break
            print("Found best match", track_segments.index(best_match),best_match)
            current_segment.merge(best_match)
            merged_segments.append(best_match)
            segments_clone.remove(best_match)
        else:
            print("Ran out of matches")
            break
    return current_segment, merged_segments

def findBestMatchingClusters(track_segments):
    current_segments = track_segments
    with open("cluster_output.log", "a+") as f:
        f.write("\n\n\n")
        f.write("===========================Start==========================\n")
        f.write("Segment Count " + str(len(track_segments)) + "\n")

    results = []
    while len(current_segments) > 0:
        current_segments = sorted(current_segments, key=lambda x:len(x.tracking_history),reverse=True)
        if len(current_segments[0].tracking_history) < 10:
            break
        result = []
        result.append(current_segments[0])
        print("Processing Segment", track_segments.index(current_segments[0]) , current_segments[0])
        with open("cluster_output.log", "a+") as f:
            f.write("\nProcessing Segment " + str(track_segments.index(current_segments[0])) + " " + str(current_segments[0]) + "\n")
        best_segment, merged_segments = recursiveFindBestMatches(current_segments[0],current_segments)

        current_segments.remove(current_segments[0])
        for s in merged_segments:
            current_segments.remove(s)
            result.append(s)
        results.append(result)

        with open("cluster_output.log","a+") as f:
            f.write("Found Cluster " + str(best_segment) +"\n")
            for r in result:
                f.write("Cluster " + str(track_segments.index(r)) +  " : " + str(r) + "\n")

        print("Found Cluster", best_segment)
        for r in result:
            print("Cluster",track_segments.index(r),":", r)


    return results

if __name__ == "__main__":
    # for t1 in track_segments:
    #     for t2 in track_segments[track_segments.index(t1)+1:]:
    #         speed_profile = t1.getMergedSpeedProfile(t2)
    #         max_speed = float("nan")
    #         if len(speed_profile) > 0:
    #             max_speed = max(list(speed_profile.values()))
    #         print(max_speed,"SP:", getSpeedPercentile(max_speed), t1.getFrameCollisionsCount(t2), len(t1.tracking_history), len(t2.tracking_history),t1.getTimeDifference(t2))
    # print(track_segments)
    #print("Processing 76")
    #best_segment, merged_segments = recursiveFindBestMatches(track_segments[76],track_segments)
    #for segment in merged_segments:
    #    print("Final Result:",track_segments.index(segment),segment)
    clusters = findBestMatchingClusters(track_segments)
    print("Final Result")
    for cluster in clusters:
        print("Cluster")
        for c in cluster:
            print("Segment",track_segments.index(c),":",c)
    #findBestMatches(track_segments[106],track_segments)


