/**
 * Message types
 */
export class Message {
  public static SUCCESS: string = "Success";
  public static ERROR: string = "Error";

  public static MESSAGE_CONFIG = {
    'Success': {
      'class': 'alert-success',
      'icon': 'fa-check'
    },
    'Error': {
      'class': 'alert-danger',
      'icon': 'fa-ban'
    }
  };

  private _message: string;
  private _messageType: string;
  private _config: any;

  constructor(message: string, messageType: string) {
    this.message = message;
    this.messageType = messageType;
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
    this._config = Message.MESSAGE_CONFIG[value];
  }

  get config(): any {
    return this._config;
  }

  set config(value: any) {
    this._config = value;
  }
}
