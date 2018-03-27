import {Component, Input} from '@angular/core';
import {Zone} from '../../../resources/zone';
import {ConfigService} from '../../../services/config.service';
import {Message} from '../../../lib/message';

@Component({
  selector: 'app-zones-config-info',
  templateUrl: './zones-config-info.component.html',
  styleUrls: ['./zones-config-info.component.css']
})
export class ZonesConfigInfoComponent {

  private _message: Message;
  private _zone: Zone;
  private _zones: Zone[];

  constructor(private configService: ConfigService) {
  }

  public save(): void {
    console.log(this.zone);
    this.configService.updateZone(this.zone).then(success => {
      this.message = new Message('Successfully updated zone', Message.SUCCESS);
    }).catch(reason => {
      this.message = new Message('Unable to update zone', Message.ERROR);
    });
  }

  public delete(): void {
    this.configService.deleteZone(this.zone.id).then(success => {
      this.message = new Message('Successfully deleted zone', Message.SUCCESS);
      const index = this.zones.indexOf(this.zone);
      this.zones.splice(index, 1);
    }).catch(reason => {
      console.log(reason);
      this.message = new Message('Unable to delete zone', Message.ERROR);
    });
  }

  get zone(): Zone {
    return this._zone;
  }

  @Input() set zone(value: Zone) {
    this._zone = value;
    this.message = null;
  }

  get message(): Message {
    return this._message;
  }

  set message(value: Message) {
    this._message = value;
  }

  get zones(): Zone[] {
    return this._zones;
  }

  @Input() set zones(value: Zone[]) {
    this._zones = value;
  }
}
