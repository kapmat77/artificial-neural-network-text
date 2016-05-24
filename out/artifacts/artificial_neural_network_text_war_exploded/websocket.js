//window.onload = init;
var socket = new WebSocket("ws://localhost:8080/actions");
socket.onmessage = onMessage;

var nodeColor = "#FFFFFF";
var nodeBorderColor = "#000";
var circleSize = 22;
var wordSize = 10;
var levelSize = 12;
var pathId = 1;

//COEFF
var coeffSize = 12;
var coeffWordSize = 10;
var coeffColor = "#50FF73";

var lineDefColor = "#000";
var lineActiveColor = "#FFF700";

var radius = 40;

// set up SVG for D3
var width = window.screen.availWidth-290,
    height = window.screen.availHeight-112;
var svg = d3.select('body')
    .append('svg')
    .attr('width', width)
    .attr('height', height)
    .style('background', "#ACACAC");


var index = 9;
var action = "add";

var nodes = [],
    links = [];

// handles to link and node element groups
var path = svg.append('svg:g').selectAll('path'),
    circle = svg.append('svg:g').selectAll('g'),
	coeff = svg.append('svg:g').selectAll('c');

// mouse event vars
var selected_node = null,
    selected_link = null;
//    mousedown_link = null;
//    mousedown_node = null,
//    mouseup_node = null;

var currentNode;

function refresh() {
//	path = svg.append('svg:g').selectAll('path');
//	circle = svg.append('svg:g').selectAll('g');
	
	    force = d3.layout.force()
        .nodes(nodes)
        .links(links)
        .size([width, height])
        .linkDistance(200)
		.linkStrength(0.0001)
//		.linkStrength(0.1)
//		.theta(0)
        .charge(-8)
//        .charge(0)
		.gravity(0.0003)
//		.gravity(0.002)
        .on('tick', tick);

    restart("add");
}

function onMessage(event) {
	var object = JSON.parse(event.data);
	currentNode = object;
	
    switch (object.action) {
        case "add":
			refresh();
            printNodeElement(object);
            break;
        case "addSentence":
            printNewSentenceElement(object);
            break;
		case "updateSentence":
            printAddWordToSentence(object);
            break;
        case "removeSentence":
			printRemoveSentence(object);
//			document.getElementById(object.id).remove();
//			object.parentNode.removeChild(object);
            break;
        case "update":
            printUpdatedNode(object);
            break;
		case "addLines":
			printAddLines(object);
			break;
		case "updateLines":
			printUpdateLines(object);
			break;
		case "activeNeuron":
			printActiveNeuron(object);
			break;
		case "updateBestLines":
			printUpdateBestLines(object);
			break;
		case "resetLines":
			printResetLines(object);
    }
}

function startCreatingGraphMonkey() {
    var StartAction = {
        action: "start",
        name: "monkey",
		speed: document.getElementById("speed").value
    };
    socket.send(JSON.stringify(StartAction));
}

function startCreatingGraphTest() {
    var StartAction = {
        action: "start",
        name: "test",
		speed: document.getElementById("speed").value
    };
    socket.send(JSON.stringify(StartAction));
}

function updateGraph() {
    var UpdateAction = {
        action: "update",
		word: document.getElementById("selectedWord").value,
		speed: document.getElementById("speed").value
    };
    socket.send(JSON.stringify(UpdateAction));
}

function resetPage() {
    location.reload();
}

function resetLines() {
	var ResetLinesAction = {
        action: "resetLines"
    };
    socket.send(JSON.stringify(ResetLinesAction));
}

function printNodeElement(node) {
    nodes.push(
        {id: node.id, name: node.name, level: node.level}
    );
	
    restart("add");
}

function isInArray(source, target, array) {
  	for (var k = 0; k<array.length; k++) {
		if (array[k].source===source && array[k].target===target) {
			return true;
		}
	}
	return false;
}

function printAddLines(node) {
	var neigh = node.neighbours.split(" ");
	var coeff = node.coeff.split(" ");
	console.log(currentNode.name);
	console.log(neigh);
	console.log(coeff);
	
	
	for (i=1; i<neigh.length; i++) {

		var lookup = {};
		for (var k = 0; k<nodes.length; k++) {
			lookup[nodes[k].name] = nodes[k];
		}	
		
		if(!isInArray(lookup[node.name],lookup[neigh[i]],links)) {
			links = links.concat(
				{id:lookup[node.name].name+lookup[neigh[i]].name, name:lookup[node.name].name, source: lookup[node.name], target: lookup[neigh[i]], coeff: coeff[i],left: false, right: true}
			);
//			console.log(lookup[node.name].name+lookup[neigh[i]].name);
			restart("addLine", lookup[node.name].name+lookup[neigh[i]].name)
		}
	}

//restart("addLine");
}

function printResetLines(nodes) {
	restart("resetLines");
}

function printUpdateLines(node) {
	restart("updateLine");
}

function printUpdateBestLines() {
	restart("updateBestLine");
}

function printNewSentenceElement(sentence) {

    //Remove old sentence if exist
    if (document.getElementById(sentence.id - 1) !== null) {
        document.getElementById(sentence.id - 1).remove();
    }

    var sentenceHTML = document.getElementById("sentenceHTML");

    var sentenceDiv = document.createElement("div");
    sentenceDiv.setAttribute("id", sentence.id);
    sentenceHTML.appendChild(sentenceDiv);

    var sentenceName = document.createElement("span");
    sentenceName.setAttribute("class", "sentenceName");
    sentenceName.innerHTML = sentence.name;
    sentenceName.style.fontSize = "15px";
    sentenceName.style.fontFamily = "Tahoma";
    sentenceName.style.color = "black";
    sentenceName.className = "animationSentence";
    sentenceDiv.appendChild(sentenceName);
}

function printAddWordToSentence(word) {
    var sentenceHTML = document.getElementById("sentenceHTML");

//    var sentenceDiv = document.createElement("div");
//    sentenceDiv.setAttribute("id", word.id);
//    sentenceHTML.appendChild(sentenceDiv);

	var sentenceDiv = document.getElementById(word.id);
	sentenceHTML.appendChild(sentenceDiv);
	
    var sentenceName = document.createElement("span");
    sentenceName.setAttribute("class", "sentenceName");
    sentenceName.innerHTML = word.name + " / ";
    sentenceName.style.fontSize = "15px";
    sentenceName.style.fontFamily = "Tahoma";
    sentenceName.style.color = "black";
    sentenceName.className = "animationSentence";
    sentenceDiv.appendChild(sentenceName);
}

function printRemoveSentence(sentence) {
	//Remove old sentence if exist
    if (document.getElementById(sentence.id) !== null) {
        document.getElementById(sentence.id).remove();
    }
}

function printUpdatedNode(node) {
    restart("update",node);
}

function printActiveNeuron(node) {
	
	var mainPart = document.getElementById("nodeMain"+node.name);
	mainPart.style.fill="#00FF33";
	var oldColor = "#FFFFFF";
	switch(node.level) {
		case 1:
			oldColor="#FFFFFF";
			break;
		case 2:
			oldColor="#FFD2D2";
			break;
		case 3:
			oldColor="#FFB3B3";
			break;
		case 4:
			oldColor="#FF8D8D";
			break;
		case 5:
			oldColor="#FF4646";
			break;
		defaulty:
			oldColor="#FF4646";
			break;
	}
	setTimeout(function(){ mainPart.style.fill=oldColor; }, 3600);
}

function tick() {
    // draw directed edges with proper padding from node centers
    path.attr('d', function (d) {
        var deltaX = d.target.x - d.source.x,
            deltaY = d.target.y - d.source.y,
            dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY),
            normX = deltaX / dist,
            normY = deltaY / dist,
            sourcePadding = d.left ? circleSize + 2 : circleSize - 5,
            targetPadding = d.right ? circleSize + 2 : circleSize - 5,
            sourceX = d.source.x + (sourcePadding * normX),
            sourceY = d.source.y + (sourcePadding * normY),
            targetX = d.target.x - (targetPadding * normX),
            targetY = d.target.y - (targetPadding * normY);
        return 'M' + sourceX + ',' + sourceY + 'L' + targetX + ',' + targetY;
    });

	circle.attr("cx", function(d) { return d.x = Math.max(radius, Math.min(width - radius, d.x)); })
        .attr("cy", function(d) { return d.y = Math.max(radius, Math.min(height - radius, d.y)); });

//	coeff.attr("cx", function(d) { return d.x; })
//		.attr("cy", function(d) { return d.y; });
	coeff.attr("cx", function(d) { return d.x; })
		.attr("cy", function(d) { return d.y; });
	
    path.attr("x1", function(d) {  return d.source.x; })
        .attr("y1", function(d) {  return d.source.y; })
        .attr("x2", function(d) {  return d.target.x; })
        .attr("y2", function(d) {  return d.target.y; });

    circle.attr('transform', function (d) {
        return 'translate(' + d.x + ',' + d.y + ')';
	});
	
	coeff.attr('transform', function (d) {
//		return 'translate(' + d.x + ',' + d.y + ')';
		return 'translate(' + (d.source.x*1.2+d.target.x*0.8)/2 + ',' + (d.source.y*1.2+d.target.y*0.8)/2 + ')';
    });
//	}
}

function isInSingleArray(coeffX, coeffY, array) {
	for (var k = 0; k<array.length; k++) {
		if (array[k].x===coeffX && array[k].y===coeffY) {
			return false;
		}
	}
	return true;
}

function isInArray(source, target, array) {
  	for (var k = 0; k<array.length; k++) {
		if (array[k].source===source && array[k].target===target) {
			return true;
		}
	}
	return false;
}

// update graph (called when needed)
function restart(action, idPath) {
	
	if(action === "addLine") {
		
				var name = '';
		if (currentNode!==undefined) {
			name = currentNode.name;
		}
		
		// path (link) group
    path = path.data(links);

	var ccc = 0;
	
    // add new links
    path.enter().append('svg:path')
        .attr('class', 'link')
		.attr('id', 'path'+idPath)
		.style('stroke', lineDefColor)
		.style('stroke-width', 1)
        .classed('selected', function (d) {
            return d === selected_link;
        })
        .style('marker-start', function (d) {
            return d.left ? 'url(#start-arrow)' : '';
        })
        .style('marker-end', function (d) {
            return d.right ? 'url(#end-arrow)' : '';
        })
				.text(function(d) { 
					ccc = d.coeff; 
		})
        .on('mousedown', function (d) {
            if (d3.event.ctrlKey) return;

            // select link
            mousedown_link = d;
            if (mousedown_link === selected_link) selected_link = null;
            else selected_link = mousedown_link;
            selected_node = null;
        });
	
		//COEFF !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

		
	
//		var a = 0;
//		a = 1 (function(d) {return d.coeff; }) + 1;
//		if(function(d) { a = d.coeff; return d.coeff; } > 1) {
//
//		}

//	if(ccc>0) {
//		coeff = coeff.data(links);
//		var c = coeff.enter().append('svg:g');
//		
//		var name = '';
//		if (currentNode!==undefined) {
//			name = currentNode.name;
//		}
//
//		var mainPart = document.getElementById("nodeMain"+idPath);
//		
//		if(mainPart === null) {
//
//        c.append('svg:circle')
//			.attr('id', "nodeMain" + idPath)
//            .attr('class', 'node')
//            .attr('r', coeffSize)
//            .style('fill', coeffColor)
//            .style('stroke', nodeBorderColor);
//			
//		
//
//        // print node name
//        c.append('svg:text')
//			.attr('id', "nodeWord" + idPath)
//            .attr('x', 0)
//            .attr('y', 2)
//            .attr('class', 'id')
//            .style('font-size', coeffWordSize)
//            .text(ccc);
//	
//		} else {
//		 // print node name
//			c.append('svg:text')
//			.attr('id', "nodeWord" + idPath)
//            .attr('x', 0)
//            .attr('y', 2)
//            .attr('class', 'id')
//            .style('font-size', coeffWordSize)
//            .text(ccc);
//		}
//
////  // remove old nodes
//        coeff.exit().remove();
//
//        // set the graph in motion
////        force.start();
//	}
		
	}
    

		

	// circle (node) group
    // NB: the function arg is crucial here! nodes are known by id, not by index!
    circle = circle.data(nodes, function (d) {
        return d.id;
    });
	


    // add new nodes
    if (action === "add") {
		
        var g = circle.enter().append('svg:g');
		
		var name = '';
		if (currentNode!==undefined) {
			name = currentNode.name;
		}
		

        g.append('svg:circle')
			.attr('id', "nodeMain" + name)
            .attr('class', 'node')
            .attr('r', circleSize)
            .style('fill', nodeColor)
            .style('stroke', nodeBorderColor);

		var mainPart = document.getElementById("nodeMain"+name);

        // print node name
        g.append('svg:text')
			.attr('id', "nodeWord" + name)
            .attr('x', 0)
            .attr('y', 2)
            .attr('class', 'id')
            .style('font-size', wordSize)
            //      .text(nodes[0].name);
            .text(function(d) { return d.name; });

        // print node level
        g.append('svg:text')
			.attr('id', "nodeLevel" + name)
            .attr('x', 0)
            .attr('y', 16)
            .attr('class', 'id')
            .style('font-size', levelSize)
            .style('fill', "black")
                  .text(function(d) { return d.level; });
//            .text(index);
        index++;
//  // remove old nodes
        circle.exit().remove();

        // set the graph in motion
        force.start();

    } else if (action === "update") {

//		var pathName = document.getElementById("path"+name);
//		pathName.style.stroke = lineActiveColor;
		
		var name = '';
		if (currentNode!==undefined) {
			name = currentNode.name;
		}
		
		var mainPart = document.getElementById("nodeMain"+name);
		
		var wordPart = document.getElementById("nodeWord"+name);
		
		var levelPart = document.getElementById("nodeLevel"+name);
		
		var multCS = 4;
		var multWS = 2;
		var paramY = levelPart.getAttribute("y");
		
		switch(currentNode.level) {
			case 1:
				mainPart.style.fill="#FFFFFF";
				break;
			case 2:
				mainPart.style.fill="#FFD2D2";
				mainPart.setAttribute("r",circleSize+multCS*1);
				wordPart.style.font = (wordSize+multWS*1)+"px arial";
				levelPart.style.font = (levelSize+multWS*1)+"px arial";
				for(var k = 0; k<multWS*1; k++) {
					paramY++;
				}
				levelPart.setAttribute("y",paramY);
				break;
			case 3:
				mainPart.style.fill="#FFB3B3";
				mainPart.setAttribute("r",circleSize+multCS*2);
				wordPart.style.font = (wordSize+multWS*2)+"px arial";
				levelPart.style.font = (levelSize+multWS*2)+"px arial";
				for(var k = 0; k<multWS*1; k++) {
					paramY++;
				}
				levelPart.setAttribute("y",paramY);
				break;
			case 4:
				mainPart.style.fill="#FF8D8D";
				mainPart.setAttribute("r",circleSize+multCS*3);
				wordPart.style.font = (wordSize+multWS*3)+"px arial";
				levelPart.style.font = (levelSize+multWS*3)+"px arial";
				for(var k = 0; k<multWS*1; k++) {
					paramY++;
				}
				levelPart.setAttribute("y",paramY);
				break;
			case 5:
				mainPart.style.fill="#FF4646";
				mainPart.setAttribute("r",circleSize+multCS*4);
				wordPart.style.font = "bold " +(wordSize+multWS*4)+"px arial";
				levelPart.style.font = (levelSize+multWS*4)+"px arial";
				for(var k = 0; k<multWS*1; k++) {
					paramY++;
				}
				levelPart.setAttribute("y",paramY);
				break;
			defaulty:
				mainPart.style.fill="#FF4646";
				mainPart.setAttribute("r",circleSize+multCS*5);
				wordPart.style.font = (wordSize+multWS*5)+"px bold arial";
				levelPart.style.font = (levelSize+multWS*5)+"px arial";
				for(var k = 0; k<multWS*1; k++) {
					paramY++;
				}
				levelPart.setAttribute("y",paramY);
				break;
		}
		
		var textnode = document.getElementById("nodeLevel"+name);
		textnode.textContent = currentNode.level;
		
		
		
        // set the graph in motion
//        force.start();
		
//		refresh();
    } else if (action==="updateLine") {
		
		var name = '';
		if (currentNode!==undefined) {
			name = currentNode.name;
		}
			
//			var result = links.filter(function( obj ) {
////				return obj.name === currentNode.name;
//				return (obj.source.name === currentNode.name) || (obj.target.name === currentNode.name);
//			});
//			
////			console.log(result);
//			
//			var re = result.filter(function( obj ) {
////				return obj.name === currentNode.name;
//				return (obj.source.name === currentNode.name);
//			});
//			
////			console.log(re);
//			
//			var goodNodes = result.filter(function(item) {
//				return re.indexOf(item) === -1;
//			});
//			
////			console.log(goodNodes);
//			
//			var paths = [];
//			for(var i=0; i<goodNodes.length; i++) {
//				var rest = result.filter(function( obj ) {
//					return (obj.target.name === goodNodes[i].source.name) && (obj.source.name === goodNodes[i].target.name);
//				});
//				for(var j=0; j<rest.length; j++) {
//					paths.push(rest[j]);
//				}	
//			}
//			
//			for(var k=0; k<goodNodes.length; k++) {
//				paths.push(goodNodes[k]);
//			}	
			
			var neigh = currentNode.neighbours.split(" ");
//			console.log(neigh);
			for(var i=1; i<neigh.length; i++) {
//				var singlePath = document.getElementById("path" + paths[i].id);
//				console.log(neigh[i]);
				var singlePath = document.getElementById("path" + neigh[i] + currentNode.name);
//				var singlePath = document.getElementById("pathVERYCLEVER");
				singlePath.style.stroke="yellow";
				singlePath.style['stroke-width']="6px";
				(function(capturedI) {
					setTimeout(function(){ 
						var sPath = document.getElementById("path" + neigh[capturedI] + currentNode.name);
//						
//						console.log(neigh[capturedI] + currentNode.name);
						sPath.style.stroke="#000";
						sPath.style['stroke-width']="1px";}, 1500);
				
				})(i);
			
			}
//			for(var i=0; i<paths.length; i++) {
//				
////				console.log(pathName);
//				setTimeout(function(){ 
//					var singlePath = document.getElementById("path" + paths[i].id);
//					singlePath.style.stroke="#000";
//					singlePath.style['stroke-width']="2px";
//									}, 500);
//								}
//			result[0].style.stroke = "#000";
//			result[1].style.stroke = "#FFF";
//			result[2].style.stroke = "#ABC";
			
			
			
	} else if (action === "updateBestLine") {
				var name = '';
		if (currentNode!==undefined) {
			name = currentNode.name;
		}
			
//			var result = links.filter(function( obj ) {
////				return obj.name === currentNode.name;
//				return (obj.source.name === currentNode.name) || (obj.target.name === currentNode.name);
//			});
//			
////			console.log(result);
//			
//			var re = result.filter(function( obj ) {
////				return obj.name === currentNode.name;
//				return (obj.source.name === currentNode.name);
//			});
//			
////			console.log(re);
//			
//			var goodNodes = result.filter(function(item) {
//				return re.indexOf(item) === -1;
//			});
//			
////			console.log(goodNodes);
//			
//			var paths = [];
//			for(var i=0; i<goodNodes.length; i++) {
//				var rest = result.filter(function( obj ) {
//					return (obj.target.name === goodNodes[i].source.name) && (obj.source.name === goodNodes[i].target.name);
//				});
//				for(var j=0; j<rest.length; j++) {
//					paths.push(rest[j]);
//				}	
//			}
//			
//			for(var k=0; k<goodNodes.length; k++) {
//				paths.push(goodNodes[k]);
//			}	
			
			var neigh = currentNode.neighbours.split(" ");
//			console.log(neigh);
			for(var i=1; i<neigh.length; i++) {
//				var singlePath = document.getElementById("path" + paths[i].id);
//				console.log(neigh[i]);
				var singlePath = document.getElementById("path" + neigh[i] + currentNode.name);
//				var singlePath = document.getElementById("pathVERYCLEVER");
				singlePath.style.stroke="red";
				singlePath.style['stroke-width']="8px";
				(function(capturedI) {
					setTimeout(function(){ 
						var sPath = document.getElementById("path" + neigh[capturedI] + currentNode.name);
//						console.log(neigh[capturedI] + currentNode.name);
						sPath.style.stroke="#000";
						sPath.style['stroke-width']="1px";}, 5000);
				
				})(i);
			
			}
	} else if(action==="resetLines") {
			var result = links.filter(function( obj ) {
				return obj.name;
//				return (obj.source.name === currentNode.name) || (obj.target.name === currentNode.name);
			});
//			alert(document.getElementById("selectedWord").value);
//			console.log(neigh);
			for(var i=0; i<result.length; i++) {
//				var singlePath = document.getElementById("path" + paths[i].id);
//				console.log(neigh[i]);
				var singlePath = document.getElementById("path" + result[i].id);
//				var singlePath = document.getElementById("pathVERYCLEVER");
				console.log(result[i].id);
				singlePath.style.stroke="000";
				singlePath.style['stroke-width']="1px";
			}
	}
}

// only respond once per keydown
var lastKeyDown = -1;

function keydown() {
    d3.event.preventDefault();

    if (lastKeyDown !== -1) return;
    lastKeyDown = d3.event.keyCode;

    // ctrl
    if (d3.event.keyCode === 17) {
        circle.call(force.drag);
        svg.classed('ctrl', true);
    }
	
	if (d3.event.keyCode === 82) {
		refresh();
	}
}

function keyup() {
    lastKeyDown = -1;

    // ctrl
    if (d3.event.keyCode === 17) {
        circle
            .on('mousedown.drag', null)
            .on('touchstart.drag', null);
        svg.classed('ctrl', false);
    }
	
	if (d3.event.keyCode === 82) {
		refresh();
	}
}

d3.select(window)
    .on('keydown', keydown)
  .on('keyup', keyup);
//restart("add");
//refresh();

