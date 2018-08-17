# gentle-reminder
A small service that can remind about periodic events

## slack token
Slack token can be provided in `reference.conf` file (of course you can put it on `application.conf`):
```
slack {
  token = "something"
  channelId = "channelId"
}
```

## scheduled events
Event definitions should also live in the same config file as `slack`:
```
akka {
  quartz {
    schedules {
      EveryMinuteHeartBeat {
        description = "A cron job that fires off every minute"
        expression = "0 * * ? * *"
      }
    }
  }
}
```
Useful tool for managing cron expressions can be found [here](https://www.freeformatter.com/cron-expression-generator-quartz.html).
