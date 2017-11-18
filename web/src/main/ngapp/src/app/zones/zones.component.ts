import {Component, OnInit} from '@angular/core';
import {AnalyticsService} from "../services/analytics.service";
import {Zone} from "../resources/zone";
import {ConfigService} from "../services/config.service";
import {GlobalMap} from "../resources/global-map";
import {ZoneStatistic} from "../resources/zone-statistic";
import {Observable} from "rxjs/Observable";

@Component({
  selector: 'app-zones',
  templateUrl: './zones.component.html',
  styleUrls: ['./zones.component.css']
})

export class ZonesComponent implements OnInit {

  zones: Zone[] = [];
  globalMap: GlobalMap;

  polygons: any[] = [];

  zoneStatistics : ZoneStatistic[] = [];
  selectedZoneIndex : number = -1;
  totalPeople: number;

  private _fromDate : Date = new Date(0);
  private _toDate : Date = new Date();

  get fromDate() : Date{
    return this._fromDate;
  }

  set fromDate(value : Date){
    this._fromDate = value;
    this.fetchResults();
  }

  get toDate() : Date{
    return this._toDate;
  }

  set toDate(value : Date){
    this._toDate = value;
    this.fetchResults();
  }

  constructor(private analyticsService: AnalyticsService, private configService: ConfigService) {
  }

  private zoneClicked(index: number): void {
    this.selectedZoneIndex = index;
  }

  private backgroundClicked(){
    this.selectedZoneIndex = -1;
    console.log("Unselected");

    this.zoneStatistics.forEach((v)=>{
      if(v.zoneId == 0)
      {
        //Rest of the world zone found
        this.selectedZoneIndex = this.zoneStatistics.indexOf(v);
      }
    });

  }




  private fetchResults() : void{
    if(this.fromDate == null)
      return;
    if(this.toDate == null)
      return;

    this.analyticsService.getZoneStatistics(this.fromDate.getTime(), this.toDate.getTime()).then((zs) => {
      this.zoneStatistics = zs;
      if(this.zoneStatistics.length > 0)
        this.totalPeople = this.zoneStatistics.map((item) => item.averagePersonCount).reduce((r1,r2)=> r1+ r2);
      else
        this.totalPeople = 0;

      this.polygons.map((item) => {
        item.outgoingMap.length = 0;
        item.incomingMap.length = 0;

        let matches = zs.filter((match) => {
          return item.zone.id == match.zoneId
        });
        if(matches.length > 0){
          item.zoneStatistic = matches[0];

          for(var key in item.zoneStatistic.outgoingMap) {
            if(item.zoneStatistic.outgoingMap.hasOwnProperty(key)) {
              // find matching polygon
              let matching_polys = this.polygons.filter((mat_poly)=>{
                return key == mat_poly.zone.id;
              });
              if(matching_polys.length > 0){
                let matching_poly = matching_polys[0];
                let transmission = { midX: matching_poly.midX, midY: matching_poly.midY, count: item.zoneStatistic.outgoingMap[key] / item.zoneStatistic.totalOutgoing};
                item.outgoingMap.push(transmission);
              }
            }
          }

          for(var key in item.zoneStatistic.incomingMap) {
            if(item.zoneStatistic.incomingMap.hasOwnProperty(key)) {
              // find matching polygon
              let matching_polys = this.polygons.filter((mat_poly)=>{
                return key == mat_poly.zone.id;
              });
              if(matching_polys.length > 0){
                let matching_poly = matching_polys[0];
                let transmission = { midX: matching_poly.midX, midY: matching_poly.midY, count: item.zoneStatistic.incomingMap[key] / item.zoneStatistic.totalIncoming};
                item.incomingMap.push(transmission);
              }
            }
          }

        }



      });
      //this.selectedZoneStatistic = zs[0];
      console.log(zs);
    });


  }



  ngOnInit() {
    this.configService.getZones().then((zones) => {
      this.zones = zones;
      for (let zone of zones) {
        let poly: { polygon: String, midX: number, midY: number, zone: Zone, zoneStatistic: ZoneStatistic, outgoingMap:any, incomingMap : any } = {
          polygon: null,
          midX: 0,
          midY: 0,
          zone: null,
          zoneStatistic: null,
          outgoingMap : [],
          incomingMap : []
        };
        let midX = 0;
        let midY = 0;

        let p = "";
        for (let i in zone.xCoordinates) {
          p += zone.xCoordinates[i] + "," + zone.yCoordinates[i] + " ";
          midX += zone.xCoordinates[i];
          midY += zone.yCoordinates[i];
        }

        midX /= zone.xCoordinates.length;
        midY /= zone.yCoordinates.length;

        poly.polygon = p;
        poly.midX = midX;
        poly.midY = midY;
        poly.zone = zone;



        this.polygons.push(poly);
      }
    });

    this.configService.getMap().then((globalMap) => {
      this.globalMap = globalMap;
      console.log(this.globalMap);
    });

    Observable.interval(2000).subscribe(x => {
      this.fetchResults();
    });


  }
}
