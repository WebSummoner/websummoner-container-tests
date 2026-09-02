# WebSummoner Container Tests

[![Test](https://github.com/WebSummoner/websummoner-container-tests/actions/workflows/test.yml/badge.svg)](https://github.com/WebSummoner/websummoner-container-tests/actions/workflows/test.yml)

An automated Java test suite run against every [WebSummoner](https://github.com/WebSummoner/websummoner)
container to verify all major [Selenium](https://www.selenium.dev/) features work as expected.

## Prerequisites

- JDK 25 (or just Docker — see below)
- Maven 3.9+

## Running the full suite

The suite drives a `RemoteWebDriver`, so it needs a running WebSummoner (or plain
Selenium) grid to connect to. Point it at your grid and pick a browser:

```
$ mvn clean test \
    -Dgrid.connection.url=http://my-websummoner-host.example.com:4444/wd/hub \
    -Dgrid.browser.name=chrome \
    -Dgrid.browser.version=152.0
```

### Configuration properties

All are passed as `-D<name>=<value>`; the defaults match a local grid.

| Property | Default | Purpose |
| --- | --- | --- |
| `grid.connection.url` | `http://localhost:4444/wd/hub` | Grid endpoint the tests connect to |
| `grid.browser.name` | `chrome` | Browser to request (`chrome`, `firefox`, `opera`, `yandex`, …) |
| `grid.browser.version` | `152.0` | Browser version capability |
| `pages.base.url` | `https://websummoner.riadvice.com/websummoner-container-tests/pages` | Base URL for the HTML test fixtures in `pages/` |

## Build checks without a grid

Compilation (with [Error Prone](https://errorprone.info/)), formatting
([Spotless](https://github.com/diffplug/spotless) with Palantir Java Format) and
the unit tests need only JDK + Maven — no grid. Run them in a basic Maven container:

```
$ docker run --rm -v "$PWD":/app -w /app maven:3-eclipse-temurin-25 \
    mvn -B verify -Dtest='*Test'
```

`-Dtest='*Test'` runs only the grid-free unit tests (`*Test`) and skips the
Selenium integration tests (`Test*`). This is what CI runs on every pull request
(see [`.github/workflows/test.yml`](.github/workflows/test.yml)).

## Contributing

- Run `mvn spotless:apply` before pushing — the build fails on unformatted code.
- Error Prone runs during compilation and flags likely bugs; keep the build warning-clean.
- Unit tests live alongside the code they cover and are named `*Test`; the
  browser integration tests are named `Test*`.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
