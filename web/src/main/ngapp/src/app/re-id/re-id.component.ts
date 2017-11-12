import { Component, OnInit } from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {AnalyticsService} from "../services/analytics.service";
import {ConfigService} from "../services/config.service";
import {PersonSnapshot} from "../resources/person-snapshot";
import {PersonImage} from "../resources/person-image";

@Component({
  selector: 'app-re-id',
  templateUrl: './re-id.component.html',
  styleUrls: ['./re-id.component.css']
})
export class ReIdComponent implements OnInit {
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

  _uuid: String;

  private _matchedPersons : PersonImage[] = null;

  set matchedPersons(value : PersonImage[]){
    this._matchedPersons = value;
  }

  private _startTime: Date = new Date(0);
  private _endTime: Date = new Date();

  _person : PersonImage;

  get uuid() : String {
    return this._uuid;
  }

  get person() : PersonImage {
    return this._person;
  }

  set uuid(value : String){
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

  }

  constructor(private activatedRoute: ActivatedRoute, private analyticsService: AnalyticsService, private configService: ConfigService) {
  }

  ngOnInit() {
    // subscribe to router event
    this.activatedRoute.params.subscribe((params: Params) => {
      this.uuid = params['id'];
      console.log(this.uuid);
    });
  }

  triggerReId() : void {
    this.analyticsService.invokeReId(this.uuid, this.startTime.getTime(), this.endTime.getTime()).then(result=>{
      if(result){
        console.log("Re-id invoked");
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
        console.log(result.results);
      }
      else{
        console.log(result);
      }
    });
  }
}
