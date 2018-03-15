import {GlobalMap} from './global-map';

export class CameraGroup {
  private _id: number;
  private _name: string;
  private _map: GlobalMap;

  constructor(id: number, name: string, map: GlobalMap) {
    this.id = id;
    this.name = name;
    this.map = map;
  }

  public static fromJSON(val: any) {
    if (!val) {
      return null;
    }

    return new CameraGroup(val.id, val.name, GlobalMap.fromJSON(val.map));
  }

  get id(): number {
    return this._id;
  }

  set id(value: number) {
    this._id = value;
  }

  get name(): string {
    return this._name;
  }

  set name(value: string) {
    this._name = value;
  }

  get map(): GlobalMap {
    return this._map;
  }

  set map(value: GlobalMap) {
    this._map = value;
  }

  public toJSON() {
    return {
      id: this.id,
      name: this.name,
      map: this.map.toJSON()
    };
  }
}
