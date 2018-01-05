import {Component, Input, OnInit} from '@angular/core';
import {Message} from "../../lib/message";

@Component({
  selector: 'helper-message',
  templateUrl: './success-message.component.html',
  styleUrls: ['./success-message.component.css']
})
export class SuccessMessageComponent implements OnInit {

  private _message: Message;
  private _timeout: number = 10000;
  private timer: any;

  constructor() {
  }

  ngOnInit() {
  }

  get message(): Message {
    return this._message;
  }

  @Input() set message(value: Message) {
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

  get timeout(): number {
    return this._timeout;
  }

  set timeout(value: number) {
    this._timeout = value;
  }
}
