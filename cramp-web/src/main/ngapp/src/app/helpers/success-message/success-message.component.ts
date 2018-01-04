import {Component, Input, OnInit} from '@angular/core';
import {MessageType} from "../../lib/message-type";

@Component({
  selector: 'helper-message',
  templateUrl: './success-message.component.html',
  styleUrls: ['./success-message.component.css']
})
export class SuccessMessageComponent implements OnInit {

  private _message: string;
  private _messageType: string;
  private _timeout: number = 10000;
  private timer: any;
  messageConfig: any;

  constructor() {
  }

  ngOnInit() {
  }

  get message(): string {
    return this._message;
  }

  @Input() set message(value: string) {
    this._message = value;
    if (value != null) {
      if (this.timer != null) {
        clearTimeout(this.timer);
      }

      this.timer = setTimeout(() => {
        this.message = null;
      }, this.timeout);
    }
  }

  get messageType(): string {
    return this._messageType;
  }

  @Input() set messageType(value: string) {
    this._messageType = value;
    this.messageConfig = MessageType.MESSAGE_CONFIG[value]
  }

  get timeout(): number {
    return this._timeout;
  }

  @Input() set timeout(value: number) {
    this._timeout = value;
  }
}
