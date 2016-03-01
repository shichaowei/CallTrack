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

import java.security.Permission;
import java.io.FileDescriptor;
import java.net.InetAddress;

/**
 * TODO: add documentation
 *
 */
public class DriverSecurityManager extends SecurityManager
{
  public static final String HANDLE_EXIT_VM = "com.yworks.demo.exitVM";

  private static final SecurityManager DEFAULT_SECURITY_MANAGER =
  new SecurityManager()
  {
    public void checkPermission( final Permission perm )
    {
    }
  };


  private final SecurityManager delegate;
  private final Exception stacktrace;

  public DriverSecurityManager( final SecurityManager delegate )
  {
    this.delegate = delegate != null ? delegate : DEFAULT_SECURITY_MANAGER;
    this.stacktrace = new Exception();
  }

  public SecurityManager getDelegate()
  {
    return delegate != DEFAULT_SECURITY_MANAGER ? delegate : null;
  }

  public void checkAwtEventQueueAccess()
  {
    delegate.checkAwtEventQueueAccess();
  }

  public void checkCreateClassLoader()
  {
    delegate.checkCreateClassLoader();
  }

  public void checkPrintJobAccess()
  {
    delegate.checkPrintJobAccess();
  }

  public void checkPropertiesAccess()
  {
    delegate.checkPropertiesAccess();
  }

  public void checkSetFactory()
  {
    delegate.checkSetFactory();
  }

  public void checkSystemClipboardAccess()
  {
    delegate.checkSystemClipboardAccess();
  }

  public boolean getInCheck()
  {
    return delegate.getInCheck();
  }

  public void checkExit( final int status )
  {
    if (!isJFrameSetDefaultCloseOperation()) {
      throw new SecurityException(HANDLE_EXIT_VM);
    }
  }

  public void checkListen( final int port )
  {
    delegate.checkListen(port);
  }

  public void checkRead( final FileDescriptor fd )
  {
    delegate.checkRead(fd);
  }

  public void checkWrite( final FileDescriptor fd )
  {
    delegate.checkWrite(fd);
  }

  public void checkMemberAccess( final Class clazz, final int which )
  {
    delegate.checkMemberAccess(clazz, which);
  }

  public Object getSecurityContext()
  {
    return delegate.getSecurityContext();
  }

  public boolean checkTopLevelWindow( final Object window )
  {
    return delegate.checkTopLevelWindow(window);
  }

  public void checkDelete( final String file )
  {
    delegate.checkDelete(file);
  }

  public void checkExec( final String cmd )
  {
    delegate.checkExec(cmd);
  }

  public void checkLink( final String lib )
  {
    delegate.checkLink(lib);
  }

  public void checkPackageAccess( final String pkg )
  {
    delegate.checkPackageAccess(pkg);
  }

  public void checkPackageDefinition( final String pkg )
  {
    delegate.checkPackageDefinition(pkg);
  }

  public void checkPropertyAccess( final String key )
  {
    delegate.checkPropertyAccess(key);
  }

  public void checkRead( final String file )
  {
    delegate.checkRead(file);
  }

  public void checkSecurityAccess( final String target )
  {
    delegate.checkSecurityAccess(target);
  }

  public void checkWrite( final String file )
  {
    delegate.checkWrite(file);
  }

  public void checkAccept( final String host, final int port )
  {
    delegate.checkAccept(host, port);
  }

  public void checkConnect( final String host, final int port )
  {
    delegate.checkConnect(host, port);
  }

  public void checkAccess( final Thread t )
  {
    delegate.checkAccess(t);
  }

  public ThreadGroup getThreadGroup()
  {
    return delegate.getThreadGroup();
  }

  public void checkAccess( final ThreadGroup g )
  {
    delegate.checkAccess(g);
  }

  public void checkMulticast( final InetAddress maddr )
  {
    delegate.checkMulticast(maddr);
  }

  public void checkMulticast( final InetAddress maddr, final byte ttl )
  {
    delegate.checkMulticast(maddr, ttl);
  }

  public void checkPermission( final Permission perm )
  {
    if ("exitVM".equals(perm.getName())) {
      if (!isJFrameSetDefaultCloseOperation()) {
        throw new SecurityException(HANDLE_EXIT_VM);
      }
    }
    delegate.checkPermission(perm);
  }

  public void checkConnect( final String host, final int port, final Object context )
  {
    delegate.checkConnect(host, port, context);
  }

  public void checkRead( final String file, final Object context )
  {
    delegate.checkRead(file, context);
  }

  public void checkPermission( final Permission perm, final Object context )
  {
    delegate.checkPermission(perm, context);
  }

  private boolean isJFrameSetDefaultCloseOperation()
  {
    stacktrace.fillInStackTrace();
    final StackTraceElement[] ste = stacktrace.getStackTrace();
    for (int i = 0; i < ste.length; ++i) {
      if ("javax.swing.JFrame".equals(ste[i].getClassName()) &&
          "setDefaultCloseOperation".equals(ste[i].getMethodName())) {
        return true;
      }
    }
    return false;
  }
}
