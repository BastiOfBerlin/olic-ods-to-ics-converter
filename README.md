# OLIC ODS to ICS converter

Tool to convert OLIC Dienstpläne in ods format to ics calendars.
Run in terminal:

## Usage

```shell
java -jar olic-ods-to-ics-converter-1.0-SNAPSHOT.jar input.ods output.ics
```

or use the `--help` commandline switch for further details.

## Development

The project uses [ODF Toolkit](https://odftoolkit.org)'s odfdom-java for parsing and [iCal4j](https://www.ical4j.org)
for event serialization.

### Requirements

* Java 25+
* IntelliJ
