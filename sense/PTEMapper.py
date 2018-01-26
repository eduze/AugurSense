import QuadToQuadMap


class PTEMapper:
    def __init__(self, screen_points, world_points):
        self.screen_points = screen_points
        self.world_points = world_points

    def isReady(self):
        return len(self.screen_points) == 4 and len(self.world_points) == 4

    def mapScreenToWorld(self, x, y):
        transformMat = QuadToQuadMap.getTransformMat(self.screen_points[0], self.screen_points[1],
                                                     self.screen_points[2], self.screen_points[3],
                                                     self.world_points[0], self.world_points[1],
                                                     self.world_points[2], self.world_points[3])
        transformed_coord = QuadToQuadMap.getXY(transformMat, x, y)
        return transformed_coord
