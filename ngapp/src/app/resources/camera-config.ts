import {PointMapping} from './point-mapping';
import {CameraView} from './camera-view';
import {CameraGroup} from './camera-group';
import {Point} from "./point";

export class CameraConfig {
  cameraId: number;
  ipAndPort: string;
  pointMapping: PointMapping;
  cameraView: CameraView;
  cameraGroup: CameraGroup;

  constructor(cameraId: number, ipAndPort: string, pointMapping: PointMapping, cameraView: CameraView, cameraGroup: CameraGroup) {
    this.cameraId = cameraId;
    this.ipAndPort = ipAndPort;
    this.pointMapping = pointMapping;
    this.cameraView = cameraView;
    this.cameraGroup = cameraGroup;
  }

  public static fromJSON(config: any): CameraConfig {
    const cameraId = config.cameraId;
    const view = 'data:image/JPEG;base64,' + config.view;
    const ipPort = config.ipAndPort;
    const cameraGroup = CameraGroup.fromJSON(config.cameraGroup);
    const cameraConfig = new CameraConfig(cameraId, ipPort, new PointMapping(), new CameraView(view), cameraGroup);

    for (const i in config.pointMapping.screenSpacePoints) {
      const screenSpacePoint = new Point(config.pointMapping.screenSpacePoints[i].x, config.pointMapping.screenSpacePoints[i].y);
      cameraConfig.pointMapping.screenSpacePoints.push(screenSpacePoint);
      const worldSpacePoint = new Point(config.pointMapping.worldSpacePoints[i].x, config.pointMapping.worldSpacePoints[i].y);
      cameraConfig.pointMapping.worldSpacePoints.push(worldSpacePoint);
    }
    return cameraConfig;
  }

  public toJSON(key: string): Object {
    return {
      cameraId: this.cameraId,
      ipAndPort: this.ipAndPort,
      pointMapping: this.pointMapping,
      view: this.cameraView.view.split(',')[1],
      cameraGroup: this.cameraGroup
    };
  }
}
