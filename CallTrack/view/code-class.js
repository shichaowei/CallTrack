$(function(){ // on dom ready

var defaultStyle = cytoscape.stylesheet()
    .selector('node')
      .css({
        'content': 'data(label)',
      })
    .selector('edge')
      .css({
        'target-arrow-shape': 'triangle',
        'width': 4,
        'line-color': '#ddd',
        'target-arrow-color': '#ddd'
      })
    .selector('.highlighted')
      .css({
        'background-color': '#61bffc',
        'line-color': '#61bffc',
        'target-arrow-color': '#61bffc',
        'transition-property': 'background-color, line-color, target-arrow-color',
        'transition-duration': '0.5s'
      }).selector('.startNode')
        .css({
          'background-color': '#00FF00'

        });

var json  =  eval(data);

console.log(json.edges[0]);

var cy = cytoscape({
  container: document.getElementById('cy'),

  boxSelectionEnabled: false,
  autounselectify: true,

  style: defaultStyle,
  
  elements: {
      nodes: json.nodes, 
      
      edges: json.edges
    },
  
  layout: {
    name: 'cose', //'cose' is the best the best until now
    fit: true,
    directed: true,
    roots: '#' + json.nodes[0].data.id,
     padding: 10,
     avoidOverlap: true,
     animate: false,
     maximalAdjustments: 100 
  }
});

console.log("Starting with " + json.nodes[0].data.id);




  var firstTap = true;
//console.log(eval("{ data: { id: 'a' , label: 'label de a'} }"));
cy.on('tap','node', function(evt){


    
      cy.elements().each(function(i, ele){
        ele.removeClass('highlighted');
        ele.removeClass('startNode');
      });
    


  //console.log(evt.data.foo);
  var i = 0;
   var node = evt.cyTarget;
   console.log( 'tapped ' + node.id() );
   var bfs = cy.elements().bfs('#' + node.id(), function(){}, true);
   // kick off first highlight
   cy.getElementById(node.id()).addClass('startNode');
   var highlightNextEle = function(){
  if( i < bfs.path.length ){
    bfs.path[i].addClass('highlighted');
    var element = cy.getElementById(bfs.path[i].id());

    //From element.data() i can acess the custom values in Nodes or Edges;
    //From element.isNode() or element.isEdge() i can to know if the element is a node or a Edge respectively 
    i++;
    setTimeout(highlightNextEle, 100);
  }
};

   highlightNextEle();




});







}); // on dom ready