package play.core.server

import org.jboss.netty.buffer._
import org.jboss.netty.channel._
import org.jboss.netty.bootstrap._
import org.jboss.netty.channel.Channels._
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.channel.socket.nio._
import org.jboss.netty.handler.stream._
import org.jboss.netty.handler.codec.http.HttpHeaders._
import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.handler.codec.http.HttpHeaders.Values._
import org.jboss.netty.handler.ssl.SslHandler

import org.jboss.netty.channel.group._
import java.util.concurrent._

import play.core._
import play.core.server.websocket._
import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.iteratee.Input._
import play.api.libs.concurrent._

import scala.collection.JavaConverters._
import netty._

/**
 * provides a stopable Server
 */
trait ServerWithStop {
  def stop(): Unit
}

/**
 * creates a Server implementation based Netty
 */
class NettyServer(appProvider: ApplicationProvider, port: Int, address: String = "0.0.0.0", val mode: Mode.Mode = Mode.Prod) extends Server with ServerWithStop {

  def applicationProvider = appProvider

  val bootstrap = new ServerBootstrap(
    new org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory(
      Executors.newCachedThreadPool(),
      Executors.newCachedThreadPool()))

  val allChannels = new DefaultChannelGroup

  val defaultUpStreamHandler = new PlayDefaultUpstreamHandler(this, allChannels)

  class DefaultPipelineFactory extends ChannelPipelineFactory {
    def getPipeline = {
      val newPipeline = pipeline()
      newPipeline.addLast("decoder", new HttpRequestDecoder(4096, 8192, 8192))
      newPipeline.addLast("encoder", new HttpResponseEncoder())
      newPipeline.addLast("handler", defaultUpStreamHandler)
      newPipeline
    }
  }


  class SecurePipelineFactory extends DefaultPipelineFactory with Ssl {
    override def getPipeline = {
      val engine = createSslContext.createSSLEngine
      engine.setUseClientMode(false)
      val pipe = super.getPipeline
      System.out.println("in getPipline")
      pipe.addFirst("ssl",new SslHandler(engine))
      pipe
    }
  }
  //  bootstrap.setPipelineFactory(new DefaultPipelineFactory)
  bootstrap.setPipelineFactory(new SecurePipelineFactory)


  /** Provides security dependencies */
  trait Security {
    import javax.net.ssl.SSLContext
    /** create an SSLContext from which an SSLEngine can be created */
    def createSslContext: SSLContext
  }

  /** Provides basic ssl support.
   * A keyStore and keyStorePassword are required and default to using the system property values
   * "jetty.ssl.keyStore" and "jetty.ssl.keyStorePassword" respectively. */
  trait Ssl extends Security {
    import java.io.FileInputStream
    import java.security.{KeyStore, SecureRandom}
    import javax.net.ssl.{KeyManager, KeyManagerFactory, SSLContext}

    def requiredProperty(name: String) = System.getProperty(name) match {
      case null => { val msg="required system property not set %s" format name
        System.out.println(msg)
        sys.error(msg)
      }
      case prop => prop
    }

    lazy val keyStore = requiredProperty("netty.ssl.keyStore")
    lazy val keyStorePassword = requiredProperty("netty.ssl.keyStorePassword")

    def keyManagers = {
      val keys = KeyStore.getInstance(System.getProperty(
        "netty.ssl.keyStoreType", KeyStore.getDefaultType))
      IO.use(new FileInputStream(keyStore)) { in=>
        System.out.println("fetching file "+keyStore+" with password "+keyStorePassword)
        keys.load(in, keyStorePassword.toCharArray)
      }
      val keyManFact = KeyManagerFactory.getInstance(System.getProperty(
        "netty.ssl.keyStoreAlgorithm", KeyManagerFactory.getDefaultAlgorithm))
      keyManFact.init(keys, keyStorePassword.toCharArray)
      keyManFact.getKeyManagers
    }

    def createSslContext = {
      val context = SSLContext.getInstance("TLS")
      initSslContext(context)
      context
    }

    def initSslContext(ctx: SSLContext) =
      ctx.init(keyManagers, null, new SecureRandom)
  }

  allChannels.add(bootstrap.bind(new java.net.InetSocketAddress(address, port)))


  mode match {
    case Mode.Test =>
    case _ => Logger("play").info("Listening for HTTP on port %s...".format(port))
  }

  override def stop() {

    try {
      Play.stop()
    } catch {
      case e => Logger("play").error("Error while stopping the application", e)
    }

    try {
      super.stop()
    } catch {
      case e => Logger("play").error("Error while stopping akka", e)
    }

    mode match {
      case Mode.Test =>
      case _ => Logger("play").info("Stopping server...")
    }

    allChannels.close().awaitUninterruptibly()
    bootstrap.releaseExternalResources()

  }

}

/**
 * bootstraps Play application with a NettyServer backened
 */
object NettyServer {

  import java.io._
  import java.net._

  /**
   * creates a NettyServer based on the application represented by applicationPath
   * @param applicationPath path to application
   */
  def createServer(applicationPath: File): Option[NettyServer] = {

    // Manage RUNNING_PID file
    java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split('@').headOption.map { pid =>
      val pidFile = new File(applicationPath, "RUNNING_PID")

      if (pidFile.exists) {
        println("This application is already running (Or delete the RUNNING_PID file).")
        System.exit(-1)
      }

      // The Logger is not initialized yet, we print the Process ID on STDOUT
      println("Play server process ID is " + pid)

      new FileOutputStream(pidFile).write(pid.getBytes)
      Runtime.getRuntime.addShutdownHook(new Thread {
        override def run {
          pidFile.delete()
        }
      })
    }

    try {
      Some(new NettyServer(
        new StaticApplication(applicationPath),
        Option(System.getProperty("http.port")).map(Integer.parseInt(_)).getOrElse(9000),
        Option(System.getProperty("http.address")).getOrElse("0.0.0.0")))
    } catch {
      case e => {
        println("Oops, cannot start the server.")
        e.printStackTrace()
        None
      }
    }

  }

  /**
   * attempts to create a NettyServer based on either
   * passed in argument or `user.dir` System property or current directory
   * @param args
   */
  def main(args: Array[String]) {
    args.headOption.orElse(
      Option(System.getProperty("user.dir"))).map(new File(_)).filter(p => p.exists && p.isDirectory).map { applicationPath =>
        createServer(applicationPath).getOrElse(System.exit(-1))
      }.getOrElse {
        println("Not a valid Play application")
      }
  }

  /**
   * provides a NettyServer for the dev environment
   */
  def mainDev(sbtLink: SBTLink, port: Int): NettyServer = {
    play.utils.Threads.withContextClassLoader(this.getClass.getClassLoader) {
      try {
        val appProvider = new ReloadableApplication(sbtLink)
        new NettyServer(appProvider, port, mode = Mode.Dev)
      } catch {
        case e => {
          throw e match {
            case e: ExceptionInInitializerError => e.getCause
            case e => e
          }
        }
      }

    }
  }

}

/* should be part of major library somewhere?*/
trait IO {
  /** Manage the usage of some object that must be closed after use */
  def use[C <: { def close() }, T](c: C)(f: C => T): T = try {
    f(c)
  } finally {
    c.close()
  }
}

object IO extends IO