import cv2
import tensorflow as tf
import numpy as np
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
        self.inference = tf.nn.softmax(self.logits)

        self.optimizer = tf.train.MomentumOptimizer(self.learning_rate, momentum=0.9)
        self.train = self.optimizer.minimize(self.loss, global_step=self.global_step)
        self.lr = FLAGS.learning_rate
        self.sess = tf.Session()

        self.sess.run(tf.global_variables_initializer())
        self.saver = tf.train.Saver()

        self.ckpt = tf.train.get_checkpoint_state(FLAGS.logs_dir)
        if self.ckpt and self.ckpt.model_checkpoint_path:
            print('Restore model')
            self.saver.restore(self.sess, self.ckpt.model_checkpoint_path)

    def match(self, image1, image2):
        confidence = self.getConfidence(image1,image2)
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
                results.append((gallery[i][1], self.getConfidence(look_fors[j],gallery[i][0])))
        sorted_results = sorted(results,key=lambda x : x[1],reverse=True)
        return sorted_results


    def close(self):
        self.sess.close()

if __name__ == "__main__":
    api = ReIDAPI()
    image1 = cv2.imread(FLAGS.image1)
    image2 = cv2.imread(FLAGS.image2)
    print(api.match(image1,image1))
    api.close()
