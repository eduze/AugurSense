import {Component, Input, OnInit} from '@angular/core';
import {PersonSnapshot} from "../resources/person-snapshot";
import {GlobalMap} from "../resources/global-map";
import {AnalyticsService} from "../services/analytics.service";
import {ConfigService} from "../services/config.service";

@Component({
  selector: 'app-time-bound-map',
  templateUrl: './time-bound-map.component.html',
  styleUrls: ['./time-bound-map.component.css']
})
export class TimeBoundMapComponent implements OnInit {
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

  @Input()
  set startTime(value: Date) {
    this._startTime = value;
    this.refresh();
  }

  get endTime(): Date {
    return this._endTime;
  }

  @Input()
  set endTime(value: Date) {
    this._endTime = value;
    this.refresh();
  }

  private toDateTimeString(timestamp: number) : string{
    return new Date(timestamp).toLocaleString();
  }

  private getColour(index: number) : string{
    return "rgb(" + Math.round(((index / 256 / 256) * 40) % 256).toString() + "," + Math.round(((index / 256) * 40) % 256).toString() + "," + Math.round((index * 40) % 256).toString() + ")";
  }

  private getStandSitColour(person: PersonSnapshot): string{
    const standCol = Math.round(person.standProbability * 255);
    const sitCol = Math.round(person.sitProbability * 255);

    return "rgb("+standCol.toString()+", " +sitCol.toString() + ",255 )";
  }

  private get from() : number{
    if(this.startTime == null || this.secondRange[0] == null)
      return null;
    return this.startTime.getTime() + this.secondRange[0] * 1000;
  }

  private get to() : number{
    if(this.endTime == null || this.secondRange[1] == null)
      return null;
    return this.endTime.getTime() + this.secondRange[1] * 1000;
  }

  constructor(private analyticsService: AnalyticsService, private configService: ConfigService) { }

  private _startTime: Date = new Date(0);
  private _endTime: Date = new Date(0);

  private _secondRange : number[] = [0,60];
  private personSnapshots: PersonSnapshot[][] = [[]];
  globalMap: GlobalMap;

  private _selectedTrackIndex : number = -1;


  private backgroundClicked() : void{
    this.selectedTrackIndex = -1;
  }

  private trackClicked(track:PersonSnapshot[]): void{
    if(track[0].ids.length > 0)
      this.selectedTrackIndex = track[0].ids[0];
    console.log("Track clicked" + track[0].ids[0].toString());
    console.log(track);
  }

  private refresh() : void {

    if(this.startTime == null)
      return;

    if(this.endTime == null)
      return;

    this.analyticsService.getTimeboundMap(this.from,this.to)
      .then(ps => {
        this.personSnapshots = ps;
        ps.forEach((item)=>{
          item.reverse();
          item[0]["colour"] = this.getColour(item[0].ids[0]);
          item[0]["standSitColour"] = this.getStandSitColour(item[0]);
        });
        //console.log(this.personSnapshots);
        //this.drawOnCanvas(personSnapshots);
      })
      .catch(reason => console.log(reason));

  }

  ngOnInit() {
    this.configService.getMap().then((globalMap) => {
      this.globalMap = globalMap;
      console.log(this.globalMap);
    });
    this.refresh();
  }

}
