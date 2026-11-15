# OLIC ODS to ICS converter

Tool to convert OLIC Dienstpläne in ods format to ics calendars.
Run in terminal:

## Usage

Build the self-contained jar and run it (requires Java 25):

```shell
./gradlew shadowJar
java -jar build/libs/olic-ods-to-ics-converter-1.0-SNAPSHOT-all.jar input.ods output.ics
```

Or install launcher scripts to run it like a native command:

```shell
./gradlew installShadowDist
build/install/olic-ods-to-ics-shadow/bin/olic-ods-to-ics input.ods output.ics   # .bat on Windows
```

Add `build/install/olic-ods-to-ics-shadow/bin` to your `PATH` to call `olic-ods-to-ics` from anywhere.
`./gradlew shadowDistZip` produces a zip of the same for distribution.

Use the `--help` commandline switch for further details.

## Development

The project uses [ODF Toolkit](https://odftoolkit.org)'s odfdom-java for parsing and [iCal4j](https://www.ical4j.org)
for event serialization.

### Requirements

* Java 25+
* IntelliJ
