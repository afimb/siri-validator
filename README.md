# Siri Validator

Siri Validator is a java open source project on SIRI V2.0 normalization projet. It's composed on one module :
* siri-client-france : command line client for tests and validation purpose on SIRI server

## Release Notes

The release notes (in French) can be found in [CHANGELOG](./CHANGELOG.md) file 

## Requirements

* oraclejdk7
* openjdk7
* oraclejdk8
* openjdk8

## External Deps

On Debian/Ubuntu/Kubuntu OS :
```sh
sudo apt-get install openjdk-7-jdk
```

## Installation from binary

siri.client.france is avalable as a zip :
download siri.client.france.x.y.z.zip from [maven repository](http://maven.chouette.mobi/irys/siri.client.france)

[Install and configure Wildfly](./doc/install/wildfly.md) 

in wildfly installation repository :
```sh
bin/jboss-cli.sh connect, deploy --force  (path to ...)/siri_lite.ear
bin/jboss-cli.sh -c "/system-property=siri_lite.producer.address:add(value='web service SIRI server endpoint address')"
```

## Documentation

a user manual (in french) is available on [Chouette Project website](http://www.chouette.mobi).

## License

This project is licensed under the CeCILL-B license, a copy of which can be found in the [LICENSE](./LICENSE.md) file.

## Support

Users looking for support should file an issue on the GitHub [issue tracking page](../../issues), or file a [pull request](../../pulls) if you have a fix available.
