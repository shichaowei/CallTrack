

<h3>About CallTrack</h3>
This project is a open source project developed in my internship consisting in a tool to analyse what the impact caused when method or class is changed in software process artifacts.</br>
This project will use apache bcel api to generate a graph of calls(dependencies) from a Jar file and implements a BFS algorithm to show the impact caused by the change that the developer intends to make and then show a HTML/JS/CSS view to simulate a change in a method and simulate the impact caused by the change until software process artifacts..


<h3> How it works </h3>
  1 - Generate call relations usin Apache bcel reflection API from a Java project JAR file.</br>
  2 - Generate a graph data structure(in "back-end") based on edges generated from step 1.</br>
  3 - Use a config file to mapping the artifacts of software development process on endpoints(methods or/and classes).</br>
  4 - Generate a front-end Data Structure of graph to be used in cytoscape JS framework.</br>
  5 - And finally the developer can simulate change and see what impact this change makes.</br></br>

  We will have two type of call hierarchy: base on method call and based on class "call"(dependency, on really).
  
  
  <h3>How to use </h3>
  
  After generate the jar of project, you can call the command:<br> 
  <pre><b>calltrack path/of/jarfile.jar project.package.to.match</pre></b>
  
  
  <h3>License</h3>
  

<pre>This software is licensed under Apache 2.0 license.</pre>
  
