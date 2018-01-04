import {Component, Input} from '@angular/core';
import {Zone} from "../../../resources/zone";
import {ConfigService} from "../../../services/config.service";
import {MessageType} from "../../../lib/message-type";

@Component({
  selector: 'app-zones-config-info',
  templateUrl: './zones-config-info.component.html',
  styleUrls: ['./zones-config-info.component.css']
})
export class ZonesConfigInfoComponent {

  private _zone: Zone;
  private _message: string;
  private _messageType: string;

  constructor(private configService: ConfigService) {
  }

  public save(): void {
    this.message = null;

    this.configService.updateZone(this.zone).then(success => {
      this.message = "Successfully updated zone";
      this._messageType = MessageType.SUCCESS;
    }).catch(reason => {
      this.message = "Unable to update zone";
      this._messageType = MessageType.ERROR;
    })
  }

  public delete(): void {
    this.message = null;

    this.configService.deleteZone(this.zone.id).then(success => {
      this.message = "Successfully deleted zone";
      this._messageType = MessageType.SUCCESS;
    }).catch(reason => {
      this.message = "Unable to deleted zone";
      this._messageType = MessageType.ERROR;
    })
  }

  get zone(): Zone {
    return this._zone;
  }

  @Input() set zone(value: Zone) {
    this._zone = value;
    this.message = null;
  }

  get message(): string {
    return this._message;
  }

  set message(value: string) {
    this._message = value;
  }

  get messageType(): string {
    return this._messageType;
  }

  set messageType(value: string) {
    this._messageType = value;
  }
}
