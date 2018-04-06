import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'timeAgo',
  pure: true
})
export class TimeAgoPipe implements PipeTransform {

  transform(epochTime: number, args?: any): any {
    let currentTime = Date.now();
    let seconds = Math.floor((currentTime - epochTime) / 1000);
    let text = "";
    if (seconds > 0) {
      let days = Math.floor(seconds / (3600 * 24));
      if (days > 0) {
        text += days + " days ";
      }

      seconds -= days * 3600 * 24;
      let hrs = Math.floor(seconds / 3600);
      if (hrs > 0) {
        text += hrs + " hours ";
      }

      seconds -= hrs * 3600;
      let mnts = Math.floor(seconds / 60);
      if (mnts > 0) {
        text += mnts + " minutes ";
      }

      seconds -= mnts * 60;
      text += seconds + " seconds ago"
    }

    return text;
  }

}
