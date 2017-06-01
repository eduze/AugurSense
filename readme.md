#Analytics Engine

This is the data aggregation core of the `Human Analytics Framework` which is being developed for 
our Final Year Project.

## Endpoints

Base URL is `/api/v1/*`

| URL       | Method    | Data Format                   | Response      |
| :--- :    | :-------: | :---------                    | :-------:     |
| realtime  |  GET      | -                             | { status : 1 }|
| realtime  |  POST     | `{`                           | 200 OK|
|           |           | `camera : { id: # },`         |               |
|           |           | `timestamp : #,`              |               |
|           |           | `coordinates : [[p1x,p1y], ...]`|             |
|           |           |  `}`          |               |
|           |           |               |               |


