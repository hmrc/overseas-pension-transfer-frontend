
# Overseas Pension Transfer Frontend

This is the frontend microservice application that is part of the Managing Pensions Schemes service concerned with moving pensions abroad. It is connected to the MPS dashboard.

## Dependencies
| Service    | Link |
| -------- | ------- |
| overseas-pension-transfer-backend  | https://github.com/hmrc/overseas-pension-transfer-backend    |
| address-lookup  | https://github.com/hmrc/address-lookup    |

## Running the service

1. Make sure you run all the dependant services through the service manager:

   > `sm2 --start OVERSEAS_PENSION_TRANSFER_ALL`

2. Stop the frontend microservice from the service manager and run it locally:

   > `sm2 --stop OVERSEAS_PENSION_TRANSFER_FRONTEND`

   > `sbt run`

The service runs on port `15600` by default.

## Navigating the service

### Claim enrolment journey

1. Navigate to http://localhost:15600/overseas-pension-transfer-frontend

## Running tests

### Tests
| Repositories    | Link |
| -------- | ------- |
| Journey Tests | https://github.com/hmrc/overseas-pension-transfer-ui-tests |
| Performance Tests | https://github.com/hmrc/overseas-pension-transfer-performance-tests |

### Unit tests

> `sbt test`

### Integration tests

> `sbt it/test`

## Scalafmt and Scalastyle

To check if all the scala files in the project are formatted correctly:
> `sbt scalafmtCheckAll`

To format all the scala files in the project correctly:
> `sbt scalafmtAll`

To check if there are any scalastyle errors, warnings or infos:
> `sbt scalastyle`
>

## All tests and checks

This is an sbt command alias specific to this project. It will run a scala format
check, run a scala style check, run unit tests, run integration tests and produce a coverage report:
> `sbt runAllChecks`


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
