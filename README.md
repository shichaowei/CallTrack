# CallTrack
This is intership project of Walter A. Alves that consist in a tool to analyse what impact causes in method or class change. 
This project will use call graph api  to generate call peers, an algorthm to create and colour transitively to show change impact.


The main idea of this project, is tracking change comportamental <b>atomic</b> on some <b>software artefacts</b>(like Use Case, Test Case, etc).

The steps to track are:
  1 - Generate call relations using CallGraph lib
  2 - Generate a graph data structure based on edges generated from CallGraph(from step 1)
  3 - Use some visual lib to show graph

We will have two type of call hierarchy: base on method call and based on class "call"(dependency, on really).
