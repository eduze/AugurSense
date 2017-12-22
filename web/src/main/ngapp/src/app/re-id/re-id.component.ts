import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Params} from '@angular/router';
import {AnalyticsService} from "../services/analytics.service";
import {ConfigService} from "../services/config.service";
import {PersonSnapshot} from "../resources/person-snapshot";
import {PersonImage} from "../resources/person-image";
import {Observable} from "rxjs/Observable";
import {GlobalMap} from "../resources/global-map";
import {TimelineTrack} from "../resources/timeline-track";

@Component({
  selector: 'app-re-id',
  templateUrl: './re-id.component.html',
  styleUrls: ['./re-id.component.css']
})
export class ReIdComponent implements OnInit {
  get selectedSegmentIndex(): number {
    return this._selectedSegmentIndex;
  }

  set selectedSegmentIndex(value: number) {
    this._selectedSegmentIndex = value;
  }

  get selectedTrackIndex(): number {
    return this._selectedTrackIndex;
  }

  set selectedTrackIndex(value: number) {
    this._selectedTrackIndex = value;
  }

  get useTrackSegments(): boolean {
    return this._useTrackSegments;
  }


  set useTrackSegments(value: boolean) {
    this._useTrackSegments = value;
    this.selectedIndices.length = 0;
    this.timelineTracks.length = 0;
    this.updateMapTracks();
    this.loadMainTimeline();
  }

  globalMap: GlobalMap;

  private _useTrackSegments: boolean = true;
  private _selectedTrackIndex: number = -1;
  private _selectedSegmentIndex: number = -1;

  personSnapshots: PersonSnapshot[][] = [];
  currentPersonSnapshots: PersonSnapshot[] = [];

  private mainTimeline: TimelineTrack = null;


  private isSelected(p: PersonSnapshot): boolean {
    if (p.ids.length == 0)
      return false;

    if (!this.useTrackSegments) {
      return p.ids[0] == this.selectedTrackIndex;
    }
    else {
      return p.ids[0] == this.selectedTrackIndex && p.trackSegmentIndex == this.selectedSegmentIndex;
    }
  }


  private getColour(index: number): string {
    return "rgb(" + Math.round(((index / 256 / 256) * 40) % 256).toString() + "," + Math.round(((index / 256) * 40) % 256).toString() + "," + Math.round((index * 40) % 256).toString() + ")";
  }

  private getStandSitColour(person: PersonSnapshot): string {
    const standCol = Math.round(person.standProbability * 255);
    const sitCol = Math.round(person.sitProbability * 255);

    return "rgb(" + standCol.toString() + ", " + sitCol.toString() + ",255 )";
  }

  private backgroundClicked(): void {
    this.selectedTrackIndex = -1;
  }

  private trackClicked(track: PersonSnapshot[]): void {
    if (track[0].ids.length > 0)
      this.selectedTrackIndex = track[0].ids[0];
    console.log("Track clicked" + track[0].ids[0].toString());
    console.log(track);
  }

  get selectedIndices(): number[][] {
    return this._selectedIndices;
  }

  set selectedIndices(value: number[][]) {
    this._selectedIndices = value;
  }

  get matchedPersons(): PersonImage[] {
    return this._matchedPersons;
  }

  get startTime(): Date {
    return this._startTime;
  }

  set startTime(value: Date) {
    this._startTime = value;
  }

  get endTime(): Date {
    return this._endTime;
  }

  set endTime(value: Date) {
    this._endTime = value;
  }

  _uuid: string;

  private _matchedPersons: PersonImage[] = null;

  set matchedPersons(value: PersonImage[]) {
    this._matchedPersons = value;
  }

  private _startTime: Date = new Date(0);
  private _endTime: Date = new Date();

  _person: PersonImage;

  private _selectedIndices: number[][] = [];

  toggleSelection(p: PersonSnapshot): void {
    if (this.useTrackSegments) {
      if (this.selectedIndices.filter((v) => v[0] == p.ids[0] && v[1] == p.trackSegmentIndex).length > 0) {
        let i = this.selectedIndices.indexOf(this.selectedIndices.filter((v) => v[0] == p.ids[0] && v[1] == p.trackSegmentIndex)[0]);
        this.selectedIndices.splice(i, 1);

        this.personSnapshots.filter((t) => t[0].ids[0] == p.ids[0] && t[0].trackSegmentIndex == p.trackSegmentIndex).forEach((t) => {
          let i2 = this.personSnapshots.indexOf(t);
          this.personSnapshots.splice(i2, 1);
        });

        this.timelineTracks.filter((t) => t.person.ids[0] == p.ids[0] && t.person.trackSegmentIndex == p.trackSegmentIndex).forEach((t) => {
          if (this.mainTimeline == t)
            return;
          let i2 = this.timelineTracks.indexOf(t);
          this.timelineTracks.splice(i2, 1);
          this.timelineTracksUI = this.timelineTracks.slice(0);
          console.log("Spliced");
        });


      }
      else {
        this.selectedIndices.push([p.ids[0], p.trackSegmentIndex]);
        this.addMapTrack(p);
        this.addTimeline(p);
      }
    }
    else {
      if (this.selectedIndices.filter((v) => v[0] == p.ids[0]).length > 0) {
        let i = this.selectedIndices.indexOf(this.selectedIndices.filter((v) => v[0] == p.ids[0])[0]);
        this.selectedIndices.splice(i, 1);
        this.personSnapshots.filter((t) => t[0].ids[0] == p.ids[0]).forEach((t) => {
          let i2 = this.personSnapshots.indexOf(t);
          this.personSnapshots.splice(i2, 1);
        });

        this.timelineTracks.filter((t) => t.person.ids[0] == p.ids[0]).forEach((t) => {
          if (this.mainTimeline == t)
            return;
          let i2 = this.timelineTracks.indexOf(t);
          this.timelineTracks.splice(i2, 1);
          this.timelineTracksUI = this.timelineTracks.slice(0);
          console.log("Spliced-NonSegment");
        });
        this.loadMainTimeline();
      }
      else {
        this.selectedIndices.push([p.ids[0], p.trackSegmentIndex]);
        this.addMapTrack(p);
        this.addTimeline(p);
      }
    }

  }

  get uuid(): string {
    return this._uuid;
  }

  get person(): PersonImage {
    return this._person;
  }

  set uuid(value: string) {
    this._uuid = value;
    this.updateProfile();
  }

  getDateString(timestamp: number) {
    return new Date(timestamp).toLocaleString();
  }

  updateProfile(): void {
    this.analyticsService.getProfile(this.uuid)
      .then(person => {
        this._person = person;

        console.log(this._person);

        this.loadMainTimeline();

        //this.drawOnCanvas(personSnapshots);
      })
      .catch(reason => console.log(reason));

    this.updateMapTracks();

  }

  updateMapTracks(): void {
    console.debug("Loading target track history");
    this.analyticsService.getTrackFromUUID(this.startTime.getTime(), this.endTime.getTime(), this.uuid, this.useTrackSegments)
      .then(ps => {

        console.debug("target track history loaded");
        this.currentPersonSnapshots = ps;
        ps[0]["colour"] = this.getColour(ps[0].ids[0]);
        ps[0]["standSitColour"] = this.getStandSitColour(ps[0]);

        console.log(ps);


        //this.drawOnCanvas(personSnapshots);
      })
      .catch(reason => console.log(reason));

    let clonePersons = this.personSnapshots.splice(0);
    this.personSnapshots.length = 0;
    clonePersons.forEach((clone_person) => {
      this.addMapTrack(clone_person[0]);
    });

  }

  timelineTracks: TimelineTrack[] = [];

  timelineTracksUI: TimelineTrack[] = [];

  private searchInvoked: boolean = false;

  // loadedTimelineTracks: TimelineTrack[] = [
  //   {
  //     label: "Person1",
  //     person: null,
  //     timelineZones:[
  //       {
  //         zoneId: 1,
  //         startTime: 100,
  //         endTime: 200,
  //         zoneName: "bottom",
  //         person: null,
  //         colour: "red"
  //       },
  //       {
  //         zoneId: 2,
  //         startTime: 200,
  //         endTime: 220,
  //         zoneName: "center",
  //         person: null,
  //         colour: "green"
  //       }
  //     ]
  //   },
  //   {
  //     trackName: "Person2",
  //     person: null,
  //     timelineZones:[
  //       {
  //         zoneId: 1,
  //         startTime: 50,
  //         endTime: 200,
  //         zoneName: "left",
  //         person: null,
  //         colour: "blue"
  //       },
  //       {
  //         zoneId: 2,
  //         startTime: 250,
  //         endTime: 350,
  //         zoneName: "right",
  //         person: null,
  //         colour: "yellow"
  //       }
  //     ]
  //   },
  //   {
  //     trackName: "Person3",
  //     person: null,
  //     timelineZones:[
  //       {
  //         zoneId: 1,
  //         startTime: 0,
  //         endTime: 100,
  //         zoneName: "in",
  //         person: null,
  //         colour: "orange"
  //       },
  //       {
  //         zoneId: 2,
  //         startTime: 150,
  //         endTime: 350,
  //         zoneName: "out",
  //         person: null,
  //         colour: "purple"
  //       }
  //     ]
  //   }
  // ];

  private startResultsLoop(): void {
    Observable.interval(2000).subscribe(x => {
      if (this.searchInvoked) {
        console.debug("Loading search results...");
        this.getReIdResults();
      }

    });
  }

  constructor(private activatedRoute: ActivatedRoute, private analyticsService: AnalyticsService, private configService: ConfigService) {
  }

  ngOnInit() {
    // subscribe to router event
    this.activatedRoute.params.subscribe((params: Params) => {
      this.uuid = params['id'];
      console.log(this.uuid);
    });

    this.configService.getMap().then((globalMap) => {
      this.globalMap = globalMap;
      console.log(this.globalMap);
    });

    this.startResultsLoop();


  }

  loadedMainTimelineId: number = -1;
  loadedMainTimelineSegment: number = -1;
  loadedSegmented: boolean;

  loadMainTimeline(): void {
    if (this.loadedMainTimelineId == this.person.ids[0] && this.loadedMainTimelineSegment == this.person.trackSegmentIndex && this.loadedSegmented == this.useTrackSegments)
      return;

    if (this.timelineTracks.includes(this.mainTimeline)) {
      return;
    }

    console.log("Loading main timeline");
    this.analyticsService.getTimelineFromTrack(this.person.ids[0], this.person.trackSegmentIndex, this.useTrackSegments).then(result => {
      this.mainTimeline = result;
      this.mainTimeline.label = "Probe";
      console.log("Main timeline:");
      console.log(result);
      this.loadedMainTimelineId = this.person.ids[0];
      this.loadedMainTimelineSegment = this.person.trackSegmentIndex;
      this.loadedSegmented = this.useTrackSegments;
      this.timelineTracks = [this.mainTimeline].concat(this.timelineTracks);
      this.timelineTracksUI = this.timelineTracks.slice(0);
    });
  }


  triggerReId(): void {
    this.analyticsService.invokeReId(this.uuid, this.startTime.getTime(), this.endTime.getTime(), this.useTrackSegments).then(result => {
      if (result) {
        console.log("Re-id invoked");
        this.searchInvoked = true;
      }
      else {
        console.log("Re-id invoke failed");
      }
    });
  }

  getReIdResults(): void {
    this.analyticsService.getReIdResults(this.uuid, this.startTime.getTime(), this.endTime.getTime(), this.useTrackSegments).then(result => {
      if (result.completed) {
        console.log("RE_ID_Results");
        console.log(result.results);
        this.matchedPersons = result.results;

        this.matchedPersons.forEach((v) => {
          v["colour"] = this.getColour(v.ids[0]);
        });

        console.log("Completed");
        this.searchInvoked = false;

      }
      else {
        console.log(result);
      }
    });
  }

  isImageSelected(matchedPerson: PersonImage): boolean {
    if (this.useTrackSegments) {
      if (this.selectedIndices.filter((v) => matchedPerson.ids.includes(v[0]) && matchedPerson.trackSegmentIndex == v[1]).length > 0) {
        return true;
      }
    }
    else {
      if (this.selectedIndices.filter((v) => matchedPerson.ids.includes(v[0])).length > 0) {
        return true;
      }
    }
    return false;
  }

  private addTimeline(p: PersonSnapshot): void {
    console.debug("Loading timeline for");
    console.debug(p);
    this.analyticsService.getTimelineFromTrack(p.ids[0], p.trackSegmentIndex, this.useTrackSegments).then(result => {
      if (result.person == null) {
        console.debug("Received no person");
        return;
      }

      if (this.selectedIndices.filter((v) => (this.useTrackSegments && v[0] == result.person.ids[0] && v[1] == result.person.trackSegmentIndex) || (!this.useTrackSegments && v[0] == result.person.ids[0])).length > 0) {
        this.timelineTracks.push(result);
        this.timelineTracksUI = this.timelineTracks.slice(0);
        console.log("Timeline Tracks:");
        console.log(this.timelineTracks);
      }
    }).catch(reason => console.error(reason));
  }


  private addMapTrack(p: PersonSnapshot) {
    console.debug("Requesting track for");
    console.debug(p);

    this.analyticsService.getTrackFromUUID(this.startTime.getTime(), this.endTime.getTime(), p.uuid, this.useTrackSegments)
      .then(ps => {
        if (this.selectedIndices.filter((v) => (this.useTrackSegments && v[0] == ps[0].ids[0] && v[1] == ps[0].trackSegmentIndex) || (!this.useTrackSegments && v[0] == ps[0].ids[0])).length > 0) {
          this.personSnapshots.push(ps);
          ps[0]["colour"] = this.getColour(ps[0].ids[0]);
          ps[0]["standSitColour"] = this.getStandSitColour(ps[0]);

          console.log(this.personSnapshots);
        }

        //this.drawOnCanvas(personSnapshots);
      })
      .catch(reason => console.log(reason));
  }
}
