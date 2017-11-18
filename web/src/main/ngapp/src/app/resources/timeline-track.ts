import {PersonSnapshot} from "./person-snapshot";
import {TimelineZone} from "./timeline-zone";

export class TimelineTrack {
  person: PersonSnapshot;
  timelineZones : TimelineZone[];
  label: string;
}
