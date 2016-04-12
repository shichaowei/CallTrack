# CallTrack
This is intership project of Walter A. Alves that consist in a tool to analyse what impact causes in method or class change. 
This project will use call graph api  to generate call peers, an algorthm to create and colour transitively to show change impact.


The main idea of this project, is tracking change comportamental <b>atomic</b> on some <b>software artefacts</b>(like Use Case, Test Case, etc).

The steps to track are:</br>
  1 - Generate call relations.</br>
  2 - Generate a graph data structure based on edges generated from CallGraph(from step 1).</br>
  3 - Use Annotations to mark the Elected methods/class to possible change.</br>
  4 - Use Annotations to mark Methods/class as artefacts end-points.</br>
  5 - Process the project with annotations to Mapping change propagation.</br></br>

We will have two type of call hierarchy: base on method call and based on class "call"(dependency, on really).
