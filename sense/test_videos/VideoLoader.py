import ast
import configparser

import cv2

'''
Helper methods for loading sample videos with configurations
'''

'''
Input: 0
Screen:  [(645, 429), (50, 476), (32, 272), (469, 253)]
World:  [(162, 241), (104, 195), (138, 119), (217, 182)]
Input: 1
Screen:  [(339, 312), (200, 268), (450, 200), (706, 238)]
World:  [(161, 241), (104, 195), (137, 121), (217, 182)]
'''


def load_video(name, scale=1):
    config = configparser.ConfigParser()
    config.read("test_videos/videos.conf")

    video = config[name]
    cap = cv2.VideoCapture(video['path'])

    print(video['path'])
    print(video['markers'])
    print(video['map_markers'])

    markers = ast.literal_eval(video['markers'])
    marker_scale = float(video.get('marker_scale', '1'))
    markers = [x * (scale / marker_scale) for x in markers]

    map_markers = ast.literal_eval(video['map_markers'])
    map_marker_scale = float(video.get('map_marker_scale', '1'))
    map_markers = [x * (scale / map_marker_scale) for x in map_markers]

    return cap, markers, map_markers


def save_config(name, markers, map_markers, marker_scale=1, map_marker_scale=1):
    config = configparser.ConfigParser()
    config.read("videos.conf")

    config[name]['markers'] = str(markers)
    config[name]['map_markers'] = str(map_markers)
    config[name]['marker_scale'] = str(marker_scale)
    config[name]['map_marker_scale'] = str(map_marker_scale)

    with open('test_videos/videos.conf', 'w') as configfile:
        config.write(configfile)


def loadOfficeRoomTest():
    cap = cv2.VideoCapture("test_videos/test_office.mp4")
    cap.set(1, 300)
    markers = [(615, 340), (13, 334), (175, 90), (430, 85)]  # Scale = 0.5
    # markers = [(int(x * 0.5/0.3),int(y * 0.5/0.3)) for (x,y) in markers]

    map_markers = [(400, 350), (200, 350), (200, 100), (400, 100)]
    return cap, markers, map_markers


def loadAroundRectangleTest2():
    cap = cv2.VideoCapture("test_videos/test_ar2.mp4")

    markers = [(357, 238), (100, 233), (196, 127), (337, 123)]  # scale = 0.3
    markers = [(int(x * 0.5 / 0.3), int(y * 0.5 / 0.3)) for (x, y) in markers]

    map_markers = [(400, 350), (200, 350), (200, 100), (400, 100)]
    return cap, markers, map_markers


def loadPETS09S0RF1403V2():
    cap = cv2.VideoCapture("test_videos/S0_RF_14_03_V2.mp4")
    # NOTE: Markers are invalid
    markers = [(462, 467), (124, 364), (550, 104), (760, 139)]

    map_markers = [(400, 350), (200, 350), (200, 100), (400, 100)]
    return cap, markers, map_markers


def loadPETS09S2L1V4():
    cap = cv2.VideoCapture("test_videos/S2_L1_V4.mp4")
    # NOTE: Markers are invalid
    markers = [(462, 467), (124, 364), (550, 104), (760, 139)]

    map_markers = [(400, 350), (200, 350), (200, 100), (400, 100)]
    return cap, markers, map_markers


def loadPETS09S2L1V8():
    cap = cv2.VideoCapture("test_videos/S2_L1_V8.mp4")
    # NOTE: Markers are invalid
    markers = [(462, 467), (124, 364), (550, 104), (760, 139)]

    map_markers = [(400, 350), (200, 350), (200, 100), (400, 100)]
    return cap, markers, map_markers


def loadPETS09S2L1V6():
    cap = cv2.VideoCapture("test_videos/S2_L1_V6.mp4")
    # NOTE: Markers are invalid
    markers = [(462, 467), (124, 364), (550, 104), (760, 139)]

    map_markers = [(400, 350), (200, 350), (200, 100), (400, 100)]
    return cap, markers, map_markers


def loadPETS09S2L1V7():
    cap = cv2.VideoCapture("test_videos/S2_L1_V7.mp4")
    # NOTE: Markers are invalid
    markers = [(462, 467), (124, 364), (550, 104), (760, 139)]

    map_markers = [(400, 350), (200, 350), (200, 100), (400, 100)]
    return cap, markers, map_markers


def loadCSELounge():
    cap = cv2.VideoCapture("test_videos/CSELounge.mp4")
    # NOTE: Markers are invalid
    cap.set(1, 1000)

    markers = [(61, 171), (321, 171), (306, 135), (85, 137)]

    map_markers = [(200, 150), (400, 150), (400, 75), (200, 75)]
    return cap, markers, map_markers


def loadPETS09S2L1V5():
    cap = cv2.VideoCapture("test_videos/S2_L1_V5.mp4")
    # NOTE: Markers are invalid
    markers = [(645, 429), (50, 476), (32, 272), (469, 253)]

    map_markers = [(162, 241), (104, 195), (138, 119), (217, 182)]
    return cap, markers, map_markers


def loadPETS09S2L1V1():
    cap = cv2.VideoCapture("test_videos/S2_L1_V1.mp4")
    # NOTE: Markers are invalid
    markers = [(339, 312), (200, 268), (450, 200), (706, 238)]

    map_markers = [(162, 241), (104, 195), (138, 119), (217, 182)]
    return cap, markers, map_markers


def loadCam29():
    cap = cv2.VideoCapture("test_videos/MTMC_Cam2_9.MTS")
    # NOTE: Markers are invalid
    markers = [(462, 467), (124, 364), (550, 104), (760, 139)]

    map_markers = [(400, 350), (200, 350), (200, 100), (400, 100)]
    return cap, markers, map_markers


def loadCam59():
    cap = cv2.VideoCapture("test_videos/MTMC_Cam5_9.MTS")
    # NOTE: Markers are invalid
    markers = [(462, 467), (124, 364), (550, 104), (760, 139)]

    map_markers = [(400, 350), (200, 350), (200, 100), (400, 100)]
    return cap, markers, map_markers


def loadCam39():
    cap = cv2.VideoCapture("test_videos/MTMC_Cam3_9.MTS")
    # NOTE: Markers are invalid
    markers = [(462, 467), (124, 364), (550, 104), (760, 139)]

    map_markers = [(400, 350), (200, 350), (200, 100), (400, 100)]
    return cap, markers, map_markers


def loadCam89():
    cap = cv2.VideoCapture("test_videos/MTMC_Cam8_9.MTS")
    # NOTE: Markers are invalid
    markers = [(462, 467), (124, 364), (550, 104), (760, 139)]

    map_markers = [(400, 350), (200, 350), (200, 100), (400, 100)]
    return cap, markers, map_markers


def loadSitStand():
    cap = cv2.VideoCapture("test_videos/test_sit_stand.webm")
    # NOTE: Markers are invalid
    markers = [(462, 467), (124, 364), (550, 104), (760, 139)]

    map_markers = [(400, 350), (200, 350), (200, 100), (400, 100)]
    return cap, markers, map_markers


def loadAroundRectangleTest1():
    cap = cv2.VideoCapture("test_videos/test_ar1.mp4")
    markers = [(371, 241), (134, 236), (200, 128), (345, 123)]  # scale = 0.3
    markers = [(int(x * 0.5 / 0.3), int(y * 0.5 / 0.3)) for (x, y) in markers]

    map_markers = [(400, 350), (200, 350), (200, 100), (400, 100)]
    return cap, markers, map_markers


def loadTownCenterTest():
    cap = cv2.VideoCapture("test_videos/test_towncentre.avi")
    markers = [(462, 467), (124, 364), (550, 104), (760, 139)]  # scale = 0.5
    # markers = [(int(x * 0.5 / 0.3), int(y * 0.5 / 0.3)) for (x, y) in markers]

    map_markers = [(400, 350), (200, 350), (200, 100), (400, 100)]
    return cap, markers, map_markers


def load_ntb_middle():
    cap = cv2.VideoCapture("/home/imesha/Desktop/city branch/Organized/Middle-1.dav")
    markers = [(238, 158), (433, 119), (556, 204), (342, 269)]
    map_markers = [(206, 120), (308, 120), (230, 206), (148, 207)]
    return cap, markers, map_markers


def load_ntb_entrance():
    cap = cv2.VideoCapture("/home/imesha/Desktop/city branch/Organized/Entrance-1.dav")
    markers = [(141, 213), (304, 82), (452, 145), (419, 279)]
    map_markers = [(110, 460), (112, 308), (197, 362), (197, 461)]
    return cap, markers, map_markers


def load_ntb_server_room():
    cap = cv2.VideoCapture("/home/imesha/Desktop/city branch/Organized/Server_Room-1.dav")
    markers = [(238, 158), (433, 119), (556, 204), (342, 269)]
    map_markers = [(206, 120), (308, 120), (230, 206), (148, 207)]
    return cap, markers, map_markers


def load_ntb_counter_1():
    cap = cv2.VideoCapture("/home/imesha/Desktop/city branch/Organized/Cash_Counter_1-1.dav")
    width = cap.get(cv2.CAP_PROP_FRAME_WIDTH)
    height = cap.get(cv2.CAP_PROP_FRAME_HEIGHT)
    markers = [(433.000000, 129.000000), (544.000000, 80.000000), (648.000000, 72.000000), (616.000000, 158.000000)]
    map_markers = [(204.00, 118.00), (307.000000, 117.000000), (336.000000, 136.000000), (206.0000, 188.00)]
    return cap, markers, map_markers


if __name__ == "__main__":
    cap = cv2.VideoCapture("/home/imesha/Desktop/city branch/Organized/Middle-1.dav")
    ret, frame = cap.read()
    cv2.imwrite("tmp.jpg", frame, [cv2.IMWRITE_JPEG_QUALITY, 100])
    cap.release()
