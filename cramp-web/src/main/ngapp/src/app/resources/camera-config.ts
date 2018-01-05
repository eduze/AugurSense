import {PointMapping} from "./point-mapping";
import {CameraView} from "./camera-view";

export class CameraConfig {
  cameraId: number;
  ipAndPort: string;
  pointMapping: PointMapping;
  cameraView: CameraView;

  constructor(cameraId: number, ipAndPort: string, pointMapping: PointMapping, cameraView: CameraView) {
    this.cameraId = cameraId;
    this.ipAndPort = ipAndPort;
    this.pointMapping = pointMapping;
    this.cameraView = cameraView;
  }

  public toJSON(key: string): Object {
    return {
      cameraId: this.cameraId,
      ipAndPort: this.ipAndPort,
      pointMapping: this.pointMapping,
      view: this.cameraView.view.split(",")[1]
    };
  }
}
