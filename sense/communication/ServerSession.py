import logging
from time import sleep

from communication.APIAccess import APIAccess

config_host = "127.0.0.1"
config_port = 8000
config_my_ip = "127.0.0.1"
config_my_port = 1024
server_session = None


def getServerSessionInstance(host=config_host, port=config_port, my_ip=config_my_ip, my_port=config_my_port):
    '''
    Obtain singleton instance
    :param host: 
    :param port: 
    :param my_ip: 
    :param my_port: 
    :return: 
    '''
    global server_session
    if server_session is None:
        server_session = ServerSession(host, port, my_ip, my_port)

    return server_session


class ServerSession:
    '''
    Manages session with analytics engine
    '''

    def __init__(self, host=config_host, port=config_port, my_ip=config_my_ip, my_port=config_my_port):
        '''
        Initialize
        :param host: server host 
        :param port: server port
        :param my_ip: camera ip
        :param my_port: camera port
        '''
        self.api_access = APIAccess(host, port)
        self.my_ip = my_ip
        self.my_port = my_port
        self.camera_id = -1
        self.mapping = None
        self.logger = logging.getLogger("ServerSession")

    def configureMapping(self, captured_image, w, h, markers=None, map_markers=None):
        '''
        Send camera view to analytics engine
        :param markers:
        :param map_markers:
        :param captured_image:
        :return: 
        '''
        id = self.api_access.postCameraView(self.camera_id, captured_image,
                                            self.my_ip, self.my_port, w, h, markers, map_markers)
        self.logger.info("Got camera ID: %d", id)
        self.camera_id = id

    def obtainMapping(self):
        '''
        Request configuration of camera from analytics engine. Blocking call.
        :return: 
        '''
        results = self.api_access.getMap(self.camera_id)
        while not results:
            results = self.api_access.getMap(self.camera_id)
            sleep(10)

        self.mapping = Mapping()
        screenSpaceList = results["pointMapping"]["screenSpacePoints"]
        worldSpaceList = results["pointMapping"]["worldSpacePoints"]
        for p in screenSpaceList:
            self.mapping.screen_space_points.append((int(p["x"]), int(p["y"])))
        for p in worldSpaceList:
            self.mapping.world_space_points.append((int(p["x"]), int(p["y"])))

        # self.mapping.map_width = results["mapWidth"]
        # self.mapping.map_height = results["mapHeight"]

        return self.mapping

    def publish(self, timestamp, coordinates):
        self.api_access.publish(self.camera_id, timestamp, coordinates)


class Mapping:
    '''
    Mapping configuration from camera view to world space
    '''

    def __init__(self):
        self.screen_space_points = []
        self.world_space_points = []
        self.image = None
        self.map_width = 0
        self.map_height = 0
