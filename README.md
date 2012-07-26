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

## Running over TLS

### TLS in server mode

It is useful to be able to run TLS in dev mode. This can be done from the shell with 

> run -Dhttps.port=8443

This will run the server on port 8443 on the local machine, with a localhost certificate.

To see the certificate used by the server you can connect with your browser and click in the URL bar on the connection for such information, or you can use the following command line 

> openssl s_client -showcerts -connect localhost:443

### configurability

One can now specify any keystore in the `conf/application.conf` file with the following properties

```
https.port=8443
https.port8443.keystore { 
                          location : "conf/KEYSTORE.jks"
                          type : JKS
                          alias : selfsigned
                          password : secret
                          algorithm : SunX509
                          trust : noCA }
```

where `alias` is the alias of the server certificate placed in the keystore `KEYSTORE.jks` . Such a keystore can be built with java's `keygen` tool - for a hypothetical swiss company `coolapps.ch` - as follows

```
$ keytool -genkey -alias selfsigned -keyalg RSA -dname "CN=coolapps.ch, OU=software, L=Zurich, ST=Switzerland, C=CH" -storepass secret -validity 2000 -keystore KEYSTORE.jks
```

By following the procedures listed by your favorite Certificate Autority, you can also place a certificate in the keystore that is signed by that CA and have the server use that. (Note that [startssl.org](https://www.startssl.com/) provides free signed certificates.) This will allow your users to connect to your server on `https://` without an ugly warning box appearing. Hopefully greater support of [IETF Dane](http://tools.ietf.org/wg/dane/) by browsers will make it possible to use self signed certificates too.

### client certificates

The server can also allow you to request client certificates for any resource.
To try it out you can execute in the console

> run -Dhttps.port=8443 -Dhttps.server.clientTrust=noCA

The "https.server.clientTrust=noCA" means that Play2.0 won't refuse certificates just because they were not signed by a well known Certificate Authority (CA). You can for example get a Certificate easily from [My-Profile](http://my-profile.eu/). This is very useful when debugging - as getting client certificates signed by CAs would be extremely tedious (and expensive). Furthermore it will also permit implementation of the [WebID protocol](http://webid.info/spec/), which bypasses CA verification for just the reasons cited above . In any case the public key is still verified: that is the TLS layer verifies that the client has the private key corresponding to the public key sent in the certificate to the Play instance - this is enough for most use cases.

Client certificates can be retrieved programmatically from the method in `RequestHeader`

```scala
trait RequestHeader  {
   //...
    def certs: Promise[Seq[Certificate]]
  //...
}
```
The certs request returns a Promise of a certificate chain.  To test this out create yourself the following controller:

```scala
package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent._

object Application extends Controller {

  def index = Action { req =>
    Async {
      //timeouts should be set as transport specific options as explained in Netty's ChannelFuture
      req.certs.extend1{  
        case Redeemed(cert) => Ok("your cert is: \n\n "+cert )
        case Thrown(e) => InternalServerError("received error: \n"+e )
      } 
    } 
  } 
}
```

If you get yourself a certificate at https://my-profile.eu/ using the [Get a WebID](https://my-profile.eu/profile.php ) service, you can then point the browser you used there - and which should now contain a certificate - to authenticate on https://localhost:8443/ - ie the local server you started above.

You will notice that other resources on your server are still accessible without you being required to provide TLS client certificates. This is because the server does not request a cert up front - as many TLS servers do - but only requests a cert when the programmer calls the `req.certs` method: this forces a TLS renegotiation (if the certificate it not available). 

   Note: There were some bugs in TLS renegotiation a few years ago that have since been fixed. This is explained in detail in the [Java™ Secure Socket Extension (JSSE) Reference Guide - Transport Layer Security (TLS) Renegotiation Issue chapter](http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.html#tlsRenegotiation), and you may want to set some other properties as described there to tune the security of your server.


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
