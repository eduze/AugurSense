import matplotlib.pyplot as plt
import numpy as np


class HumanDensityHeatmapViewer:
    def __init__(self, time_length):
        self.frames = []
        self.time_length = time_length
        self.window_title = "Heatmap"
        self.width = 800
        self.height = 600
        self.fig = None
        self.ax = None
        self.im = None

    def setupWindow(self):
        # create the figure
        self.fig = plt.figure()
        self.fig.suptitle(self.window_title)
        self.ax = self.fig.add_subplot(111)
        self.im = self.ax.imshow(np.random.random((self.height, self.width)))
        plt.show(block=False)

    def __appendFrame(self, sensed_persons):
        self.frames.append(sensed_persons)

        while len(self.frames) > self.time_length:
            del self.frames[0]

    def processFrame(self, sensed_persons):
        self.__appendFrame(sensed_persons)

        new_map = np.zeros((self.height, self.width))
        index = 0
        for sensed_persons in self.frames[-self.time_length:]:
            for sensed_person in sensed_persons:
                for y in range(int(sensed_person.position[1]) - 10,
                               int(sensed_person.position[1]) + 10):
                    for x in range(int(sensed_person.position[0]) - 10,
                                   int(sensed_person.position[0]) + 10):
                        if 0 <= x <= new_map.shape[1] and 0 <= y <= new_map.shape[0]:
                            d = np.math.sqrt((x - sensed_person.position[0]) ** 2 + (
                            y - sensed_person.position[1]) ** 2)
                            t = 1 / (1 + d * 2)
                            if t < 0.05:
                                t = t ** 1.25
                            if t < 0.01:
                                t = t ** 2
                            # t = 0.5
                            new_map[y, x] += t * (index / float(self.time_length))
            index += 1

        # replace the image contents
        self.im.set_array(new_map)
        # redraw the figure
        self.fig.canvas.draw()
        plt.pause(0.001)
