import {PersonSnapshot} from "./person-snapshot";
import {Zone} from "./zone";

export class TimelineZone {
  startTime: number;
  endTime : number;
  zone: Zone;
  person: PersonSnapshot;
  colour: string;
}
