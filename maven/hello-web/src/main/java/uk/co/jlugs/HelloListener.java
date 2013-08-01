package uk.co.jlugs;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * This is a context listener!
 */
public class HelloListener implements ServletContextListener {

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    App.hello();
  }

  @Override
  public void contextInitialized(ServletContextEvent event) {
    App.goodbye();
  }
}
