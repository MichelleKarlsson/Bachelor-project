import cv2
import numpy as np
import matplotlib.pyplot as plt
import glob
import os

directory = "C:/Users/miche/Desktop/dataset"

for f in os.listdir(directory):

    version = 1

    img = cv2.imread(os.path.join(directory, f))    
    rows, cols, ch = img.shape

    pts1 = np.float32([[50,50],[200,50],[50,200]])
    pts2 = np.float32([[10,90],[150,50],[100,300]])

    M = cv2.getAffineTransform(pts1, pts2)

    dst = cv2.warpAffine(img, M, (cols, rows))

    filename, fileext = os.path.splitext(f)

    if not cv2.imwrite(os.path.join(directory, filename + str(version) + fileext), dst):
        raise Exception("could not write image " +  os.path.join(directory, f, str(version)))

    version = 2
    
    M2 = np.float32([[2,0,0],[0,2,0]])
    res = cv2.warpAffine(img, M2, (cols, rows))

    if not cv2.imwrite(os.path.join(directory, filename + str(version)+fileext), res):
            raise Exception("could not write image " + os.path.join(directory, f, str(version)))

    


