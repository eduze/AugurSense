import time

import cv2

MODES = ("zone", "mapping")


def run_mapper(background_image, capture, scale):
    map_points = []
    screen_points = []
    zones = []
    active_zone = []
    mode = 'zone'

    def mouse_clicked(event, x, y, flags, param):
        if event == cv2.EVENT_LBUTTONDOWN:
            if mode == "zone":
                active_zone.append((x, y))
                print(active_zone)
            elif mode == "mapping":
                param.append((x, y))
                print(param)
        elif event == cv2.EVENT_RBUTTONDOWN:
            if mode == "zone":
                if len(active_zone) > 0:
                    active_zone.pop()
                    print(active_zone)
            elif mode == "mapping":
                if len(param) > 0:
                    param.pop()
                    print(param)

    cv2.namedWindow("Screen")
    cv2.namedWindow("Map")
    cv2.setMouseCallback("Screen", mouse_clicked, param=screen_points)
    cv2.setMouseCallback("Map", mouse_clicked, param=map_points)

    while True:
        map_image = background_image.copy()
        ret, frame = capture.read()
        if not ret:
            break
        time.sleep(0.5)

        frame = cv2.resize(frame, (0, 0), fx=scale[0], fy=scale[1])

        for point in screen_points:
            cv2.drawMarker(frame, point, (0, 255, 0), thickness=2)

        for point in map_points:
            cv2.drawMarker(map_image, point, (0, 255, 0), thickness=2)

        for point in active_zone:
            cv2.drawMarker(map_image, point, (255, 255, 0), thickness=2)

        for zone in zones:
            for point in zone:
                cv2.drawMarker(map_image, (point[0], point[1]), (255, 0, 0), thickness=2)

        cv2.imshow("Screen", frame)
        cv2.imshow("Map", map_image)

        key = cv2.waitKey(1)
        if key & 0xFF == ord('q'):
            break
        elif key & 0xFF == ord('n'):
            if len(active_zone) > 0:
                zones.append(active_zone)
                active_zone = []
        elif key & 0xFF == ord('m'):
            mode = MODES[1]
        elif key & 0xFF == ord('z'):
            mode = MODES[0]

    print("Screen Space: ", screen_points)
    print("World Space: ", map_points)
    print("Zones: ", zones)


if __name__ == "__main__":
    cap = cv2.VideoCapture("rtsp://admin:1234@10.201.81.105/cam/realmonitor?channel=01&subtype=01")
    background_image = cv2.imread('counter.jpg')
    run_mapper(background_image, cap, (1, 1))
