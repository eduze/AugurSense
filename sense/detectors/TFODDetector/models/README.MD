The models are downloaded from Tensorflow Object Detection API
https://github.com/tensorflow/models/tree/master/research/object_detection

Performance on 940MX
===

- faster_rcnn_inception_v2_coco_2017_11_08 - 0.46s
    + Ok. Detects closer people from entrance camera
- faster_rcnn_inception_resnet_v2_atrous_lowproposals_coco_2017_11_08 - 4.5s 
    + Detects everyone in counter correctly
- faster_rcnn_inception_resnet_v2_atrous_coco_2017_11_08 - 4.6s
    + Correctly identify the people from entrance CCTV view
- faster_rcnn_nas_coco_2017_11_08 - 4.3s
    + Not accurate enough
- ssd_mobilenet_v1_coco_2017_11_17 - 0.1s
    + Worst. Not detecting people
- ssd_inception_v2_coco_2017_11_17 - 0.13s
    + Not detecting people
- rfcn_resnet101_coco_2017_11_08 - 0.67s
    + Similar to 1st one accuracy wise
