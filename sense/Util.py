import cv2


def restEncodeImage(image):
    encode_param = [int(cv2.IMWRITE_JPEG_QUALITY), 90]
    result, encimg = cv2.imencode('.jpg', image, encode_param)

    if not result:
        raise Exception("Unable to convert to JPEG")

    viewBytes = encimg.tolist()
    viewBytes = [x[0] for x in viewBytes]
    return viewBytes
