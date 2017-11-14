import { Component, OnInit } from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {AnalyticsService} from "../services/analytics.service";
import {ConfigService} from "../services/config.service";
import {PersonSnapshot} from "../resources/person-snapshot";
import {PersonImage} from "../resources/person-image";
import {Observable} from "rxjs/Observable";
import {GlobalMap} from "../resources/global-map";

@Component({
  selector: 'app-re-id',
  templateUrl: './re-id.component.html',
  styleUrls: ['./re-id.component.css']
})
export class ReIdComponent implements OnInit {

  globalMap: GlobalMap;

  selectedTrackIndex: number = -1;

  personSnapshots: PersonSnapshot[][] = [];
  currentPersonSnapshots : PersonSnapshot[] = [];

  private getColour(index: number) : string{
    return "rgb(" + Math.round(((index / 256 / 256) * 40) % 256).toString() + "," + Math.round(((index / 256) * 40) % 256).toString() + "," + Math.round((index * 40) % 256).toString() + ")";
  }

  private getStandSitColour(person: PersonSnapshot): string{
    const standCol = Math.round(person.standProbability * 255);
    const sitCol = Math.round(person.sitProbability * 255);

    return "rgb("+standCol.toString()+", " +sitCol.toString() + ",255 )";
  }

  private backgroundClicked() : void{
    this.selectedTrackIndex = -1;
  }

  private trackClicked(track:PersonSnapshot[]): void{
    if(track[0].ids.length > 0)
      this.selectedTrackIndex = track[0].ids[0];
    console.log("Track clicked" + track[0].ids[0].toString());
    console.log(track);
  }

  get selectedIndices(): number[] {
    return this._selectedIndices;
  }

  set selectedIndices(value: number[]) {
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

  private _matchedPersons : PersonImage[] = null;

  set matchedPersons(value : PersonImage[]){
    this._matchedPersons = value;
  }

  private _startTime: Date = new Date(0);
  private _endTime: Date = new Date();

  _person : PersonImage;

  private _selectedIndices : number[] = [];

  toggleSelection(p:PersonSnapshot) : void{
    if(this.selectedIndices.includes(p.ids[0])){
      let i = this.selectedIndices.indexOf(p.ids[0]);
      this.selectedIndices.splice(i,1);
      this.personSnapshots.filter((t)=>t[0].ids[0]==p.ids[0]).forEach((t)=>{
        let i2 = this.personSnapshots.indexOf(t);
        this.personSnapshots.splice(i2,1);
      });
    }
    else{
      this.selectedIndices.push(p.ids[0]);
      this.addMapTrack(p);
    }
  }

  get uuid() : string {
    return this._uuid;
  }

  get person() : PersonImage {
    return this._person;
  }

  set uuid(value : string){
    this._uuid = value;
    this.updateProfile();
  }

  getDateString(timestamp: number) {
    return new Date(timestamp).toLocaleString();
  }

  updateProfile(): void{
    this.analyticsService.getProfile(this.uuid)
      .then(person => {
        this._person = person;

        console.log(this._person);
        //this.drawOnCanvas(personSnapshots);
      })
      .catch(reason => console.log(reason));
    console.debug("Loading target track history");
    this.analyticsService.getTrackFromUUID(this.startTime.getTime(), this.endTime.getTime(),this.uuid)
      .then(ps => {

        console.debug("target track history loaded");
        this.currentPersonSnapshots = ps;
        ps[0]["colour"] = this.getColour(ps[0].ids[0]);
        ps[0]["standSitColour"] = this.getStandSitColour(ps[0]);

        console.log(ps);


        //this.drawOnCanvas(personSnapshots);
      })
      .catch(reason => console.log(reason));
  }

  private searchInvoked:boolean = false;
  private startResultsLoop(): void {
    Observable.interval(2000).subscribe(x => {
      if(this.searchInvoked)
      {
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

  triggerReId() : void {
    this.analyticsService.invokeReId(this.uuid, this.startTime.getTime(), this.endTime.getTime()).then(result=>{
      if(result){
        console.log("Re-id invoked");
        this.searchInvoked = true;
      }
      else{
        console.log("Re-id invoke failed");
      }
    });
  }

  getReIdResults() : void {
    this.analyticsService.getReIdResults(this.uuid, this.startTime.getTime(), this.endTime.getTime()).then(result=>{
      if(result.completed){
        this.matchedPersons = result.results;
        console.log("Completed");
        this.searchInvoked = false;
        console.log(result.results);
      }
      else{
        console.log(result);
      }
    });
  }

  private addMapTrack(p: PersonSnapshot) {
    console.debug("Requesting track for");
    console.debug(p);

    this.analyticsService.getTrackFromUUID(this.startTime.getTime(), this.endTime.getTime(),p.uuid)
      .then(ps => {
        if(this.selectedIndices.includes(ps[0].ids[0]))
        {
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
