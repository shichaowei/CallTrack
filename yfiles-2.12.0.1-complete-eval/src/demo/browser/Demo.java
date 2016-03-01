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

import java.net.URL;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.util.StringTokenizer;

/**
 * TODO: add documentation
 *
 */
public class Demo extends AbstractDemoDisplayable
{
  String sourcePath;
  String source;
  boolean executable;

  Demo()
  {
    this.sourcePath = "";
    this.source = null;
    this.executable = true;
  }

  public boolean isDemo()
  {
    return true;
  }

  public String getSourcePath()
  {
    return sourcePath;
  }

  public String getSource()
  {
    return source;
  }

  public boolean isExecutable()
  {
    return executable;
  }

  public URL getBase()
  {
    return base;
  }

  public String toString()
  {
    return "Demo[sourcePath=" + getSourcePath() +
           "; qualifiedName=" + getQualifiedName() +
           "; displayName=" + getDisplayName() +
           "; summary=" + getSummary() +
           "; description=" + getDescription() +
           "; base=" + getBase() +
           "; executable=" + isExecutable() + "]";
  }

  public String readSource()
  {
    final StringBuffer sb = new StringBuffer();
    URL sourceUrl = getClass().getClassLoader().getResource(getSourcePath());
    if (sourceUrl == null) {
      try { //source file not found. try wild heuristic
        StringTokenizer stok = new StringTokenizer(getSourcePath(), "/");
        int slashCount = stok.countTokens() - 1;
        String backPath = "";
        for(int i = 0; i < slashCount; i++) backPath += "../";
        sourceUrl = new URL(getBase(), backPath + "src/" + getSourcePath());
        if(!new File(sourceUrl.getPath()).exists()) sourceUrl = null;
      }catch(MalformedURLException mex) {}
    }
    if (sourceUrl != null) {
      try {
        BufferedReader br = null;
        try {
          br = new BufferedReader(new InputStreamReader(sourceUrl.openStream()));
          br = new BufferedReader(new InputStreamReader(sourceUrl.openStream()));
          String line;
          while ((line = br.readLine()) != null) {
            sb.append(line).append('\n');
          }
        } finally {
          if (br != null) {
            br.close();
          }
        }
      } catch (IOException ioe) {
        sb.setLength(0);
        sb.append(ioe.getMessage());
      }
    } else {
      sb.append("Could not locate file \"").append(getSourcePath()).append("\" in classpath.");
    }

    return sb.toString();
  }
}
