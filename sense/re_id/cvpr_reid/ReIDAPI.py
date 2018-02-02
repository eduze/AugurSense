import os
import time

import cv2
import numpy as np
import tensorflow as tf
from tensorflow.python.saved_model import tag_constants

from re_id.cvpr_reid.run import FLAGS
from re_id.cvpr_reid.run import network, preprocess

IMAGE_WIDTH = 60
IMAGE_HEIGHT = 160


class ReIDAPI:
    def __init__(self):
        FLAGS.batch_size = 1
        self.learning_rate = tf.placeholder(tf.float32, name='learning_rate')
        self.images = tf.placeholder(tf.float32, [2, FLAGS.batch_size, IMAGE_HEIGHT, IMAGE_WIDTH, 3], name='images')
        self.labels = tf.placeholder(tf.float32, [FLAGS.batch_size, 2], name='labels')
        self.is_train = tf.placeholder(tf.bool, name='is_train')
        self.global_step = tf.Variable(0, name='global_step', trainable=False)
        self.weight_decay = 0.0005
        self.train_num_id = 0
        self.val_num_id = 0
        self.images1, self.images2 = preprocess(self.images, self.is_train)

        print('Build network')
        self.logits = network(self.images1, self.images2, self.weight_decay)
        self.loss = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(labels=self.labels, logits=self.logits))
        self.inference = tf.nn.softmax(self.logits, name="Inference_Final")

        self.optimizer = tf.train.MomentumOptimizer(self.learning_rate, momentum=0.9)
        self.train = self.optimizer.minimize(self.loss, global_step=self.global_step)
        self.lr = FLAGS.learning_rate
        self.sess = tf.Session()

        self.sess.run(tf.global_variables_initializer())
        self.saver = tf.train.Saver()

        self.ckpt = tf.train.get_checkpoint_state(FLAGS.logs_dir)
        if self.ckpt and self.ckpt.model_checkpoint_path:
            print('Restore model: %s' % self.ckpt.model_checkpoint_path)
            self.saver.restore(self.sess, self.ckpt.model_checkpoint_path)

        # tf.train.write_graph(self.sess.graph_def, FLAGS.logs_dir, "model.pb", False)
        # tf.train.export_meta_graph(filename="meta_graph.meta", as_text=True)
        # self.saver.save(self.sess, FLAGS.logs_dir + 'model.ckpt', 11000)
        tensor_info_images = tf.saved_model.utils.build_tensor_info(self.images)
        tensor_info_is_train = tf.saved_model.utils.build_tensor_info(self.is_train)
        tensor_info_inference = tf.saved_model.utils.build_tensor_info(self.inference)

        prediction_signature = (
            tf.saved_model.signature_def_utils.build_signature_def(
                inputs={
                    'images': tensor_info_images,
                    'is_train': tensor_info_is_train
                },
                outputs={
                    'Inference_Final': tensor_info_inference
                },
                method_name=tf.saved_model.signature_constants.PREDICT_METHOD_NAME))

        legacy_init_op = tf.group(tf.tables_initializer(), name='legacy_init_op')

        export_path_base = "sense/re_id/cvpr_reid/model"
        export_path = os.path.join(
            tf.compat.as_bytes(export_path_base),
            tf.compat.as_bytes(str(1)))
        print('Exporting trained model to', export_path)
        builder = tf.saved_model.builder.SavedModelBuilder(export_path)
        builder.add_meta_graph_and_variables(
            self.sess, [tag_constants.SERVING],
            signature_def_map={
                'predict_images': prediction_signature
            },
            legacy_init_op=legacy_init_op)
        builder.save()

    def match(self, image1, image2):
        confidence = self.getConfidence(image1, image2)
        return confidence > 0.5

    def getConfidence(self, image1, image2):
        image1 = cv2.resize(image1, (IMAGE_WIDTH, IMAGE_HEIGHT))
        image1 = cv2.cvtColor(image1, cv2.COLOR_BGR2RGB)
        image1 = np.reshape(image1, (1, IMAGE_HEIGHT, IMAGE_WIDTH, 3)).astype(float)
        image2 = cv2.resize(image2, (IMAGE_WIDTH, IMAGE_HEIGHT))
        image2 = cv2.cvtColor(image2, cv2.COLOR_BGR2RGB)
        image2 = np.reshape(image2, (1, IMAGE_HEIGHT, IMAGE_WIDTH, 3)).astype(float)
        test_images = np.array([image1, image2])

        feed_dict = {self.images: test_images, self.is_train: False}
        prediction = self.sess.run(self.inference, feed_dict=feed_dict)
        positive = prediction[0][0]
        negative = prediction[0][1]
        confidence = positive / (positive + negative)
        return confidence

    def findBestMatches(self, gallery, look_fors):
        results = []
        for i in range(len(gallery)):
            for j in range(len(look_fors)):
                results.append((gallery[i][1], self.getConfidence(look_fors[j], gallery[i][0])))
        sorted_results = sorted(results, key=lambda x: x[1], reverse=True)
        return sorted_results

    def close(self):
        self.sess.close()


def load_from_file(image1, image2):
    with tf.Session() as sess:
        print("load graph")
        model_name = "sense/re_id/cvpr_reid/logs/model.ckpt-11000"
        saver = tf.train.import_meta_graph('{}.meta'.format(model_name))
        saver.restore(sess, '{}'.format(model_name))

        graph = tf.get_default_graph()
        images = graph.get_tensor_by_name("images:0")
        is_train = graph.get_tensor_by_name("is_train:0")
        infer = graph.get_tensor_by_name('Inference_Final:0')

        image1 = cv2.resize(image1, (IMAGE_WIDTH, IMAGE_HEIGHT))
        image1 = cv2.cvtColor(image1, cv2.COLOR_BGR2RGB)
        image1 = np.reshape(image1, (1, IMAGE_HEIGHT, IMAGE_WIDTH, 3)).astype(float)
        image2 = cv2.resize(image2, (IMAGE_WIDTH, IMAGE_HEIGHT))
        image2 = cv2.cvtColor(image2, cv2.COLOR_BGR2RGB)
        image2 = np.reshape(image2, (1, IMAGE_HEIGHT, IMAGE_WIDTH, 3)).astype(float)

        test_images = np.array([image1, image2])

        feed_dict = {images: test_images, is_train: False}
        prediction = sess.run(infer, feed_dict=feed_dict)
        positive = prediction[0][0]
        negative = prediction[0][1]
        confidence = positive / (positive + negative)
        print(confidence)
        print("DONE")


def load_from_file2(image1, image2):
    with tf.Session(graph=tf.Graph()) as sess:
        tf.saved_model.loader.load(sess, [tag_constants.SERVING], "sense/re_id/cvpr_reid/model/1")
        graph = sess.graph
        images = graph.get_tensor_by_name("images:0")
        is_train = graph.get_tensor_by_name("is_train:0")
        infer = graph.get_tensor_by_name('Inference_Final:0')

        image1 = cv2.resize(image1, (IMAGE_WIDTH, IMAGE_HEIGHT))
        image1 = cv2.cvtColor(image1, cv2.COLOR_BGR2RGB)
        image1 = np.reshape(image1, (1, IMAGE_HEIGHT, IMAGE_WIDTH, 3)).astype(float)
        image2 = cv2.resize(image2, (IMAGE_WIDTH, IMAGE_HEIGHT))
        image2 = cv2.cvtColor(image2, cv2.COLOR_BGR2RGB)
        image2 = np.reshape(image2, (1, IMAGE_HEIGHT, IMAGE_WIDTH, 3)).astype(float)

        test_images = np.array([image1, image2])

        feed_dict = {images: test_images, is_train: False}
        prediction = sess.run(infer, feed_dict=feed_dict)
        positive = prediction[0][0]
        negative = prediction[0][1]
        confidence = positive / (positive + negative)
        print(confidence)
        print("DONE")


if __name__ == "__main__":
    # api = ReIDAPI()
    image1 = cv2.imread(
        "/home/imesha/Documents/Projects/FYP/Eduze/CRAMP_Accumulator/cramp-ui/src/main/resources/CRAMP_re_id/gallery/12783_312.jpg")
    image2 = cv2.imread(
        "/home/imesha/Documents/Projects/FYP/Eduze/CRAMP_Accumulator/cramp-ui/src/main/resources/CRAMP_re_id/gallery/797898_71.jpg")
    image3 = cv2.imread(
        "/home/imesha/Documents/Projects/FYP/Eduze/CRAMP_Accumulator/cramp-ui/src/main/resources/CRAMP_re_id/gallery/92076_425.jpg")
    image4 = cv2.imread(
        "/home/imesha/Documents/Projects/FYP/Eduze/CRAMP_Accumulator/cramp-ui/src/main/resources/CRAMP_re_id/gallery/55422_370.jpg")
    gallery = [(image1, "Black"), (image2, "sitting"), (image3, "Blue_Shirt"), (image4, "Black_Another")]
    t = time.time()
    # print(api.findBestMatches(gallery, [image1]))
    # load_from_file(image1,image2)
    # load_from_file2(image1, image4)
    load_from_file2(image1, image4)
    print("Time : %f" % ((time.time() - t) / 4))
    # api.close()
