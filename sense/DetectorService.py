import logging
from multiprocessing import Queue


class DetectorService:
    """
    Detector service for multiprocessing
    """

    def __init__(self, detector, queues=[]):
        self.detector = detector
        self.queues = queues
        self.running = False
        self.logger = logging.getLogger("DetectorService")

    def add_queues(self, input_queue: Queue, output_queue: Queue):
        self.queues.append((input_queue, output_queue))

    def start(self):
        self.logger.info("Starting")
        self.running = True

        while self.running:
            for pair in self.queues:
                if pair[0].qsize() > 0:
                    frame = pair[0].get()
                    persons = self.detector.detectPersons(frame, None)
                    pair[1].put(persons)

        self.logger.info("Stopped")

    def stop(self):
        self.logger.info("Stopping")
        self.running = False
