import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'reverse',
  pure: false
})
export class ReversePipe implements PipeTransform {

  transform(value: any, args?: any): any {
    if (value) {
      return value.reverse();
    }
  }

}
