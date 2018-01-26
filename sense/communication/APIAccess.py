import json

import requests

from Util import restEncodeImage


class APIAccess:
    '''
    Stubs for REST Endpoints of Analytics Engine Server
    '''

    def __init__(self, host, port):
        '''
        Initialize
        :param host: Server host 
        :param port: Server port
        '''
        self.host = "http://" + str(host) + ":" + str(port) + "/"
        self.api_root = self.host + "api/v1/"
        self.config_root = self.api_root + "config/"
        self.realtime_root = self.api_root + "realtime"

    def requestCameraId(self):
        '''
        Request a camera_id from analytics engine
        :return: 
        '''
        response = requests.get(self.config_root + "cameraId")
        if response.status_code == 200:
            json_data = json.loads(response.text)
            return json_data["id"]
        else:
            raise ConnectionError(response.status_code)

    def postCameraView(self, camera_id, image, my_ip, my_port, markers=None, map_markers=None):
        '''
        Send camera captured image to analytics engine
        :param camera_id: camera_id
        :param image: image
        :param my_ip: camera_ip
        :param my_port: camera_port
        :return: 
        '''
        viewBytes = restEncodeImage(image)

        ipAndPort = my_ip + ":" + str(my_port)

        screenSpacePoints = []
        worldSpacePoints = []
        if markers is not None:
            for x, y in markers:
                screenSpacePoints.append({"x": x, "y": y})
            for x, y in map_markers:
                worldSpacePoints.append({"x": x, "y": y})

        params = {
            "cameraId": str(camera_id),
            "ipAndPort": ipAndPort,
            "pointMapping": {
                "screenSpacePoints": screenSpacePoints,
                "worldSpacePoints": worldSpacePoints
            },
            "view": viewBytes
        }

        print(json.dumps(params))
        response = requests.post(self.config_root + "cameraConfig", json=params)

        if response.status_code == 200:
            return True
        else:
            raise ConnectionError(response.status_code)

    def getMap(self, camera_id):
        '''
        Obtain camera markers mapping from analytics engine
        :param camera_id: 
        :return: 
        '''
        response = requests.get(self.config_root + str(camera_id))
        if response.status_code == 200:
            return json.loads(response.text)
        else:
            raise ConnectionError(response.status_code)

    def publish(self, camera_id, timestamp, coords):
        '''
        Publish mapped person coordinates to analytics engine
        :param camera_id: camera_id
        :param timestamp: timestamp
        :param coords: person_coordinates
        :return: 
        '''
        result = {
            "camera": {"id": camera_id},
            "timestamp": timestamp,
            "coordinates": []
        }

        for coord in coords:
            result["coordinates"].append({"x": coord[0], "y": coord[1]})

        response = requests.get(self.realtime_root, json=result)

        if response.status_code == 200:
            return True
        else:
            raise ConnectionError(response.status_code)
