import tensorflow as tf
import cv2

from detectors.TFOPDetector.common import preprocess, estimate_pose
from detectors.TFOPDetector.networks import get_network
from detectors.TFOPDetector.realtime_webcam import cb_showimg


class PoseAPI:
    def __init__(self, model="mobilenet", input_width=368, input_height=368, stage_level = 6, show_preview = False):
        self.input_width = input_width
        self.input_height = input_height
        self.stage_level = stage_level
        self.model = model
        self.show_preview = show_preview

        self._input_node = tf.placeholder(tf.float32, shape=(1, input_height, input_width, 3), name='image')

        self._sess = tf.Session()
        self._net, _, self._last_layer = get_network(model, self._input_node, self._sess)
        self.outputWidth = 0
        self.outputHeight = 0

    def processFrame(self, img):
        preprocessed = preprocess(img, self.input_width, self.input_height)

        pafMat, heatMat = self._sess.run(
            [
                self._net.get_output(name=self._last_layer.format(stage=self.stage_level, aux=1)),
                self._net.get_output(name=self._last_layer.format(stage=self.stage_level, aux=2))
            ], feed_dict={'image:0': [preprocessed]}
        )
        heatMat, pafMat = heatMat[0], pafMat[0]

        humans = estimate_pose(heatMat, pafMat)

        image = img
        self.outputHeight, self.outputWidth = image.shape[:2]

        if self.show_preview:
            cb_showimg(img, preprocessed, heatMat, pafMat, humans, show_process=True)

        return humans

    def close(self):
        self._sess.close()


if __name__ == "__main__":
    api = PoseAPI(show_preview=True)

    cap = cv2.VideoCapture("/home/madhawa/test_videos/leapset.mp4")
    cap.set(cv2.CAP_PROP_POS_MSEC, 85000)

    while True:
        ret, frame = cap.read()

        humans = api.processFrame(frame)

        if cv2.waitKey(1) == 27:
            break  # esc to quit

    api.close()