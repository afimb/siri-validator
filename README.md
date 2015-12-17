# Siri Validator

Siri Validator is a java open source project on SIRI V2.0 normalization projet. It's composed on one module :
* siri-client-france : command line client for tests and validation purpose on SIRI server

Requirements
------------

* oraclejdk7
* openjdk7
* oraclejdk8
* openjdk8

External Deps
-------------
On Debian/Ubuntu/Kubuntu OS :
```sh
sudo apt-get install openjdk-7-jdk
sudo apt-get install git
```

Installation
------------

Get git repository
```sh
cd workspace
git clone -b V1_0 git://github.com/afimb/siri-validator
cd siri-validator
```

Test
----

```sh
mvn test
```

More Information
----------------

More information can be found on the [project website on GitHub](.).
There is extensive usage documentation available [on the wiki](../../wiki).

Installation
-------------

Install
```sh
mvn -Dmaven.test.skip=true install
```

Usage
-------------

a user manual (in french) is available on [Chouette Project website](http://www.chouette.mobi).


License
-------

This project is licensed under the CeCILL-B license, a copy of which can be found in the [LICENSE](./LICENSE.md) file.

Release Notes
-------------

The release notes can be found in [CHANGELOG](./CHANGELOG.md) file

Support
-------

Users looking for support should file an issue on the GitHub [issue tracking page](../../issues), or file a [pull request](../../pulls) if you have a fix available.
