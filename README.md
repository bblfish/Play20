# WebID/TLS Branch of Play 2.0

This branch of Play is implementing the [WebID authentication protocol](http://webid.info/). 

It fixes [issue 269](https://play.lighthouseapp.com/projects/82401-play-20/tickets/269). It  will run with TLS enabled (https://... ) only. 
This is an extension of the [TLS branch](https://github.com/bblfish/Play20/tree/TLS_support) for which [issue 215](https://play.lighthouseapp.com/projects/82401-play-20/tickets/215-tls-https-support-in-play-20) was filed.

# WebID auth

More later on this

# TLS

The code is set up to by default allows the server to ask
the client for an X509 certificate based authentication, by calling `play.api.mvc.Request.certs` method.

```scala
object Application extends Controller {
  def index = Action { request =>
    Ok(views.html.index("Your cert chain size is "+request.certs.size))
  }
}
```
This can then be used for [WebID authentication](http://webid.info/spec/) ([video](http://webid.info/)),

The server willlook up the client certificate in the TLS session, or ask the client if none is 
available. This allows you to have large parts of your site TLS protected but only ask the client
for his identity when the resources requested is protected .

To get authentication to work the server must be started with the PLAY_PARAMS environmental variable set something like the following

```bash
export PLAY_PARAMS="-Dnetty.ssl.keyStoreType=JKS -Dnetty.ssl.keyStore=$PLAY_HOME/TestKEYSTORE.jks"
export PLAY_PARAMS="$PLAY_PARAMS -Dnetty.ssl.keyStorePassword=secret -Dnetty.ssl.keyAlias=selfsigned"
export PLAY_PARAMS="$PLAY_PARAMS -Dsun.security.ssl.allowUnsafeRenegotiation=true -Dsun.security.ssl.allowLegacyHelloMessages=true" 
``` 

where 

 * `netty.ssl.keyStore` is pointing to a keystore containing your CA signed certificate and  
 * `netty.ssl.keyStoreType` is the type of that keystore, Java Key Store usually (JKS)
 * `netty.ssl.keyStorePassword` is the password for the keystore 
 * `netty.ssl.keyAlias` is the alias of the key and certificate to be used by the server 
 * `sun.security.ssl.allowUnsafeRenegotiation` allows maximum browser compatibility with a trade off in security, see [Transport Layer Security (TLS) Renegotiation Issue](http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.html#tlsRenegotiation)
 * `sun.security.ssl.allowLegacyHelloMessages` bypasses a security fix, see [Transport Layer Security (TLS) Renegotiation Issue](http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.html#tlsRenegotiation)

The `TestKEYSTORE.jks` should not be used in production environments as its private key is now public, and it is self signed.

> Note there seems to be a bug on OSX Java 7 where the server freezes up completely. It works on Solaris Java7 though.

> Check the [NOTICE.md](/bblfish/Play20/blob/TLS_support/NOTICE.md) file for updates to the licence


# Play 2.0 

Play 2.0 is a high productivity Java and Scala Web application framework, integrating all components and API needed for modern Web application development. 

It is based on a lightweight stateless Web friendly architecture and features predictable and minimal resources consumption (CPU, Memory, Threads) for highly scalable applications thanks to its reactive model based on Iteratee IO.

http://www.playframework.org

## Installing

For convenience, you should add the framework installation directory to your system PATH. On UNIX systems will be something like:

```bash
export PATH=$PATH:/path/to/play2.0
```

On windows systems you'll need to set it in the global environment variables.

> If you’re on UNIX, make sure that the play script is executable (otherwise do a chmod a+x play).

## Getting started

Enter any existing Play 2.0 application directory and use the `play` command to launch the development console:

```
$ cd ~/workspace/myApplication
$ play
```

You can also directly use `play run` to run the application:

```
$ cd ~/workspace/myApplication
$ play run
```

Use `play new yourNewApplication` to create a new application:

```
$ cd ~/workspace
$ play new myNewApplication
```

Once the application is created, use it as any existing application:

```bash
$ cd myNewApplication
$ play
```

## Running the sample applications

There are several samples applications included in the `samples/` directory. For example, to run the included Scala Hello World application:

```
$ cd ~/workspace/play2.0/samples/scala/helloworld/
$ play run
```
> The application will be available on port 9000.

## Documentation

The edge documentation is available at https://github.com/playframework/Play20/wiki.

## Issues tracker

Report issues at https://play.lighthouseapp.com/projects/82401-play-20/overview.

## Contributors

Check for all contributors at https://github.com/playframework/Play20/contributors.

## Licence

This software is licensed under the Apache 2 license, quoted below.

Copyright 2012 Typesafe (http://www.typesafe.com).

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
