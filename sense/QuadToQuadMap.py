'''
Perspective Transform Estimation Implementation
'''
import numpy as np


def getXY(TransformMat, x, y):
    '''
    Obtain X,Y using PTE of x,y by TransformMat
    :param TransformMat: Transform Matrix
    :param x: source x
    :param y: source y
    :return: 
    '''
    g = TransformMat[2][0]
    h = TransformMat[2][1]
    W = g * x + h * y + 1
    inputs = [[x], [y], [1]]
    results = np.matmul(TransformMat, inputs)
    return [results[0][0] / W, results[1][0] / W]


def getTransformMat(A1, A2, A3, A4, B1, B2, B3, B4):
    '''
    Obtain PTE matrix using 4 coordinate point pairs
    :param A1: 
    :param A2: 
    :param A3: 
    :param A4: 
    :param B1: 
    :param B2: 
    :param B3: 
    :param B4: 
    :return: 
    '''
    (x1, y1) = A1[:]
    (x2, y2) = A2[:]
    (x3, y3) = A3[:]
    (x4, y4) = A4[:]
    (X1, Y1) = B1[:]
    (X2, Y2) = B2[:]
    (X3, Y3) = B3[:]
    (X4, Y4) = B4[:]

    A = [[x1, y1, 1, 0, 0, 0, -X1 * x1, -X1 * y1],
         [0, 0, 0, x1, y1, 1, -Y1 * x1, -Y1 * y1],
         [x2, y2, 1, 0, 0, 0, -X2 * x2, -X2 * y2],
         [0, 0, 0, x2, y2, 1, -Y2 * x2, -Y2 * y2],
         [x3, y3, 1, 0, 0, 0, -X3 * x3, -X3 * y3],
         [0, 0, 0, x3, y3, 1, -Y3 * x3, -Y3 * y3],
         [x4, y4, 1, 0, 0, 0, -X4 * x4, -X4 * y4],
         [0, 0, 0, x4, y4, 1, -Y4 * x4, -Y4 * y4]]
    B = [[X1], [Y1], [X2], [Y2], [X3], [Y3], [X4], [Y4]]

    # Al=B
    A = np.array(A)
    B = np.array(B)
    l = np.matmul(np.linalg.inv(np.matmul(A.transpose(), A)), np.matmul(A.transpose(), B))
    [a, b, c, d, e, f, g, h] = l[:]
    transformMat = [[a, b, c], [d, e, f], [g, h, 1]]
    return transformMat


if __name__ == "__main__":
    # Test Snippet
    TransformMat = getTransformMat([0, 0], [1, 0], [1, 1], [0, 1], [10, 10], [15, 10], [15, 15], [10, 15])
    (X, Y) = getXY(TransformMat=TransformMat, x=0.5, y=0)
    print(X, Y)
