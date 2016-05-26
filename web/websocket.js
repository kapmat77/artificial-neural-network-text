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
var width = window.screen.availWidth - 290,
    height = window.screen.availHeight - 82;
var svg = d3.select('body')
    .append('svg')
    .attr('width', width)
    .attr('height', height)
    .style('background', "#ACACAC");


var nodes = [],
    links = [];

// handles to link and node element groups
var path = svg.append('svg:g').selectAll('path'),
    circle = svg.append('svg:g').selectAll('g'),
    coeff = svg.append('svg:g').selectAll('c');

// mouse event vars
var selected_node = null,
    selected_link = null;

var currentNode;

function refresh() {

    force = d3.layout.force()
        .nodes(nodes)
        .links(links)
        .size([width, height])
            //TODO zmienić aby odległości między węzłami odpowiadały wagom między tymi węzłami
        // .linkDistance(function(d) { return  d.distance; })
        // .linkStrength(1)
        .linkDistance(200)
        .linkStrength(0.01)
        //		.theta(0)
        .charge(-100)
        .gravity(0.0)
        //		.gravity(0.0001)
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
        case "updateCharge":
            restart("updateCharge")
            break
        case "resetLines":
            printResetLines(object);
            break;
        case "resetNodes":
            restart("resetNodes");
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

// function updateGraph() {
//     var UpdateAction = {
//         action: "update",
//         word: document.getElementById("selectedWord").value,
//         speed: document.getElementById("speed").value
//     };
//     socket.send(JSON.stringify(UpdateAction));
// }

function updateGraph() {
    var UpdateData = {
        action: "update",
        speed: document.getElementById("speed").value,
        word: document.getElementById("objectParameters").value
    };
    socket.send(JSON.stringify(UpdateData));
}

function resetPage() {
    var KillChargeThread = {
        action: "killChargeThread"
    };
    socket.send(JSON.stringify(KillChargeThread));
    location.reload();
}

function resetLines() {
    var ResetLinesAction = {
        action: "resetLines"
    };
    socket.send(JSON.stringify(ResetLinesAction));
}

function updateParameters() {
    var UpdateData = {
        action: "updateParameters",
        speed: document.getElementById("speed").value,
        parameters: (document.getElementById("alfa").value +" "+ document.getElementById("beta").value +" "+ document.getElementById("teta").value)
    };
    alert("Dane zostały wprowadzone");
    socket.send(JSON.stringify(UpdateData));
}

function printNodeElement(node) {
    nodes.push(
        {id: node.id, name: node.name, level: node.level, chargeLevel: node.chargeLevel, distance: node.distance}
    );

    restart("add");
}

function isInArray(source, target, array) {
    for (var k = 0; k < array.length; k++) {
        if (array[k].source === source && array[k].target === target) {
            return true;
        }
    }
    return false;
}

function printAddLines(node) {
    var neigh = node.neighbours.split(" ");
    var coeff = node.coeff.split(" ");


    for (i = 1; i < neigh.length; i++) {

        var lookup = {};
        for (var k = 0; k < nodes.length; k++) {
            lookup[nodes[k].name] = nodes[k];
        }

        if (!isInArray(lookup[node.name], lookup[neigh[i]], links)) {
            links = links.concat(
                {
                    id: lookup[node.name].name + lookup[neigh[i]].name,
                    name: lookup[node.name].name,
                    source: lookup[node.name],
                    target: lookup[neigh[i]],
                    coeff: coeff[i],
                    left: false,
                    right: true,
                    distance: currentNode.distance
                }
            );
            restart("addLine", lookup[node.name].name + lookup[neigh[i]].name)
        }
    }

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
    sentenceName.style.fontSize = "14px";
    sentenceName.style.fontFamily = "Tahoma";
    sentenceName.style.color = "black";
    sentenceName.className = "animationSentence";
    sentenceDiv.appendChild(sentenceName);
}

function printAddWordToSentence(word) {
    var sentenceHTML = document.getElementById("sentenceHTML");

    var sentenceDiv = document.getElementById(word.id);
    sentenceHTML.appendChild(sentenceDiv);

    var sentenceName = document.createElement("span");
    sentenceName.setAttribute("class", "sentenceName");
    if (word.name!=="#") {
        sentenceName.innerHTML = word.name + " / ";
        sentenceName.style.color = "black";
    } else {
        sentenceName.innerHTML = word.name;
        sentenceName.style.color = "red";
    }
    sentenceName.style.fontSize = "11px";
    sentenceName.style.fontFamily = "Tahoma";
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
    restart("update", node);
}

function printActiveNeuron(node) {

    var mainPart = document.getElementById("nodeMain" + node.name);
    mainPart.style.fill = currentNode.color;
    console.log(mainPart.style.fill);
    // mainPart.style.fill="#00FF33";
    var oldColor = "#FFFFFF";
    switch (node.level) {
        case 1:
            oldColor = "#FFFFFF";
            break;
        case 2:
            oldColor = "#FFD2D2";
            break;
        case 3:
            oldColor = "#FFB3B3";
            break;
        case 4:
            oldColor = "#FF8D8D";
            break;
        case 5:
            oldColor = "#FF4646";
            break;
            defaulty:
                oldColor = "#FF4646";
            break;
    }
    // setTimeout(function(){ mainPart.style.fill=oldColor; }, 3600);
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

    circle.attr("cx", function (d) {
        return d.x = Math.max(radius, Math.min(width - radius, d.x));
    })
        .attr("cy", function (d) {
            return d.y = Math.max(radius, Math.min(height - radius, d.y));
        });

    coeff.attr("cx", function (d) {
        return d.x;
    })
        .attr("cy", function (d) {
            return d.y;
        });

    path.attr("x1", function (d) {
        return d.source.x;
    })
        .attr("y1", function (d) {
            return d.source.y;
        })
        .attr("x2", function (d) {
            return d.target.x;
        })
        .attr("y2", function (d) {
            return d.target.y;
        });

    circle.attr('transform', function (d) {
        return 'translate(' + d.x + ',' + d.y + ')';
    });

    coeff.attr('transform', function (d) {
//		return 'translate(' + d.x + ',' + d.y + ')';
        return 'translate(' + (d.source.x * 1.2 + d.target.x * 0.8) / 2 + ',' + (d.source.y * 1.2 + d.target.y * 0.8) / 2 + ')';
    });
//	}
}

function isInSingleArray(coeffX, coeffY, array) {
    for (var k = 0; k < array.length; k++) {
        if (array[k].x === coeffX && array[k].y === coeffY) {
            return false;
        }
    }
    return true;
}

function isInArray(source, target, array) {
    for (var k = 0; k < array.length; k++) {
        if (array[k].source === source && array[k].target === target) {
            return true;
        }
    }
    return false;
}

// update graph (called when needed)
function restart(action, idPath) {


    var name = '';
    if (currentNode !== undefined) {
        name = currentNode.name;
    }

    if (action === "addLine") {

        var name = '';
        if (currentNode !== undefined) {
            name = currentNode.name;
        }

        // path (link) group
        path = path.data(links);

        var ccc = 0;

        // add new links
        path.enter().append('svg:path')
            .attr('class', 'link')
            .attr('id', 'path' + idPath)
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
            .text(function (d) {
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
        if (currentNode !== undefined) {
            name = currentNode.name;
        }


        g.append('svg:circle')
            .attr('id', "nodeMain" + name)
            .attr('class', 'node')
            .attr('r', circleSize)
            .style('fill', nodeColor)
            .style('stroke', nodeBorderColor);

        var mainPart = document.getElementById("nodeMain" + name);

        // print node name
        g.append('svg:text')
            .attr('id', "nodeWord" + name)
            .attr('x', 0)
            .attr('y', 2)
            .attr('class', 'id')
            .style('font-size', wordSize)
            //      .text(nodes[0].name);
            .text(function (d) {
                return d.name;
            });

        // print node level
        g.append('svg:text')
            .attr('id', "nodeLevel" + name)
            .attr('x', 0)
            .attr('y', 16)
            .attr('class', 'id')
            .style('font-size', levelSize)
            .style('fill', "black")
            .text(function (d) {
                return d.level;
            });

        // print charge level
        g.append('svg:text')
            .attr('id', "nodeCharge" + name)
            .attr('x', 0)
            .attr('y', -10)
            .attr('class', 'id')
            .style('font-size', (levelSize - 1))
            .style('fill', "blue")
            // .text("C_L");
            .text(function (d) {
                return d.chargeLevel;
            });

//   remove old nodes
        circle.exit().remove();

        // set the graph in motion
        force.start();

    } else if (action === "update") {

        var name = '';
        if (currentNode !== undefined) {
            name = currentNode.name;
        }

        var mainPart = document.getElementById("nodeMain" + name);
        var wordPart = document.getElementById("nodeWord" + name);
        var levelPart = document.getElementById("nodeLevel" + name);
        var chargePart = document.getElementById("nodeCharge" + name);

        var multCS = 4;
        var multWS = 2;
        var paramLevelY = levelPart.getAttribute("y");
        var paramChargeY = levelPart.getAttribute("y");

        switch (currentNode.level) {
            case 1:
                mainPart.style.fill = "#FFFFFF";
                break;
            case 2:
                mainPart.style.fill = "#FFD2D2";
                mainPart.setAttribute("r", circleSize + multCS * 1);
                wordPart.style.font = (wordSize + multWS * 1) + "px arial";
                levelPart.style.font = (levelSize + multWS * 1) + "px arial";
                for (var k = 0; k < multWS * 1; k++) {
                    paramLevelY++;
                }
                paramChargeY = (-paramLevelY) + 4;
                levelPart.setAttribute("y", paramLevelY);
                chargePart.setAttribute("y", paramChargeY);
                break;
            case 3:
                mainPart.style.fill = "#FFB3B3";
                mainPart.setAttribute("r", circleSize + multCS * 2);
                wordPart.style.font = (wordSize + multWS * 2) + "px arial";
                levelPart.style.font = (levelSize + multWS * 2) + "px arial";
                for (var k = 0; k < multWS * 1; k++) {
                    paramLevelY++;
                }
                paramChargeY = (-paramLevelY) + 4;
                levelPart.setAttribute("y", paramLevelY);
                chargePart.setAttribute("y", paramChargeY);
                break;
            case 4:
                mainPart.style.fill = "#FF8D8D";
                mainPart.setAttribute("r", circleSize + multCS * 3);
                wordPart.style.font = (wordSize + multWS * 3) + "px arial";
                levelPart.style.font = (levelSize + multWS * 3) + "px arial";
                for (var k = 0; k < multWS * 1; k++) {
                    paramLevelY++;
                }
                paramChargeY = (-paramLevelY) + 4;
                levelPart.setAttribute("y", paramLevelY);
                chargePart.setAttribute("y", paramChargeY);
                break;
            case 5:
                mainPart.style.fill = "#FF4646";
                mainPart.setAttribute("r", circleSize + multCS * 4);
                wordPart.style.font = "bold " + (wordSize + multWS * 4) + "px arial";
                levelPart.style.font = (levelSize + multWS * 4) + "px arial";
                for (var k = 0; k < multWS * 1; k++) {
                    paramLevelY++;
                }
                paramChargeY = (-paramLevelY) + 4;
                levelPart.setAttribute("y", paramLevelY);
                chargePart.setAttribute("y", paramChargeY);
                break;
                defaulty:
                    mainPart.style.fill = "#FF4646";
                mainPart.setAttribute("r", circleSize + multCS * 5);
                wordPart.style.font = (wordSize + multWS * 5) + "px bold arial";
                levelPart.style.font = (levelSize + multWS * 5) + "px arial";
                for (var k = 0; k < multWS * 1; k++) {
                    paramLevelY++;
                }
                paramChargeY = (-paramLevelY) + 4;
                levelPart.setAttribute("y", paramLevelY);
                chargePart.setAttribute("y", paramChargeY);
                break;
        }

        //UPDATE LEVEL
        var textnode = document.getElementById("nodeLevel" + name);
        textnode.textContent = currentNode.level;


    } else if (action == "updateCharge") {
        //UPDATE CHARGE LEVEL
        var textCharge = document.getElementById("nodeCharge" + name);
        textCharge.textContent = currentNode.chargeLevel;

    } else if (action === "updateLine") {

        var name = '';
        if (currentNode !== undefined) {
            name = currentNode.name;
        }

        var neigh = currentNode.neighbours.split(" ");
        for (var i = 1; i < neigh.length; i++) {
            var singlePath = document.getElementById("path" + neigh[i] + currentNode.name);
            if (singlePath.style.stroke != "red") {
                singlePath.style.stroke = "yellow";
                singlePath.style['stroke-width'] = "6px";
            }
            // (function(capturedI) {
            // 	setTimeout(function(){
            // 		var sPath = document.getElementById("path" + neigh[capturedI] + currentNode.name);
            // 		sPath.style.stroke="#000";
            // 		sPath.style['stroke-width']="1px";}, 1500);
            //
            // })(i);

        }
    } else if (action === "updateBestLine") {
        var name = '';
        if (currentNode !== undefined) {
            name = currentNode.name;
        }
        var neigh = currentNode.neighbours.split(" ");
        for (var i = 1; i < neigh.length; i++) {
            var singlePath = document.getElementById("path" + neigh[i] + currentNode.name);
            singlePath.style.stroke = "red";
            singlePath.style['stroke-width'] = "8px";
            (function (capturedI) {
                setTimeout(function () {
                    var sPath = document.getElementById("path" + neigh[capturedI] + currentNode.name);
                    sPath.style.stroke = "#000";
                    sPath.style['stroke-width'] = "1px";
                }, 5000);

            })(i);

        }
    } else if (action === "resetLines") {
        var result = links.filter(function (obj) {
            return obj.name;
        });
        for (var i = 0; i < result.length; i++) {
            var singlePath = document.getElementById("path" + result[i].id);
            singlePath.style.stroke = "000";
            singlePath.style['stroke-width'] = "1px";
        }
        restart("resetNodes");
    } else if (action === "resetNodes") {
        var result = nodes.filter(function (obj) {
            return obj.name;
        });
        for (var i = 0; i < result.length; i++) {
            console.log("ID: " + "nodeMain" + result[i].id);
            console.log("Name: " + "nodeMain" + result[i].name);
            var mainPart = document.getElementById("nodeMain" + result[i].name);
            mainPart.style.fill = "#FFF";
        }
    }
}

function keyup() {
    var lastKeyDown = -1;

    // ctrl
    if (d3.event.keyCode === 17) {
        circle.call(force.drag);
        svg.classed('ctrl', true);
    }

    if (d3.event.keyCode === 82) {
        refresh();
    }
}
var timer = 0;
$(document).ready(
    function () {
        setInterval(function () {
            if (currentNode.action == "updateTimer") {
                timer = currentNode.clock;
            }
            $('#show').text(
                'Timer: '
                + (timer));
        }, 100);
    }
);

d3.select(window)
    .on('keyup', keyup);

