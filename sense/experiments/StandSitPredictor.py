stand_neck_hip_knee_ratio = 1.5
sit_neck_hip_knee_ratio = 1.2


def predictStandProbability(neck_hip_knee_ratio):
    if neck_hip_knee_ratio is None:
        return 0.5
    result = (neck_hip_knee_ratio - sit_neck_hip_knee_ratio) / (stand_neck_hip_knee_ratio - sit_neck_hip_knee_ratio)
    result = min(result, 1)
    result = max(result, 0)
    return result
