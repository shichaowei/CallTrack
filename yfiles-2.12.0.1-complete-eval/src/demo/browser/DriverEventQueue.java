/****************************************************************************
 * This demo file is part of yFiles for Java 2.12.0.1.
 * Copyright (c) 2000-2016 by yWorks GmbH, Vor dem Kreuzberg 28,
 * 72070 Tuebingen, Germany. All rights reserved.
 * 
 * yFiles demo files exhibit yFiles for Java functionalities. Any redistribution
 * of demo files in source code or binary form, with or without
 * modification, is not permitted.
 * 
 * Owners of a valid software license for a yFiles for Java version that this
 * demo is shipped with are allowed to use the demo source code as basis
 * for their own yFiles for Java powered applications. Use of such programs is
 * governed by the rights and conditions as set out in the yFiles for Java
 * license agreement.
 * 
 * THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL yWorks BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ***************************************************************************/
package demo.browser;

import java.awt.AWTEvent;
import java.awt.EventQueue;

/**
 * Custom {@link EventQueue} to trap system exit.
 *
 */
public class DriverEventQueue extends EventQueue
{
  private Driver driver;
  private DriverSecurityManager securityManager;

  public Driver getDriver()
  {
    return driver;
  }

  public void setDriver( final Driver driver )
  {
    this.driver = driver;
    if (driver != null) {
      if (securityManager != null) {
        securityManager = new DriverSecurityManager(securityManager.getDelegate());
      } else {
        securityManager = new DriverSecurityManager(System.getSecurityManager());
      }
      System.setSecurityManager(securityManager);
    } else {
      if (securityManager != null) {
        System.setSecurityManager(securityManager.getDelegate());
        securityManager = null;
      }
    }
  }

  protected void dispatchEvent( final AWTEvent event )
  {
    try {
      super.dispatchEvent(event);
    } catch (SecurityException se) {
      if (DriverSecurityManager.HANDLE_EXIT_VM.equals(se.getMessage())) {
        if (driver != null) {
          driver.dispose();
        }
        if (securityManager != null) {
          System.setSecurityManager(securityManager.getDelegate());
          securityManager = null;
        }
      } else {
        throw se;
      }
    }
  }
}
