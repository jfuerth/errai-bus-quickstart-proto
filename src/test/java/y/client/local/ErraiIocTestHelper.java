package y.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.ioc.client.api.EntryPoint;

import com.google.gwt.user.client.Timer;

/**
 * This class provides a target for injecting parts of the application that the
 * test cases need access to. Think of it as your test suite's window into the
 * CDI container. Test cases can access the injected members using the following
 * code:
 * 
 * <pre>
 *   ErraiIocTestHelper.instance.<i>injectedFieldName</i>
 * </pre>
 * <p>
 * You can also set up CDI event producers and observers here if your test needs
 * to fire events or assert that a particular event was fired.
 * <p>
 * Note that this "CDI Test Helper" pattern is just a workaround. If there were
 * something like the BeanManager available in the client, it would be
 * preferable for the tests to create and destroy managed beans on demand.
 * <p>
 * As an alternative workaround, you could dispense with this class altogether
 * and have your main Entry Point class keep a static reference to itself.
 * However, this would pollute the API with an unwanted singleton pattern: there
 * would be the possibility of classes referring to the entry point class through
 * its singleton rather than allowing it to be injected.
 * 
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@EntryPoint
public class ErraiIocTestHelper {

  static ErraiIocTestHelper instance;

  static boolean busInitialized = false;
  
  @Inject HelloWorldClient client;
  @Inject MessageBus bus;
  
  @PostConstruct
  void saveStaticReference() {
    instance = this;
  }

  /**
   * Runs the given runnable in the browser's JavaScript thread once the Errai
   * bus has finished its initialization phase and the client is connected to
   * the server. Once the runnable is executed, all {@link EntryPoint} classes
   * will have been created and have their dependencies injected, and all
   * components listening for it will have received the BusReady event.
   * 
   * @param runnable
   *          The code to run once Errai CDI is up and running in the context of
   *          the web page.
   */
  public static void afterBusInitialized(final Runnable runnable) {
    final Timer t = new Timer() {
      @Override
      public void run() {
        ClientMessageBus bus = (ClientMessageBus) instance.bus;
        if (bus != null && bus.isInitialized()) {
          System.out.println("Bus initialized. Running test... (bus=" + bus + ")");
          runnable.run();
        } else {
          // poll again later
          System.out.println("Bus not initialized yet (bus=" + bus + ")");
          schedule(500);
        }
      }
    };
    t.schedule(500);
  }
}
