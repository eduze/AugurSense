import {Component, Input, OnInit} from '@angular/core';
import * as moment from 'moment';

import {TimelineTrack} from "../resources/timeline-track";
@Component({
  selector: 'app-timeline',
  templateUrl: './timeline.component.html',
  styleUrls: ['./timeline.component.css']
})
export class TimelineComponent implements OnInit {
  get tracks(): TimelineTrack[] {
    return this._tracks;
  }

  @Input()
  set tracks(value: TimelineTrack[]) {
    this._tracks = value;
    console.log("Setted called");
    this.refresh();
  }

  private _tracks: TimelineTrack[] = null;

  @Input()
  public chartOptions : any = {
    animation:{ duration: 0 },
    legend: {
      display: false
    },
    responsive:true,
    scales: {
      xAxes: [{
        stacked: true,
        ticks:{
          callback:(v)=> TimelineComponent.epoch_to_hh_mm_ss(v),
          stepSize:5, //add a tick every 5 minutes
        }
      }],
      yAxes: [{
        stacked: true
      }],
    },
    tooltips: {
      callbacks: {
        label: function(tooltipItem, data) {
          if(tooltipItem.xLabel == 0)
            return null;
          if(data.datasets[tooltipItem.datasetIndex].backgroundColor == "transparent")
            return null;
          return data.datasets[tooltipItem.datasetIndex].label + ': ' + TimelineComponent.epoch_to_hh_mm_ss(tooltipItem.xLabel)
        }
      }
    }
  };

  static epoch_to_hh_mm_ss(epoch) {
    return new Date(epoch).toISOString().substr(12, 7)
  }

  constructor() {
  }

  data: any;

  ngOnInit() {
    this.refresh();
  }

  refresh() : void {
    if(this.tracks == null){
      this.data = null;
      return;
    }

    console.log("Reloading chart!");
    let dataLabels: string[] = [];
    let ds: any[] = [];

    this.tracks.forEach((track)=>dataLabels.push(track.label));

    this.tracks.forEach((track)=>{
      let currentOffset = 0;

      let preZeroes = [];
      for(let i = 0; i < this.tracks.indexOf(track); i++)
        preZeroes.push(0);
      console.log("PreZeroes");
      console.log(preZeroes);

      let postZeroes = [];
      for(let i = this.tracks.indexOf(track); i < this.tracks.length-1; i++)
        postZeroes.push(0);
      console.log("PostZeroes");
      console.log(postZeroes);

      track.timelineZones.forEach((zone)=>{
        if(zone.startTime > currentOffset) {
          //add a dummy zone
          let dummyZone = {
            label: 'Empty',
            backgroundColor: 'transparent',
            borderColor: '#1E88E5',
            data: preZeroes.concat([zone.startTime - currentOffset]).concat(postZeroes)
          };

          ds.push(dummyZone);
          currentOffset = zone.startTime;
        }

        let dataZone = {
          label: zone.zone.zoneName,
          backgroundColor: zone.colour,
          borderColor: '#1E88E5',
          data: preZeroes.concat([zone.endTime - currentOffset]).concat(postZeroes)
        };
        ds.push(dataZone);
        currentOffset = zone.endTime;


      });
    });

    this.data = {
      labels:dataLabels,
      datasets:ds
    };
  }

}
