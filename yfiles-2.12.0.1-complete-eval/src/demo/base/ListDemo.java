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
package demo.base;

import y.base.ListCell;
import y.base.YCursor;
import y.base.YList;

import java.util.Comparator;

/**
 * Demonstrates how to use the linked list data type YList and the YCursor interface.
 * <p>
 * <b>usage:</b> java demo.base.ListDemo
 * </p>

 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/containers.html#containers">Section Containers</a> in the yFiles for Java Developer's Guide
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/iteration.html#iteration">Section Iteration Mechanisms</a> in the yFiles for Java Developer's Guide
 */
public class ListDemo 
{
  public ListDemo()
  {
    //create new YList instance
    YList list = new YList();
    
    //add numbered String elements to list 
    for(int i = 0; i < 20; i++)
      list.addLast(""+i);
    
    //iterate over list from first to last
    System.out.println("List elements from front to back");
    for(YCursor c = list.cursor(); c.ok(); c.next())
    {
      //output element at cursor position
      System.out.println(c.current());
    }
    
    //iterate over list from last to first
    System.out.println("List elements from back to front");
    YCursor rc = list.cursor();
    for(rc.toLast(); rc.ok(); rc.prev())
    {
      //output element at cursor position
      System.out.println(rc.current());
    }
    
    //sort list lexicografically
    list.sort(new Comparator() 
              {
                public int compare(Object a, Object b)
                  {
                    return ((String)a).compareTo((String)b);
                  }
              });
    
    
    //iterate over list from first to last
    System.out.println("Lexicographically sorted list");
    for(YCursor c = list.cursor(); c.ok(); c.next())
    {
      //output element at cursor position
      System.out.println(c.current());
    }
    
    //low level iteration on list cells (non-const iteration)
    for(ListCell cell = list.firstCell(); cell != null; cell = cell.succ())
    {
      String s = (String)cell.getInfo();
      //remove all Strings from list that have length == 1 
      if(s.length() == 1)
      {
        list.removeCell(cell); 
        //note that cell is still half-valid, i.e it's succ() and pred() 
        //pointers are still unchanged. therefore cell = cell.succ() is
        //valid in the for-statement
      }
    }
    
    System.out.println("list after element removal");
    System.out.println(list);
    
    //initialize list2 with the elements from list
    YList list2 = new YList(list.cursor());
    System.out.println("list2 after creation");
    System.out.println(list2);
    
    //reverse element order in list2
    list2.reverse();
    System.out.println("list2 after reversal");
    System.out.println(list2);
    
    //move all elements of list2 to the end of list
    list.splice(list2);
    
    System.out.println("list after splicing");
    System.out.println(list);
    System.out.println("list2 after splicing");
    System.out.println(list2);
    
  }
  
  public static void main(String[] args)
  {
    new ListDemo();
  }
  
}