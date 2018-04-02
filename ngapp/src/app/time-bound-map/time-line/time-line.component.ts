import {Component, Input, OnInit} from '@angular/core';
import {PersonSnapshot} from '../../resources/person-snapshot';
import {AnalyticsService} from '../../services/analytics.service';
import {CameraGroup} from '../../resources/camera-group';
import {PersonImage} from '../../resources/person-image';

@Component({
  selector: 'app-time-line',
  templateUrl: './time-line.component.html'
})
export class TimeLineComponent implements OnInit {

  private _startTime: Date;
  private _endTime: Date;
  private _secondRange: number[] = [0, 60];
  private personSnapshots: PersonSnapshot[][] = [];

  private _cameraGroup: CameraGroup;
  private _selectedTrackIndex = -1;
  selectedTrack: PersonSnapshot[];

  constructor(private analyticsService: AnalyticsService) {
    this.endTime = new Date();
    this.startTime = new Date();
    this.startTime.setDate(this.startTime.getDate() - 7);
  }

  ngOnInit() {
    this.refresh();
  }

  get selectedTrackIndex(): number {
    return this._selectedTrackIndex;
  }

  set selectedTrackIndex(value: number) {
    this._selectedTrackIndex = value;
  }

  get secondRange(): number[] {
    return this._secondRange;
  }

  set secondRange(value: number[]) {
    this._secondRange = value;
    this.refresh();
  }

  get startTime(): Date {
    return this._startTime;
  }

  set startTime(value: Date) {
    this._startTime = value;
    this.refresh();
  }

  get endTime(): Date {
    return this._endTime;
  }

  set endTime(value: Date) {
    this._endTime = value;
    this.refresh();
  }

  private getColour(index: number): string {
    return 'rgb(' + Math.round(((index / 256 / 256) * 40) % 256).toString() + ',' + Math.round(((index / 256) * 40) % 256).toString()
      + ',' + Math.round((index * 40) % 256).toString() + ')';
  }

  private getStandSitColour(person: PersonSnapshot): string {
    const standCol = Math.round(person.standProbability * 255);
    const sitCol = Math.round(person.sitProbability * 255);

    return 'rgb(' + standCol.toString() + ', ' + sitCol.toString() + ',255 )';
  }

  private get from(): number {
    if (this.startTime == null || this.secondRange[0] == null) {
      return null;
    }
    return this.startTime.getTime() + this.secondRange[0] * 1000;
  }

  private get to(): number {
    if (this.endTime == null || this.secondRange[1] == null) {
      return null;
    }
    return this.endTime.getTime() + this.secondRange[1] * 1000;
  }

  private backgroundClicked(): void {
    this.selectedTrackIndex = -1;
    this.selectedTrack = null;
  }

  private isSelected(p: PersonSnapshot): boolean {
    if (p.id === 0) {
      return false;
    }

    return p.id === this.selectedTrackIndex;
  }

  private trackClicked(snapshots: PersonSnapshot[]): void {
    this.selectedTrackIndex = snapshots[0].id;
    console.log('Track clicked', snapshots[0].id);
    this.selectedTrack = snapshots;
  }

  private refresh(): void {
    if (!this.cameraGroup) {
      console.log('No camera groups set');
      return;
    }

    this.analyticsService.getTimeboundMap(this.cameraGroup.id, this.from, this.to)
      .then(ps => {
        this.personSnapshots = ps;
        console.log('Snaps', ps[0]);
        ps.forEach((item) => {
          item.reverse();
          item[0]['colour'] = this.getColour(item[0].id);
          item[0]['standSitColour'] = this.getStandSitColour(item[0]);
        });
        // console.log(this.personSnapshots);
        // this.drawOnCanvas(personSnapshots);
      })
      .catch(reason => console.log(reason));

  }

  personClicked(person: PersonImage): void {
    this.selectedTrackIndex = person.id;
    console.log('Track Segment Index: ', person.trackSegmentIndex);
    console.log('Person clicked', person.id);
  }

  get cameraGroup(): CameraGroup {
    return this._cameraGroup;
  }

  @Input()
  set cameraGroup(value: CameraGroup) {
    this._cameraGroup = value;
    this.refresh();
  }
}
