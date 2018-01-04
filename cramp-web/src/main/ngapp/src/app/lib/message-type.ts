/**
 * Message types
 */
export class MessageType {
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
  }
}
