'''Maps Angle from Screen Domain to World Domain'''
import math

import QuadToQuadMap


class AngleMapper:
    def __init__(self, mapper):
        self.mapper = mapper

    def transformAngle(self, screen_point, screen_angle):
        '''
        Maps an angle in screen space to world space
        :param screen_point: reference point in screen where angle is located
        :param screen_angle: angle in degrees
        :return: 2D unit Vector heading at direction in world space
        '''
        if screen_angle is None:
            return None

        screen_angle = screen_angle / 180 * math.pi

        deltaY = math.cos(screen_angle)
        deltaX = -math.sin(screen_angle)

        # Obtain new screen point
        new_point = (screen_point[0] + deltaX, screen_point[1] + deltaY)

        # Take screen point to world
        new_point_world = self.mapper.mapScreenToWorld(new_point[0], new_point[1])
        screen_point_world = self.mapper.mapScreenToWorld(screen_point[0], screen_point[1])

        world_vector = (new_point_world[0] - screen_point_world[0], new_point_world[1] - screen_point_world[1])

        d = math.sqrt(world_vector[0] * world_vector[0] + world_vector[1] * world_vector[1])

        # Normalize world vector
        world_vector = (world_vector[0] / d, world_vector[1] / d)
        return world_vector
