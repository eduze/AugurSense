import logging
import multiprocessing
from multiprocessing import Queue, Process
from time import sleep

from DetectorService import DetectorService
from LightWeightCamServer import LightWeightCamServer
from OpenPersonDetector import OpenPersonDetector
from PTEMapper import PTEMapper
from Sense import Sense
from WorldSpaceTracker import WorldSpaceTracker
from experiments.AngleMapper import AngleMapper
from experiments.Snapy import Snapy
from test_videos.VideoLoader import load_video

logging.basicConfig(level=logging.DEBUG)


def run_cam_server_pier2(input_queue, output_queue):
    cap, markers, map_markers = load_video("bia.pier2")
    position_mapper = PTEMapper(markers, map_markers)
    sense = Sense(input_queue, output_queue, position_mapper, AngleMapper(position_mapper), WorldSpaceTracker(),
                  Snapy())

    server = LightWeightCamServer(10005, sense, cap, (0.5, 0.5))
    server.load_config(markers, map_markers)
    server.start_cam_server()


def run_cam_server_departure(input_queue, output_queue):
    cap, markers, map_markers = load_video("bia.departure")
    position_mapper = PTEMapper(markers, map_markers)
    sense = Sense(input_queue, output_queue, position_mapper, AngleMapper(position_mapper), WorldSpaceTracker(),
                  Snapy())

    server = LightWeightCamServer(10004, sense, cap, (0.5, 0.5))
    server.load_config(markers, map_markers)
    server.start_cam_server()


def run_detector_service(queue_pairs):
    detector = OpenPersonDetector(preview=False)
    service = DetectorService(detector, queue_pairs)
    service.start()


if __name__ == "__main__":
    multiprocessing.set_start_method("spawn")

    pier2_input = Queue()
    pier2_output = Queue()

    departure_input = Queue()
    departure_output = Queue()

    queue_pairs = [(pier2_input, pier2_output), (departure_input, departure_output)]

    service_process = Process(target=run_detector_service, args=([queue_pairs]))
    service_process.daemon = True
    service_process.start()

    pier2_process = Process(target=run_cam_server_pier2, args=(pier2_input, pier2_output))
    pier2_process.daemon = True
    pier2_process.start()

    departure_process = Process(target=run_cam_server_departure, args=(departure_input, departure_output))
    departure_process.daemon = True
    departure_process.start()

    processes = [service_process, pier2_process, departure_process]

    running = True
    while running:
        for process in processes:
            if not process.is_alive():
                running = False
                break
        sleep(5)

    for process in processes:
        if process.is_alive():
            process.terminate()
